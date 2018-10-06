package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import java.util.List;

public interface PeerDiscoverer {
    List<String> getPeerList(); // TODO find somewhere else to put this thing
    void discover();
    void cleanUp();
}
