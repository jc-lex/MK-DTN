package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeInstant;

public final class StatusReport extends AdminRecord {
    
    public enum Reason {
        NO_OTHER_INFO, LIFETIME_EXPIRED, DESTINATION_EID_UNINTELLIGIBLE
    }
    
    public interface StatusFlags {
        int BUNDLE_DELETED = 16;
        int BUNDLE_DELIVERED = 8;
        int INVALID_FLAG_SET = -1;
    }
    
    public Reason reasonCode;
    public int status;
    public DTNTimeInstant timeOfStatus = DTNTimeInstant.ZERO;
    
    @Override
    public String toString() {
        return "StatusReport{" +
            "recordType=" + recordType +
            ",subjectBundleID=" + subjectBundleID +
            ",isForAFragment=" + isForAFragment +
            ",detailsIfForAFragment=" + detailsIfForAFragment +
            ",status=" + status +
            ",reasonCode=" + reasonCode +
            ",timeOfStatus=" + timeOfStatus +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusReport)) return false;
        if (!super.equals(o)) return false;
        
        StatusReport report = (StatusReport) o;
        
        if (status != report.status) return false;
        if (reasonCode != report.reasonCode) return false;
        return timeOfStatus.equals(report.timeOfStatus);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + reasonCode.hashCode();
        result = 31 * result + status;
        result = 31 * result + timeOfStatus.hashCode();
        return result;
    }
}
