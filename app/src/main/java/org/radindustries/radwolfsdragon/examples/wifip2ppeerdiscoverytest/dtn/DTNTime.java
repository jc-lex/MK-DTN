package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import java.math.BigInteger;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = DTNTimeDB.CLOCK_TABLE_NAME)
final class DTNTime {
    
    @PrimaryKey
    @ColumnInfo(name = DTNTimeDB.COL_ID)
    private long id;
    
    @ColumnInfo(name = DTNTimeDB.COL_CURRENT_TIME)
    private String currentTime;
    
    static final long MY_TIME = 1L;
    
    DTNTime() {
        id = MY_TIME;
        currentTime = BigInteger.ZERO.toString();
    }
    
    long getId() {
        return id;
    }
    
    void setId(long id) {
        this.id = id;
    }
    
    String getCurrentTime() {
        return currentTime;
    }
    
    void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }
}
