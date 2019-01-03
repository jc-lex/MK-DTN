package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;

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
        new InsertIndexTask(dao).execute(ni);
    }
    
    private static class InsertIndexTask extends AsyncTask<NeighbourhoodIndex, Void, Void> {
        
        private NeighbourhoodIndexDAO niDAO;
        InsertIndexTask(NeighbourhoodIndexDAO niDAO) {
            this.niDAO = niDAO;
        }
    
        @Override
        protected Void doInBackground(NeighbourhoodIndex... neighbourhoodIndices) {
            niDAO.insert(neighbourhoodIndices[0]);
            return null;
        }
    }
    
    void update(NeighbourhoodIndex ni) {
        new UpdateIndexTask(dao).execute(ni);
    }
    
    private static class UpdateIndexTask extends AsyncTask<NeighbourhoodIndex, Void, Void> {
        private NeighbourhoodIndexDAO niDAO;
        UpdateIndexTask(NeighbourhoodIndexDAO niDAO) {
            this.niDAO = niDAO;
        }
    
        @Override
        protected Void doInBackground(NeighbourhoodIndex... neighbourhoodIndices) {
            niDAO.update(neighbourhoodIndices[0]);
            return null;
        }
    }
    
    void delete(String nodeEID) {
        new DeleteIndexTask(dao).execute(nodeEID);
    }
    
    private static class DeleteIndexTask extends AsyncTask<String, Void, Void> {
        private NeighbourhoodIndexDAO niDAO;
        DeleteIndexTask(NeighbourhoodIndexDAO niDAO) {
            this.niDAO = niDAO;
        }
    
        @Override
        protected Void doInBackground(String... strings) {
            niDAO.delete(strings[0]);
            return null;
        }
    }
    
    NeighbourhoodIndex getIndex(String nodeEID) {
        GetIndexTask getIndexTask = new GetIndexTask(dao);
        NeighbourhoodIndex ni = new NeighbourhoodIndex();
        try {
            getIndexTask.execute(nodeEID);
            ni = getIndexTask.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return ni;
    }
    
    private static class GetIndexTask extends AsyncTask<String, Void, NeighbourhoodIndex> {
    
        private NeighbourhoodIndexDAO niDAO;
        GetIndexTask(NeighbourhoodIndexDAO niDAO) {
            this.niDAO = niDAO;
        }
    
        @Override
        protected NeighbourhoodIndex doInBackground(String... strings) {
            return niDAO.getNeighbourhoodIndex(strings[0]);
        }
    }
}
