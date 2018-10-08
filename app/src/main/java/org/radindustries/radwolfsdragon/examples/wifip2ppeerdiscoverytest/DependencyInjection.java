package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import android.content.Context;

public final class DependencyInjection {
    private static RadPeerDiscoverer discoverer = null;

    private DependencyInjection() {}

    public static PeerDiscovererToManager getPeerDiscovererToManager(Context context) {
        if (discoverer == null) {
            discoverer = new RadPeerDiscoverer(context);
        }
        return discoverer;
    }

    public static PeerDiscovererToDaemon getPeerDiscovererToDaemon(Context context) {
        if (discoverer == null) {
            discoverer = new RadPeerDiscoverer(context);
        }
        return discoverer;
    }

}
