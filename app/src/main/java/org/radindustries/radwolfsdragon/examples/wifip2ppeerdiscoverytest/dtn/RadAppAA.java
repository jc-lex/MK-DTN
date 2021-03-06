package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNClient;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNTextMessenger;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNUI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.Daemon2AppAA;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.AppAA2Daemon;
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
    public synchronized void send(
        byte[] message, String recipient, PrimaryBlock.PriorityClass priorityClass,
        PrimaryBlock.LifeTime lifeTime, Daemon2Router.RoutingProtocol routingProtocol
    ) {
        DTNEndpointID receiver = DTNEndpointID.parse(recipient);
        DTNBundle bundleToSend
            = createUserBundle(message, receiver, priorityClass, lifeTime);
        
        daemon.transmit(bundleToSend, routingProtocol);
    }
    
    private synchronized DTNBundle createUserBundle(
        byte[] message, DTNEndpointID recipient, PrimaryBlock.PriorityClass priorityClass,
        PrimaryBlock.LifeTime lifetime
    ) {
        PrimaryBlock primaryBlock = makePrimaryBlock(recipient, priorityClass, lifetime);
        
        CanonicalBlock payloadCBlock = makePayloadCBlock(message);
    
        DTNBundle userBundle = new DTNBundle();
        userBundle.primaryBlock = primaryBlock;
        userBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.PAYLOAD, payloadCBlock);
    
        return userBundle;
    }
    
    private synchronized PrimaryBlock makePrimaryBlock(
        DTNEndpointID recipient, PrimaryBlock.PriorityClass priorityClass,
        PrimaryBlock.LifeTime lifeTime
    ) {
        PrimaryBlock primaryBlock = new PrimaryBlock();
        
        primaryBlock.bundleProcessingControlFlags = makeBundlePCFs();
        primaryBlock.priorityClass = priorityClass;
        primaryBlock.bundleID
            = DTNBundleID.from(daemon.getThisNodezEID(), System.currentTimeMillis());
        primaryBlock.lifeTime = lifeTime.getDuration();
        primaryBlock.destinationEID = recipient;
        primaryBlock.custodianEID = daemon.getThisNodezEID();
        primaryBlock.reportToEID = daemon.getThisNodezEID();
        
        return primaryBlock;
    }
    
    private synchronized BigInteger makeBundlePCFs() {
        
        return BigInteger.ZERO
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_CUSTODY_TRANSFER_REQUESTED)
            .setBit(PrimaryBlock.BundlePCF.DESTINATION_ENDPOINT_IS_A_SINGLETON)
            /*request for a SINGLE bundle delivery report a.k.a return-receipt
            (from destination only)
            here, report-to dtnEndpointID == source dtnEndpointID*/
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_DELIVERY_REPORT_REQUESTED)
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_DELETION_REPORT_REQUESTED);
    }
    
    private synchronized CanonicalBlock makePayloadCBlock(byte[] message) {
        CanonicalBlock payloadCBlock = new CanonicalBlock();
        payloadCBlock.blockTypeSpecificDataFields = makePayloadADU(message);
        payloadCBlock.blockType = CanonicalBlock.BlockType.PAYLOAD;
        payloadCBlock.mustBeReplicatedInAllFragments = true;
        return payloadCBlock;
    }
    
    private synchronized PayloadADU makePayloadADU(byte[] message) {
        
        PayloadADU payload = new PayloadADU();
        payload.ADU = message;
        
        return payload;
    }
    
    @Override
    public synchronized void deliver(DTNBundle bundle) {
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
    public void notifyBundleStatus(String recipient, String msg) {
        ui.onBundleStatusReceived(recipient, msg);
    }
    
    @Override
    public synchronized void notifyPeerListChanged(Set<DTNEndpointID> peers) {
        ArrayList<String> eids = new ArrayList<>();
        for (DTNEndpointID eid : peers) {
            eids.add(eid.toString());
        }
        ui.onPeerListChanged(eids.toArray(new String[]{}));
    }
    
    @Override
    public synchronized String[] getPeerList() {
        Set<DTNEndpointID> peers = daemon.getPeerList();
        ArrayList<String> eids = new ArrayList<>();
        for (DTNEndpointID eid : peers) {
            eids.add(eid.toString());
        }
        return eids.toArray(new String[]{});
    }
    
    @Override
    public synchronized String getID() {
        return daemon.getThisNodezEID().toString();
    }
    
    @Override
    public synchronized List<DTNTextMessage> getDeliveredTextMessages() {
        List<DTNTextMessage> result = new ArrayList<>();
        List<DTNBundle> messages = daemon.getDeliveredMessages();
        
        for (DTNBundle bundle : messages) {
            if (DTNUtils.isUserData(bundle)) {
                result.add(getMessageFromPayloadADU(bundle));
            } else if (DTNUtils.isStatusReport(bundle)) {
                result.add(getMessageFromStatusReport(bundle));
            }
        }
        
        return result;
    }
    
    private synchronized DTNTextMessage getMessageFromStatusReport(DTNBundle bundle) {
        CanonicalBlock adminRecordCBlock
            = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
        
        if (adminRecordCBlock != null &&
            adminRecordCBlock.blockTypeSpecificDataFields instanceof StatusReport) {
            StatusReport statusReport
                = (StatusReport) adminRecordCBlock.blockTypeSpecificDataFields;
            
            DTNTextMessage deliveryReportText = new DTNTextMessage();
            deliveryReportText.sender = bundle.primaryBlock.bundleID.sourceEID.toString();
            
            if (statusReport.status == StatusReport.StatusFlags.BUNDLE_DELIVERED) {
                deliveryReportText.textMessage = "Message received @ "
                    + statusReport.timeOfStatus;
            } else if (statusReport.status == StatusReport.StatusFlags.BUNDLE_DELETED) {
                deliveryReportText.textMessage
                    = "Message deleted @ " + statusReport.timeOfStatus
                    + " because " + statusReport.reasonCode.toString();
            } else deliveryReportText.textMessage = bundle.toString();
            setTimestamps(deliveryReportText, bundle);
            
            return deliveryReportText;
        }
        return new DTNTextMessage();
    }
    
    private synchronized DTNTextMessage getMessageFromPayloadADU(DTNBundle bundle) {
        CanonicalBlock payloadCBlock
            = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.PAYLOAD);
        if (payloadCBlock != null &&
            payloadCBlock.blockTypeSpecificDataFields instanceof PayloadADU) {
            
            PayloadADU payloadADU = (PayloadADU) payloadCBlock.blockTypeSpecificDataFields;
            
            DTNTextMessage msgFromSender = new DTNTextMessage();
            msgFromSender.sender = bundle.primaryBlock.bundleID.sourceEID.toString();
            msgFromSender.textMessage = new String(payloadADU.ADU);
            setTimestamps(msgFromSender, bundle);
            
            return msgFromSender;
        } else return new DTNTextMessage();
    }
    
    private synchronized void setTimestamps(DTNTextMessage msg, DTNBundle bundle) {
        // from src to dest (overall net delay)
        msg.creationTimestamp = bundle.primaryBlock.bundleID.creationTimestamp + " ms";
        msg.deliveryTimestamp = bundle.timeOfDelivery + " ms";
    }
}
