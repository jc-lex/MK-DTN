package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.util.HashMap;

public final class DTNBundleNode {
    
    public interface CLAKey {
        String NEARBY = "nearby";
    }
    
    public DTNEndpointID dtnEndpointID;
    public HashMap<String, String> CLAAddresses = new HashMap<>();
    
    public static DTNBundleNode from(String eidString, String CLAAddress) {
        DTNBundleNode peerNode = new DTNBundleNode();
        peerNode.dtnEndpointID = DTNEndpointID.parse(eidString);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTNBundleNode)) return false;
        
        DTNBundleNode that = (DTNBundleNode) o;
        
        if (!dtnEndpointID.equals(that.dtnEndpointID)) return false;
        return CLAAddresses.equals(that.CLAAddresses);
    }
    
    @Override
    public int hashCode() {
        int result = dtnEndpointID.hashCode();
        result = 31 * result + CLAAddresses.hashCode();
        return result;
    }
}
