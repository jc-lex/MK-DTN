package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = RadNECTARRoutingTable.TABLE_NAME)
final class NeighbourhoodIndex {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = RadNECTARRoutingTable.COL_ROOM_ID)
    private int id;
    
    @ColumnInfo(name = RadNECTARRoutingTable.COL_NODE_EID)
    private String nodeEID;
    
    @ColumnInfo(name = RadNECTARRoutingTable.COL_MEETING_FREQUENCY)
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
