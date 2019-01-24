package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import org.junit.Test;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    
    enum Day {SUN}
    
    @Test
    public void testEnumsFromStrings() {
        Day today = Day.SUN;
        
        String todayStr = today.toString();
        
        assertEquals(today, Day.valueOf(todayStr));
    }
    
    @Test
    public void testSysTime() {
        System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());
        System.out.println("System.nanoTime() = " + System.nanoTime());
        System.out.println("Long.MAX_VALUE = " + Long.MAX_VALUE);
        
        long yearMillis = 1000 * 60 * 60;
        long l2 = 24 * 7 * 4 * 12;
    
        BigInteger year = BigInteger.valueOf(yearMillis).multiply(BigInteger.valueOf(l2));
        System.out.println("year = " + year);
        
        BigInteger years = BigInteger.valueOf(Long.MAX_VALUE).divide(year);
        System.out.println("years = " + years);
    
        // UTC uses atomic clocks & its the scientific version... only 4 real men B)
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
        System.out.println("c.getTimeInMillis() = " + c.getTimeInMillis());
        System.out.println("c.getTimeZone() = " + c.getTimeZone());
    }
    
    @Test
    public void testArraySorting() {
        int[] f = new int[]{3, 4, 1, 5, 2, 7, 1, 4, 2};
        Arrays.sort(f);
        assertEquals(7, f[8]);
    
//        ArrayList<int[]> fList = (ArrayList<int[]>) Arrays.asList(f);
    }
    
    @Test
    public void testSynchronisedInboundQueue() {
        List<DTNBundle> inboundQueue
            = Collections.synchronizedList(new LinkedList<DTNBundle>());
        
        DTNBundle testBundle = new DTNBundle();
        
        assertTrue(inboundQueue.add(testBundle));
        assertTrue(inboundQueue.add(new DTNBundle()));
        
        synchronized (inboundQueue) {
            ListIterator<DTNBundle> iterator = inboundQueue.listIterator();
            assertTrue(iterator.hasNext());
            assertEquals(testBundle, iterator.next());
        }
    }
}