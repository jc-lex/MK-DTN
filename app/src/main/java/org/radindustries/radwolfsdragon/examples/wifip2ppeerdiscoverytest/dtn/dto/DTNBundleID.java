package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;

public final class DTNBundleID implements Serializable {
    public DTNEndpointID sourceEID;
    public long creationTimestamp;
    
    public static DTNBundleID from(DTNEndpointID src, long cts) {
        DTNBundleID id = new DTNBundleID();
        id.creationTimestamp = cts;
        id.sourceEID = DTNEndpointID.from(src);
        return id;
    }
    
    public static DTNBundleID from(DTNBundleID other) {
        return from(other.sourceEID, other.creationTimestamp);
    }
    
    @Override
    public String toString() {
        return "DTNBundleID{" +
            "sourceEID=" + sourceEID +
            ",creationTimestamp=" + creationTimestamp +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTNBundleID)) return false;
        
        DTNBundleID that = (DTNBundleID) o;
        
        if (creationTimestamp != that.creationTimestamp) return false;
        return sourceEID.equals(that.sourceEID);
    }
    
    @Override
    public int hashCode() {
        int result = sourceEID.hashCode();
        result = 31 * result + (int) (creationTimestamp ^ (creationTimestamp >>> 32));
        return result;
    }
}
