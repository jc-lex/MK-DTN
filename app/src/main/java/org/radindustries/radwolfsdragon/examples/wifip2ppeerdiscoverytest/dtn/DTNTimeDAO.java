package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
interface DTNTimeDAO {
    
    @Insert
    void insert(DTNTime time);
    
    @Update
    void update(DTNTime time);
    
    @Delete
    void delete(DTNTime time);
    
    @Query("SELECT * FROM " + DTNTimeDB.CLOCK_TABLE_NAME
        + " WHERE " + DTNTimeDB.COL_ID + " = " + DTNTime.MY_TIME)
    DTNTime getTime();
}
