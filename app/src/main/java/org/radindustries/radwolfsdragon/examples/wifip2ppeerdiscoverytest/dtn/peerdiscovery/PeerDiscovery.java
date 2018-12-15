package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;

import java.util.Set;

public interface PeerDiscovery {
    Set<DTNBundleNode> getPeerList();
    void init();
    void cleanUp();
    void setThisBundleNodezEndpointId(DTNEndpointID thisBundleNodezEndpointId);
}
