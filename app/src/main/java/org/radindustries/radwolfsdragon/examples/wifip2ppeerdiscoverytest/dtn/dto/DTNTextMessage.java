package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;

public final class DTNTextMessage implements Serializable {
    public String sender;
    public String textMessage;
    public String creationTimestamp;
    public String deliveryTimestamp;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTNTextMessage)) return false;
        
        DTNTextMessage that = (DTNTextMessage) o;
        
        if (!sender.equals(that.sender)) return false;
        if (!textMessage.equals(that.textMessage)) return false;
        if (!creationTimestamp.equals(that.creationTimestamp)) return false;
        return deliveryTimestamp.equals(that.deliveryTimestamp);
    }
    
    @Override
    public int hashCode() {
        int result = sender.hashCode();
        result = 31 * result + textMessage.hashCode();
        result = 31 * result + creationTimestamp.hashCode();
        result = 31 * result + deliveryTimestamp.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "DTNTextMessage{" +
            "sender='" + sender + '\'' +
            ", textMessage='" + textMessage + '\'' +
            ", creationTimestamp='" + creationTimestamp + '\'' +
            ", deliveryTimestamp='" + deliveryTimestamp + '\'' +
            '}';
    }
}
