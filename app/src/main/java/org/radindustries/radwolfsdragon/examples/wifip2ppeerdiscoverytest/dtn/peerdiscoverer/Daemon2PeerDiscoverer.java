package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscoverer;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.BuildConfig;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;

import java.util.Set;

public interface Daemon2PeerDiscoverer {
    enum ServiceMode {SOURCE, SINK}
    String DTN_SERVICE_ID = BuildConfig.APPLICATION_ID;
    String SINK_SERVICE = DTN_SERVICE_ID + ".SINK";
    String SOURCE_SERVICE = DTN_SERVICE_ID + ".SOURCE";
    
    Set<DTNBundleNode> getPeerList();
    void start(ServiceMode serviceMode);
    void stop(ServiceMode serviceMode);
}
