package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;

import java.util.Set;

public interface AppAA2Daemon extends EIDProvider, AA2Daemon {
    void transmit(DTNBundle bundle, Daemon2Router.RoutingProtocol routingProtocol);
    Set<DTNEndpointID> getPeerList();
}
