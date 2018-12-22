package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;

public interface NECTARPeerDiscoverer2Daemon extends PeerDiscoverer2Daemon {
    void incrementMeetingFrequency(DTNEndpointID nodeEID);
}
