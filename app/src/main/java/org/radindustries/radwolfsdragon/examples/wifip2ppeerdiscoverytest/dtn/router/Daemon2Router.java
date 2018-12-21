package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;

import java.util.Set;

public interface Daemon2Router {
    enum RoutingProtocol {
        //flooding-based
        EPIDEMIC, SPRAY_AND_WAIT, DIRECT_CONTACT, TWO_HOP,
        
        //forwarding-based
        PER_HOP, NECTAR, PER_CONTACT
    }
    
    Set<DTNBundleNode> chooseNextHop(
        Set<DTNBundleNode> neighbours, RoutingProtocol routingProtocol, DTNBundle bundle
    );
}
