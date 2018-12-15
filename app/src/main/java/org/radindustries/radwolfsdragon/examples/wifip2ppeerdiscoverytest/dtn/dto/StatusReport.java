package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Objects;

public final class StatusReport extends AdminRecord {
    
    public static final class StatusFlag {
        private StatusFlag() {}
        
        // the only report we are currently interested in
        public static final int BUNDLE_DELIVERED = 3;
    }
    
    public static final class ReasonCode implements AdminRecord.ReasonCode {
        public static final BigInteger LIFETIME_EXPIRED = BigInteger.ONE;
        public static final BigInteger TRANSMISSION_CANCELLED = new BigInteger("3", 16);
    }
    
    public BigInteger statusFlags;
    public BigInteger reasonCode;
    public Instant timeOfDelivery;
    
    @Override
    public String toString() {
        return "StatusReport{" +
            "statusFlags=" + statusFlags.toString(2) +
            ",reasonCode=" + reasonCode.toString(2) +
            ",timeOfDelivery=" + timeOfDelivery +
            ",detailsIfForFragment=" + detailsIfForFragment +
            ",creationTimestampOfBundle=" + creationTimestampOfBundle +
            ",sourceEIDOfBundle=" + sourceEIDOfBundle +
            ",recordTypeCodeAndFlags=" + recordTypeCodeAndFlags.toString(2) +
            "}";
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.statusFlags);
        hash = 23 * hash + Objects.hashCode(this.reasonCode);
        hash = 23 * hash + Objects.hashCode(this.timeOfDelivery);
        hash = 23 * hash + super.hashCode();
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
        final StatusReport other = (StatusReport) obj;
        if (!Objects.equals(this.statusFlags, other.statusFlags)) {
            return false;
        }
        if (!Objects.equals(this.reasonCode, other.reasonCode)) {
            return false;
        }
        if (!Objects.equals(this.timeOfDelivery, other.timeOfDelivery)) {
            return false;
        }
        return super.equals(obj);
    }
}
