package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Objects;

public final class CustodySignal extends AdminRecord {
    
    public static final class StatusFlag {
        private StatusFlag() {}
        public static final int CUSTODY_TRANSFER_ACCEPTED = 7;
    }
    
    public static final class ReasonCode implements AdminRecord.ReasonCode {
        private ReasonCode() {}
        public static final BigInteger REDUNDANT_RECEPTION = new BigInteger("3", 16);
    }
    
    public BigInteger status;
    public Instant timeOfSignal;
    
    @Override
    public String toString() {
        return "CustodySignal{" +
            "status=" + status.toString(2) +
            ",timeOfSignal=" + timeOfSignal +
            ",detailsIfForFragment=" + detailsIfForFragment +
            ",creationTimestampOfBundle=" + creationTimestampOfBundle +
            ",sourceEIDOfBundle=" + sourceEIDOfBundle +
            ",recordTypeCodeAndFlags=" + recordTypeCodeAndFlags.toString(2) +
            "}";
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + Objects.hashCode(this.status);
        hash = 13 * hash + Objects.hashCode(this.timeOfSignal);
        hash = 13 * hash + super.hashCode();
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
        final CustodySignal other = (CustodySignal) obj;
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!Objects.equals(this.timeOfSignal, other.timeOfSignal)) {
            return false;
        }
        return super.equals(obj);
    }
}
