package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AdminRecord;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AgeBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CustodySignal;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.NECTARRoutingInfo;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PRoPHETRoutingInfo;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PayloadADU;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.StatusReport;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.Scanner;

public final class DTNUtils {
    private DTNUtils() {}
    
    private static final long DEFAULT_CPU_SPEED_IN_KHZ = 1_100_000L;
    
    public static final BigInteger DAY = BigInteger.valueOf(getMaxCPUFrequencyInKHz())
        .multiply(BigInteger.valueOf(60 * 60 * 24));
    
    static CanonicalBlock makeAgeCBlock() {
        CanonicalBlock ageCBlock = new CanonicalBlock();
    
        AgeBlock ageBlock = new AgeBlock();
        ageBlock.sourceCPUSpeedInKHz = getMaxCPUFrequencyInKHz();
        
        ageCBlock.blockTypeSpecificDataFields = ageBlock;
        ageCBlock.blockType = CanonicalBlock.BlockType.AGE;
        ageCBlock.mustBeReplicatedInAllFragments = true;
        
        return ageCBlock;
    }
    
    static void setTimeReceivedByWRTRx(DTNBundle receivedBundle, BigInteger currentTime) {
        if (!isValid(receivedBundle)) return;
        
        CanonicalBlock ageCBlock = receivedBundle.canonicalBlocks.get(DTNBundle.CBlockNumber.AGE);
        if (ageCBlock != null && ageCBlock.blockTypeSpecificDataFields != null) {
            AgeBlock ageBlock = (AgeBlock) ageCBlock.blockTypeSpecificDataFields;
            ageBlock.receivingTimestamp = currentTime;
        }
    }
    
    static BigInteger getTimeReceivedWRTSrc(DTNBundle bundle) {
        if (!isValid(bundle)) return BigInteger.ZERO.subtract(BigInteger.ONE);
        
        CanonicalBlock ageCBlock = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.AGE);
        if (ageCBlock != null && ageCBlock.blockTypeSpecificDataFields instanceof AgeBlock) {
            AgeBlock ageBlock = (AgeBlock) ageCBlock.blockTypeSpecificDataFields;
            return ageBlock.T;
        } else return BigInteger.ZERO.subtract(BigInteger.ONE);
    }
    
    static boolean isFragment(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            if (bundlePCFs != null &&
                bundlePCFs.testBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT)) {
        
                if (bundle.primaryBlock.detailsIfFragment != null &&
                    !bundle.primaryBlock.detailsIfFragment.isEmpty()) {
                 String aduLength = bundle.primaryBlock.detailsIfFragment
                        .get(PrimaryBlock.FragmentField.TOTAL_ADU_LENGTH);
                 
                 return aduLength != null && Integer.parseInt(aduLength) > 0;
                }
            }
        }
        return false;
    }
    
    static boolean isAdminRecord(DTNBundle bundle) {
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
    
    static boolean isUserData(DTNBundle bundle) {
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
    
    static boolean isCustodySignal(DTNBundle bundle) {
        if (isAdminRecord(bundle)) {
            CanonicalBlock custodySignalCBlock
                = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
            if (custodySignalCBlock != null &&
                custodySignalCBlock.blockTypeSpecificDataFields instanceof CustodySignal) {
    
                CustodySignal custodySignal
                    = (CustodySignal) custodySignalCBlock.blockTypeSpecificDataFields;
    
                return custodySignal.recordType == AdminRecord.RecordType.CUSTODY_SIGNAL &&
                    custodySignal.timeOfSignal.compareTo(BigInteger.ZERO) > 0; // t > 0
            }
        }
        return false;
    }
    
    static boolean isStatusReport(DTNBundle bundle) {
        if (isAdminRecord(bundle)) {
            CanonicalBlock statusReportCBlock
                = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
            if (statusReportCBlock != null &&
                statusReportCBlock.blockTypeSpecificDataFields instanceof StatusReport) {
    
                StatusReport statusReport
                    = (StatusReport) statusReportCBlock.blockTypeSpecificDataFields;
    
                return statusReport.recordType == AdminRecord.RecordType.STATUS_REPORT &&
                    statusReport.timeOfDelivery.compareTo(BigInteger.ZERO) > 0; // t > 0
            }
        }
        return false;
    }
    
    static boolean isValid(DTNBundle bundle) {
        return bundle != null &&
            isValidPrimaryBlock(bundle.primaryBlock) &&
            bundle.canonicalBlocks != null &&
            !bundle.canonicalBlocks.isEmpty() &&
            
            (isValidAgeCBlock(bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.AGE)) &&
            
            (
                isValidPayloadCBlock(bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.PAYLOAD)) ||
                isValidAdminRecord(bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD))
            ));
    }
    
    private static boolean isValidPrimaryBlock(PrimaryBlock primaryBlock) {
        return primaryBlock.bundleID != null &&
            primaryBlock.bundleID.creationTimestamp.compareTo(BigInteger.ZERO) > 0 && // t > 0
            primaryBlock.bundleID.sourceEID != null &&
            primaryBlock.destinationEID != null &&
            primaryBlock.custodianEID != null &&
            primaryBlock.reportToEID != null &&
            primaryBlock.bundleProcessingControlFlags != null &&
            primaryBlock.priorityClass != null &&
            primaryBlock.lifeTime.compareTo(BigInteger.ZERO) > 0; // t > 0
    }
    
    private static boolean isValidAgeCBlock(CanonicalBlock cBlock) {
//        if (cBlock != null &&
//            cBlock.blockType == CanonicalBlock.BlockType.AGE &&
//            cBlock.blockTypeSpecificDataFields instanceof AgeBlock) {
//
//            AgeBlock ageBlock = (AgeBlock) cBlock.blockTypeSpecificDataFields;
//            return ageBlock.sendingTimestamp > 0L &&
//                ageBlock.receivingTimestamp > 0L &&
//                ageBlock.sourceCPUSpeedInKHz > 0L;
//        }
        return cBlock != null &&
            cBlock.blockType == CanonicalBlock.BlockType.AGE &&
            cBlock.blockTypeSpecificDataFields instanceof AgeBlock;
    }
    
    private static boolean isValidPayloadCBlock(CanonicalBlock cBlock) {
        if (cBlock != null &&
            cBlock.blockType == CanonicalBlock.BlockType.PAYLOAD &&
            cBlock.blockTypeSpecificDataFields instanceof PayloadADU) {
            
            PayloadADU payloadADU = (PayloadADU) cBlock.blockTypeSpecificDataFields;
            return payloadADU.ADU != null && payloadADU.ADU.length > 0;
        }
        return false;
    }
    
    static boolean isValidNECTARCBlock(CanonicalBlock cBlock) {
        return cBlock != null &&
            cBlock.blockType == CanonicalBlock.BlockType.NECTAR_ROUTING_INFO &&
            cBlock.blockTypeSpecificDataFields instanceof NECTARRoutingInfo;
    }
    
    static boolean isValidPRoPHETCBlock(CanonicalBlock cBlock) {
        return cBlock != null &&
            cBlock.blockType == CanonicalBlock.BlockType.PROPHET_ROUTING_INFO &&
            cBlock.blockTypeSpecificDataFields instanceof PRoPHETRoutingInfo;
    }
    
    private static boolean isValidAdminRecord(CanonicalBlock cBlock) {
        if (cBlock != null &&
            cBlock.blockType == CanonicalBlock.BlockType.ADMIN_RECORD &&
            cBlock.blockTypeSpecificDataFields instanceof AdminRecord) {
            
            AdminRecord adminRecord = (AdminRecord) cBlock.blockTypeSpecificDataFields;
            if (adminRecord.subjectBundleID != null && adminRecord.isForAFragment) {
                
                if (adminRecord.detailsIfForAFragment != null &&
                    !adminRecord.detailsIfForAFragment.isEmpty()) {
                    String fragmentLength = adminRecord.detailsIfForAFragment
                        .get(AdminRecord.FragmentField.FRAGMENT_LENGTH);
        
                    return fragmentLength != null && Integer.parseInt(fragmentLength) > 0;
                }
            }
        }
        return false;
    }
    
    static boolean forSingletonDestination(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            return bundlePCFs != null &&
                bundlePCFs.testBit(PrimaryBlock.BundlePCF.DESTINATION_ENDPOINT_IS_A_SINGLETON);
        }
        return false;
    }
    
    static boolean isBundleDeliveryReportRequested(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            return bundlePCFs != null &&
                bundlePCFs.testBit(PrimaryBlock.BundlePCF.BUNDLE_DELIVERY_REPORT_REQUESTED);
        }
        return false;
    }
    
    static boolean isCustodyTransferRequested(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            return bundlePCFs != null &&
                bundlePCFs.testBit(PrimaryBlock.BundlePCF.BUNDLE_CUSTODY_TRANSFER_REQUESTED);
        }
        return false;
    }
    
    /**
     * (c) 2010 Nicolas Gramlich
     * (c) 2011 Zynga Inc.
     *
     * @author Nicolas Gramlich
     * @since 15:50:31 - 14.07.2010
     */
    static long getMaxCPUFrequencyInKHz() {
        try {
            return readCPUSystemFileAsInt();
        } catch (Exception e) {
            return DEFAULT_CPU_SPEED_IN_KHZ;
        }
    }
    
    private static int readCPUSystemFileAsInt() throws Exception {
        InputStream in;
        try {
            final Process process = new ProcessBuilder(
                "/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"
            ).start();
            
            in = process.getInputStream();
            final String content = readFully(in);
            return Integer.parseInt(content);
        } catch (final Exception e) {
            throw new Exception(e);
        }
    }
    
    private static String readFully(final InputStream pInputStream) {
        final StringBuilder sb = new StringBuilder();
        final Scanner sc = new Scanner(pInputStream);
        while(sc.hasNextLine()) {
            sb.append(sc.nextLine());
        }
        return sb.toString();
    }
}
