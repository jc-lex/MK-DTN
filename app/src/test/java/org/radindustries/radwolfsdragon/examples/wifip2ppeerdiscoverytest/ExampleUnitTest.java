package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import org.junit.Test;

import static org.junit.Assert.*;

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
    }
}