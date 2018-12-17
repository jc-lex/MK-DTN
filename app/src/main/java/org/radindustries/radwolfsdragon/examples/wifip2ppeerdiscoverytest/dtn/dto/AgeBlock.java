package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.time.Duration;
import java.time.Instant;

public final class AgeBlock extends BlockTypeSpecificDataFields {
    
    public long sourceCPUSpeedInKHz;
    public Instant sendingTimestamp;
    public Duration age;
    public Duration agePrime;
    public Instant T;
    
    public static AgeBlock from(AgeBlock other) {
        AgeBlock newOne = new AgeBlock();
        newOne.sourceCPUSpeedInKHz = other.sourceCPUSpeedInKHz;
        newOne.sendingTimestamp = Instant.parse(other.sendingTimestamp.toString());
        newOne.T = Instant.parse(other.T.toString());
        newOne.age = Duration.parse(other.age.toString());
        newOne.agePrime = Duration.parse(other.agePrime.toString());
        return newOne;
    }
    
    @Override
    public String toString() {
        return "AgeBlock{"
            + "sourceCPUSpeedInKHz=" + sourceCPUSpeedInKHz
            + ",sendingTimestamp=" + sendingTimestamp
            + ",age=" + age
            + ",agePrime=" + agePrime
            + ",T=" + T
            + "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgeBlock)) return false;
        
        AgeBlock ageBlock = (AgeBlock) o;
        
        if (sourceCPUSpeedInKHz != ageBlock.sourceCPUSpeedInKHz) return false;
        if (!sendingTimestamp.equals(ageBlock.sendingTimestamp)) return false;
        if (!age.equals(ageBlock.age)) return false;
        if (!agePrime.equals(ageBlock.agePrime)) return false;
        return T.equals(ageBlock.T);
    }
    
    @Override
    public int hashCode() {
        int result = (int) (sourceCPUSpeedInKHz ^ (sourceCPUSpeedInKHz >>> 32));
        result = 31 * result + sendingTimestamp.hashCode();
        result = 31 * result + age.hashCode();
        result = 31 * result + agePrime.hashCode();
        result = 31 * result + T.hashCode();
        return result;
    }
}
