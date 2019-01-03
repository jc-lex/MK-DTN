package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class NECTARRepoTest {
    
    private static NECTARRepo repo;
    private static final String TEST_EXISTING_EID = "dtn:f1b1";
    private static final String TEST_NON_EXISTENT_EID = "dtn:b111";
    
    @BeforeClass
    public static void setUpClass() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        repo = NECTARRepo.getRepo(appContext);
    }
    
    @Before
    public void setUp() {
        NeighbourhoodIndex testNI = new NeighbourhoodIndex();
        testNI.setNodeEID(TEST_EXISTING_EID);
        testNI.setFirstEncounterTimestamp(System.currentTimeMillis());
        testNI.setMeetingCount(1);
        
        repo.insert(testNI);
    }
    
    @After
    public void tearDown() {
        repo.delete(TEST_EXISTING_EID);
    }
    
    @Test
    public void testQueryingNonExistentIndices() {
        NeighbourhoodIndex result = repo.getIndex(TEST_NON_EXISTENT_EID);
        assertNull(result);
    }
    
    @Test
    public void testQueryingExistingIndices() {
        NeighbourhoodIndex result = repo.getIndex(TEST_EXISTING_EID);
        assertNotNull(result);
        assertEquals(1, result.getMeetingCount());
    }
    
    @Test
    public void testUpdatingExistingIndices() {
        NeighbourhoodIndex index = repo.getIndex(TEST_EXISTING_EID);
        
        int expectedResult = 20;
        index.setMeetingCount(expectedResult);
        
        repo.update(index);
    
        index = repo.getIndex(TEST_EXISTING_EID);
        assertEquals(expectedResult, index.getMeetingCount());
    }
    
    @Test
    public void testDeletingIndices() {
        repo.delete(TEST_EXISTING_EID);

        NeighbourhoodIndex index = repo.getIndex(TEST_EXISTING_EID);
        assertNull(index);
    }
}
