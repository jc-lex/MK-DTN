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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayloadADU)) return false;
        
        PayloadADU that = (PayloadADU) o;
    
        return Arrays.equals(ADU, that.ADU);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(ADU);
    }
}
