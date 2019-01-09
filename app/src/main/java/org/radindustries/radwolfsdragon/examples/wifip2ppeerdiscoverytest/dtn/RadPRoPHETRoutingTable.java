package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.EIDProvider;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PRoPHETRoutingInfo;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.Daemon2Managable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2PRoPHETRoutingTable;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;

final class RadPRoPHETRoutingTable implements Daemon2PRoPHETRoutingTable, Daemon2Managable {
    
    private RouterDBHandler routerDBHandler;
    private EIDProvider eidProvider;
    
    private ExecutorService executorService;
    private Future<?> ageDPTaskFuture;
    private class AgeDPTask implements Runnable {
        private AgeDPTask() {}
        
        @Override
        public void run() {
            System.out.println("DP Aging started");
            age();
            System.out.println("DP Aging stopped");
        }
        
        private void age() {
            while (!Thread.interrupted()) {
                List<DeliveryPredictability> dps = routerDBHandler.getAllDPs();
                float lambda = (float) Math.log(2) / HALF_LIFE_IN_SECONDS;
        
                if (!dps.isEmpty()) {
                    for (DeliveryPredictability dp : dps) {
                        dp.setProbability(
                            dp.getProbability() * (1 / (1 + lambda))
                        );
//                        System.out.println(dp.getNodeEID() + " -> " + dp.getProbability());
                    }
                }
        
                try {
                    Thread.sleep(1000); // one second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
    
    private RadPRoPHETRoutingTable() {}
    
    RadPRoPHETRoutingTable(@NonNull Context context, @NonNull EIDProvider eidProvider) {
        routerDBHandler = RouterDBHandler.getHandler(context);
        this.eidProvider = eidProvider;
        executorService = Executors.newSingleThreadExecutor();
    }
    
    @Override
    public void updateDeliveryPredictability(DTNEndpointID nodeEID) {
        if (nodeEID.equals(eidProvider.getThisNodezEID())) return;
        
        DeliveryPredictability dp = routerDBHandler.getDP(nodeEID.toString());
        if (dp != null) { // updating
            if (dp.getProbability() < P_FIRST_THRESHOLD) {
                dp.setProbability(P_ENC_FIRST);
            } else {
                dp.setProbability(
                    dp.getProbability() + (1 - DELTA - dp.getProbability()) * P_ENC_MAX
                );
            }
            routerDBHandler.update(dp);
        } else { // inserting
            DeliveryPredictability newDP = new DeliveryPredictability();
            newDP.setNodeEID(nodeEID.toString());
            newDP.setProbability(P_ENC_FIRST);
            routerDBHandler.insert(newDP);
        }
    }
    
    @Override
    public void calculateDPTransitivity(DTNBundle bundle) {
        if (bundle.primaryBlock.destinationEID.equals(eidProvider.getThisNodezEID()) ||
            bundle.primaryBlock.destinationEID.equals(bundle.primaryBlock.custodianEID)) return;
        
        CanonicalBlock prophetCBlock
            = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.PROPHET_ROUTING_INFO);
        
        if (prophetCBlock != null &&
            prophetCBlock.blockType == CanonicalBlock.BlockType.PROPHET_ROUTING_INFO) {
            
            PRoPHETRoutingInfo info
                = (PRoPHETRoutingInfo) prophetCBlock.blockTypeSpecificDataFields;
            float probCustodian2Destination = info.deliveryPredictability;
            
            float probMe2Custodian = getDeliveryPredictability(bundle.primaryBlock.custodianEID);
            
            DeliveryPredictability dp
                = routerDBHandler.getDP(bundle.primaryBlock.destinationEID.toString());
            float probMe2Destination;
            
            if (dp != null) {
                probMe2Destination = dp.getProbability();
                dp.setProbability(
                    Math.max(
                        probMe2Destination,
                        probMe2Custodian * probCustodian2Destination * BETA
                    )
                );
                
                routerDBHandler.update(dp);
            } else {
                probMe2Destination = 0.0F;
                DeliveryPredictability newDP = new DeliveryPredictability();
                newDP.setNodeEID(bundle.primaryBlock.destinationEID.toString());
                newDP.setProbability(
                    Math.max(
                        probMe2Destination,
                        probMe2Custodian * probCustodian2Destination * BETA
                    )
                );
                
                routerDBHandler.insert(newDP);
            }
        }
    }
    
    @Override
    public float getDeliveryPredictability(DTNEndpointID nodeEID) {
        if (nodeEID.equals(eidProvider.getThisNodezEID())) return 1.0F;
        
        DeliveryPredictability dp = routerDBHandler.getDP(nodeEID.toString());
        return dp != null ? dp.getProbability() : 0.0F;
    }
    
    @Override
    public boolean start() {
        if (ageDPTaskFuture == null || ageDPTaskFuture.isCancelled())
            ageDPTaskFuture = executorService.submit(new AgeDPTask());
        return true;
    }
    
    @Override
    public boolean stop() {
        if (ageDPTaskFuture != null && !ageDPTaskFuture.isCancelled()) {
            ageDPTaskFuture.cancel(true);
            return ageDPTaskFuture.isCancelled();
        } else return true;
    }
}
