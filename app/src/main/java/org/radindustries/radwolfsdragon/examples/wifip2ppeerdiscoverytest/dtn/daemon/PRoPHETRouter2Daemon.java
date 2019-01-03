package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;

public interface PRoPHETRouter2Daemon extends Router2Daemon {
    float getDeliveryPredictability(DTNEndpointID nodeEID);
}
