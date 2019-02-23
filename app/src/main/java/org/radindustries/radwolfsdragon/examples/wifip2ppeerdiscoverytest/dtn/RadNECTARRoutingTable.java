package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.EIDProvider;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2NECTARRoutingTable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeDuration;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeInstant;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.WallClock;

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
            ni.getMeetingCount() *
                DTNTimeDuration.between(
                    DTNTimeInstant.parse(ni.getFirstEncounterTimestamp()),
                        clock.getCurrentTime()
                    ).inDays()
            : 0.0F;
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
