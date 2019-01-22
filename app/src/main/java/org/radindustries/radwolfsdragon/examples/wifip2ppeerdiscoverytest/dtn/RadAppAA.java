package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNClient;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNTextMessenger;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNUI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.Daemon2AppAA;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.AppAA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AdminRecord;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AgeBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNTextMessage;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PayloadADU;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.StatusReport;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;

final class RadAppAA implements DTNClient, DTNTextMessenger, Daemon2AppAA {
    private DTNUI ui;
    private AppAA2Daemon daemon;
    
    RadAppAA(@NonNull DTNUI ui, @NonNull AppAA2Daemon daemon) {
        this.ui = ui;
        this.daemon = daemon;
    }
    
    @Override
    public void send(
        byte[] message, String recipient, PrimaryBlock.PriorityClass priorityClass,
        PrimaryBlock.LifeTime lifeTime, Daemon2Router.RoutingProtocol routingProtocol
    ) {
        DTNEndpointID receiver = DTNEndpointID.parse(recipient);
        DTNBundle bundleToSend
            = createUserBundle(message, receiver, priorityClass, lifeTime);
        
        daemon.transmit(bundleToSend, routingProtocol);
    }
    
    private DTNBundle createUserBundle(
        byte[] message, DTNEndpointID recipient, PrimaryBlock.PriorityClass priorityClass,
        PrimaryBlock.LifeTime lifetime
    ) {
        PrimaryBlock primaryBlock = makePrimaryBlock(recipient, priorityClass, lifetime);
    
        CanonicalBlock ageCBlock
            = DTNUtils.makeAgeCBlock(primaryBlock.bundleID.creationTimestamp);
        
        CanonicalBlock payloadCBlock = makePayloadCBlock(message);
    
        DTNBundle userBundle = new DTNBundle();
        userBundle.primaryBlock = primaryBlock;
        userBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.PAYLOAD, payloadCBlock);
        userBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.AGE, ageCBlock);
    
        return userBundle;
    }
    
    private PrimaryBlock makePrimaryBlock(
        DTNEndpointID recipient, PrimaryBlock.PriorityClass priorityClass,
        PrimaryBlock.LifeTime lifeTime
    ) {
        PrimaryBlock primaryBlock = new PrimaryBlock();
        
        primaryBlock.bundleProcessingControlFlags = makeBundlePCFs();
        primaryBlock.priorityClass = priorityClass;
        primaryBlock.bundleID
            = DTNBundleID.from(daemon.getThisNodezEID(), System.currentTimeMillis());
        primaryBlock.lifeTime = lifeTime.getPeriod();
        primaryBlock.destinationEID = recipient;
        primaryBlock.custodianEID = DTNEndpointID.from(primaryBlock.bundleID.sourceEID);
        primaryBlock.reportToEID = DTNEndpointID.from(primaryBlock.bundleID.sourceEID);
        
        return primaryBlock;
    }
    
    private BigInteger makeBundlePCFs() {
        
        return BigInteger.ZERO
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_CUSTODY_TRANSFER_REQUESTED)
            .setBit(PrimaryBlock.BundlePCF.DESTINATION_ENDPOINT_IS_A_SINGLETON)
            /*request for a SINGLE bundle delivery report a.k.a return-receipt
            (from destination only)
            here, report-to dtnEndpointID == source dtnEndpointID*/
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_DELIVERY_REPORT_REQUESTED);
    }
    
    private CanonicalBlock makePayloadCBlock(byte[] message) {
        CanonicalBlock payloadCBlock = new CanonicalBlock();
        payloadCBlock.blockTypeSpecificDataFields = makePayloadADU(message);
        payloadCBlock.blockType = CanonicalBlock.BlockType.PAYLOAD;
        payloadCBlock.blockProcessingControlFlags = BigInteger.ZERO.setBit(
            CanonicalBlock.BlockPCF.BLOCK_MUST_BE_REPLICATED_IN_ALL_FRAGMENTS
        );
        return payloadCBlock;
    }
    
    private PayloadADU makePayloadADU(byte[] message) {
        
        PayloadADU payload = new PayloadADU();
        payload.ADU = Arrays.copyOf(message, message.length);
        
        return payload;
    }
    
    @Override
    public void deliver(DTNBundle bundle) {
        byte[] message;
    
        CanonicalBlock payloadCBlock = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.PAYLOAD);
        assert payloadCBlock != null;
        
        PayloadADU adu = (PayloadADU) payloadCBlock.blockTypeSpecificDataFields;
        message = adu.ADU;
    
        PrimaryBlock primaryBlock = bundle.primaryBlock;
        DTNEndpointID src =  primaryBlock.bundleID.sourceEID;
        
        ui.onReceiveDTNMessage(message, src.toString());
    }
    
    @Override
    public void notifyOutboundBundleReceived(String recipient) {
        ui.onOutboundBundleReceived(recipient);
    }
    
    @Override
    public void notifyPeerListChanged(Set<DTNEndpointID> peers) {
        ArrayList<String> eids = new ArrayList<>();
        for (DTNEndpointID eid : peers) {
            eids.add(eid.toString());
        }
        ui.onPeerListChanged(eids.toArray(new String[]{}));
    }
    
    @Override
    public String[] getPeerList() {
        Set<DTNEndpointID> peers = daemon.getPeerList();
        ArrayList<String> eids = new ArrayList<>();
        for (DTNEndpointID eid : peers) {
            eids.add(eid.toString());
        }
        return eids.toArray(new String[]{});
    }
    
    @Override
    public String getID() {
        return daemon.getThisNodezEID().toString();
    }
    
    @Override
    public List<DTNTextMessage> getDeliveredTextMessages() {
        List<DTNTextMessage> result = new ArrayList<>();
        List<DTNBundle> messages = daemon.getDeliveredMessages();
        
        for (DTNBundle bundle : messages) {
            if (isPayloadADU(bundle)) {
                result.add(getMessageFromPayloadADU(bundle));
            } else if (isStatusReport(bundle)) {
                result.add(getMessageFromStatusReport(bundle));
            }
        }
        
        return result;
    }
    
    // TODO take isStatusReport() and isPayloadADU() to DTNUtils
    
    private boolean isStatusReport(DTNBundle bundle) {
        if (bundle != null) {
            if (bundle.primaryBlock.bundleProcessingControlFlags
                .testBit(PrimaryBlock.BundlePCF.ADU_IS_AN_ADMIN_RECORD)) {
    
                CanonicalBlock adminRecordCBlock
                    = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
                assert adminRecordCBlock != null;
    
                AdminRecord adminRecord
                    = (AdminRecord) adminRecordCBlock.blockTypeSpecificDataFields;
    
                return adminRecord.recordType.equals(AdminRecord.RecordType.STATUS_REPORT);
            } else return false;
        } else return false;
    }
    
    private boolean isPayloadADU(DTNBundle bundle) {
        if (bundle != null) {
            return !bundle.primaryBlock.bundleProcessingControlFlags
                .testBit(PrimaryBlock.BundlePCF.ADU_IS_AN_ADMIN_RECORD);
        } else return false;
    }
    
    private DTNTextMessage getMessageFromStatusReport(DTNBundle bundle) {
        if (bundle != null) {
            CanonicalBlock adminRecordCBlock
                = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
            assert adminRecordCBlock != null;
    
            StatusReport statusReport
                = (StatusReport) adminRecordCBlock.blockTypeSpecificDataFields;
            
            DTNTextMessage deliveryReportText = new DTNTextMessage();
            deliveryReportText.sender = bundle.primaryBlock.bundleID.sourceEID.toString();
            deliveryReportText.textMessage
                = "Message received @ " + statusReport.timeOfDelivery;
            deliveryReportText.creationTimestamp
                = bundle.primaryBlock.bundleID.creationTimestamp;
            deliveryReportText.receivedTimestamp = getReceivedTimestamp(bundle);
            
            return deliveryReportText;
        } else return new DTNTextMessage();
    }
    
    private DTNTextMessage getMessageFromPayloadADU(DTNBundle bundle) {
        if (bundle != null) {
            CanonicalBlock payloadCBlock
                = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.PAYLOAD);
            assert payloadCBlock != null;
            
            PayloadADU payloadADU = (PayloadADU) payloadCBlock.blockTypeSpecificDataFields;
            
            DTNTextMessage msgFromSender = new DTNTextMessage();
            msgFromSender.sender = bundle.primaryBlock.bundleID.sourceEID.toString();
            msgFromSender.textMessage = new String(payloadADU.ADU);
            msgFromSender.creationTimestamp
                = bundle.primaryBlock.bundleID.creationTimestamp;
            msgFromSender.receivedTimestamp = getReceivedTimestamp(bundle);
            
            return msgFromSender;
        } else return new DTNTextMessage();
    }
    
    private long getReceivedTimestamp(DTNBundle bundle) {
        CanonicalBlock ageCBlock = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.AGE);
        assert ageCBlock != null;
    
        AgeBlock ageBlock = (AgeBlock) ageCBlock.blockTypeSpecificDataFields;
        
        return ageBlock.receivingTimestamp;
    }
}
