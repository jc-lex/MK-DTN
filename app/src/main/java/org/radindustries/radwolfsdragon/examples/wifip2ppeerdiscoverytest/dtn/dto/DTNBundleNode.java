package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.util.HashMap;
import java.util.Objects;

public final class DTNBundleNode {
    
    public static final class CLAKey {
        private CLAKey() {}
        public static final String NEARBY = "nearby";
    }
    
    public DTNEndpointID dtnEndpointID;
    public HashMap<String, String> CLAAddresses;
    
    public static DTNBundleNode from(String eidString, String CLAAddress) {
        DTNBundleNode peerNode = new DTNBundleNode();
        peerNode.dtnEndpointID = DTNEndpointID.parse(eidString);
        peerNode.CLAAddresses = new HashMap<>();
        peerNode.CLAAddresses.put(DTNBundleNode.CLAKey.NEARBY, CLAAddress);
        return peerNode;
    }
    
    @Override
    public String toString() {
        return "DTNBundleNode{"
            + "dtnEndpointID=" + dtnEndpointID
            + ",CLAAddresses=" + CLAAddresses
            + '}';
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.dtnEndpointID);
        hash = 37 * hash + Objects.hashCode(this.CLAAddresses);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DTNBundleNode other = (DTNBundleNode) obj;
        if (!Objects.equals(this.dtnEndpointID, other.dtnEndpointID)) {
            return false;
        }
        if (!Objects.equals(this.CLAAddresses, other.CLAAddresses)) {
            return false;
        }
        return true;
    }
}
