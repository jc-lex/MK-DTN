package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.DTNUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class DTNTimeDuration implements Serializable, Comparable<DTNTimeDuration> {
    private static final DTNTimeDuration DAY = of(DTNUtils.DAY);
    private static final DTNTimeDuration WEEK = of(DTNUtils.DAY.multiply(BigInteger.valueOf(7)));
    private static final DTNTimeDuration MONTH = of(DTNUtils.DAY.multiply(BigInteger.valueOf(28)));
    public static final DTNTimeDuration ZERO = of(BigInteger.ZERO);
    
    private BigInteger duration;
    
    BigInteger getDuration() {
        return duration;
    }
    
    private void setDuration(BigInteger duration) {
        this.duration = duration;
    }
    
    private static DTNTimeDuration of(BigInteger d) {
        DTNTimeDuration time = new DTNTimeDuration();
        time.setDuration(d);
        return time;
    }
    
    private static DTNTimeDuration parse(String timeString) {
        return of(new BigInteger(timeString));
    }
    
    public static DTNTimeDuration ofDays(long days) {
        return of(DAY.getDuration().multiply(BigInteger.valueOf(days)));
    }
    
    public float inDays() {
        BigDecimal factor = BigDecimal.ONE.divide(new BigDecimal(DTNUtils.DAY), RoundingMode.UP);
        return factor.multiply(new BigDecimal(this.getDuration())).floatValue();
    }
    
    public static DTNTimeDuration ofWeeks(long weeks) {
        return of(WEEK.getDuration().multiply(BigInteger.valueOf(weeks)));
    }
    
    public static DTNTimeDuration ofMonths(long months) {
        return of(MONTH.getDuration().multiply(BigInteger.valueOf(months)));
    }
    
    public static DTNTimeDuration between(DTNTimeInstant from, DTNTimeInstant to) {
        BigInteger t1 = from.getInstant();
        BigInteger t2 = to.getInstant();
        return of(t2.subtract(t1).abs());
    }
    
    public DTNTimeDuration plus(DTNTimeDuration d) {
        return of(this.duration.add(d.getDuration()));
    }
    
    public static DTNTimeDuration copyOf(DTNTimeDuration other) {
        return other.plus(ZERO);
    }
    
//    private DTNTimeDuration minus(DTNTimeDuration d) {
//        return of(this.duration.subtract(d.getDuration()));
//    }
    
    private DTNTimeDuration scale(BigDecimal factor) {
        return of(factor.multiply(new BigDecimal(this.duration)).toBigInteger());
    }
    
    public DTNTimeDuration scale(double factor) {
        return scale(BigDecimal.valueOf(factor));
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTNTimeDuration)) return false;
        
        DTNTimeDuration that = (DTNTimeDuration) o;
    
        return duration.equals(that.duration);
    }
    
    @Override
    public int hashCode() {
        return duration.hashCode();
    }
    
    @Override
    public String toString() {
        return duration.toString();
    }
    
    @Override
    public int compareTo(DTNTimeDuration o) {
        return this.duration.compareTo(o.getDuration());
    }
}
