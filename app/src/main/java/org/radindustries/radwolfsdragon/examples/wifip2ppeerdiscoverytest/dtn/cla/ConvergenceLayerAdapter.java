package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;

public interface ConvergenceLayerAdapter {
    void transmitBundle(DTNBundle dtnBundleToSend, DTNNode node);
    void setRouter(CLAToRouter router);
}
