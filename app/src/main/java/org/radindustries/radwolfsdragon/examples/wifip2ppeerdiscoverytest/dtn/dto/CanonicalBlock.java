package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;
import java.math.BigInteger;

public final class CanonicalBlock implements Serializable {
    
    public enum BlockType {PAYLOAD, ADMIN_RECORD, AGE}
    
    public interface BlockPCF {
        int BLOCK_MUST_BE_REPLICATED_IN_ALL_FRAGMENTS = 0;
        int TRANSMIT_STATUS_REPORT_IF_BLOCK_CANNOT_BE_PROCESSED = 1;
        int DELETE_BUNDLE_IF_BLOCK_CANNOT_BE_PROCESSED = 2;
        int LAST_BLOCK = 3;
        int DISCARD_BLOCK_IF_IT_CANNOT_BE_PROCESSED = 4;
    }
    
    public BlockType blockType;
    public BigInteger blockProcessingControlFlags;
    public BlockTypeSpecificDataFields blockTypeSpecificDataFields;
    
    @Override
    public String toString() {
        return "CanonicalBlock{"
            + "blockType=" + blockType
            + ",blockProcessingControlFlags=" + blockProcessingControlFlags.toString(2)
            + ",blockTypeSpecificDataFields=" + blockTypeSpecificDataFields
            + '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CanonicalBlock)) return false;
        
        CanonicalBlock that = (CanonicalBlock) o;
        
        if (blockType != that.blockType) return false;
        if (!blockProcessingControlFlags.equals(that.blockProcessingControlFlags)) return false;
        return blockTypeSpecificDataFields.equals(that.blockTypeSpecificDataFields);
    }
    
    @Override
    public int hashCode() {
        int result = blockType.hashCode();
        result = 31 * result + blockProcessingControlFlags.hashCode();
        result = 31 * result + blockTypeSpecificDataFields.hashCode();
        return result;
    }
}
