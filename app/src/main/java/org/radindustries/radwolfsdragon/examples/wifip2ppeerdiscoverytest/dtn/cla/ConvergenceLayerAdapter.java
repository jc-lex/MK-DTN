package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;

public interface ConvergenceLayerAdapter {
    void transmit(DTNBundle dtnBundleToSend, DTNBundleNode node);
    void setRouter(CLAToRouter router);
}
