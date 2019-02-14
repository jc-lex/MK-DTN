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

import androidx.annotation.NonNull;

final class RadPRoPHETRoutingTable implements Daemon2PRoPHETRoutingTable, Daemon2Managable {
    
    private RouterDBHandler routerDBHandler;
    private EIDProvider eidProvider;
    
    private Thread dpAgingExecutor;
    private class AgeDeliveryPredictabilityTask implements Runnable {
        private AgeDeliveryPredictabilityTask() {}
        
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    List<DeliveryPredictability> dps = routerDBHandler.getAllDPs();
        
                    if (!dps.isEmpty()) {
                        float lambda = (float) Math.log(2) / HALF_LIFE_IN_SECONDS;
            
                        for (DeliveryPredictability dp : dps) {
                            dp.setProbability(
                                dp.getProbability() * (1 / (1 + lambda))
                            );
                        }
            
                        routerDBHandler.update(dps);
                    }
        
                    Thread.sleep(1000); // one second
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private RadPRoPHETRoutingTable() {}
    
    RadPRoPHETRoutingTable(@NonNull Context context, @NonNull EIDProvider eidProvider) {
        routerDBHandler = RouterDBHandler.getHandler(context);
        this.eidProvider = eidProvider;
    }
    
    @Override
    public void updateDeliveryPredictability(DTNEndpointID nodeEID) {
        if (eidProvider.isUs(nodeEID)) return;
        
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
        if (!DTNUtils.isValid(bundle)) return;
        
        if (eidProvider.isForUs(bundle) ||
            bundle.primaryBlock.destinationEID.equals(bundle.primaryBlock.custodianEID)) return;
    
        CanonicalBlock prophetCBlock
            = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.PROPHET_ROUTING_INFO);
        
        if (DTNUtils.isValidPRoPHETCBlock(prophetCBlock)) {
            
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
        if (eidProvider.isUs(nodeEID)) return 1.0F;
        
        DeliveryPredictability dp = routerDBHandler.getDP(nodeEID.toString());
        return dp != null ? dp.getProbability() : 0.0F;
    }
    
    @Override
    public boolean start() {
        if (dpAgingExecutor == null) {
            dpAgingExecutor = new Thread(new AgeDeliveryPredictabilityTask());
            dpAgingExecutor.start();
            return dpAgingExecutor.isAlive();
        } else return true;
    }
    
    @Override
    public boolean stop() {
        if (dpAgingExecutor != null) {
            dpAgingExecutor.interrupt();
            boolean done = dpAgingExecutor.isInterrupted();
            dpAgingExecutor = null;
            return done;
        } else return true;
    }
}
