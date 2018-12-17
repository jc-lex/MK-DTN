package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

public final class AgeBlock extends BlockTypeSpecificDataFields {
    
    public long sourceCPUSpeedInKHz;
    public long sendingTimestamp;
    public long age;
    public long agePrime;
    public long T;
    
    public static AgeBlock from(AgeBlock other) {
        AgeBlock newOne = new AgeBlock();
        newOne.sourceCPUSpeedInKHz = other.sourceCPUSpeedInKHz;
        newOne.sendingTimestamp = other.sendingTimestamp;
        newOne.T = other.T;
        newOne.age = other.age;
        newOne.agePrime = other.agePrime;
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
        if (sendingTimestamp != ageBlock.sendingTimestamp) return false;
        if (age != ageBlock.age) return false;
        if (agePrime != ageBlock.agePrime) return false;
        return T == ageBlock.T;
    }
    
    @Override
    public int hashCode() {
        int result = (int) (sourceCPUSpeedInKHz ^ (sourceCPUSpeedInKHz >>> 32));
        result = 31 * result + (int) (sendingTimestamp ^ (sendingTimestamp >>> 32));
        result = 31 * result + (int) (age ^ (age >>> 32));
        result = 31 * result + (int) (agePrime ^ (agePrime >>> 32));
        result = 31 * result + (int) (T ^ (T >>> 32));
        return result;
    }
}
