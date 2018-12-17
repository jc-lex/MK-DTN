package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AgeBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;

import java.io.InputStream;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

final class DTNUtils {
    private DTNUtils() {}
    
    static final String DTN_REGISTRATION_TYPE = "_dtn._tcp.";
    
    private static final long DEFAULT_CPU_SPEED = 1_100_000L;
    
    static CanonicalBlock makeAgeCBlock(Instant creationTimestamp) {
        CanonicalBlock ageCBlock = new CanonicalBlock();
        
        ageCBlock.blockTypeSpecificDataFields = makeAgeBlock(creationTimestamp);
        ageCBlock.blockType = CanonicalBlock.BlockType.AGE;
        ageCBlock.blockProcessingControlFlags = makeAgeBlockPCFs();
        
        return ageCBlock;
    }
    
    private static AgeBlock makeAgeBlock(Instant bundleCreationTimestamp) {
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
        ageBlock.sendingTimestamp = Instant.now();
        
        ageBlock.age = Duration.between(bundleCreationTimestamp, ageBlock.sendingTimestamp);
        ageBlock.agePrime = Duration.ZERO;
        ageBlock.T = Instant.parse(bundleCreationTimestamp.toString());
        
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
