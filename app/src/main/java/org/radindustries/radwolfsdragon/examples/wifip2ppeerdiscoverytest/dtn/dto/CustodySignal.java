package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.time.Instant;

public final class CustodySignal extends AdminRecord {
    
    public enum Reason {
        NO_OTHER_INFO, DEPLETED_STORAGE, REDUNDANT_RECEPTION
    }
    
    public boolean custodyTransferSucceeded;
    public Reason reasonCode;
    public Instant timeOfSignal;
    
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
        
        CustodySignal that = (CustodySignal) o;
        
        if (custodyTransferSucceeded != that.custodyTransferSucceeded) return false;
        if (reasonCode != that.reasonCode) return false;
        return timeOfSignal.equals(that.timeOfSignal);
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
