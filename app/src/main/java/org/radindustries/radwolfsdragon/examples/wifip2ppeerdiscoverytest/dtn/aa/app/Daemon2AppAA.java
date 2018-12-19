package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;

import java.util.Set;

public interface Daemon2AppAA {
    void deliver(DTNBundle bundle);
    void notifyOutboundBundleReceived(String recipient);
    void notifyPeerListChanged(Set<DTNEndpointID> peers);
}
