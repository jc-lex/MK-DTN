package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;

public interface PRoPHETPeerDiscoverer2Daemon extends PeerDiscoverer2Daemon {
    void updateDeliveryPredictability(DTNEndpointID nodeEID);
}
