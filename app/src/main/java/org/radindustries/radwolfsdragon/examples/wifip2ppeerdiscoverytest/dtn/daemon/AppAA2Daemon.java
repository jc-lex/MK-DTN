package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;

public interface AppAA2Daemon extends EIDProvider, AA2Daemon {
    PrimaryBlock.PriorityClass DEFAULT_PRIORITY_CLASS
        = PrimaryBlock.PriorityClass.NORMAL;
    PrimaryBlock.LifeTime DEFAULT_LIFETIME
        = PrimaryBlock.LifeTime.THREE_DAYS;
    Daemon2Router.RoutingProtocol DEFAULT_ROUTING_PROTOCOL
        = Daemon2Router.RoutingProtocol.PER_HOP;
    
    void transmit(DTNBundle bundle, Daemon2Router.RoutingProtocol routingProtocol);
}
