package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.support.annotation.NonNull;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2NECTARRoutingTable;

final class RadNECTARRoutingTable implements Daemon2NECTARRoutingTable {
    
    static final String DB_NAME = "NECTAR_routing_db";
    static final String TABLE_NAME = "neighbourhood_index_table";
    static final String COL_ROOM_ID = "id";
    static final String COL_NODE_EID = "node_eid";
    static final String COL_MEETING_FREQUENCY = "meeting_frequency";
    
    private static NECTARRepo nectarRepo;
    
    private RadNECTARRoutingTable() {}
    
    RadNECTARRoutingTable(@NonNull Context context) {
        nectarRepo = NECTARRepo.getRepo(context);
    }
    
    @Override
    public int getMeetingFrequency(DTNEndpointID nodeEID) {
        NeighbourhoodIndex ni = nectarRepo.getIndex(nodeEID.toString());
        return ni.getMeetingFrequency();
    }
    
    @Override
    public void incrementMeetingFrequency(DTNEndpointID nodeEID) {
        NeighbourhoodIndex ni = nectarRepo.getIndex(nodeEID.toString());
        if (ni != null) {
            ni.setMeetingFrequency(ni.getMeetingFrequency() + 1);
            nectarRepo.update(ni);
        } else {
            NeighbourhoodIndex newNI = new NeighbourhoodIndex();
            newNI.setNodeEID(nodeEID.toString());
            newNI.setMeetingFrequency(1);
            nectarRepo.insert(newNI);
        }
    }
}
