package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;

public interface CLAToRouter {
    void deliverDTNBundle(DTNBundle bundle);
    void notifyBundleForwardingComplete(int bundleNodeCount);
}
