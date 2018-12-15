package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public final class CanonicalBlock implements Serializable {
    
    public static final class TypeCode {
        private TypeCode() {}
        public static final BigInteger PAYLOAD = BigInteger.ONE;
        public static final BigInteger ADMIN_RECORD = new BigInteger("ff", 16);
        public static final BigInteger AGE = new BigInteger("fe", 16);
    }
    
    public static final class BlockPCF {
        private BlockPCF() {}
        public static final int BLOCK_MUST_BE_REPLICATED_IN_ALL_FRAGMENTS = 0;
        public static final int TRANSMIT_STATUS_REPORT_IF_BLOCK_CANNOT_BE_PROCESSED = 1;
        public static final int DELETE_BUNDLE_IF_BLOCK_CANNOT_BE_PROCESSED = 2;
        public static final int LAST_BLOCK = 3;
        public static final int DISCARD_BLOCK_IF_IT_CANNOT_BE_PROCESSED = 4;
    }
    
    public BigInteger blockTypeCode;
    public BigInteger blockProcessingControlFlags;
    public BlockTypeSpecificDataFields blockTypeSpecificDataFields;
    
    @Override
    public String toString() {
        return "CanonicalBlock{"
            + "blockTypeCode=" + blockTypeCode
            + ",blockProcessingControlFlags=" + blockProcessingControlFlags.toString(2)
            + ",blockTypeSpecificDataFields=" + blockTypeSpecificDataFields
            + '}';
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.blockTypeCode);
        hash = 41 * hash + Objects.hashCode(this.blockProcessingControlFlags);
        hash = 41 * hash + Objects.hashCode(this.blockTypeSpecificDataFields);
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
        final CanonicalBlock other = (CanonicalBlock) obj;
        if (!Objects.equals(this.blockTypeCode, other.blockTypeCode)) {
            return false;
        }
        if (!Objects.equals(
            this.blockProcessingControlFlags, other.blockProcessingControlFlags
        )) {
            return false;
        }
        if (!Objects.equals(
            this.blockTypeSpecificDataFields, other.blockTypeSpecificDataFields
        )) {
            return false;
        }
        return true;
    }
}
