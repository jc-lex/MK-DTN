package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;
import java.time.Instant;

public final class DTNBundleID implements Serializable {
    public DTNEndpointID sourceEID;
    public Instant creationTimestamp;
    
    public static DTNBundleID from(DTNEndpointID src, Instant cts) {
        DTNBundleID id = new DTNBundleID();
        id.creationTimestamp = Instant.parse(cts.toString());
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
        
        if (!sourceEID.equals(that.sourceEID)) return false;
        return creationTimestamp.equals(that.creationTimestamp);
    }
    
    @Override
    public int hashCode() {
        int result = sourceEID.hashCode();
        result = 31 * result + creationTimestamp.hashCode();
        return result;
    }
}
