package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.EIDProvider;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2NECTARRoutingTable;

import androidx.annotation.NonNull;

final class RadNECTARRoutingTable implements Daemon2NECTARRoutingTable {
    
    private static RouterDBHandler routerDBHandler;
    private EIDProvider eidProvider;
    
    private RadNECTARRoutingTable() {}
    
    RadNECTARRoutingTable(@NonNull Context context, @NonNull EIDProvider eidProvider) {
        routerDBHandler = RouterDBHandler.getHandler(context);
        this.eidProvider = eidProvider;
    }
    
    @Override
    public float getMeetingFrequency(DTNEndpointID nodeEID) {
        if (eidProvider.isUs(nodeEID)) return Float.MAX_VALUE;
        
        NeighbourhoodIndex ni = routerDBHandler.getIndex(nodeEID.toString());
        
        return ni != null ?
            ni.getMeetingCount() * (
                (System.currentTimeMillis() - ni.getFirstEncounterTimestamp()) /
                    (float) DTNUtils.DAY_MILLIS
            )
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
            newNI.setFirstEncounterTimestamp(System.currentTimeMillis());
            newNI.setMeetingCount(1);
            routerDBHandler.insert(newNI);
        }
    }
}
