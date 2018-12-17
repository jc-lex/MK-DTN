package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.util.HashMap;

public class AdminRecord extends BlockTypeSpecificDataFields {
    
    public static final class FragmentField implements DTNBundle.FragmentField {
        private FragmentField() {}
        public static final String FRAGMENT_LENGTH = "fragment_length";
    }
    
    public enum RecordType {STATUS_REPORT, CUSTODY_SIGNAL}
    
    public RecordType recordType;
    public DTNBundleID subjectBundleID;
    public boolean isForAFragment;
    public HashMap<String, String> detailsIfForAFragment = new HashMap<>(2);
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdminRecord)) return false;
        
        AdminRecord that = (AdminRecord) o;
        
        if (isForAFragment != that.isForAFragment) return false;
        if (!detailsIfForAFragment.equals(that.detailsIfForAFragment)) return false;
        if (!subjectBundleID.equals(that.subjectBundleID)) return false;
        return recordType == that.recordType;
    }
    
    @Override
    public int hashCode() {
        int result = detailsIfForAFragment.hashCode();
        result = 31 * result + subjectBundleID.hashCode();
        result = 31 * result + recordType.hashCode();
        result = 31 * result + (isForAFragment ? 1 : 0);
        return result;
    }
}
