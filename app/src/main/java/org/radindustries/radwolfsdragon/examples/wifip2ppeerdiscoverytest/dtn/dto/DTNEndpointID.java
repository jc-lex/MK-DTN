package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;
import java.util.Objects;

public final class DTNEndpointID implements Serializable {
    
    public static final String DTN_SCHEME = "dtn";
    private static final String SEP = ":";
    
    public String scheme;
    public String ssp;
    
    public static DTNEndpointID from(String scheme, String ssp) {
        DTNEndpointID endpointID = new DTNEndpointID();
        endpointID.scheme = new String(scheme.getBytes());
        endpointID.ssp = new String (ssp.getBytes());
        return endpointID;
    }
    
    public static DTNEndpointID from (DTNEndpointID endpointID) {
        return from(endpointID.scheme, endpointID.ssp);
    }
    
    public static DTNEndpointID parse(String eidString) {
        String[] parts = eidString.split(SEP);
        return from(parts[0], parts[1]);
    }
    
    @Override
    public String toString() {
        return scheme + SEP + ssp;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.scheme);
        hash = 29 * hash + Objects.hashCode(this.ssp);
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
        final DTNEndpointID other = (DTNEndpointID) obj;
        if (!Objects.equals(this.scheme, other.scheme)) {
            return false;
        }
        if (!Objects.equals(this.ssp, other.ssp)) {
            return false;
        }
        return true;
    }
}
