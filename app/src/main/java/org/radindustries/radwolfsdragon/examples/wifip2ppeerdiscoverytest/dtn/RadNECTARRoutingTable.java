package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.support.annotation.NonNull;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2NECTARRoutingTable;

final class RadNECTARRoutingTable implements Daemon2NECTARRoutingTable {
    
    static final String DB_NAME = "NECTAR_routing_db";
    static final String TABLE_NAME = "neighbourhood_index_table";
    static final String COL_ROOM_ID = "id";
    static final String COL_NODE_EID = "node_EID";
    static final String COL_FIRST_ENCOUNTER_TIMESTAMP = "first_encounter_timestamp";
    static final String COL_MEETING_COUNT = "meeting_count";
    
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    
    private static NECTARRepo nectarRepo;
    
    private RadNECTARRoutingTable() {}
    
    RadNECTARRoutingTable(@NonNull Context context) {
        nectarRepo = NECTARRepo.getRepo(context);
    }
    
    @Override
    public float getMeetingFrequency(DTNEndpointID nodeEID) {
        NeighbourhoodIndex ni = nectarRepo.getIndex(nodeEID.toString());
        
        return ni != null ?
            (ni.getMeetingCount() * DAY_IN_MILLIS) /
            (System.currentTimeMillis() - ni.getFirstEncounterTimestamp()) :
            0.0F;
    }
    
    @Override
    public void incrementMeetingCount(DTNEndpointID nodeEID) {
        NeighbourhoodIndex ni = nectarRepo.getIndex(nodeEID.toString());
        if (ni != null) { // updating
            ni.setMeetingCount(ni.getMeetingCount() + 1);
            nectarRepo.update(ni);
        } else { // inserting
            NeighbourhoodIndex newNI = new NeighbourhoodIndex();
            newNI.setNodeEID(nodeEID.toString());
            newNI.setFirstEncounterTimestamp(System.currentTimeMillis());
            newNI.setMeetingCount(1);
            nectarRepo.insert(newNI);
        }
    }
}
