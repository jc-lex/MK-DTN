package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.annotation.SuppressLint;
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

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;

final class RadAdminAA implements Daemon2AdminAA {
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + RadAdminAA.class.getSimpleName();
    
    private AdminAA2Daemon daemon;
    
    RadAdminAA(AdminAA2Daemon daemon) {
        this.daemon = daemon;
    }
    
    @Override
    public void processAdminRecord(DTNBundle adminRecord) {
        CanonicalBlock adminCBlock
            = adminRecord.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
        
        assert adminCBlock != null;
        AdminRecord record = (AdminRecord) adminCBlock.blockTypeSpecificDataFields;
        
        switch (record.recordType) {
            case STATUS_REPORT:
                processStatusReport((StatusReport) record);
                break;
            case CUSTODY_SIGNAL:
                processCustodySignal((CustodySignal) record);
                break;
            default: break;
        }
    }
    
    private void processCustodySignal(CustodySignal signal) {
        if (signal.custodyTransferSucceeded) {
            if (signal.isForAFragment) {
                String fragmentOffset
                    = signal.detailsIfForAFragment.get(PrimaryBlock.FragmentField.FRAGMENT_OFFSET);
                
                assert fragmentOffset != null;
                daemon.delete(signal.subjectBundleID, Integer.parseInt(fragmentOffset));
            } else {
                daemon.delete(signal.subjectBundleID);
            }
        } else {
            if (signal.isForAFragment) {
                String fragmentOffset
                    = signal.detailsIfForAFragment.get(PrimaryBlock.FragmentField.FRAGMENT_OFFSET);
                
                assert fragmentOffset != null;
                Log.i(LOG_TAG, "Custody transfer for fragment "+ fragmentOffset
                    +" of bundle "
                    + signal.subjectBundleID + " failed: " + signal.reasonCode);
            } else {
                Log.i(LOG_TAG, "Custody transfer for bundle "
                    + signal.subjectBundleID + " failed: " + signal.reasonCode);
            }
        }
    }
    
    private void processStatusReport(StatusReport report) {
        if (!report.isForAFragment) {
            if (report.bundleDelivered) {
                String ssp = report.subjectBundleID.sourceEID.ssp;
                
                daemon.notifyOutboundBundleDelivered(ssp);
            } else {
                Log.i(LOG_TAG, "Bundle delivery for "
                    + report.subjectBundleID + " failed: " + report.reasonCode);
            }
        }
    }
    
    @Override
    public DTNBundle makeCustodySignal(
        DTNBundle userBundle, boolean custodyAccepted, CustodySignal.Reason reasonCode
    ) {
        AdminRecord custodySignalAR = makeCustodySignalForUserBundle(
            userBundle, custodyAccepted, reasonCode
        );
        
        return makeAdminRecordBundle(custodySignalAR, userBundle.primaryBlock);
    }
    
    private AdminRecord makeCustodySignalForUserBundle(
        DTNBundle userBundle, boolean custodyAccepted, CustodySignal.Reason reasonCode
    ) {
        CustodySignal signal = new CustodySignal();
    
        signal.recordType = AdminRecord.RecordType.CUSTODY_SIGNAL;
        signal.reasonCode = reasonCode;
        signal.custodyTransferSucceeded = custodyAccepted;
        signal.timeOfSignal = Instant.now();
        
        return processOtherAdminRecordDetails(userBundle, signal);
    }
    
    @Override
    public DTNBundle makeStatusReport(
        DTNBundle userBundle, boolean bundleDelivered, StatusReport.Reason reasonCode
    ) {
        AdminRecord statusReportAR = makeStatusReportForUserBundle(
            userBundle, bundleDelivered, reasonCode
        );
        
        return makeAdminRecordBundle(statusReportAR, userBundle.primaryBlock);
    }
    
    private AdminRecord makeStatusReportForUserBundle(
        DTNBundle userBundle, boolean bundleDelivered, StatusReport.Reason reasonCode
    ) {
        StatusReport report = new StatusReport();
        
        report.recordType = AdminRecord.RecordType.STATUS_REPORT;
        report.reasonCode = reasonCode;
        report.bundleDelivered = bundleDelivered;
        report.timeOfDelivery = Instant.now();
        
        return processOtherAdminRecordDetails(userBundle, report);
    }
    
    private AdminRecord processOtherAdminRecordDetails(
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
    
    @SuppressLint("UseSparseArrays")
    private DTNBundle makeAdminRecordBundle(
        AdminRecord adminRecord, PrimaryBlock userBundlePrimaryBlock
    ) {
        PrimaryBlock primaryBlock = makeAdminRecordPrimaryBlock(userBundlePrimaryBlock);
        
        CanonicalBlock adminCBlock = makeAdminCBlock(adminRecord);
        
        CanonicalBlock ageBlock
            = DTNUtils.makeAgeCBlock(primaryBlock.bundleID.creationTimestamp);
    
        DTNBundle adminBundle = new DTNBundle();
        adminBundle.primaryBlock = primaryBlock;
        adminBundle.canonicalBlocks = new HashMap<>();
        adminBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.ADMIN_RECORD, adminCBlock);
        adminBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.AGE, ageBlock);
        
        return adminBundle;
    }
    
    private PrimaryBlock makeAdminRecordPrimaryBlock(PrimaryBlock userBundlePrimaryBlock) {
        PrimaryBlock primaryBlock = new PrimaryBlock();
        
        primaryBlock.bundleProcessingControlFlags
            = makeBundlePCFsForAdminRecord(
            PrimaryBlock.PriorityClass.getPriorityClass(
                userBundlePrimaryBlock.bundleProcessingControlFlags
            )
        ); //for now
        primaryBlock.bundleID = DTNBundleID.from(daemon.getThisNodezEID(), Instant.now());
        primaryBlock.destinationEID = DTNEndpointID.from(userBundlePrimaryBlock.bundleID.sourceEID);
        primaryBlock.custodianEID = DTNEndpointID.from(primaryBlock.bundleID.sourceEID);
        primaryBlock.reportToEID = DTNEndpointID.from(primaryBlock.bundleID.sourceEID);
        primaryBlock.lifeTime = PrimaryBlock.LifeTime.setLifeTime(
            PrimaryBlock.LifeTime.THREE_DAYS
        ); // for now
        
        return primaryBlock;
    }
    
    private BigInteger makeBundlePCFsForAdminRecord(PrimaryBlock.PriorityClass priorityClass) {
        return PrimaryBlock.PriorityClass.setPriorityClass(BigInteger.ZERO, priorityClass)
            .setBit(PrimaryBlock.BundlePCF.ADU_IS_AN_ADMIN_RECORD)
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_MUST_NOT_BE_FRAGMENTED)
            .setBit(PrimaryBlock.BundlePCF.DESTINATION_ENDPOINT_IS_SINGLETON);
    }
    
    private CanonicalBlock makeAdminCBlock(AdminRecord adminRecord) {
        CanonicalBlock adminCBlock = new CanonicalBlock();
        adminCBlock.blockTypeCode = CanonicalBlock.TypeCode.ADMIN_RECORD;
        adminCBlock.blockProcessingControlFlags = BigInteger.ZERO; //for now
        adminCBlock.blockTypeSpecificDataFields = adminRecord;
        return adminCBlock;
    }
}
