package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;

public interface CLA2Daemon extends EIDProvider {
    void onBundleReceived(DTNBundle bundle);
//    void onTransmissionComplete(int numNodesSentTo);
}
