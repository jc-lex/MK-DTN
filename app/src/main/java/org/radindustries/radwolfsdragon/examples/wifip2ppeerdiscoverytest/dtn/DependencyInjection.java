package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.support.annotation.NonNull;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNAPI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNUI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.Daemon2CLA;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.CLA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.PeerDiscoverer2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.fragmentmanager.Daemon2FragmentManager;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscoverer.Daemon2PeerDiscoverer;

public final class DependencyInjection {
    private static RadAppAA radAppAA = null;
    private static RadFragMgr radFragMgr = null;
    private static RadDiscoverer radDiscoverer = null;
    private static RadCLA radCLA = null;

    private DependencyInjection() {}
    
    public static DTNAPI getDTNClient(DTNUI ui) {
        if (radAppAA == null) {
            radAppAA = new RadAppAA(ui, null);
        }
        return radAppAA;
    }
    
    static Daemon2FragmentManager getFragmentManager() {
        if (radFragMgr == null) {
            radFragMgr = new RadFragMgr();
        }
        return radFragMgr;
    }
    
    public static Daemon2PeerDiscoverer getPeerDiscoverer(
        @NonNull PeerDiscoverer2Daemon d1, @NonNull CLA2Daemon d2, @NonNull Context context
    ) {
        if (radCLA == null) {
            radCLA = new RadCLA(d2, context);
        }
        if (radDiscoverer == null) {
            radDiscoverer = new RadDiscoverer(d1, context, radCLA);
        }
        return radDiscoverer;
    }
    
    public static Daemon2CLA getCLA(@NonNull CLA2Daemon daemon, @NonNull Context context) {
        if (radCLA == null) {
            radCLA = new RadCLA(daemon, context);
        }
        return radCLA;
    }
}
