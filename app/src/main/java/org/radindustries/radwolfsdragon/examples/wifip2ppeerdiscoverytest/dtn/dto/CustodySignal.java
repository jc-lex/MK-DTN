package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.math.BigInteger;

public final class CustodySignal extends AdminRecord {
    
    public enum Reason {
        NO_OTHER_INFO, DEPLETED_STORAGE, REDUNDANT_RECEPTION, DEPLETED_POWER
    }
    
    public boolean custodyTransferSucceeded;
    public Reason reasonCode;
    public BigInteger timeOfSignal;
    
    @Override
    public String toString() {
        return "CustodySignal{" +
            "recordType=" + recordType +
            ",subjectBundleID=" + subjectBundleID +
            ",isForAFragment=" + isForAFragment +
            ",detailsIfForAFragment=" + detailsIfForAFragment +
            ",custodyTransferSucceeded=" + custodyTransferSucceeded +
            ",reasonCode=" + reasonCode +
            ",timeOfSignal=" + timeOfSignal +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustodySignal)) return false;
        if (!super.equals(o)) return false;
        
        CustodySignal signal = (CustodySignal) o;
        
        if (custodyTransferSucceeded != signal.custodyTransferSucceeded) return false;
        if (reasonCode != signal.reasonCode) return false;
        return timeOfSignal.equals(signal.timeOfSignal);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (custodyTransferSucceeded ? 1 : 0);
        result = 31 * result + reasonCode.hashCode();
        result = 31 * result + timeOfSignal.hashCode();
        return result;
    }
}
