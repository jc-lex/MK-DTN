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
    
    @Query("SELECT * FROM " + RadNECTARRoutingTable.TABLE_NAME + " WHERE "
        + RadNECTARRoutingTable.COL_NODE_EID + " = :nodeEID")
    NeighbourhoodIndex getNeighbourhoodIndex(String nodeEID);
}
