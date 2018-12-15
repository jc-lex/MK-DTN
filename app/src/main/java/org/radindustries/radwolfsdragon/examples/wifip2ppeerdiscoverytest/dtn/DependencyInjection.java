package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNAPI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNUI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.ConvergenceLayerAdapter;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery.PeerDiscovery;

public final class DependencyInjection {
    private static RadP2PService radP2PService = null;
    private static RadAppAA radAppAA = null;

    private DependencyInjection() {}

    public static ConvergenceLayerAdapter getConvergenceLayerAdapter(Context context) {
        if (radP2PService == null) {
            radP2PService = new RadP2PService(context);
        }
        return radP2PService;
    }

    public static PeerDiscovery getPeerDiscoverer(Context context) {
        if (radP2PService == null) {
            radP2PService = new RadP2PService(context);
        }
        return radP2PService;
    }
    
    public static DTNAPI getDTNClient(DTNUI ui) {
        if (radAppAA == null) {
            radAppAA = new RadAppAA(ui, null);
        }
        return radAppAA;
    }
}
