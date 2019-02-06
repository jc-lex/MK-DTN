package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.io.Serializable;

public final class DTNTextMessage implements Serializable {
    public String sender;
    public String textMessage;
    public String receivedTimestamp;
    public String creationTimestamp;
    
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
        
        if (!sender.equals(that.sender)) return false;
        if (!textMessage.equals(that.textMessage)) return false;
        if (!receivedTimestamp.equals(that.receivedTimestamp)) return false;
        return creationTimestamp.equals(that.creationTimestamp);
    }
    
    @Override
    public int hashCode() {
        int result = sender.hashCode();
        result = 31 * result + textMessage.hashCode();
        result = 31 * result + receivedTimestamp.hashCode();
        result = 31 * result + creationTimestamp.hashCode();
        return result;
    }
}
