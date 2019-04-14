package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AdminRecord;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CustodySignal;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.NECTARRoutingInfo;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PRoPHETRoutingInfo;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PayloadADU;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.StatusReport;

import java.math.BigInteger;

public final class DTNUtils {
    private DTNUtils() {}
    
    public static final long HOUR_MILLIS = 3_600_000L;
    static final long DAY_MILLIS = 86_400_000L;
    
    static synchronized boolean isFragment(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            if (bundlePCFs != null &&
                bundlePCFs.testBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT)) {
        
                if (bundle.primaryBlock.detailsIfFragment != null &&
                    !bundle.primaryBlock.detailsIfFragment.isEmpty()) {
                 String aduLength = bundle.primaryBlock.detailsIfFragment.get(
                     PrimaryBlock.FragmentField.TOTAL_ADU_LENGTH
                 );
                 
                 String offset = bundle.primaryBlock.detailsIfFragment.get(
                     PrimaryBlock.FragmentField.FRAGMENT_OFFSET
                 );
                 
                 return aduLength != null && Integer.parseInt(aduLength) > 0 &&
                     offset != null && Integer.parseInt(offset) > -1;
                }
            }
        }
        return false;
    }
    
    static synchronized boolean isAdminRecord(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            if (bundlePCFs != null &&
                bundlePCFs.testBit(PrimaryBlock.BundlePCF.ADU_IS_AN_ADMIN_RECORD)) {
            
                return isValidAdminRecord(bundle.canonicalBlocks.get(
                    DTNBundle.CBlockNumber.ADMIN_RECORD
                ));
            }
        }
        return false;
    }
    
    static synchronized boolean isUserData(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            if (bundlePCFs != null &&
                !bundlePCFs.testBit(PrimaryBlock.BundlePCF.ADU_IS_AN_ADMIN_RECORD)) {
                
                return isValidPayloadCBlock(bundle.canonicalBlocks.get(
                    DTNBundle.CBlockNumber.PAYLOAD
                ));
            }
        }
        return false;
    }
    
    static synchronized boolean isCustodySignal(DTNBundle bundle) {
        if (isAdminRecord(bundle)) {
            CanonicalBlock custodySignalCBlock
                = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
            if (custodySignalCBlock != null &&
                custodySignalCBlock.blockTypeSpecificDataFields instanceof CustodySignal) {
    
                CustodySignal custodySignal
                    = (CustodySignal) custodySignalCBlock.blockTypeSpecificDataFields;
    
                return custodySignal.recordType == AdminRecord.RecordType.CUSTODY_SIGNAL &&
                    custodySignal.timeOfSignal > 0;
            }
        }
        return false;
    }
    
    static synchronized boolean isStatusReport(DTNBundle bundle) {
        if (isAdminRecord(bundle)) {
            CanonicalBlock statusReportCBlock
                = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
            if (statusReportCBlock != null &&
                statusReportCBlock.blockTypeSpecificDataFields instanceof StatusReport) {
    
                StatusReport statusReport
                    = (StatusReport) statusReportCBlock.blockTypeSpecificDataFields;
    
                return statusReport.recordType == AdminRecord.RecordType.STATUS_REPORT &&
                    statusReport.status != StatusReport.StatusFlags.INVALID_FLAG_SET &&
                    statusReport.timeOfStatus  > 0;
            }
        }
        return false;
    }
    
    static synchronized boolean isValid(DTNBundle bundle) {
        return bundle != null &&
            isValidPrimaryBlock(bundle.primaryBlock) &&
            bundle.canonicalBlocks != null &&
            !bundle.canonicalBlocks.isEmpty() &&
            (isValidPayloadCBlock(bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.PAYLOAD)) ||
                isValidAdminRecord(bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD)));
    }
    
    private static synchronized boolean isValidPrimaryBlock(PrimaryBlock primaryBlock) {
        return primaryBlock != null && primaryBlock.bundleID != null &&
            primaryBlock.bundleID.creationTimestamp > 0 &&
            primaryBlock.bundleID.sourceEID != null &&
            primaryBlock.destinationEID != null &&
            primaryBlock.custodianEID != null &&
            primaryBlock.reportToEID != null &&
            primaryBlock.bundleProcessingControlFlags != null &&
            primaryBlock.priorityClass != null &&
            primaryBlock.lifeTime > 0;
    }
    
    private static synchronized boolean isValidPayloadCBlock(CanonicalBlock cBlock) {
        if (cBlock != null &&
            cBlock.blockType == CanonicalBlock.BlockType.PAYLOAD &&
            cBlock.blockTypeSpecificDataFields instanceof PayloadADU) {
            
            PayloadADU payloadADU = (PayloadADU) cBlock.blockTypeSpecificDataFields;
            return payloadADU.ADU != null && payloadADU.ADU.length > 0;
        }
        return false;
    }
    
    static synchronized boolean isValidNECTARCBlock(CanonicalBlock cBlock) {
        return cBlock != null &&
            cBlock.blockType == CanonicalBlock.BlockType.NECTAR_ROUTING_INFO &&
            cBlock.blockTypeSpecificDataFields instanceof NECTARRoutingInfo;
    }
    
    static synchronized boolean isValidPRoPHETCBlock(CanonicalBlock cBlock) {
        return cBlock != null &&
            cBlock.blockType == CanonicalBlock.BlockType.PROPHET_ROUTING_INFO &&
            cBlock.blockTypeSpecificDataFields instanceof PRoPHETRoutingInfo;
    }
    
    private static synchronized boolean isValidAdminRecord(CanonicalBlock cBlock) {
        if (cBlock != null &&
            cBlock.blockType == CanonicalBlock.BlockType.ADMIN_RECORD &&
            cBlock.blockTypeSpecificDataFields instanceof AdminRecord) {
            
            AdminRecord adminRecord = (AdminRecord) cBlock.blockTypeSpecificDataFields;
            if (adminRecord.subjectBundleID != null) {
                
                if (adminRecord.isForAFragment) {
                    if (adminRecord.detailsIfForAFragment != null &&
                        !adminRecord.detailsIfForAFragment.isEmpty()) {
                        String fragmentLength = adminRecord.detailsIfForAFragment.get(
                            AdminRecord.FragmentField.FRAGMENT_LENGTH
                        );
                        
                        String offset = adminRecord.detailsIfForAFragment.get(
                            AdminRecord.FragmentField.FRAGMENT_OFFSET
                        );
        
                        return fragmentLength != null && Integer.parseInt(fragmentLength) > 0 &&
                            offset != null && Integer.parseInt(offset) > -1;
                    }
                }
                else return true;
            }
        }
        return false;
    }
    
    static synchronized boolean forSingletonDestination(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            return bundlePCFs != null &&
                bundlePCFs.testBit(PrimaryBlock.BundlePCF.DESTINATION_ENDPOINT_IS_A_SINGLETON);
        }
        return false;
    }
    
    static synchronized boolean isBundleDeliveryReportRequested(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            return bundlePCFs != null &&
                bundlePCFs.testBit(PrimaryBlock.BundlePCF.BUNDLE_DELIVERY_REPORT_REQUESTED);
        }
        return false;
    }
    
    static synchronized boolean isBundleDeletionReportRequested(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            return bundlePCFs != null &&
                bundlePCFs.testBit(PrimaryBlock.BundlePCF.BUNDLE_DELETION_REPORT_REQUESTED);
        }
        return false;
    }
    
    static synchronized boolean isCustodyTransferRequested(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            return bundlePCFs != null &&
                bundlePCFs.testBit(PrimaryBlock.BundlePCF.BUNDLE_CUSTODY_TRANSFER_REQUESTED);
        }
        return false;
    }
}
