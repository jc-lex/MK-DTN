package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.util.Log;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.admin.Daemon2AdminAA;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.AdminAA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AdminRecord;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CustodySignal;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PayloadADU;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.StatusReport;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.WallClock;

import java.math.BigInteger;

import androidx.annotation.NonNull;

final class RadAdminAA implements Daemon2AdminAA {
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + RadAdminAA.class.getSimpleName();
    
    private AdminAA2Daemon daemon;
    private DTNBundle bundle;
    private WallClock clock;
    
    RadAdminAA(@NonNull AdminAA2Daemon daemon, @NonNull WallClock clock) {
        this.daemon = daemon;
        this.clock = clock;
    }
    
    @Override
    public synchronized void processAdminRecord(DTNBundle adminRecord) {
        if (!DTNUtils.isAdminRecord(adminRecord)) return;
    
        if (!daemon.isForUs(adminRecord)) {
            DummyStorage.OUTBOUND_BUNDLES_QUEUE.add(adminRecord);
            return;
        }
    
        bundle = adminRecord;
        
        CanonicalBlock adminCBlock
            = adminRecord.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
        assert adminCBlock != null;
        
        AdminRecord record = (AdminRecord) adminCBlock.blockTypeSpecificDataFields;
        DTNEndpointID src = adminRecord.primaryBlock.bundleID.sourceEID;
        
        switch (record.recordType) {
            case STATUS_REPORT:
                if (DTNUtils.isStatusReport(adminRecord))
                    processStatusReport((StatusReport) record, src);
                break;
            case CUSTODY_SIGNAL:
                if (DTNUtils.isCustodySignal(adminRecord))
                    processCustodySignal((CustodySignal) record);
                break;
            default: break;
        }
    }
    
    private synchronized void processCustodySignal(CustodySignal signal) {
        if (signal.custodyTransferSucceeded) {
            if (signal.isForAFragment) {
                String fragmentOffset
                    = signal.detailsIfForAFragment.get(AdminRecord.FragmentField.FRAGMENT_OFFSET);
                
                assert fragmentOffset != null;
                daemon.delete(signal.subjectBundleID, Integer.parseInt(fragmentOffset));
            } else {
                daemon.delete(signal.subjectBundleID);
            }
        } else {
            if (signal.isForAFragment) {
                String fragmentOffset
                    = signal.detailsIfForAFragment.get(AdminRecord.FragmentField.FRAGMENT_OFFSET);
                
                assert fragmentOffset != null;
                Log.e(LOG_TAG, "Custody transfer for fragment "+ fragmentOffset
                    +" of bundle "
                    + signal.subjectBundleID + " failed: " + signal.reasonCode);
            } else {
                Log.e(LOG_TAG, "Custody transfer for bundle "
                    + signal.subjectBundleID + " failed: " + signal.reasonCode);
            }
        }
    }
    
    private synchronized void processStatusReport(StatusReport report, DTNEndpointID recipient) {
        if (!report.isForAFragment) {
            if (daemon.isUs(report.subjectBundleID.sourceEID)) {
                if (report.statusFlags.testBit(StatusReport.StatusFlags.INVALID_FLAG_SET))
                    return;
                DummyStorage.DELIVERED_BUNDLES_QUEUE.add(bundle);
                
                String msg = "";
                if (report.statusFlags.testBit(StatusReport.StatusFlags.BUNDLE_DELIVERED)) {
                    msg = "BUNDLE_DELIVERED";
                } else if (report.statusFlags.testBit(StatusReport.StatusFlags.BUNDLE_DELETED)) {
                    msg = "BUNDLE_DELETED";
                }
                
                daemon.notifyBundleStatus(recipient.toString(), msg);
            }
        }
    }
    
    @Override
    public synchronized DTNBundle makeCustodySignal(
        DTNBundle userBundle, boolean custodyAccepted, CustodySignal.Reason reasonCode
    ) {
        AdminRecord custodySignalAR = makeCustodySignalForUserBundle(
            userBundle, custodyAccepted, reasonCode
        );
        
        return makeAdminRecordBundle(custodySignalAR, userBundle.primaryBlock);
    }
    
    private synchronized AdminRecord makeCustodySignalForUserBundle(
        DTNBundle userBundle, boolean custodyAccepted, CustodySignal.Reason reasonCode
    ) {
        CustodySignal signal = new CustodySignal();
    
        signal.recordType = AdminRecord.RecordType.CUSTODY_SIGNAL;
        signal.reasonCode = reasonCode;
        signal.custodyTransferSucceeded = custodyAccepted;
        signal.timeOfSignal = clock.getCurrentTime();
        
        return processOtherAdminRecordDetails(userBundle, signal);
    }
    
    @Override
    public synchronized DTNBundle makeStatusReport(
        DTNBundle userBundle, int statusCode, StatusReport.Reason reasonCode
    ) {
        AdminRecord statusReportAR = makeStatusReportForUserBundle(
            userBundle, statusCode, reasonCode
        );
        
        return makeAdminRecordBundle(statusReportAR, userBundle.primaryBlock);
    }
    
    private synchronized AdminRecord makeStatusReportForUserBundle(
        DTNBundle userBundle, int statusCode, StatusReport.Reason reasonCode
    ) {
        if (statusCode != StatusReport.StatusFlags.BUNDLE_DELIVERED &&
        statusCode != StatusReport.StatusFlags.BUNDLE_DELETED) {
            statusCode = StatusReport.StatusFlags.INVALID_FLAG_SET;
            reasonCode = StatusReport.Reason.NO_OTHER_INFO;
        }
        
        StatusReport report = new StatusReport();
        
        report.recordType = AdminRecord.RecordType.STATUS_REPORT;
        report.reasonCode = reasonCode;
        report.statusFlags = report.statusFlags.setBit(statusCode);
        report.statusTimes.put(statusCode, clock.getCurrentTime());
        
        return processOtherAdminRecordDetails(userBundle, report);
    }
    
    private synchronized AdminRecord processOtherAdminRecordDetails(
        DTNBundle userBundle, AdminRecord adminRecord
    ) {
        adminRecord.isForAFragment = userBundle.primaryBlock.bundleProcessingControlFlags
            .testBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT);
        
        if (adminRecord.isForAFragment) {
            
            String fragmentOffset
                = userBundle.primaryBlock.detailsIfFragment.get(
                    PrimaryBlock.FragmentField.FRAGMENT_OFFSET
            );
    
            CanonicalBlock payloadCBlock
                = userBundle.canonicalBlocks.get(DTNBundle.CBlockNumber.PAYLOAD);
            
            assert payloadCBlock != null;
            PayloadADU adu = (PayloadADU) payloadCBlock.blockTypeSpecificDataFields;
            
            String fragmentLength = Integer.toString(adu.ADU.length);
    
            adminRecord.detailsIfForAFragment.put(
                AdminRecord.FragmentField.FRAGMENT_OFFSET, fragmentOffset
            );
            adminRecord.detailsIfForAFragment.put(
                AdminRecord.FragmentField.FRAGMENT_LENGTH, fragmentLength
            );
            
        }
        
        adminRecord.subjectBundleID = DTNBundleID.from(userBundle.primaryBlock.bundleID);
        
        return adminRecord;
    }
    
    private synchronized DTNBundle makeAdminRecordBundle(
        AdminRecord adminRecord, PrimaryBlock userBundlePrimaryBlock
    ) {
        PrimaryBlock primaryBlock = makeAdminRecordPrimaryBlock(userBundlePrimaryBlock);
        
        CanonicalBlock adminCBlock = makeAdminCBlock(adminRecord);
        
        CanonicalBlock ageBlock = DTNUtils.makeAgeCBlock();
    
        DTNBundle adminBundle = new DTNBundle();
        adminBundle.primaryBlock = primaryBlock;
        adminBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.ADMIN_RECORD, adminCBlock);
        adminBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.AGE, ageBlock);
        
        return adminBundle;
    }
    
    private synchronized PrimaryBlock makeAdminRecordPrimaryBlock(
        PrimaryBlock userBundlePrimaryBlock
    ) {
        PrimaryBlock primaryBlock = new PrimaryBlock();
        
        primaryBlock.bundleProcessingControlFlags = makeBundlePCFsForAdminRecord();
        primaryBlock.priorityClass = userBundlePrimaryBlock.priorityClass;
        primaryBlock.bundleID
            = DTNBundleID.from(daemon.getThisNodezEID(), clock.getCurrentTime());
        primaryBlock.destinationEID = DTNEndpointID.from(userBundlePrimaryBlock.bundleID.sourceEID);
        primaryBlock.custodianEID = DTNEndpointID.from(primaryBlock.bundleID.sourceEID);
        primaryBlock.reportToEID = DTNEndpointID.from(primaryBlock.bundleID.sourceEID);
        primaryBlock.lifeTime = userBundlePrimaryBlock.lifeTime;
        
        return primaryBlock;
    }
    
    private synchronized BigInteger makeBundlePCFsForAdminRecord() {
        return BigInteger.ZERO
            .setBit(PrimaryBlock.BundlePCF.ADU_IS_AN_ADMIN_RECORD)
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_MUST_NOT_BE_FRAGMENTED)
            .setBit(PrimaryBlock.BundlePCF.DESTINATION_ENDPOINT_IS_A_SINGLETON);
    }
    
    private synchronized CanonicalBlock makeAdminCBlock(AdminRecord adminRecord) {
        CanonicalBlock adminCBlock = new CanonicalBlock();
        adminCBlock.blockType = CanonicalBlock.BlockType.ADMIN_RECORD;
        adminCBlock.mustBeReplicatedInAllFragments = false;
        adminCBlock.blockTypeSpecificDataFields = adminRecord;
        return adminCBlock;
    }
}
