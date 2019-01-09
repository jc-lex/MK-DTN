package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
interface DeliveryPredictabilityDAO {
    
    @Insert
    long insert(DeliveryPredictability dp);
    
    @Update
    int update(DeliveryPredictability... dp);
    
    @Query("SELECT * FROM " + RouterDatabase.PROPHET_TABLE_NAME + " WHERE "
        + RouterDatabase.COL_NODE_EID + " = :nodeEID")
    DeliveryPredictability getDeliveryPredictability(String nodeEID);
    
    @Query("SELECT * FROM " + RouterDatabase.PROPHET_TABLE_NAME)
    List<DeliveryPredictability> getAllDPs();
    
    @Query("DELETE FROM " + RouterDatabase.PROPHET_TABLE_NAME + " WHERE "
        + RouterDatabase.COL_NODE_EID + " = :nodeEID")
    int delete(String nodeEID);
}
