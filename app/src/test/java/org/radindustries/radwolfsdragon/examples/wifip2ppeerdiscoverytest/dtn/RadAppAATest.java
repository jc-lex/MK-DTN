package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNUI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.AppAA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PayloadADU;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;

import java.time.Instant;
import java.time.Period;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class RadAppAATest {
    
    private static final String TEST_TEXT_MESSAGE = "William + Phoebe = <3";
    private static final String TEST_RECIPIENT = "f1b1";
    private static final String TEST_SENDER = "f1f1";
    private static final Period TEST_LIFETIME = Period.ofDays(3);
    private static final PrimaryBlock.PriorityClass TEST_PRIORITY
        = PrimaryBlock.PriorityClass.NORMAL;
    
    // manual mock objects
    private static final AppAA2Daemon daemon = new AppAA2Daemon() {
        @Override
        public void transmit(DTNBundle bundle) {
            assertFalse(bundle.primaryBlock.bundleProcessingControlFlags.testBit(
                    PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT
                )
            );
            
            CanonicalBlock payloadCBlock
                = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.PAYLOAD);
            assertNotNull(payloadCBlock);
            
            PayloadADU adu = (PayloadADU) payloadCBlock.blockTypeSpecificDataFields;
            assertNotNull(adu.ADU);
            
            String text = new String(adu.ADU);
            assertEquals(TEST_TEXT_MESSAGE, text);
    
            assertEquals(TEST_LIFETIME, bundle.primaryBlock.lifeTime);
    
            assertEquals(TEST_PRIORITY, PrimaryBlock.PriorityClass.getPriorityClass(
                bundle.primaryBlock.bundleProcessingControlFlags));
        }
    
        @Override
        public DTNEndpointID getThisNodezEID() {
            // short IDs for testing purposes only
            String eid = Long.toHexString(UUID.randomUUID().getMostSignificantBits());
            return DTNEndpointID.from(DTNEndpointID.DTN_SCHEME, eid.substring(0, 8));
        }
    };
    
    private static final DTNUI ui = new DTNUI() {
        @Override
        public void onReceiveDTNMessage(byte[] message, String sender) {
            String text = new String(message);
            assertEquals(TEST_TEXT_MESSAGE, text);
            
            assertEquals(TEST_SENDER, sender);
        }
    
        @Override
        public void onOutboundBundleReceived(String recipient) {
        
        }
    };
    
    private static RadAppAA appAA;
    
    @BeforeClass
    public static void setUp() {
        appAA = new RadAppAA(ui, daemon);
    }
    
    @Test
    public void testSending() {
        appAA.send(
            TEST_TEXT_MESSAGE.getBytes(),
            TEST_RECIPIENT,
            TEST_PRIORITY,
            PrimaryBlock.LifeTime.THREE_DAYS
        );
    }
    
    @Test
    public void testDeliveringBundle() {
        DTNBundle testBundle = createTestUserBundle(TEST_TEXT_MESSAGE.getBytes());
        appAA.deliver(testBundle);
    }
    
    private DTNBundle createTestUserBundle(byte[] message) {
        PrimaryBlock primaryBlock = makePrimaryBlockForUserBundle();
        
        CanonicalBlock payloadCBlock = new CanonicalBlock();
        payloadCBlock.blockTypeSpecificDataFields = makePayloadForUserBundle(message);
        
        DTNBundle userBundle = new DTNBundle();
        userBundle.primaryBlock = primaryBlock;
        userBundle.canonicalBlocks = new HashMap<>();
        userBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.PAYLOAD, payloadCBlock);
        
        return userBundle;
    }
    
    private PayloadADU makePayloadForUserBundle(byte[] message) {
        
        PayloadADU payload = new PayloadADU();
        payload.ADU = Arrays.copyOf(message, message.length);
        
        return payload;
    }
    
    private PrimaryBlock makePrimaryBlockForUserBundle() {
        PrimaryBlock primaryBlock = new PrimaryBlock();
        
        primaryBlock.bundleID = DTNBundleID.from(
            DTNEndpointID.from(DTNEndpointID.DTN_SCHEME, TEST_SENDER), Instant.now()
        );
        
        return primaryBlock;
    }
    
    @AfterClass
    public static void tearDown() {
        appAA = null;
    }
}
