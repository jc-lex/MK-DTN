package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import android.content.Context;

public final class DependencyInjection {
    private static PeerDiscoverer discoverer = null;

    private DependencyInjection() {}

    public static PeerDiscoverer getPeerDiscoverer(Context context) {
        if (discoverer == null) {
            discoverer = new RadPeerDiscoverer(context);
        }
        return discoverer;
    }


}
