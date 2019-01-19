package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

final class RouterDBHandler {
    
    private static final long WAITING_TIME_IN_SECONDS = 5L;
    private static final int ERROR_CODE = -69;
    
    private static RouterDBHandler dbHandler = null;
    
    private NeighbourhoodIndexDAO niDAO;
    private DeliveryPredictabilityDAO dpDAO;
    private ExecutorService executorService;
    
    private RouterDBHandler() {}
    private RouterDBHandler(Context context) {
        RouterDatabase db = RouterDatabase.getDatabase(context);
        executorService = Executors.newCachedThreadPool();
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
    
    long insert(final NeighbourhoodIndex ni) {
        Future<Long> future = executorService.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return niDAO.insert(ni);
            }
        });
        
        try {
            return future.get(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_CODE;
        }
    }
    
    long insert(final DeliveryPredictability dp) {
        Future<Long> future = executorService.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return dpDAO.insert(dp);
            }
        });
        
        try {
            return future.get(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_CODE;
        }
    }
    
    int update(final NeighbourhoodIndex ni) {
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return niDAO.update(ni);
            }
        });
        
        try {
            return future.get(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_CODE;
        }
    }
    
    int update(final DeliveryPredictability... dp) {
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return dpDAO.update(dp);
            }
        });
        
        try {
            return future.get(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_CODE;
        }
    }
    
    int update(List<DeliveryPredictability> dps) {
        return update(dps.toArray(new DeliveryPredictability[0]));
    }
    
    int deleteNI(final String nodeEID) {
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return niDAO.delete(nodeEID);
            }
        });
        
        try {
            return future.get(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_CODE;
        }
    }
    
    int deleteDP(final String nodeEID) {
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return dpDAO.delete(nodeEID);
            }
        });
        
        try {
            return future.get(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_CODE;
        }
    }
    
    NeighbourhoodIndex getIndex(final String nodeEID) {
        Future<NeighbourhoodIndex> future
            = executorService.submit(new Callable<NeighbourhoodIndex>() {
            @Override
            public NeighbourhoodIndex call() throws Exception {
                return niDAO.getNeighbourhoodIndex(nodeEID);
            }
        });
        
        try {
            return future.get(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return new NeighbourhoodIndex();
        }
    }
    
    DeliveryPredictability getDP(final String nodeEID) {
        Future<DeliveryPredictability> future
            = executorService.submit(new Callable<DeliveryPredictability>() {
            @Override
            public DeliveryPredictability call() throws Exception {
                return dpDAO.getDeliveryPredictability(nodeEID);
            }
        });
        
        try {
            return future.get(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return new DeliveryPredictability();
        }
    }
    
    List<DeliveryPredictability> getAllDPs() {
        Future<List<DeliveryPredictability>> future
            = executorService.submit(new Callable<List<DeliveryPredictability>>() {
            @Override
            public List<DeliveryPredictability> call() throws Exception {
                return dpDAO.getAllDPs();
            }
        });
        
        try {
            return future.get(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
