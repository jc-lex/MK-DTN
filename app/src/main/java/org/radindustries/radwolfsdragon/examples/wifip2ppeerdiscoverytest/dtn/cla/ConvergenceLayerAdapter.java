package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;

import java.util.Set;

public interface ConvergenceLayerAdapter {
    void transmitBundle(DTNBundle dtnBundleToSend, Set<DTNNode> nodes);
    void setRouter(CLAToRouter router);
}
