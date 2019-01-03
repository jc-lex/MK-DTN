package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

final class RouterDBHandler {
    
    private static RouterDBHandler dbHandler = null;
    
    private NeighbourhoodIndexDAO niDAO;
    private DeliveryPredictabilityDAO dpDAO;
    
    private RouterDBHandler(Context context) {
        RouterDatabase db = RouterDatabase.getDatabase(context);
        niDAO = db.getNIDao();
        dpDAO = db.getDPDao();
    }
    
    static RouterDBHandler getHandler(Context context) {
        if (dbHandler == null) {
            synchronized (RouterDBHandler.class) {
                dbHandler = new RouterDBHandler(context);
            }
        }
        return dbHandler;
    }
    
    void insert(NeighbourhoodIndex ni) {
        new InsertIndexTask(niDAO).execute(ni);
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
    
    long insert(DeliveryPredictability dp) {
        InsertDPTask insertDPTask = new InsertDPTask(dpDAO);
        long rowsInserted = 0;
        insertDPTask.execute(dp);
        try {
            rowsInserted = insertDPTask.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return rowsInserted;
    }
    
    private static class InsertDPTask extends AsyncTask<DeliveryPredictability, Void, Long> {
        private DeliveryPredictabilityDAO dpDAO;
        InsertDPTask(DeliveryPredictabilityDAO dpDAO) {
            this.dpDAO = dpDAO;
        }
    
        @Override
        protected Long doInBackground(DeliveryPredictability... deliveryPredictabilities) {
            return dpDAO.insert(deliveryPredictabilities[0]);
        }
    }
    
    void update(NeighbourhoodIndex ni) {
        new UpdateIndexTask(niDAO).execute(ni);
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
    
    int update(DeliveryPredictability dp) {
        UpdateDPTask updateDPTask = new UpdateDPTask(dpDAO);
        int rowsUpdated = 0;
        updateDPTask.execute(dp);
        try {
            rowsUpdated = updateDPTask.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return rowsUpdated;
    }
    
    private static class UpdateDPTask extends AsyncTask<DeliveryPredictability, Void, Integer> {
        private DeliveryPredictabilityDAO dpDAO;
        UpdateDPTask(DeliveryPredictabilityDAO dpDAO) {
            this.dpDAO = dpDAO;
        }
    
        @Override
        protected Integer doInBackground(DeliveryPredictability... deliveryPredictabilities) {
            return dpDAO.update(deliveryPredictabilities[0]);
        }
    }
    
    void deleteIndex(String nodeEID) {
        new DeleteIndexTask(niDAO).execute(nodeEID);
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
    
    void deleteDP(String nodeEID) {
        new DeleteDPTask(dpDAO).execute(nodeEID);
    }
    
    private static class DeleteDPTask extends AsyncTask<String, Void, Void> {
        private DeliveryPredictabilityDAO dpDAO;
        DeleteDPTask(DeliveryPredictabilityDAO dpDAO) {
            this.dpDAO = dpDAO;
        }
    
        @Override
        protected Void doInBackground(String... strings) {
            dpDAO.delete(strings[0]);
            return null;
        }
    }
    
    NeighbourhoodIndex getIndex(String nodeEID) {
        GetIndexTask getIndexTask = new GetIndexTask(niDAO);
        NeighbourhoodIndex ni = new NeighbourhoodIndex();
        getIndexTask.execute(nodeEID);
        try {
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
    
    DeliveryPredictability getDP(String nodeEID) {
        GetDPTask getDPTask = new GetDPTask(dpDAO);
        DeliveryPredictability dp = new DeliveryPredictability();
        getDPTask.execute(nodeEID);
        try {
            dp = getDPTask.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return dp;
    }
    
    private static class GetDPTask extends AsyncTask<String, Void, DeliveryPredictability> {
        private DeliveryPredictabilityDAO dpDAO;
        GetDPTask(DeliveryPredictabilityDAO dpDAO) {
            this.dpDAO = dpDAO;
        }
    
        @Override
        protected DeliveryPredictability doInBackground(String... strings) {
            return dpDAO.getDeliveryPredictability(strings[0]);
        }
    }
    
    List<DeliveryPredictability> getAllDPs() {
        GetAllDPsTask getAllDPsTask = new GetAllDPsTask(dpDAO);
        List<DeliveryPredictability> dps = Collections.emptyList();
        getAllDPsTask.execute();
        try {
            dps = getAllDPsTask.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return dps;
    }
    
    private static class GetAllDPsTask extends AsyncTask<Void, Void, List<DeliveryPredictability>> {
        private DeliveryPredictabilityDAO dpDAO;
        GetAllDPsTask(DeliveryPredictabilityDAO dpDAO) {
            this.dpDAO = dpDAO;
        }
    
        @Override
        protected List<DeliveryPredictability> doInBackground(Void... voids) {
            return dpDAO.getAllDPs();
        }
    }
}
