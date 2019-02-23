package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time;

import java.io.Serializable;
import java.math.BigInteger;

public final class DTNTimeInstant implements Serializable, Comparable<DTNTimeInstant> {
    public static final DTNTimeInstant ZERO = at(BigInteger.ZERO);
    
    private BigInteger instant;
    
    BigInteger getInstant() {
        return instant;
    }
    
    private void setInstant(BigInteger instant) {
        this.instant = instant;
    }
    
    public static DTNTimeInstant at(BigInteger t) {
        DTNTimeInstant time = new DTNTimeInstant();
        time.setInstant(t);
        return time;
    }
    
    public static DTNTimeInstant at(long t) {
        return at(BigInteger.valueOf(t));
    }
    
    public static DTNTimeInstant parse(String timeString) {
        return at(new BigInteger(timeString));
    }
    
    private DTNTimeInstant plus(DTNTimeDuration duration) {
        return at(this.instant.add(duration.getDuration()));
    }
    
    public static DTNTimeInstant copyOf(DTNTimeInstant other) {
        return other.plus(DTNTimeDuration.ZERO);
    }
    
//    public DTNTimeInstant minus(DTNTimeDuration duration) {
//        return at(this.instant.subtract(duration.getDuration()));
//    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTNTimeInstant)) return false;
        
        DTNTimeInstant that = (DTNTimeInstant) o;
    
        return instant.equals(that.instant);
    }
    
    @Override
    public int hashCode() {
        return instant.hashCode();
    }
    
    @Override
    public String toString() {
        return instant.toString();
    }
    
    @Override
    public int compareTo(DTNTimeInstant o) {
        return this.instant.compareTo(o.getInstant());
    }
}
