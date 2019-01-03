package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;

public interface PRoPHETCLA2Daemon extends CLA2Daemon {
    void calculateDPTransitivity(DTNBundle bundle);
}
