package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
interface NeighbourhoodIndexDAO {
    
    @Insert
    void insert(NeighbourhoodIndex ni);
    
    @Update
    void update(NeighbourhoodIndex ni);
    
    @Query("DELETE FROM " + RouterDatabase.NECTAR_TABLE_NAME + " WHERE "
        + RouterDatabase.COL_NODE_EID + " = :nodeEID")
    void delete(String nodeEID);
    
    @Query("SELECT * FROM " + RouterDatabase.NECTAR_TABLE_NAME + " WHERE "
        + RouterDatabase.COL_NODE_EID + " = :nodeEID")
    NeighbourhoodIndex getNeighbourhoodIndex(String nodeEID);
}
