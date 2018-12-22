package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;

public interface NECTARRouter2Daemon extends Router2Daemon {
    int getMeetingFrequency(DTNEndpointID nodeEID);
}
