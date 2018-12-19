package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleID;

public interface AdminAA2Daemon extends EIDProvider {
    void transmit(DTNBundle adminRecord);
    void notifyOutboundBundleDelivered(String recipient);
    void delete(DTNBundleID bundleID);
    void delete(DTNBundleID bundleID, int fragmentOffset);
}
