package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.storage;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;

import java.util.List;

public interface Storage {
    int OUTBOUND_QUEUE = 0;
    int DELIVERED_FRAGMENTS_QUEUE = 1;
    int DELIVERED_BUNDLES_QUEUE = 2;
    int TRANSMITTED_BUNDLES_QUEUE = 3;
    
    int put(int queue, DTNBundle... bundles);
    boolean put(DTNBundle bundle, int queue);
    
    DTNBundle get(DTNBundleID bundleID, int queue);
    DTNBundle get(DTNBundleID bundleID, int fragmentOffset, int queue);
    List<DTNBundle> getAll();
    List<DTNBundle> getAll(int queue);
    List<DTNBundle> getDeliveredBundles();
    
    int delete(DTNBundleID... bundleIDs);
    boolean delete(DTNBundleID bundleID);
    boolean delete(DTNBundleID bundleID, int fragmentOffset);
    
    DTNBundle next();
    
    DTNEndpointID getNodeEID();
    
    int updateAge(DTNBundle... bundles);
}
