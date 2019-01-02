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
    
    @ColumnInfo(name = RadNECTARRoutingTable.COL_FIRST_ENCOUNTER_TIMESTAMP)
    private long firstEncounterTimestamp;
    
    @ColumnInfo(name = RadNECTARRoutingTable.COL_MEETING_COUNT)
    private int meetingCount;
    
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
    
    long getFirstEncounterTimestamp() {
        return firstEncounterTimestamp;
    }
    
    void setFirstEncounterTimestamp(long firstEncounterTimestamp) {
        this.firstEncounterTimestamp = firstEncounterTimestamp;
    }
    
    int getMeetingCount() {
        return meetingCount;
    }
    
    void setMeetingCount(int meetingCount) {
        this.meetingCount = meetingCount;
    }
}
