package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeInstant;

import java.io.Serializable;

public final class DTNBundleID implements Serializable {
    public DTNEndpointID sourceEID;
    public DTNTimeInstant creationTimestamp;
    
    public static DTNBundleID from(DTNEndpointID src, DTNTimeInstant cts) {
        DTNBundleID id = new DTNBundleID();
        id.creationTimestamp = DTNTimeInstant.copyOf(cts);
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
        
        DTNBundleID bundleID = (DTNBundleID) o;
        
        if (!sourceEID.equals(bundleID.sourceEID)) return false;
        return creationTimestamp.equals(bundleID.creationTimestamp);
    }
    
    @Override
    public int hashCode() {
        int result = sourceEID.hashCode();
        result = 31 * result + creationTimestamp.hashCode();
        return result;
    }
}
