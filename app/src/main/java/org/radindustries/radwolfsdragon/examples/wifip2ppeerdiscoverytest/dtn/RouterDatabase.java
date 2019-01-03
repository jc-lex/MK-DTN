package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(
    entities = {NeighbourhoodIndex.class, DeliveryPredictability.class},
    version = 4,
    exportSchema = false
)
abstract class RouterDatabase extends RoomDatabase {
    
    private static final String DB_NAME = "router_db";
    
    static final String NECTAR_TABLE_NAME = "neighbourhood_index_table";
    static final String PROPHET_TABLE_NAME = "delivery_predictability_table";
    
    static final String COL_ROOM_ID = "id";
    static final String COL_NODE_EID = "node_EID";
    static final String COL_FIRST_ENCOUNTER_TIMESTAMP = "first_encounter_timestamp";
    static final String COL_MEETING_COUNT = "meeting_count";
    static final String COL_DELIVERY_PREDICTABILITY = "probability";
    
    private static RouterDatabase INSTANCE;
    
    static RouterDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (RouterDatabase.class) {
                INSTANCE
                    = Room.databaseBuilder(context, RouterDatabase.class, DB_NAME).build();
            }
        }
        return INSTANCE;
    }
    
    abstract NeighbourhoodIndexDAO getNIDao();
    abstract DeliveryPredictabilityDAO getDPDao();
}
