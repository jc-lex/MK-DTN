package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(
    entities = {NeighbourhoodIndex.class},
    version = 1,
    exportSchema = false
)
abstract class NECTARDatabase extends RoomDatabase {
    
    private static NECTARDatabase INSTANCE;
    
    static NECTARDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (NECTARDatabase.class) {
                INSTANCE
                    = Room.databaseBuilder(context, NECTARDatabase.class,
                    RadNECTARRoutingTable.DB_NAME).build();
            }
        }
        return INSTANCE;
    }
    
    abstract NeighbourhoodIndexDAO getNIDao();
}
