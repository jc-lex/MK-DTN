package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;

public final class DTNEndpointID implements Serializable {
    
    public static final String DTN_SCHEME = "dtn";
    private static final String SEP = ":";
    
    public String scheme;
    public String ssp;
    
    public static DTNEndpointID from(String scheme, String ssp) {
        DTNEndpointID endpointID = new DTNEndpointID();
        endpointID.scheme = scheme;
        endpointID.ssp = ssp;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTNEndpointID)) return false;
        
        DTNEndpointID that = (DTNEndpointID) o;
        
        if (!scheme.equals(that.scheme)) return false;
        return ssp.equals(that.ssp);
    }
    
    @Override
    public int hashCode() {
        int result = scheme.hashCode();
        result = 31 * result + ssp.hashCode();
        return result;
    }
}
