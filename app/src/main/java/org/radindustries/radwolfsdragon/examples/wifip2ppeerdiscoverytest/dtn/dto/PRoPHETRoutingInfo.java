package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

public final class PRoPHETRoutingInfo extends BlockTypeSpecificDataFields {
    
    public float deliveryPredictability;
    
    @Override
    public String toString() {
        return "PRoPHETRoutingInfo{" +
            "deliveryPredictability=" + deliveryPredictability +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PRoPHETRoutingInfo)) return false;
        
        PRoPHETRoutingInfo that = (PRoPHETRoutingInfo) o;
    
        return Float.compare(that.deliveryPredictability, deliveryPredictability) == 0;
    }
    
    @Override
    public int hashCode() {
        return (deliveryPredictability != +0.0f ? Float.floatToIntBits(deliveryPredictability) : 0);
    }
}
