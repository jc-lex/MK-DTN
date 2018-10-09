package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery;

import java.util.HashMap;

public interface PeerDiscovery {
    HashMap<String, String> getDiscoveredBundleNodes();
    void init();
    void cleanUp();
    void setThisBundleNodezEndpointId(String thisBundleNodezEndpointId);
}
