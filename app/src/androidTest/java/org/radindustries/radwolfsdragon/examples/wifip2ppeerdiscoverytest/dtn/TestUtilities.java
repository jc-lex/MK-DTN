package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PayloadADU;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

final class TestUtilities {
    private TestUtilities() {}
    
    private static final int TEST_FRAGMENT_OFFSET = 0;
    private static final int TEST_EID_LENGTH = 2;
    
    private static final String TEST_SENDER = "dtn:b111";
//    static final String TEST_RECIPIENT = "dtn:f1b1";
    private static final PrimaryBlock.LifeTime TEST_LIFETIME = PrimaryBlock.LifeTime.FIVE_HOURS;
    private static final PrimaryBlock.PriorityClass TEST_PRIORITY
        = PrimaryBlock.PriorityClass.NORMAL;
//    static final Daemon2Router.RoutingProtocol TEST_PROTOCOL
//        = Daemon2Router.RoutingProtocol.PER_HOP;
    
    static DTNEndpointID makeDTNEID() {
        DTNEndpointID eid = new DTNEndpointID();
        eid.scheme = DTNEndpointID.DTN_SCHEME;
        eid.ssp = generateRLUUID();
        return eid;
    }
    
    private static String generateRLUUID() {
        int i = 0;
        
        StringBuilder rluuid = new StringBuilder();
        
        while (i < TEST_EID_LENGTH) {
            UUID uuid = UUID.randomUUID();
            rluuid.append(Long.toHexString(uuid.getMostSignificantBits()));
            rluuid.append(Long.toHexString(uuid.getLeastSignificantBits()));
            i++;
        }
        
        return rluuid.toString();
    }
    
    static DTNBundle createTestUserBundle(byte[] message) {
        
        PrimaryBlock primaryBlock = makePrimaryBlockForUserBundle();
        
        CanonicalBlock payloadCBlock = new CanonicalBlock();
        payloadCBlock.blockTypeSpecificDataFields = makePayloadForUserBundle(message);
        payloadCBlock.blockType = CanonicalBlock.BlockType.PAYLOAD;
        payloadCBlock.mustBeReplicatedInAllFragments = true;
        
        DTNBundle userBundle = new DTNBundle();
        userBundle.primaryBlock = primaryBlock;
        userBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.PAYLOAD, payloadCBlock);
        
        return userBundle;
    }
    
    static DTNBundle generateNonFragmentBundle(byte[] message) {
        DTNBundle bundle = createTestUserBundle(message);
        
        bundle.primaryBlock.bundleProcessingControlFlags
            = bundle.primaryBlock.bundleProcessingControlFlags
            .clearBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT);
        
        bundle.primaryBlock.detailsIfFragment.clear();
        
        return bundle;
    }
    
    private static BigInteger generateBundlePCFsForUserBundle() {
        
        return BigInteger.ZERO
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT)
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_CUSTODY_TRANSFER_REQUESTED)
            .setBit(PrimaryBlock.BundlePCF.DESTINATION_ENDPOINT_IS_A_SINGLETON)
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_DELIVERY_REPORT_REQUESTED);
    }
    
    private static PrimaryBlock makePrimaryBlockForUserBundle() {
        PrimaryBlock primaryBlock = new PrimaryBlock();
        
        primaryBlock.bundleProcessingControlFlags
            = generateBundlePCFsForUserBundle();
        primaryBlock.priorityClass = TEST_PRIORITY;
        primaryBlock.bundleID = DTNBundleID.from(
            DTNEndpointID.parse(TEST_SENDER),
            System.currentTimeMillis()
        );
        primaryBlock.lifeTime = TEST_LIFETIME.getDuration();
        primaryBlock.destinationEID = makeDTNEID();
        primaryBlock.custodianEID = makeDTNEID();
        primaryBlock.reportToEID = DTNEndpointID.from(primaryBlock.bundleID.sourceEID);
        
        if (primaryBlock.bundleProcessingControlFlags
            .testBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT)) {
            primaryBlock.detailsIfFragment.put(
                PrimaryBlock.FragmentField.FRAGMENT_OFFSET,
                Integer.toString(TEST_FRAGMENT_OFFSET)
            );
            primaryBlock.detailsIfFragment.put(
                PrimaryBlock.FragmentField.TOTAL_ADU_LENGTH,
                Integer.toString(69)
            );
        }
        
        return primaryBlock;
    }
    
    private static PayloadADU makePayloadForUserBundle(byte[] message) {
        
        PayloadADU payload = new PayloadADU();
        payload.ADU = Arrays.copyOf(message, message.length);
        
        return payload;
    }
    
    static final String TEST_SHORT_TEXT_MESSAGE = "William + Phoebe = <3";
}
