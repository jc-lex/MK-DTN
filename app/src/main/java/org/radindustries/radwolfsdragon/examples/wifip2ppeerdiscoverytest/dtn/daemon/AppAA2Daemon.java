package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;

public interface AppAA2Daemon extends AA2Daemon {
    void transmit(DTNBundle bundle);
}
