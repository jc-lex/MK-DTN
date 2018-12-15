package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;

public class AdminRecord extends BlockTypeSpecificDataFields {
    
    public static final class FragmentField implements DTNBundle.FragmentField {
        private FragmentField() {}
        public static final String FRAGMENT_LENGTH = "fragment_length";
    }
    
    public static final class RecordFlag {
        private RecordFlag() {}
        public static final int RECORD_IS_FOR_A_FRAGMENT = 1;
        public static final int RECORD_IS_A_STATUS_REPORT = 4;
        public static final int RECORD_IS_A_CUSTODY_SIGNAL = 5;
    }
    
    public interface ReasonCode {
        BigInteger NO_OTHER_INFO = BigInteger.ZERO;
        BigInteger DEPLETED_STORAGE = new BigInteger("4", 16);
    }
    
    public HashMap<String, String> detailsIfForFragment;
    public Instant creationTimestampOfBundle;
    public DTNEndpointID sourceEIDOfBundle;
    public BigInteger recordTypeCodeAndFlags;
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.detailsIfForFragment);
        hash = 47 * hash + Objects.hashCode(this.creationTimestampOfBundle);
        hash = 47 * hash + Objects.hashCode(this.sourceEIDOfBundle);
        hash = 47 * hash + Objects.hashCode(this.recordTypeCodeAndFlags);
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
        final AdminRecord other = (AdminRecord) obj;
        if (!Objects.equals(
            this.detailsIfForFragment, other.detailsIfForFragment
        )) {
            return false;
        }
        if (!Objects.equals(
            this.creationTimestampOfBundle, other.creationTimestampOfBundle
        )) {
            return false;
        }
        if (!Objects.equals(this.sourceEIDOfBundle, other.sourceEIDOfBundle)) {
            return false;
        }
        if (!Objects.equals(
            this.recordTypeCodeAndFlags, other.recordTypeCodeAndFlags
        )) {
            return false;
        }
        return true;
    }
}
