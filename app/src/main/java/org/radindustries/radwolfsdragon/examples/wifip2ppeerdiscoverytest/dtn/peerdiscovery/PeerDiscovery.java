package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNNode;

import java.util.Set;

public interface PeerDiscovery {
    Set<DTNNode> getUpContacts();
    void init();
    void cleanUp();
    void setThisBundleNodezEndpointId(String thisBundleNodezEndpointId);
}
