package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import static org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.RadNECTARRoutingTable.COL_NODE_EID;
import static org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.RadNECTARRoutingTable.TABLE_NAME;

@Dao
interface NeighbourhoodIndexDAO {
    
    @Insert
    void insert(NeighbourhoodIndex ni);
    
    @Update
    void update(NeighbourhoodIndex ni);
    
    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_NODE_EID + " = :nodeEID")
    NeighbourhoodIndex getNeighbourhoodIndex(String nodeEID);
}
