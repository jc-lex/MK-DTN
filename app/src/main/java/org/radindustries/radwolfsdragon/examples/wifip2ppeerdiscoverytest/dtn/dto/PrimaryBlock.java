package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Period;
import java.util.HashMap;
import java.util.Objects;

public final class PrimaryBlock implements Serializable {
    
    public static final class FragmentField implements DTNBundle.FragmentField {
        private FragmentField() {}
        public static final String TOTAL_ADU_LENGTH = "total_ADU_length";
    }
    
    public static final class BundlePCF {
        private BundlePCF() {}
        public static final int BUNDLE_IS_A_FRAGMENT = 0;
        public static final int ADU_IS_AN_ADMIN_RECORD = 1;
        public static final int BUNDLE_MUST_NOT_BE_FRAGMENTED = 2;
        
        // we need this
        public static final int BUNDLE_CUSTODY_TRANSFER_REQUESTED = 3;
        
        public static final int DESTINATION_ENDPOINT_IS_SINGLETON = 4;
        
        // the only report we are currently interested in
        public static final int BUNDLE_DELIVERY_REPORT_REQUESTED = 17;
    }
    
    public enum PriorityClass {
        EXPEDITED, NORMAL, BULK;
        
        public static BigInteger setPriorityClass(
            BigInteger bundlePCFs, PriorityClass priorityClass
        ) {
            switch (priorityClass) {
                case BULK: return bundlePCFs.clearBit(8).clearBit(7); // 00
                case NORMAL: return bundlePCFs.clearBit(8).setBit(7); // 01
                case EXPEDITED: return bundlePCFs.setBit(8).clearBit(7); // 10
                default: return bundlePCFs;
            }
        }
    }
    
    public static final class LifeTime {
        private LifeTime() {}
        public static final Period THREE_DAYS = Period.ofDays(3);
        public static final Period ONE_WEEK = Period.ofWeeks(1);
        public static final Period THREE_WEEKS = Period.ofWeeks(3);
        public static final Period TWO_MONTHS = Period.ofMonths(2);
    }
    
    public BigInteger bundleProcessingControlFlags;
    public DTNBundleID bundleID;
    public DTNEndpointID destinationEID;
//    public DTNEndpointID sourceEID;
    public DTNEndpointID reportToEID;
    public DTNEndpointID custodianEID;
//    public Instant creationTimestamp;
    public Period lifeTime;
    public HashMap<String, String> detailsIfFragment;
    
    @Override
    public String toString() {
        return "PrimaryBlock{"
            + "bundleProcessingControlFlags=" + bundleProcessingControlFlags.toString(2)
            + "bundleId=" + bundleID
            + ",destinationEID=" + destinationEID
//            + ",sourceEID=" + sourceEID
            + ",reportToEID=" + reportToEID
            + ",custodianEID=" + custodianEID
//            + ",creationTimestamp=" + creationTimestamp
            + ",lifeTime=" + lifeTime
            + ",detailsIfFragment=" + detailsIfFragment
            + '}';
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.bundleProcessingControlFlags);
        hash = 67 * hash + Objects.hashCode(this.bundleID);
        hash = 67 * hash + Objects.hashCode(this.destinationEID);
//        hash = 67 * hash + Objects.hashCode(this.sourceEID);
        hash = 67 * hash + Objects.hashCode(this.reportToEID);
        hash = 67 * hash + Objects.hashCode(this.custodianEID);
//        hash = 67 * hash + Objects.hashCode(this.creationTimestamp);
        hash = 67 * hash + Objects.hashCode(this.lifeTime);
        hash = 67 * hash + Objects.hashCode(this.detailsIfFragment);
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
        final PrimaryBlock other = (PrimaryBlock) obj;
        if (!Objects.equals(
            this.bundleProcessingControlFlags, other.bundleProcessingControlFlags
        )) {
            return false;
        }
        if (!Objects.equals(this.bundleID, other.bundleID)) {
            return false;
        }
        if (!Objects.equals(this.destinationEID, other.destinationEID)) {
            return false;
        }
//        if (!Objects.equals(this.sourceEID, other.sourceEID)) {
//            return false;
//        }
        if (!Objects.equals(this.reportToEID, other.reportToEID)) {
            return false;
        }
        if (!Objects.equals(this.custodianEID, other.custodianEID)) {
            return false;
        }
//        if (!Objects.equals(this.creationTimestamp, other.creationTimestamp)) {
//            return false;
//        }
        if (!Objects.equals(this.lifeTime, other.lifeTime)) {
            return false;
        }
        if (!Objects.equals(this.detailsIfFragment, other.detailsIfFragment)) {
            return false;
        }
        return true;
    }
}
