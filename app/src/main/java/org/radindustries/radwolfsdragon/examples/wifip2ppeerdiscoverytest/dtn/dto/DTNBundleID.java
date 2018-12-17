package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

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
//        if (!(obj instanceof DTNBundleID)) return false;
        final DTNBundleID other = (DTNBundleID) obj;
        if (!Objects.equals(this.sourceEID, other.sourceEID)) {
            return false;
        }
        if (!Objects.equals(this.creationTimestamp, other.creationTimestamp)) {
            return false;
        }
        return true;
//        return Objects.equals(sourceEID, other.sourceEID) &&
//            Objects.equals(creationTimestamp, other.creationTimestamp);
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.sourceEID);
        hash = 23 * hash + Objects.hashCode(this.creationTimestamp);
        return hash; /*Objects.hash(sourceEID, creationTimestamp)*/
    }
}
