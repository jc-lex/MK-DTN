package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RouterDBHandlerTest {
    
    private static final String TEST_EXISTING_EID = "dtn:f1b1";
    private static final String TEST_NON_EXISTENT_EID = "dtn:b111";
    private static final double EPSILON = 1e-7;
    
    private static RouterDBHandler repo;
    
    private NeighbourhoodIndex testNI;
    private DeliveryPredictability testDP;
    
    @BeforeClass
    public static void setUpClass() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        repo = RouterDBHandler.getHandler(appContext);
    }
    
    @Before
    public void setUp() {
        testNI = new NeighbourhoodIndex();
        testNI.setNodeEID(TEST_EXISTING_EID);
        testNI.setFirstEncounterTimestamp(System.currentTimeMillis());
        testNI.setMeetingCount(1);

        repo.insert(testNI);
        
        testDP = new DeliveryPredictability();
        testDP.setNodeEID(TEST_EXISTING_EID);
        testDP.setProbability(0.5F);
    
        repo.insert(testDP);
    }
    
    @After
    public void tearDown() {
        repo.deleteNI(TEST_EXISTING_EID);
        repo.deleteDP(TEST_EXISTING_EID);
    }
    
    @Test
    public void testQueryingNonExistentRecords() {
        assertNull(repo.getIndex(TEST_NON_EXISTENT_EID));
        assertNull(repo.getDP(TEST_NON_EXISTENT_EID));
    }
    
    @Test
    public void testQueryingExistingRecords() {
        NeighbourhoodIndex niResult = repo.getIndex(TEST_EXISTING_EID);
        assertNotNull(niResult);
        assertEquals(1, niResult.getMeetingCount());
        
        DeliveryPredictability dpResult = repo.getDP(TEST_EXISTING_EID);
        assertNotNull(dpResult);
        assertTrue(dpResult.getProbability() - 0.5F < EPSILON);
    }
    
    @Test
    public void testFailedInsert() { // learning test; they fail with an exception
        assertEquals(-69, repo.insert(testNI));
        assertEquals(-69, repo.insert(testDP));
    }
    
    @Test
    public void testUpdatingExistingIndices() {
        NeighbourhoodIndex index = repo.getIndex(TEST_EXISTING_EID);
        assertNotNull(index);
        
        int expectedResult = 20;
        index.setMeetingCount(expectedResult);
        
        assertEquals(1, repo.update(index));
    
        index = repo.getIndex(TEST_EXISTING_EID);
        assertNotNull(index);
        assertEquals(expectedResult, index.getMeetingCount());
    }
    
    @Test
    public void testUpdatingExistingDPs() {
        DeliveryPredictability dp = repo.getDP(TEST_EXISTING_EID);
        assertNotNull(dp);
        
        float expectedResult = 0.7F;
        dp.setProbability(expectedResult);
        
        assertEquals(1, repo.update(dp));
        
        dp = repo.getDP(TEST_EXISTING_EID);
        assertNotNull(dp);
        assertTrue(dp.getProbability() - expectedResult < EPSILON);
    }
    
    @Test
    public void testBulkQueryForDPs() {
        List<DeliveryPredictability> dps = repo.getAllDPs();
        assertNotNull(dps);
        assertTrue(!dps.isEmpty());
    }
    
    @Test
    public void testDeletingWithEIDs() {
        assertEquals(1, repo.deleteNI(TEST_EXISTING_EID));
        assertEquals(1, repo.deleteDP(TEST_EXISTING_EID));
        assertNull(repo.getIndex(TEST_EXISTING_EID));
        assertNull(repo.getDP(TEST_EXISTING_EID));
    }
}
