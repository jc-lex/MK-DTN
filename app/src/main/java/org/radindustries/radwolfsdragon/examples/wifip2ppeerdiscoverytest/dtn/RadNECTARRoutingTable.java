package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.EIDProvider;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.WallClock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2NECTARRoutingTable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import androidx.annotation.NonNull;

final class RadNECTARRoutingTable implements Daemon2NECTARRoutingTable {
    
    private static RouterDBHandler routerDBHandler;
    private EIDProvider eidProvider;
    private WallClock clock;
    
    private RadNECTARRoutingTable() {}
    
    RadNECTARRoutingTable(
        @NonNull Context context, @NonNull EIDProvider eidProvider, @NonNull WallClock clock
    ) {
        routerDBHandler = RouterDBHandler.getHandler(context);
        this.eidProvider = eidProvider;
        this.clock = clock;
    }
    
    @Override
    public float getMeetingFrequency(DTNEndpointID nodeEID) {
        if (eidProvider.isUs(nodeEID)) return Float.MAX_VALUE;
        
        NeighbourhoodIndex ni = routerDBHandler.getIndex(nodeEID.toString());
        
        return ni != null ?
            new BigDecimal(DTNUtils.DAY.multiply(BigInteger.valueOf(ni.getMeetingCount())))
                
                .divide(new BigDecimal(
                    
                    clock.getCurrentTime().subtract(new BigInteger(ni.getFirstEncounterTimestamp()))
                
                ), RoundingMode.UP)
                
                .floatValue() :
            0.0F;
    }
    
    @Override
    public void incrementMeetingCount(DTNEndpointID nodeEID) {
        if (eidProvider.isUs(nodeEID)) return;
        
        NeighbourhoodIndex ni = routerDBHandler.getIndex(nodeEID.toString());
        if (ni != null) { // updating
            ni.setMeetingCount(ni.getMeetingCount() + 1);
            routerDBHandler.update(ni);
        } else { // inserting
            NeighbourhoodIndex newNI = new NeighbourhoodIndex();
            newNI.setNodeEID(nodeEID.toString());
            newNI.setFirstEncounterTimestamp(clock.getCurrentTime().toString());
            newNI.setMeetingCount(1);
            routerDBHandler.insert(newNI);
        }
    }
}
