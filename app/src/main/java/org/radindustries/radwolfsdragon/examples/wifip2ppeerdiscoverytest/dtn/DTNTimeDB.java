package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DTNTime.class}, version = 1, exportSchema = false)
abstract class DTNTimeDB extends RoomDatabase {
    
    private static final String DATABASE_NAME = "time_db";
    static final String CLOCK_TABLE_NAME = "dtn_wall_clock";
    static final String COL_CURRENT_TIME = "current_time";
    static final String COL_ID = "id";
    
    private static DTNTimeDB timeDB = null;
    static synchronized DTNTimeDB getTimeDB(@NonNull Context context) {
        if (timeDB == null) {
            timeDB = Room.databaseBuilder(context, DTNTimeDB.class, DATABASE_NAME)
                .fallbackToDestructiveMigration().build();
        }
        return timeDB;
    }
    
    abstract DTNTimeDAO getTimeDAO();
}
