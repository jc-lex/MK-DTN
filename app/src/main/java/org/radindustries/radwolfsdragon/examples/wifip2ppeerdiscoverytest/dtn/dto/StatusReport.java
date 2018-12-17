package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.time.Instant;

public final class StatusReport extends AdminRecord {
    
    public enum Reason {
        NO_OTHER_INFO, DEPLETED_STORAGE, LIFETIME_EXPIRED, TRANSMISSION_CANCELLED
    }
    
    public boolean bundleDelivered;
    public Reason reasonCode;
    public Instant timeOfDelivery;
    
    @Override
    public String toString() {
        return "StatusReport{" +
            "recordType=" + recordType +
            ",subjectBundleID=" + subjectBundleID +
            ",isForAFragment=" + isForAFragment +
            ",detailsIfForAFragment=" + detailsIfForAFragment +
            ",bundleDelivered=" + bundleDelivered +
            ",reasonCode=" + reasonCode +
            ",timeOfDelivery=" + timeOfDelivery +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusReport)) return false;
        if (!super.equals(o)) return false;
        
        StatusReport that = (StatusReport) o;
        
        if (reasonCode != that.reasonCode) return false;
        if (bundleDelivered != that.bundleDelivered) return false;
        return timeOfDelivery.equals(that.timeOfDelivery);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (bundleDelivered ? 1 : 0);
        result = 31 * result + reasonCode.hashCode();
        result = 31 * result + timeOfDelivery.hashCode();
        return result;
    }
}
