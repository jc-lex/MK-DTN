package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.support.annotation.NonNull;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.EIDProvider;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2NECTARRoutingTable;

final class RadNECTARRoutingTable implements Daemon2NECTARRoutingTable {
    
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    
    private static RouterDBHandler routerDBHandler;
    private EIDProvider eidProvider;
    
    private RadNECTARRoutingTable() {}
    
    RadNECTARRoutingTable(@NonNull Context context, @NonNull EIDProvider eidProvider) {
        routerDBHandler = RouterDBHandler.getHandler(context);
        this.eidProvider = eidProvider;
    }
    
    @Override
    public float getMeetingFrequency(DTNEndpointID nodeEID) {
        if (nodeEID.equals(eidProvider.getThisNodezEID())) return Float.MAX_VALUE;
        
        NeighbourhoodIndex ni = routerDBHandler.getIndex(nodeEID.toString());
        
        return ni != null ?
            (ni.getMeetingCount() * DAY_IN_MILLIS) /
            (System.currentTimeMillis() - ni.getFirstEncounterTimestamp()) :
            0.0F;
    }
    
    @Override
    public void incrementMeetingCount(DTNEndpointID nodeEID) {
        if (nodeEID.equals(eidProvider.getThisNodezEID())) return;
        
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
