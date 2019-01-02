package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

public final class NECTARRoutingInfo extends BlockTypeSpecificDataFields {
    
    public float meetingFrequency;
    
    @Override
    public String toString() {
        return "NECTARRoutingInfo{" +
            "meetingFrequency=" + meetingFrequency +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NECTARRoutingInfo)) return false;
        
        NECTARRoutingInfo that = (NECTARRoutingInfo) o;
    
        return Float.compare(that.meetingFrequency, meetingFrequency) == 0;
    }
    
    @Override
    public int hashCode() {
        return (meetingFrequency != +0.0f ? Float.floatToIntBits(meetingFrequency) : 0);
    }
}
