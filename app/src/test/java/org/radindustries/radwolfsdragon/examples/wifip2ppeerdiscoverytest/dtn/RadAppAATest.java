package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNUI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.AppAA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PayloadADU;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeInstant;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.WallClock;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class RadAppAATest {
    
    private static final AppAA2Daemon daemon = new AppAA2Daemon() {
        @Override
        public boolean isUs(DTNEndpointID eid) {
            return false;
        }
    
        @Override
        public boolean isForUs(DTNBundle bundle) {
            return false;
        }
    
        @Override
        public boolean isFromUs(DTNBundle bundle) {
            return false;
        }
    
        @Override
        public void transmit(DTNBundle bundle, Daemon2Router.RoutingProtocol routingProtocol) {
            transmit(bundle);
        }
    
        @Override
        public void transmit(DTNBundle bundle) {
            assertFalse(bundle.primaryBlock.bundleProcessingControlFlags.testBit(
                    PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT
                )
            );
            
            assertNotNull(bundle.primaryBlock.detailsIfFragment);
            assertNotNull(bundle.canonicalBlocks);
            
            CanonicalBlock payloadCBlock
                = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.PAYLOAD);
            assertNotNull(payloadCBlock);
            
            PayloadADU adu = (PayloadADU) payloadCBlock.blockTypeSpecificDataFields;
            assertNotNull(adu.ADU);
            
            String text = new String(adu.ADU);
            assertEquals(TestUtilities.TEST_SHORT_TEXT_MESSAGE, text);
    
            assertEquals(TestUtilities.TEST_LIFETIME.getPeriod(), bundle.primaryBlock.lifeTime);
    
            assertEquals(TestUtilities.TEST_PRIORITY, bundle.primaryBlock.priorityClass);
        }
    
        @Override
        public DTNEndpointID getThisNodezEID() {
            return TestUtilities.makeDTNEID();
        }
    
        @Override
        public Set<DTNEndpointID> getPeerList() {
            return null;
        }
    
        @Override
        public List<DTNBundle> getDeliveredMessages() {
            return null;
        }
    };
    
    private static final DTNUI ui = new DTNUI() {
        @Override
        public void onReceiveDTNMessage(byte[] message, String sender) {
            String text = new String(message);
            assertEquals(TestUtilities.TEST_SHORT_TEXT_MESSAGE, text);
            
            assertEquals(TestUtilities.TEST_SENDER, sender);
        }
    
        @Override
        public void onOutboundBundleReceived(String recipient) {
        
        }
    
        @Override
        public void onOutboundBundleDeliveryFailed(String recipient, String reason) {
        
        }
    
        @Override
        public void onPeerListChanged(String[] peerList) {
        
        }
    };
    
    private static final WallClock clock = new WallClock() {
        @Override
        public DTNTimeInstant getCurrentTime() {
            return DTNTimeInstant.at(System.currentTimeMillis());
        }
    };
    
    private static RadAppAA appAA;
    
    @BeforeClass
    public static void setUp() {
        appAA = new RadAppAA(ui, daemon, clock);
    }
    
    @Test
    public void testSending() {
        appAA.send(
            TestUtilities.TEST_SHORT_TEXT_MESSAGE.getBytes(),
            TestUtilities.TEST_RECIPIENT,
            TestUtilities.TEST_PRIORITY,
            TestUtilities.TEST_LIFETIME,
            TestUtilities.TEST_PROTOCOL
        );
    }
    
    @Test
    public void testDeliveringBundle() {
        DTNBundle testBundle = TestUtilities.createTestUserBundle(
            TestUtilities.TEST_SHORT_TEXT_MESSAGE.getBytes()
        );
        appAA.deliver(testBundle);
    }
    
    @AfterClass
    public static void tearDown() {
        appAA = null;
    }
}
