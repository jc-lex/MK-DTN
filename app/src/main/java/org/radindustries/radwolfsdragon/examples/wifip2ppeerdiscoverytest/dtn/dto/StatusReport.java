package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

public final class StatusReport extends AdminRecord {
    
    public enum Reason {
        NO_OTHER_INFO, DEPLETED_STORAGE, LIFETIME_EXPIRED, TRANSMISSION_CANCELLED,
        AGE_BLOCK_UNINTELLIGIBLE
    }
    
    public boolean bundleDelivered;
    public Reason reasonCode;
    public long timeOfDelivery;
    
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
        
        StatusReport report = (StatusReport) o;
        
        if (bundleDelivered != report.bundleDelivered) return false;
        if (timeOfDelivery != report.timeOfDelivery) return false;
        return reasonCode == report.reasonCode;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (bundleDelivered ? 1 : 0);
        result = 31 * result + reasonCode.hashCode();
        result = 31 * result + (int) (timeOfDelivery ^ (timeOfDelivery >>> 32));
        return result;
    }
}
