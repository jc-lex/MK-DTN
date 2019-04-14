package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.util.HashMap;

public final class DTNBundle implements Serializable {
    
    public interface CBlockNumber {
        int PAYLOAD = 0;
        int ADMIN_RECORD = 1;
        int NECTAR_ROUTING_INFO = 3;
        int PROPHET_ROUTING_INFO = 4;
    }
    
    interface FragmentField {
        String FRAGMENT_OFFSET = "fragment_offset";
    }
    
    public PrimaryBlock primaryBlock;
    
    @SuppressLint("UseSparseArrays")
    public HashMap<Integer, CanonicalBlock> canonicalBlocks = new HashMap<>();
    
    public long timeOfDelivery;
    
    @Override
    public String toString() {
        return "DTNBundle{"
            + "primaryBlock=" + primaryBlock
            + ",canonicalBlocks=" + canonicalBlocks
            + '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTNBundle)) return false;
        
        DTNBundle bundle = (DTNBundle) o;
        
        if (!primaryBlock.equals(bundle.primaryBlock)) return false;
        return canonicalBlocks.equals(bundle.canonicalBlocks);
    }
    
    @Override
    public int hashCode() {
        int result = primaryBlock.hashCode();
        result = 31 * result + canonicalBlocks.hashCode();
        return result;
    }
}
