package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Period;
import java.util.HashMap;

public final class PrimaryBlock implements Serializable {
    
    public interface FragmentField extends DTNBundle.FragmentField {
        String TOTAL_ADU_LENGTH = "total_ADU_length";
    }
    
    public interface BundlePCF {
        int BUNDLE_IS_A_FRAGMENT = 0;
        int ADU_IS_AN_ADMIN_RECORD = 1;
        int BUNDLE_MUST_NOT_BE_FRAGMENTED = 2;
        
        // we need this
        int BUNDLE_CUSTODY_TRANSFER_REQUESTED = 3;
        
        int DESTINATION_ENDPOINT_IS_A_SINGLETON = 4;
        
        // the only report we are currently interested in
        int BUNDLE_DELIVERY_REPORT_REQUESTED = 17;
    }
    
    public enum PriorityClass {EXPEDITED, NORMAL, BULK}
    
    public enum LifeTime {
        THREE_DAYS, ONE_WEEK, THREE_WEEKS, TWO_MONTHS;
        
        public static Period setLifeTime(LifeTime lifeTime) {
            switch (lifeTime) {
                case THREE_DAYS: return Period.ofDays(3);
                case ONE_WEEK: return Period.ofWeeks(1);
                case THREE_WEEKS: return Period.ofWeeks(3);
                case TWO_MONTHS: return Period.ofMonths(2);
                default: return Period.ofDays(3);
            }
        }
    }
    
    public BigInteger bundleProcessingControlFlags;
    public PriorityClass priorityClass;
    public DTNBundleID bundleID;
    public DTNEndpointID destinationEID;
    public DTNEndpointID reportToEID;
    public DTNEndpointID custodianEID;
    public Period lifeTime;
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
        
        if (!bundleProcessingControlFlags.equals(that.bundleProcessingControlFlags)) return false;
        if (priorityClass != that.priorityClass) return false;
        if (!bundleID.equals(that.bundleID)) return false;
        if (!destinationEID.equals(that.destinationEID)) return false;
        if (!reportToEID.equals(that.reportToEID)) return false;
        if (!custodianEID.equals(that.custodianEID)) return false;
        if (!lifeTime.equals(that.lifeTime)) return false;
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
        result = 31 * result + lifeTime.hashCode();
        result = 31 * result + detailsIfFragment.hashCode();
        return result;
    }
}
