package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;

public interface Daemon2PRoPHETRoutingTable {
    float P_ENC_MAX = 0.7F;
    float P_ENC_FIRST = 0.5F;
    float P_FIRST_THRESHOLD = 0.1F;
    float BETA = 0.9F;
    long HALF_LIFE_IN_SECONDS = 259_200L; // 3 days
    float DELTA = 0.01F;
    
    void updateDeliveryPredictability(DTNEndpointID nodeEID);
    void calculateDPTransitivity(DTNBundle bundle);
    float getDeliveryPredictability(DTNEndpointID nodeEID);
}
