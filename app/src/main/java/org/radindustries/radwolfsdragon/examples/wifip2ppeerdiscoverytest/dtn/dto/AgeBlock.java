package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.math.BigInteger;

public final class AgeBlock extends BlockTypeSpecificDataFields {
    
    public long sourceCPUSpeedInKHz;
    public BigInteger sendingTimestamp = BigInteger.ZERO;
    public BigInteger receivingTimestamp = BigInteger.ZERO;
    public BigInteger age = BigInteger.ZERO;
    public BigInteger agePrime = BigInteger.ZERO;
    public BigInteger T = BigInteger.ZERO;
    
    public static AgeBlock from(AgeBlock other) {
        AgeBlock newOne = new AgeBlock();
        newOne.sourceCPUSpeedInKHz = other.sourceCPUSpeedInKHz;
        newOne.sendingTimestamp = new BigInteger(other.sendingTimestamp.toString());
        newOne.receivingTimestamp = new BigInteger(other.receivingTimestamp.toString());
        newOne.T = new BigInteger(other.T.toString());
        newOne.age = new BigInteger(other.age.toString());
        newOne.agePrime = new BigInteger(other.agePrime.toString());
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
