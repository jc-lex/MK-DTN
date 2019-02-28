package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeDuration;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeInstant;

public final class AgeBlock extends BlockTypeSpecificDataFields {
    
    public long sourceCPUSpeedInKHz;
    public DTNTimeInstant sendingTimestamp = DTNTimeInstant.ZERO;
    public DTNTimeInstant receivingTimestamp = DTNTimeInstant.ZERO;
    public DTNTimeDuration age = DTNTimeDuration.ZERO;
    public DTNTimeDuration agePrime = DTNTimeDuration.ZERO;
    public DTNTimeInstant T = DTNTimeInstant.ZERO;
    
    public static AgeBlock from(AgeBlock other) {
        AgeBlock newOne = new AgeBlock();
        newOne.sourceCPUSpeedInKHz = other.sourceCPUSpeedInKHz;
        newOne.sendingTimestamp = DTNTimeInstant.copyOf(other.sendingTimestamp);
        newOne.receivingTimestamp = DTNTimeInstant.copyOf(other.receivingTimestamp);
        newOne.T = DTNTimeInstant.copyOf(other.T);
        newOne.age = DTNTimeDuration.copyOf(other.age);
        newOne.agePrime = DTNTimeDuration.copyOf(other.agePrime);
        return newOne;
    }
    
    @Override
    public String toString() {
        return "AgeBlock{" +
            "sourceCPUSpeedInKHz=" + sourceCPUSpeedInKHz +
            ",sendingTimestamp=" + sendingTimestamp +
            ",receivingTimestamp=" + receivingTimestamp +
            ",age=" + age +
            ",agePrime=" + agePrime +
            ",T=" + T +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgeBlock)) return false;
        
        AgeBlock ageBlock = (AgeBlock) o;
        
        if (sourceCPUSpeedInKHz != ageBlock.sourceCPUSpeedInKHz) return false;
        if (!sendingTimestamp.equals(ageBlock.sendingTimestamp)) return false;
        if (!receivingTimestamp.equals(ageBlock.receivingTimestamp)) return false;
        if (!age.equals(ageBlock.age)) return false;
        if (!agePrime.equals(ageBlock.agePrime)) return false;
        return T.equals(ageBlock.T);
    }
    
    @Override
    public int hashCode() {
        int result = (int) (sourceCPUSpeedInKHz ^ (sourceCPUSpeedInKHz >>> 32));
        result = 31 * result + sendingTimestamp.hashCode();
        result = 31 * result + receivingTimestamp.hashCode();
        result = 31 * result + age.hashCode();
        result = 31 * result + agePrime.hashCode();
        result = 31 * result + T.hashCode();
        return result;
    }
}
