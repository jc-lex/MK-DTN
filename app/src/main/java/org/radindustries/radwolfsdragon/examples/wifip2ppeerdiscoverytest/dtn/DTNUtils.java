package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AdminRecord;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AgeBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CustodySignal;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;

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
        ageCBlock.blockProcessingControlFlags = makeAgeBlockPCFs();
        
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
    
    private static BigInteger makeAgeBlockPCFs() {
        
        return BigInteger.ZERO
            .setBit(CanonicalBlock.BlockPCF.BLOCK_MUST_BE_REPLICATED_IN_ALL_FRAGMENTS)
            .setBit(CanonicalBlock.BlockPCF.TRANSMIT_STATUS_REPORT_IF_BLOCK_CANNOT_BE_PROCESSED)
            .setBit(CanonicalBlock.BlockPCF.DELETE_BUNDLE_IF_BLOCK_CANNOT_BE_PROCESSED)
            .setBit(CanonicalBlock.BlockPCF.LAST_BLOCK)
            .setBit(CanonicalBlock.BlockPCF.DISCARD_BLOCK_IF_IT_CANNOT_BE_PROCESSED);
    }
    
    static BigInteger makeRoutingInfoBlockPCFs() {
        
        return BigInteger.ZERO
            .setBit(CanonicalBlock.BlockPCF.BLOCK_MUST_BE_REPLICATED_IN_ALL_FRAGMENTS)
            .setBit(CanonicalBlock.BlockPCF.DELETE_BUNDLE_IF_BLOCK_CANNOT_BE_PROCESSED);
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
            if (bundlePCFs != null) {
                return bundlePCFs.testBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT);
            }
        }
        return false;
    }
    
    static boolean isAdminRecord(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            if (bundlePCFs != null &&
                bundlePCFs.testBit(PrimaryBlock.BundlePCF.ADU_IS_AN_ADMIN_RECORD)) {
            
                CanonicalBlock adminRecordCBlock
                    = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
                
                return adminRecordCBlock != null && adminRecordCBlock.blockType
                    .equals(CanonicalBlock.BlockType.ADMIN_RECORD);
            }
        }
        return false;
    }
    
    static boolean isUserData(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            if (bundlePCFs != null &&
                !bundlePCFs.testBit(PrimaryBlock.BundlePCF.ADU_IS_AN_ADMIN_RECORD)) {
    
                CanonicalBlock adminRecordCBlock
                    = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
    
                return adminRecordCBlock != null && adminRecordCBlock.blockType
                    .equals(CanonicalBlock.BlockType.PAYLOAD);
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
    
                AdminRecord custodySignal
                    = (AdminRecord) custodySignalCBlock.blockTypeSpecificDataFields;
    
                return custodySignal.recordType.equals(AdminRecord.RecordType.CUSTODY_SIGNAL);
            }
        }
        return false;
    }
    
    static boolean isStatusReport(DTNBundle bundle) {
        if (isAdminRecord(bundle)) {
            CanonicalBlock statusReportCBlock
                = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
            if (statusReportCBlock != null &&
                statusReportCBlock.blockTypeSpecificDataFields instanceof AdminRecord) {
    
                AdminRecord statusReport
                    = (AdminRecord) statusReportCBlock.blockTypeSpecificDataFields;
    
                return statusReport.recordType.equals(AdminRecord.RecordType.STATUS_REPORT);
            }
        }
        return false;
    }
    
    static boolean isValid(DTNBundle bundle) {
        return bundle != null && bundle.primaryBlock != null && bundle.canonicalBlocks != null
            && !bundle.canonicalBlocks.isEmpty();
    }
    
    static boolean forSingletonDestination(DTNBundle bundle) {
        if (isValid(bundle)) {
            BigInteger bundlePCFs = bundle.primaryBlock.bundleProcessingControlFlags;
            return bundlePCFs.testBit(PrimaryBlock.BundlePCF.DESTINATION_ENDPOINT_IS_A_SINGLETON);
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
