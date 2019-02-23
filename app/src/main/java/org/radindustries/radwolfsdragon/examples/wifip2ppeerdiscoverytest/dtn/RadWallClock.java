package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.util.Log;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.Daemon2Managable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeInstant;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.WallClock;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Scanner;

import androidx.annotation.NonNull;

final class RadWallClock implements WallClock, Daemon2Managable {
    private static final long START_TIME = 0L;
    
    private long cycle;
    private long pulse;
    private Thread ticker;
    private class TickingTask implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) tick();
        }
        
        private void tick() {
            pulse++;
            if (pulse == Long.MAX_VALUE) {
                pulse = START_TIME;
                cycle++;
                if (cycle == Long.MAX_VALUE) {
                    cycle = START_TIME; // hopefully this will happen after a really long time
                }
            }
        }
    }
    
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + RadWallClock.class.getSimpleName();
    private static final String TIME_DB = "mkdtn.time";
    private Context context;
    RadWallClock(@NonNull Context context) {
        this.context = context;
    }
    
    @Override
    public boolean start() {
        readTime();
        if (ticker == null) {
            ticker = new Thread(new TickingTask());
            ticker.start();
        }
        return true;
    }
    
    private void readTime() {
        try (Scanner scanner = new Scanner(context.openFileInput(TIME_DB))) {
            cycle = Long.parseLong(scanner.nextLine());
            pulse = Long.parseLong(scanner.nextLine());
        } catch (Exception e) {
            Log.e(LOG_TAG, "time read failed", e);
            cycle = START_TIME;
            pulse = START_TIME;
        }
    }
    
    private void writeTime() {
        context.deleteFile(TIME_DB);
        try (PrintWriter writer
                 = new PrintWriter(context.openFileOutput(TIME_DB, Context.MODE_PRIVATE))) {
            writer.println(cycle);
            writer.println(pulse);
        } catch (Exception e) {
            Log.e(LOG_TAG, "time write failed", e);
        }
    }
    
    @Override
    public boolean stop() {
        if (ticker != null) {
            ticker.interrupt();
            ticker = null;
        }
        writeTime();
        return true;
    }
    
    @Override
    public synchronized DTNTimeInstant getCurrentTime() {
        BigInteger currentTime = BigInteger.valueOf(pulse);
        currentTime = currentTime
            .add(BigInteger.valueOf(cycle).multiply(BigInteger.valueOf(Long.MAX_VALUE)));
        return DTNTimeInstant.at(currentTime);
    }
}
