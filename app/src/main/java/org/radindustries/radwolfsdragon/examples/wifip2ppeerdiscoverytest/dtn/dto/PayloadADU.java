package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.util.Arrays;

public final class PayloadADU extends BlockTypeSpecificDataFields {
    
    public byte[] ADU;
    
    @Override
    public String toString() {
        return "PayloadADU{" +
            "ADU=" + Arrays.toString(ADU) +
            "}";
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Arrays.hashCode(this.ADU);
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
        final PayloadADU other = (PayloadADU) obj;
        if (!Arrays.equals(this.ADU, other.ADU)) {
            return false;
        }
        return true;
    }
}
