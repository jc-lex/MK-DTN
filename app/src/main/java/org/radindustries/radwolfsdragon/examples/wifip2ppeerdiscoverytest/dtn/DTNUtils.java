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

final class DTNUtils {
    private DTNUtils() {}
    
    private static final long DEFAULT_CPU_SPEED = 1_100_000L;
    
    static CanonicalBlock makeAgeCBlock(long creationTimestamp) {
        CanonicalBlock ageCBlock = new CanonicalBlock();
        
        ageCBlock.blockTypeSpecificDataFields = makeAgeBlock(creationTimestamp);
        ageCBlock.blockType = CanonicalBlock.BlockType.AGE;
        ageCBlock.mustBeReplicatedInAllFragments = true;
        
        return ageCBlock;
    }
    
    private static AgeBlock makeAgeBlock(long bundleCreationTimestamp) {
        // at the source or sender,
        AgeBlock ageBlock = new AgeBlock();
        
        try {
            ageBlock.sourceCPUSpeedInKHz = getMaxCPUFrequencyInKHz();
        } catch (Exception e) {
            // we can't do anything without this clock speed. for now, lets assume a default.
            ageBlock.sourceCPUSpeedInKHz = DEFAULT_CPU_SPEED;
        }
        
        /*because ppl live in different timezones, there is need to have them all use
         * a common time reference (timezone). Therefore we use the standard UTC time for
         * everyone. This simplifies the process for determining the bundle's age.*/
        ageBlock.sendingTimestamp = System.currentTimeMillis();
        
        ageBlock.age = ageBlock.sendingTimestamp - bundleCreationTimestamp;
        ageBlock.agePrime = 0;
        ageBlock.T = bundleCreationTimestamp;
        
        /*
        at the receiver,
        ageBlock.agePrime = ageBlock.age.plus(TA); // where TA -> transmission age
        ageBlock.T = bundleCreationTimestamp.plus(ageBlock.agePrime);

         when we are gonna forward the bundle or at any other time
        ageBlock.sendingTimestamp = Instant.now();
        ageBlock.age = ageBlock.agePrime.plus(
            Duration.between(ageBlock.T, ageBlock.sendingTimestamp)
        );
        */
        
        return ageBlock;
    }
    
    static void setTimeReceived(DTNBundle receivedBundle) {
        if (!isValid(receivedBundle)) return;
        
        CanonicalBlock ageCBlock = receivedBundle.canonicalBlocks.get(DTNBundle.CBlockNumber.AGE);
        if (ageCBlock != null && ageCBlock.blockTypeSpecificDataFields != null) {
            AgeBlock ageBlock = (AgeBlock) ageCBlock.blockTypeSpecificDataFields;
            ageBlock.receivingTimestamp = System.currentTimeMillis();
        }
    }
    
    static long getTimeReceived(DTNBundle bundle) {
        if (!isValid(bundle)) return -1L;
        
        CanonicalBlock ageCBlock = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.AGE);
        if (ageCBlock != null && ageCBlock.blockTypeSpecificDataFields instanceof AgeBlock) {
            AgeBlock ageBlock = (AgeBlock) ageCBlock.blockTypeSpecificDataFields;
            return ageBlock.receivingTimestamp;
        } else return -1L;
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
                    custodySignal.timeOfSignal > 0L;
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
                    statusReport.timeOfDelivery > 0L;
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
            primaryBlock.bundleID.creationTimestamp > 0L &&
            primaryBlock.bundleID.sourceEID != null &&
            primaryBlock.destinationEID != null &&
            primaryBlock.custodianEID != null &&
            primaryBlock.reportToEID != null &&
            primaryBlock.bundleProcessingControlFlags != null &&
            primaryBlock.priorityClass != null &&
            primaryBlock.lifeTime > 0L;
    }
    
    private static boolean isValidAgeCBlock(CanonicalBlock cBlock) {
        if (cBlock != null &&
            cBlock.blockType == CanonicalBlock.BlockType.AGE &&
            cBlock.blockTypeSpecificDataFields instanceof AgeBlock) {
            
            AgeBlock ageBlock = (AgeBlock) cBlock.blockTypeSpecificDataFields;
            return ageBlock.sendingTimestamp > 0L &&
                ageBlock.receivingTimestamp > 0L &&
                ageBlock.sourceCPUSpeedInKHz > 0L;
        }
        return false;
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
    private static int getMaxCPUFrequencyInKHz() throws Exception {
        return readCPUSystemFileAsInt();
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
