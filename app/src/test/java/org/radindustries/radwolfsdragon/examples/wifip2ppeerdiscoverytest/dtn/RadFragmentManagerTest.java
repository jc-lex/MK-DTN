package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.fragmentmanager.Daemon2FragmentManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RadFragmentManagerTest {
    private static Daemon2FragmentManager radFragMgr;
    
    @BeforeClass
    public static void setUpClass() {
        radFragMgr = new RadFragMgr();
    }
    
    @Test
    public void testFragmentingShortMessages() {
        DTNBundle expectedResult = TestUtilities.generateNonFragmentBundle(
            TestUtilities.TEST_SHORT_TEXT_MESSAGE.getBytes()
        );
    
        DTNBundle[] testFragments = radFragMgr.fragment(expectedResult);
    
        assertEquals(1, testFragments.length);
        assertEquals(expectedResult, testFragments[0]);
    }
    
    @Test
    public void testFragmentingFragments() {
        DTNBundle expectedResult = TestUtilities.createTestUserBundle(
            TestUtilities.TEST_LONG_TEXT_MESSAGE.getBytes()
        );
    
        DTNBundle[] testFragments = radFragMgr.fragment(expectedResult);
        
        assertEquals(1, testFragments.length);
        assertEquals(expectedResult, testFragments[0]);
    }
    
    @Test
    public void testFragmentingLongMessages() {
        DTNBundle testBundle = TestUtilities.generateNonFragmentBundle(
            TestUtilities.TEST_LONG_TEXT_MESSAGE.getBytes()
        );
    
        DTNBundle[] testFragments = radFragMgr.fragment(testBundle);
        
        assertTrue(testFragments.length > 1);
    }
    
    @Test
    public void testDefragmentation() {
        DTNBundle expectedResult = TestUtilities.generateNonFragmentBundle(
            TestUtilities.TEST_LONG_TEXT_MESSAGE.getBytes()
        );
        
        DTNBundle[] testFragments = radFragMgr.fragment(expectedResult);
    
        assertTrue(radFragMgr.defragmentable(testFragments));
    
        DTNBundle actualResult = null;
    
        if (radFragMgr.defragmentable(testFragments))
            actualResult = radFragMgr.defragment(testFragments);
    
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void testDefragmentingNonFragments() {
        DTNBundle testBundle = TestUtilities.generateNonFragmentBundle(
            TestUtilities.TEST_LONG_TEXT_MESSAGE.getBytes()
        );
        DTNBundle[] testFragments = new DTNBundle[] {testBundle};
        
        assertFalse(radFragMgr.defragmentable(testFragments));
    }
    
    @Test
    public void testDefragmentingNonrelatedFragments() {
        DTNBundle bigFragment = TestUtilities.createTestUserBundle(
            TestUtilities.TEST_LONG_TEXT_MESSAGE.getBytes()
        );
        DTNBundle smallFragment = TestUtilities.createTestUserBundle(
            TestUtilities.TEST_SHORT_TEXT_MESSAGE.getBytes()
        );
        
        DTNBundle[] testFragments = new DTNBundle[]{smallFragment, bigFragment};
        
        assertFalse(radFragMgr.defragmentable(testFragments));
    }
    
    @AfterClass
    public static void tearDownClass() {
        radFragMgr = null;
    }
    
    
}
