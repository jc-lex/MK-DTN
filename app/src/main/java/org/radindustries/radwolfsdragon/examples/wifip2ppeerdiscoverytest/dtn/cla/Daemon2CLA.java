package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;

import java.util.Set;

public interface Daemon2CLA {
    int transmit(DTNBundle bundle, Set<DTNBundleNode> destinations)
        /*throws InterruptedException*/;
    boolean transmit(DTNBundle bundle, DTNBundleNode destination)
        /*throws InterruptedException*/;
}
