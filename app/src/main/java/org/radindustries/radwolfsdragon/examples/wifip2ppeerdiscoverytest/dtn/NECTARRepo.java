package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;

final class NECTARRepo {
    
    private static NECTARRepo repo = null;
    private NeighbourhoodIndexDAO dao;
    
    private NECTARRepo(Context context) {
        NECTARDatabase db = NECTARDatabase.getDatabase(context);
        dao = db.getNIDao();
    }
    
    static NECTARRepo getRepo(Context context) {
        if (repo == null) {
            synchronized (NECTARRepo.class) {
                repo = new NECTARRepo(context);
            }
        }
        return repo;
    }
    
    void insert(NeighbourhoodIndex ni) {
        dao.insert(ni);
    }
    
    void update(NeighbourhoodIndex ni) {
        dao.update(ni);
    }
    
    NeighbourhoodIndex getIndex(String nodeEID) {
        return dao.getNeighbourhoodIndex(nodeEID);
    }
}
