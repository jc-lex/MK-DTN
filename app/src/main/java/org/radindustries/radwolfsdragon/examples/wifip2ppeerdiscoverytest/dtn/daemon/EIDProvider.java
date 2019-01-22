package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;

public interface EIDProvider {
    DTNEndpointID getThisNodezEID();
    boolean isUs(DTNEndpointID eid);
    boolean isForUs(DTNBundle bundle);
    boolean isFromUs(DTNBundle bundle);
}
