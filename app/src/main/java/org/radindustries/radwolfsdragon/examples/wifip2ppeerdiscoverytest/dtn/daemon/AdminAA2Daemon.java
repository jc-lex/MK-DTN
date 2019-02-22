package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleID;

public interface AdminAA2Daemon extends EIDProvider, AA2Daemon {
    void notifyBundleStatus(String recipient, String msg);
    void delete(DTNBundleID bundleID);
    void delete(DTNBundleID bundleID, int fragmentOffset);
}
