package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.support.annotation.NonNull;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.DTNManager2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.DTNManager;

final class RadManager implements DTNManager {
    
    private DTNManager2Daemon daemon;
    
    private RadManager() {}
    
    RadManager(@NonNull DTNManager2Daemon daemon) {
        this.daemon = daemon;
    }
    
    @Override
    public boolean start() {
        return daemon.start();
    }
    
    @Override
    public boolean stop() {
        return daemon.stop();
    }
}
