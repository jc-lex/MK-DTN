package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = RouterDatabase.PROPHET_TABLE_NAME)
final class DeliveryPredictability {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = RouterDatabase.COL_NODE_EID)
    private String nodeEID = "";
    
    @ColumnInfo(name = RouterDatabase.COL_DELIVERY_PREDICTABILITY)
    private float probability;
    
    DeliveryPredictability() {}
    
    @NonNull String getNodeEID() {
        return nodeEID;
    }
    
    void setNodeEID(@NonNull String nodeEID) {
        this.nodeEID = nodeEID;
    }
    
    float getProbability() {
        return probability;
    }
    
    void setProbability(float probability) {
        this.probability = probability;
    }
}
