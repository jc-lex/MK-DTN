package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
interface NeighbourhoodIndexDAO {
    
    @Insert
    long insert(NeighbourhoodIndex ni);
    
    @Update
    int update(NeighbourhoodIndex ni);
    
    @Query("DELETE FROM " + RouterDatabase.NECTAR_TABLE_NAME + " WHERE "
        + RouterDatabase.COL_NODE_EID + " = :nodeEID")
    int delete(String nodeEID);
    
    @Query("SELECT * FROM " + RouterDatabase.NECTAR_TABLE_NAME + " WHERE "
        + RouterDatabase.COL_NODE_EID + " = :nodeEID")
    NeighbourhoodIndex getNeighbourhoodIndex(String nodeEID);
}
