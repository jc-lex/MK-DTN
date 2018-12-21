package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.support.annotation.NonNull;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.Router2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class RadRouter implements Daemon2Router {
    
    private Router2Daemon daemon;
    
    private RadRouter() {}
    
    RadRouter(@NonNull Router2Daemon daemon) {
        this.daemon = daemon;
    }
    
    @Override
    public Set<DTNBundleNode> chooseNextHop(
        Set<DTNBundleNode> neighbours, RoutingProtocol routingProtocol, DTNBundle bundle
    ) {
        Set<DTNBundleNode> nextHop = new HashSet<>();
        
        switch (routingProtocol) {
            case EPIDEMIC: nextHop = doEpidemicRouting(neighbours, bundle); break;
            case PER_HOP: nextHop = doPerHopRouting(neighbours, bundle); break;
            default: break;
        }
        
        return nextHop;
    }
    
    private Set<DTNBundleNode> doEpidemicRouting(Set<DTNBundleNode> neighbours, DTNBundle bundle) {
        if (bundle.primaryBlock.bundleID.sourceEID.equals(daemon.getThisNodezEID())) {
            return neighbours;
        } else {
            Set<DTNEndpointID> EIDs = getNeighbourEIDs(neighbours);
    
            Set<DTNBundleNode> remainingNeighbours = new HashSet<>();
    
            //remove the source from the list
            if (EIDs.contains(bundle.primaryBlock.bundleID.sourceEID)) {
                for (DTNBundleNode node : neighbours) {
                    if (!node.dtnEndpointID.equals(bundle.primaryBlock.bundleID.sourceEID)) {
                        remainingNeighbours.add(node);
                    }
                }
            }
            
            return remainingNeighbours;
        }
    }
    
    private Set<DTNBundleNode> doPerHopRouting(Set<DTNBundleNode> neighbours, DTNBundle bundle) {
        if (neighbours.size() == 0) {
            return neighbours;
        }
        else if (bundle.primaryBlock.bundleID.sourceEID.equals(daemon.getThisNodezEID())) {
            return perHopSelectionIfSource(neighbours, bundle);
        } else {
            return perHopSelectionIfIntermediate(neighbours, bundle);
        }
    }
    
    private Set<DTNEndpointID> getNeighbourEIDs(Set<DTNBundleNode> neighbours) {
        Set<DTNEndpointID> EIDs = new HashSet<>();
        
        for (DTNBundleNode node : neighbours) {
            EIDs.add(node.dtnEndpointID);
        }
        
        return EIDs;
    }
    
    private Set<DTNBundleNode> perHopSelectionIfSource(
        Set<DTNBundleNode> neighbours, DTNBundle bundle
    ) {
        Set<DTNEndpointID> EIDs = getNeighbourEIDs(neighbours);
        Set<DTNBundleNode> selection = new HashSet<>();
        
        if (EIDs.contains(bundle.primaryBlock.destinationEID)) {
            for (DTNBundleNode node : neighbours) {
                if (node.dtnEndpointID.equals(bundle.primaryBlock.destinationEID)) {
                    selection.add(node);
                    return selection;
                }
            }
        } else {
            DTNBundleNode[] nodesArray = neighbours.toArray(new DTNBundleNode[]{});
            int randomNumber = (int) (Math.random() * nodesArray.length); // Z : [0, len)
            selection.add(nodesArray[randomNumber]);
            return selection;
        }
        
        return Collections.emptySet();
    }
    
    private Set<DTNBundleNode> perHopSelectionIfIntermediate(
        Set<DTNBundleNode> neighbours, DTNBundle bundle
    ) {
        Set<DTNEndpointID> EIDs = getNeighbourEIDs(neighbours);
        
        Set<DTNBundleNode> remainingNeighbours = new HashSet<>();
        
        //remove the source from the list
        if (EIDs.contains(bundle.primaryBlock.bundleID.sourceEID)) {
            for (DTNBundleNode node : neighbours) {
                if (!node.dtnEndpointID.equals(bundle.primaryBlock.bundleID.sourceEID)) {
                    remainingNeighbours.add(node);
                }
            }
        }
        
        return perHopSelectionIfSource(remainingNeighbours, bundle);
    }
}
