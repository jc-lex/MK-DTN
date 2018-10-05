package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import android.content.Context;
import android.os.Looper;

public final class DependencyInjection {
    private static PeerDiscoverer discoverer = null;

    private DependencyInjection() {}

    public static PeerDiscoverer getPeerDiscoverer(Context context, Looper looper) {
        if (discoverer == null) {
            discoverer = new RadPeerDiscoverer(context, looper);
        }
        return discoverer;
    }


}
