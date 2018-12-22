package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.support.annotation.NonNull;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.NECTARRouter2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.Router2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2NECTARRouter;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class RadRouter implements Daemon2Router, Daemon2NECTARRouter {
    
    private Router2Daemon daemon;
    private NECTARRouter2Daemon NECTARDaemon;
    
    private RadRouter() {}
    
    RadRouter(@NonNull Router2Daemon daemon, @NonNull NECTARRouter2Daemon NECTARDaemon) {
        this.daemon = daemon;
        this.NECTARDaemon = NECTARDaemon;
    }
    
    @Override
    public Set<DTNBundleNode> chooseNextHop(
        Set<DTNBundleNode> neighbours, RoutingProtocol routingProtocol, DTNBundle bundle
    ) {
        Set<DTNBundleNode> nextHop = Collections.emptySet();
        
        if (neighbours.isEmpty()) return nextHop;
        
        Set<DTNEndpointID> EIDs = getNeighbourEIDs(neighbours);
        if (EIDs.contains(bundle.primaryBlock.destinationEID)) {
            return findDestination(neighbours, bundle.primaryBlock.destinationEID);
        }
        
        switch (routingProtocol) {
            case EPIDEMIC: nextHop = doEpidemicRouting(neighbours, bundle); break;
            case PER_HOP: nextHop = doPerHopRouting(neighbours, bundle); break;
            case TWO_HOP: nextHop = doTwoHopRouting(neighbours, bundle); break;
            case SPRAY_AND_WAIT: nextHop = doSprayAndWaitRouting(neighbours, bundle); break;
            case NECTAR: nextHop = doNECTARRouting(neighbours); break;
            case PROPHET: nextHop = doProPHETRouting(neighbours, bundle); break;
            case DIRECT_CONTACT: default: break;
        }
        
        return nextHop;
    }
    
    private Set<DTNBundleNode> doProPHETRouting(Set<DTNBundleNode> neighbours, DTNBundle bundle) {
        return Collections.emptySet();
    }
    
    private Set<DTNBundleNode> doNECTARRouting(Set<DTNBundleNode> neighbours) {
        Set<DTNEndpointID> EIDs = getNeighbourEIDs(neighbours);
        DTNEndpointID[] EIDArray = EIDs.toArray(new DTNEndpointID[]{});
        int[] freq = new int[EIDArray.length];
        
        int maxFreq = 0;
        
        for (int i = 0; i < EIDArray.length; i++) {
            freq[i] = NECTARDaemon.getMeetingFrequency(EIDArray[i]);
            if (freq[i] > maxFreq) maxFreq = freq[i];
        }
        
        Set<DTNBundleNode> selection = new HashSet<>();
        for (int j = 0; j < freq.length; j++) {
            if (freq[j] == maxFreq) {
                for (DTNBundleNode node : neighbours) {
                    if (node.dtnEndpointID.equals(EIDArray[j])) {
                        selection.add(node);
                    }
                }
            }
        }
        return selection;
    }
    
    private Set<DTNBundleNode> doSprayAndWaitRouting(
        Set<DTNBundleNode> neighbours, DTNBundle bundle
    ) {
        DTNEndpointID previousCustodian = bundle.primaryBlock.custodianEID;
        DTNEndpointID source = bundle.primaryBlock.bundleID.sourceEID;
        
        if (!(source.equals(daemon.getThisNodezEID()) || previousCustodian.equals(source))) {
            // waiting phase
            Set<DTNEndpointID> EIDs = getNeighbourEIDs(neighbours);
            
            if (EIDs.contains(bundle.primaryBlock.destinationEID)) {
                return findDestination(neighbours, bundle.primaryBlock.destinationEID);
            } else {
                return Collections.emptySet();
            }
        } else {
            // spraying phase
            return selectRandomly(neighbours, NUM_MULTICAST_NODES);
        }
    }
    
    private Set<DTNBundleNode> doTwoHopRouting(Set<DTNBundleNode> neighbours, DTNBundle bundle) {
        if (bundle.primaryBlock.bundleID.sourceEID.equals(daemon.getThisNodezEID())) {
            return selectRandomly(neighbours, NUM_MULTICAST_NODES);
        } else {
            return Collections.emptySet();
        }
    }
    
    private Set<DTNBundleNode> doEpidemicRouting(Set<DTNBundleNode> neighbours, DTNBundle bundle) {
        if (bundle.primaryBlock.bundleID.sourceEID.equals(daemon.getThisNodezEID())) {
            return neighbours;
        } else {
            Set<DTNEndpointID> EIDs = getNeighbourEIDs(neighbours);
    
            Set<DTNBundleNode> remainingNeighbours = neighbours;
            
            if (EIDs.contains(bundle.primaryBlock.bundleID.sourceEID)) {
                remainingNeighbours
                    = removeSource(neighbours, bundle.primaryBlock.bundleID.sourceEID);
            }
            
            return remainingNeighbours;
        }
    }
    
    private Set<DTNBundleNode> doPerHopRouting(Set<DTNBundleNode> neighbours, DTNBundle bundle) {
        if (bundle.primaryBlock.bundleID.sourceEID.equals(daemon.getThisNodezEID())) {
            return selectRandomly(neighbours, NUM_UNICAST_NODES);
        } else {
            return perHopSelectionIfIntermediate(neighbours, bundle);
        }
    }
    
    private Set<DTNBundleNode> perHopSelectionIfIntermediate(
        Set<DTNBundleNode> neighbours, DTNBundle bundle
    ) {
        Set<DTNEndpointID> EIDs = getNeighbourEIDs(neighbours);
        Set<DTNBundleNode> remainingNeighbours = neighbours;
        
        if (EIDs.contains(bundle.primaryBlock.bundleID.sourceEID)) {
            remainingNeighbours = removeSource(neighbours, bundle.primaryBlock.bundleID.sourceEID);
        }
        
        return selectRandomly(remainingNeighbours, NUM_UNICAST_NODES);
    }
    
    private Set<DTNEndpointID> getNeighbourEIDs(Set<DTNBundleNode> neighbours) {
        Set<DTNEndpointID> EIDs = new HashSet<>();
        
        for (DTNBundleNode node : neighbours) {
            EIDs.add(node.dtnEndpointID);
        }
        
        return EIDs;
    }
    
    private Set<DTNBundleNode> findDestination(
        Set<DTNBundleNode> neighbours, DTNEndpointID destinationEID
    ) {
        Set<DTNBundleNode> destinationSet = new HashSet<>();
        
        for (DTNBundleNode node : neighbours) {
            if (node.dtnEndpointID.equals(destinationEID)) {
                destinationSet.add(node);
                return destinationSet;
            }
        }
        
        return Collections.emptySet();
    }
    
    private Set<DTNBundleNode> selectRandomly(Set<DTNBundleNode> neighbours, int numSelections) {
        Set<DTNBundleNode> randomSelection = new HashSet<>();
        DTNBundleNode[] nodesArray = neighbours.toArray(new DTNBundleNode[]{});
        
        int randomNumber;
        for (int i = 0; i < numSelections; i++) {
            randomNumber = (int) (Math.random() * nodesArray.length); // Z : [0, len)
            randomSelection.add(nodesArray[randomNumber]);
        }
        
        return randomSelection;
    }
    
    private Set<DTNBundleNode> removeSource(
        Set<DTNBundleNode> neighbours, DTNEndpointID sourceEID
    ) {
        Set<DTNBundleNode> setWithoutSource = new HashSet<>();
        
        for (DTNBundleNode node : neighbours) {
            if (!node.dtnEndpointID.equals(sourceEID)) {
                setWithoutSource.add(node);
            }
        }
        
        return setWithoutSource;
    }
}
