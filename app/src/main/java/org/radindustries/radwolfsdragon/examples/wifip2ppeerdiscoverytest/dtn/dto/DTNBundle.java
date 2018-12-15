package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

public final class DTNBundle implements Serializable {
    
    // initial capacity = {payload/admin_record, age_block}
    public static final int INITIAL_BLOCK_CAPACITY = 3;
    
    public static final class CBlockNumber {
        private CBlockNumber() {}
        public static final int PAYLOAD = 0;
        public static final int ADMIN_RECORD = 1;
        public static final int AGE = 2;
    }
    
    interface FragmentField {
        String FRAGMENT_OFFSET = "fragment_offset";
    }
    
    public PrimaryBlock primaryBlock;
    public HashMap<Integer, CanonicalBlock> canonicalBlocks;
    
    @Override
    public String toString() {
        return "DTNBundle{"
            + "primaryBlock=" + primaryBlock
            + ",canonicalBlocks=" + canonicalBlocks
            + '}';
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.primaryBlock);
        hash = 97 * hash + Objects.hashCode(this.canonicalBlocks);
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
        final DTNBundle other = (DTNBundle) obj;
        if (!Objects.equals(this.primaryBlock, other.primaryBlock)) {
            return false;
        }
        if (!Objects.equals(this.canonicalBlocks, other.canonicalBlocks)) {
            return false;
        }
        return true;
    }
}
