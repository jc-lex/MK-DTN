package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;

public final class DTNTextMessage implements Serializable {
    public String sender;
    public String textMessage;
    public long receivedTimestamp;
    public long creationTimestamp;
    
    @Override
    public String toString() {
        return "DTNTextMessage{" +
            "sender='" + sender + '\'' +
            ",textMessage='" + textMessage + '\'' +
            ",receivedTimestamp=" + receivedTimestamp +
            ",creationTimestamp=" + creationTimestamp +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTNTextMessage)) return false;
        
        DTNTextMessage that = (DTNTextMessage) o;
        
        if (receivedTimestamp != that.receivedTimestamp) return false;
        if (creationTimestamp != that.creationTimestamp) return false;
        if (!sender.equals(that.sender)) return false;
        return textMessage.equals(that.textMessage);
    }
    
    @Override
    public int hashCode() {
        int result = sender.hashCode();
        result = 31 * result + textMessage.hashCode();
        result = 31 * result + (int) (receivedTimestamp ^ (receivedTimestamp >>> 32));
        result = 31 * result + (int) (creationTimestamp ^ (creationTimestamp >>> 32));
        return result;
    }
}
