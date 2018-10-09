package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;

public interface ConvergenceLayerAdapter {
    void transmitBundle(DTNBundle dtnBundleToSend, String p2pEndpointId);
    void setRouter(CLAToRouter router);
}
