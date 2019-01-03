package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = RouterDatabase.PROPHET_TABLE_NAME)
final class DeliveryPredictability {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = RouterDatabase.COL_ROOM_ID)
    private int id;
    
    @ColumnInfo(name = RouterDatabase.COL_NODE_EID)
    private String nodeEID;
    
    @ColumnInfo(name = RouterDatabase.COL_DELIVERY_PREDICTABILITY)
    private float probability;
    
    DeliveryPredictability() {}
    
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
    
    float getProbability() {
        return probability;
    }
    
    void setProbability(float probability) {
        this.probability = probability;
    }
}
