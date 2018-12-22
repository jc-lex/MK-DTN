package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import static org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.RadNECTARRoutingTable.COL_MEETING_FREQUENCY;
import static org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.RadNECTARRoutingTable.COL_NODE_EID;
import static org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.RadNECTARRoutingTable.COL_ROOM_ID;
import static org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.RadNECTARRoutingTable.TABLE_NAME;

@Entity(tableName = TABLE_NAME)
final class NeighbourhoodIndex {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COL_ROOM_ID)
    private int id;
    
    @ColumnInfo(name = COL_NODE_EID)
    private String nodeEID;
    
    @ColumnInfo(name = COL_MEETING_FREQUENCY)
    private int meetingFrequency;
    
    NeighbourhoodIndex() {}
    
    int getId() {
        return id;
    }
    
    void setId(int id) {
        this.id = id;
    }
    
    String getNodeEID() {
        return nodeEID;
    }
    
    void setNodeEID(String nodeEID) {
        this.nodeEID = nodeEID;
    }
    
    int getMeetingFrequency() {
        return meetingFrequency;
    }
    
    void setMeetingFrequency(int meetingFrequency) {
        this.meetingFrequency = meetingFrequency;
    }
}
