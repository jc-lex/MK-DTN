package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.support.annotation.NonNull;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PRoPHETRoutingInfo;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.Daemon2Managable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2PRoPHETRoutingTable;

import java.util.List;

final class RadPRoPHETRoutingTable implements Daemon2PRoPHETRoutingTable, Daemon2Managable {
    
    private RouterDBHandler routerDBHandler;
    
    private final Thread dpAgingTask = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                List<DeliveryPredictability> dps = routerDBHandler.getAllDPs();
                
                if (!dps.isEmpty()) {
                    for (DeliveryPredictability dp : dps) {
                        dp.setProbability(
                            dp.getProbability() * GAMMA_POW_K
                        );
    
                        routerDBHandler.update(dp);
                    }
                }
            }
        }
    });
    
    private RadPRoPHETRoutingTable() {}
    
    RadPRoPHETRoutingTable(@NonNull Context context) {
        routerDBHandler = RouterDBHandler.getHandler(context);
    }
    
    @Override
    public void updateDeliveryPredictability(DTNEndpointID nodeEID) {
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
        DeliveryPredictability dp = routerDBHandler.getDP(nodeEID.toString());
        return dp != null ? dp.getProbability() : 0.0F;
    }
    
    @Override
    public boolean start() {
        if (!dpAgingTask.isAlive()) dpAgingTask.start();
        return dpAgingTask.isAlive();
    }
    
    @Override
    public boolean stop() {
        if (dpAgingTask.isAlive()) dpAgingTask.interrupt();
        return dpAgingTask.isAlive();
    }
}
