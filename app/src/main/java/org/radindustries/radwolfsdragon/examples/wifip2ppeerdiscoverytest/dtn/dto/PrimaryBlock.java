package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.DTNUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;

public final class PrimaryBlock implements Serializable {
    
    public interface FragmentField extends DTNBundle.FragmentField {
        String TOTAL_ADU_LENGTH = "total_ADU_length";
    }
    
    public interface BundlePCF {
        int BUNDLE_IS_A_FRAGMENT = 0;
        int ADU_IS_AN_ADMIN_RECORD = 1;
        int BUNDLE_MUST_NOT_BE_FRAGMENTED = 2;
        int BUNDLE_CUSTODY_TRANSFER_REQUESTED = 3;
        int DESTINATION_ENDPOINT_IS_A_SINGLETON = 4;
        int BUNDLE_DELIVERY_REPORT_REQUESTED = 17;
        int BUNDLE_DELETION_REPORT_REQUESTED = 18;
    }
    
    public enum PriorityClass {EXPEDITED, NORMAL, BULK}
    
    public enum LifeTime {
        FIVE_HOURS(DTNUtils.HOUR_MILLIS * 5),
        FIFTEEN_HOURS(DTNUtils.HOUR_MILLIS * 15),
        THIRTY_FIVE_HOURS(DTNUtils.HOUR_MILLIS * 35);
//        TWO_MONTHS(DTNTimeDuration.ofMonths(2));
        
        private long duration;
        
        LifeTime(long duration) {
            this.duration = duration;
        }
        
        public long getDuration() {
            return this.duration;
        }
    }
    
    public BigInteger bundleProcessingControlFlags;
    public PriorityClass priorityClass;
    public DTNBundleID bundleID;
    public DTNEndpointID destinationEID;
    public DTNEndpointID reportToEID;
    public DTNEndpointID custodianEID;
    public long lifeTime;
    public HashMap<String, String> detailsIfFragment = new HashMap<>(2);
    
    @Override
    public String toString() {
        return "PrimaryBlock{" +
            "bundleProcessingControlFlags=" + bundleProcessingControlFlags.toString(2) +
            ",priorityClass=" + priorityClass +
            ",bundleID=" + bundleID +
            ",destinationEID=" + destinationEID +
            ",reportToEID=" + reportToEID +
            ",custodianEID=" + custodianEID +
            ",lifeTime=" + lifeTime +
            ",detailsIfFragment=" + detailsIfFragment +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrimaryBlock)) return false;
        
        PrimaryBlock that = (PrimaryBlock) o;
        
        if (lifeTime != that.lifeTime) return false;
        if (!bundleProcessingControlFlags.equals(that.bundleProcessingControlFlags)) return false;
        if (priorityClass != that.priorityClass) return false;
        if (!bundleID.equals(that.bundleID)) return false;
        if (!destinationEID.equals(that.destinationEID)) return false;
        if (!reportToEID.equals(that.reportToEID)) return false;
        if (!custodianEID.equals(that.custodianEID)) return false;
        return detailsIfFragment.equals(that.detailsIfFragment);
    }
    
    @Override
    public int hashCode() {
        int result = bundleProcessingControlFlags.hashCode();
        result = 31 * result + priorityClass.hashCode();
        result = 31 * result + bundleID.hashCode();
        result = 31 * result + destinationEID.hashCode();
        result = 31 * result + reportToEID.hashCode();
        result = 31 * result + custodianEID.hashCode();
        result = 31 * result + (int) (lifeTime ^ (lifeTime >>> 32));
        result = 31 * result + detailsIfFragment.hashCode();
        return result;
    }
}
