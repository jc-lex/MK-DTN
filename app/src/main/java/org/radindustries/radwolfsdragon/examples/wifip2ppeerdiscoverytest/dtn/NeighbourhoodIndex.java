package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = RouterDatabase.NECTAR_TABLE_NAME)
final class NeighbourhoodIndex {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = RouterDatabase.COL_NODE_EID)
    private String nodeEID = "";
    
    @ColumnInfo(name = RouterDatabase.COL_FIRST_ENCOUNTER_TIMESTAMP)
    private long firstEncounterTimestamp;
    
    @ColumnInfo(name = RouterDatabase.COL_MEETING_COUNT)
    private int meetingCount;
    
    NeighbourhoodIndex() {}
    
    @NonNull String getNodeEID() {
        return nodeEID;
    }
    
    void setNodeEID(@NonNull String nodeEID) {
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
