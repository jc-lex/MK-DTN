package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;

public final class CanonicalBlock implements Serializable {
    
    public enum BlockType {
        PAYLOAD, ADMIN_RECORD, AGE, NECTAR_ROUTING_INFO, PROPHET_ROUTING_INFO
    }
    
    public BlockType blockType;
    public boolean mustBeReplicatedInAllFragments;
    public BlockTypeSpecificDataFields blockTypeSpecificDataFields;
    
    @Override
    public String toString() {
        return "CanonicalBlock{" +
            "blockType=" + blockType +
            ",mustBeReplicatedInAllFragments=" + mustBeReplicatedInAllFragments +
            ",blockTypeSpecificDataFields=" + blockTypeSpecificDataFields +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CanonicalBlock)) return false;
        
        CanonicalBlock that = (CanonicalBlock) o;
        
        if (mustBeReplicatedInAllFragments != that.mustBeReplicatedInAllFragments) return false;
        if (blockType != that.blockType) return false;
        return blockTypeSpecificDataFields.equals(that.blockTypeSpecificDataFields);
    }
    
    @Override
    public int hashCode() {
        int result = blockType.hashCode();
        result = 31 * result + (mustBeReplicatedInAllFragments ? 1 : 0);
        result = 31 * result + blockTypeSpecificDataFields.hashCode();
        return result;
    }
}
