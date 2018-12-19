package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscoverer;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;

import java.util.Set;

public interface Daemon2PeerDiscoverer {
    Set<DTNBundleNode> getPeerList();
    void init();        // ------------------ both of these are temporary
    void cleanUp();     // ------------|
}
