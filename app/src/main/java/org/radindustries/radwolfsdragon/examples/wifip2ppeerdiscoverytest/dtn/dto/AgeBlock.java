package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

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
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash
            + (int) (this.sourceCPUSpeedInKHz ^ (this.sourceCPUSpeedInKHz >>> 32));
        hash = 73 * hash + Objects.hashCode(this.sendingTimestamp);
        hash = 73 * hash + Objects.hashCode(this.age);
        hash = 73 * hash + Objects.hashCode(this.agePrime);
        hash = 73 * hash + Objects.hashCode(this.T);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AgeBlock other = (AgeBlock) obj;
        if (this.sourceCPUSpeedInKHz != other.sourceCPUSpeedInKHz) {
            return false;
        }
        if (!Objects.equals(this.sendingTimestamp, other.sendingTimestamp)) {
            return false;
        }
        if (!Objects.equals(this.age, other.age)) {
            return false;
        }
        if (!Objects.equals(this.agePrime, other.agePrime)) {
            return false;
        }
        if (!Objects.equals(this.T, other.T)) {
            return false;
        }
        return true;
    }
}
