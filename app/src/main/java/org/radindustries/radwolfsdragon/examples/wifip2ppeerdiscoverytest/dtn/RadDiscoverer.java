package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.BuildConfig;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.PeerDiscoverer2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.Daemon2Managable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscoverer.Daemon2PeerDiscoverer;

import java.util.HashSet;
import java.util.Set;

final class RadDiscoverer implements Daemon2PeerDiscoverer, Daemon2Managable {
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + RadDiscoverer.class.getSimpleName();
    
    private static final String DTN_SERVICE_ID
        = DTNUtils.DTN_REGISTRATION_TYPE + BuildConfig.APPLICATION_ID;
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;
    private static final AdvertisingOptions ADVERTISING_OPTIONS
        = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
    private static final DiscoveryOptions DISCOVERY_OPTIONS
        = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
    
    private final EndpointDiscoveryCallback endpointDiscoveryCallback
        = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(
            @NonNull String nearbyEndpointID,
            @NonNull DiscoveredEndpointInfo discoveredEndpointInfo
        ) {
            String bundleNodeEID = discoveredEndpointInfo.getEndpointName();
            String serviceId = discoveredEndpointInfo.getServiceId();
            
            DTNBundleNode foundNode = DTNBundleNode.from(bundleNodeEID, nearbyEndpointID);
            
            if (isDTNNode(serviceId)) {
                if (isWellKnown(foundNode)) {
                    updateWellKnownDTNNodezCLAAddress(nearbyEndpointID, foundNode);
                } else {
                    makeDTNNodeWellKnown(foundNode);
                }
            }
        }
        
        @Override
        public void onEndpointLost(@NonNull String nearbyEndpointID) {
            forgetDTNNode(nearbyEndpointID);
        }
    };
    
    interface CLCProvider {
        ConnectionLifecycleCallback getCLC();
    }
    
    private Set<DTNBundleNode> potentialContacts;
    private PeerDiscoverer2Daemon daemon;
    private CLCProvider provider;
    private ConnectionsClient connectionsClient;
    
    private RadDiscoverer() {}
    
    RadDiscoverer(
        @NonNull PeerDiscoverer2Daemon daemon,
        @NonNull Context context,
        @NonNull CLCProvider provider) {
        this.daemon = daemon;
        this.provider = provider;
        connectionsClient = Nearby.getConnectionsClient(context);
        potentialContacts = new HashSet<>();
    }
    
    @Override
    public Set<DTNBundleNode> getPeerList() {
        return potentialContacts;
    }
    
    @Override
    public boolean start() {
        advertise();
        discover();
        return true;
    }
    
    private void advertise() {
        connectionsClient.startAdvertising(
            daemon.getThisNodezEID().toString(),
            DTN_SERVICE_ID,
            provider.getCLC(),
            ADVERTISING_OPTIONS
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                connectionsClient.stopAllEndpoints();
                advertise();
            }
        });
    }
    
    private void discover() {
        connectionsClient.startDiscovery(
            DTN_SERVICE_ID,
            endpointDiscoveryCallback,
            DISCOVERY_OPTIONS
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                connectionsClient.stopAllEndpoints();
                discover();
            }
        });
    }
    
    @Override
    public boolean stop() {
        potentialContacts.clear();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAdvertising();
        connectionsClient.stopAllEndpoints();
        return true;
    }
    
    private boolean isWellKnown(DTNBundleNode newNode) {
        if (potentialContacts.isEmpty()) {
            return false;
        } else {
            return potentialContacts.contains(newNode);
        }
    }
    
    private void updateWellKnownDTNNodezCLAAddress(String newCLAAddress, DTNBundleNode staleNode) {
        for (DTNBundleNode node : potentialContacts) {
            if (node.equals(staleNode)) {
                node.CLAAddresses.remove(DTNBundleNode.CLAKey.NEARBY);
                node.CLAAddresses.put(DTNBundleNode.CLAKey.NEARBY, newCLAAddress);
                break;
            }
        }
    }
    
    private void makeDTNNodeWellKnown(DTNBundleNode newNode) {
        potentialContacts.add(newNode);
        Log.i(LOG_TAG, "found");
    }
    
    private void forgetDTNNode(String CLAAddress) {
        for (DTNBundleNode node : potentialContacts) {
            String claAddress = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
            if (CLAAddress.equals(claAddress)) {
                potentialContacts.remove(node);
                Log.i(LOG_TAG, "forgotten");
                break;
            }
        }
    }
    
    private boolean isDTNNode(String serviceId) {
        return serviceId.equals(DTN_SERVICE_ID);
    }
}
