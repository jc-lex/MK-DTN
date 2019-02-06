package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.util.Log;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;

import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

final class RadWallClock {
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + RadWallClock.class.getSimpleName();
    
    private BigInteger currentTime;
    synchronized BigInteger getCurrentTime() {
        return currentTime;
    }
    private synchronized void setCurrentTime(BigInteger currentTime) {
        this.currentTime = currentTime;
    }
    
    private static RadWallClock clock = null;
    private RadWallClock(@NonNull Context context) {
        DTNTimeDB timeDB = DTNTimeDB.getTimeDB(context);
        timeDAO = timeDB.getTimeDAO();
        timeKeeper = Executors.newSingleThreadExecutor();
    }
    static synchronized RadWallClock getClock(@NonNull Context context) {
        if (clock == null) clock = new RadWallClock(context);
        return clock;
    }
    
    private void tick() {
        currentTime = currentTime.add(BigInteger.ONE);
    }
    
    private class TicktockTask implements Runnable {
        @Override
        public void run() {
            Log.i(LOG_TAG, "DTN wall clock started");
            while (!Thread.interrupted()) {
                tick();
            }
            Log.i(LOG_TAG, "DTN wall clock stopped");
        }
    }
    
    private ExecutorService counter;
    
    void start() {
        while (getTime() == null) insertTime(new DTNTime()); // first-time initialisation
        
        setCurrentTime(new BigInteger(getTime().getCurrentTime()));
    
        counter = Executors.newSingleThreadExecutor();
        counter.submit(new TicktockTask());
    }
    
    void stop() {
        counter.shutdown();
        counter.shutdownNow();
    
        if (getTime() != null) {
            DTNTime time = getTime();
            time.setCurrentTime(getCurrentTime().toString());
        
            updateTime(time);
        }
    
        try {
            counter.awaitTermination(5L, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private ExecutorService timeKeeper;
    private DTNTimeDAO timeDAO;
    
    private void insertTime(final DTNTime time) {
        timeKeeper.submit(new Runnable() {
            @Override
            public void run() {
                timeDAO.insert(time);
            }
        });
    }
    
    private void updateTime(final DTNTime time) {
        timeKeeper.submit(new Runnable() {
            @Override
            public void run() {
                timeDAO.update(time);
            }
        });
    }
    
    private DTNTime getTime() {
        Future<DTNTime> future = timeKeeper.submit(new Callable<DTNTime>() {
            @Override
            public DTNTime call() throws Exception {
                return timeDAO.getTime();
            }
        });
    
        try {
            return future.get(5L, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }
}
