package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import android.annotation.SuppressLint;

import java.math.BigInteger;
import java.util.HashMap;

public final class StatusReport extends AdminRecord {
    
    public enum Reason {
        NO_OTHER_INFO, LIFETIME_EXPIRED, DESTINATION_EID_UNINTELLIGIBLE
    }
    
    public interface StatusFlags {
        int BUNDLE_DELETED = 4;
        int BUNDLE_DELIVERED = 3;
        int INVALID_FLAG_SET = 6969;
    }
    
    public BigInteger statusFlags = BigInteger.ZERO;
    public Reason reasonCode;
    
    @SuppressLint("UseSparseArrays")
    public HashMap<Integer, Long> statusTimes = new HashMap<>();
    
    @Override
    public String toString() {
        return "StatusReport{" +
            "recordType=" + recordType +
            ",subjectBundleID=" + subjectBundleID +
            ",isForAFragment=" + isForAFragment +
            ",detailsIfForAFragment=" + detailsIfForAFragment +
            ",statusFlags=" + statusFlags.toString(2) +
            ",reasonCode=" + reasonCode +
            ",statusTimes=" + statusTimes +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusReport)) return false;
        if (!super.equals(o)) return false;
        
        StatusReport that = (StatusReport) o;
        
        if (!statusFlags.equals(that.statusFlags)) return false;
        if (reasonCode != that.reasonCode) return false;
        return statusTimes.equals(that.statusTimes);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + statusFlags.hashCode();
        result = 31 * result + reasonCode.hashCode();
        result = 31 * result + statusTimes.hashCode();
        return result;
    }
}
