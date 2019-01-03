package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
interface DeliveryPredictabilityDAO {
    
    @Insert
    long insert(DeliveryPredictability dp);
    
    @Update
    int update(DeliveryPredictability dp);
    
    @Query("SELECT * FROM " + RouterDatabase.PROPHET_TABLE_NAME + " WHERE "
        + RouterDatabase.COL_NODE_EID + " = :nodeEID")
    DeliveryPredictability getDeliveryPredictability(String nodeEID);
    
    @Query("SELECT * FROM " + RouterDatabase.PROPHET_TABLE_NAME)
    List<DeliveryPredictability> getAllDPs();
    
    @Query("DELETE FROM " + RouterDatabase.PROPHET_TABLE_NAME + " WHERE "
        + RouterDatabase.COL_NODE_EID + " = :nodeEID")
    void delete(String nodeEID);
}
