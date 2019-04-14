package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.Daemon2CLA;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.CLA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.NECTARPeerDiscoverer2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.PRoPHETCLA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.PRoPHETPeerDiscoverer2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.PeerDiscoverer2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.Daemon2Managable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscoverer.Daemon2PeerDiscoverer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;

final class RadNearby implements Daemon2CLA, Daemon2PeerDiscoverer, Daemon2Managable {
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + RadNearby.class.getSimpleName();
    
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private static final AdvertisingOptions ADVERTISING_OPTIONS
        = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
    private static final DiscoveryOptions DISCOVERY_OPTIONS
        = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
    
    private PeerDiscoverer2Daemon peerDiscoverer2Daemon;
    private CLA2Daemon cla2Daemon;
    private NECTARPeerDiscoverer2Daemon nectarPeerDiscoverer2Daemon;
    private PRoPHETPeerDiscoverer2Daemon prophetPeerDiscoverer2Daemon;
    private PRoPHETCLA2Daemon prophetCLA2Daemon;
    private ConnectionsClient connectionsClient;

    private final EndpointDiscoveryCallback endpointDiscoveryCallback
        = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(
            @NonNull String nearbyEndpointID,
            @NonNull DiscoveredEndpointInfo discoveredEndpointInfo
        ) {
            final String bundleNodeEID = discoveredEndpointInfo.getEndpointName();
            String serviceId = discoveredEndpointInfo.getServiceId();
    
            if (serviceId.equals(DTN_SERVICE_ID)) {
                DTNEndpointID eid = DTNEndpointID.parse(bundleNodeEID);
                nectarPeerDiscoverer2Daemon.incrementMeetingCount(eid);
                prophetPeerDiscoverer2Daemon.updateDeliveryPredictability(eid);
                
                DTNBundleNode foundNode = DTNBundleNode.from(bundleNodeEID, nearbyEndpointID);
                if (discoveredNodes.contains(foundNode))
                    updateCLAAddress(foundNode, nearbyEndpointID);
                else discoveredNodes.add(foundNode);
    
                connectionsClient.requestConnection(
                    cla2Daemon.getThisNodezEID().toString(), // from us...
                    nearbyEndpointID, // ...to them
                    connectionLifecycleCallback
                ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "connection request to " + bundleNodeEID
                            + " failed", e);
                    }
                });
            }
        }
    
        private void updateCLAAddress(DTNBundleNode staleNode, String newCLAAddress) {
            for (DTNBundleNode node : discoveredNodes) {
                if (node.equals(staleNode)) {
                    node.CLAAddresses.remove(DTNBundleNode.CLAKey.NEARBY);
                    node.CLAAddresses.put(DTNBundleNode.CLAKey.NEARBY, newCLAAddress);
                    break;
                }
            }
        }
    
        @Override
        public void onEndpointLost(@NonNull String nearbyEndpointID) {
            forgetDTNNode(nearbyEndpointID);
        }
    
        private void forgetDTNNode(String CLAAddress) {
            for (DTNBundleNode node : discoveredNodes) {
                String claAddress = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
                if (CLAAddress.equals(claAddress)) {
                    discoveredNodes.remove(node);
                    break;
                }
            }
        }
    };
    private final ConnectionLifecycleCallback connectionLifecycleCallback
        = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(
            @NonNull String nearbyEndpointID, @NonNull ConnectionInfo connectionInfo
        ) {
            connectionsClient.acceptConnection(nearbyEndpointID, payloadCallback);
        }
    
        @Override
        public void onConnectionResult(
            @NonNull String nearbyEndpointID, @NonNull ConnectionResolution connectionResolution
        ) {
            int status = connectionResolution.getStatus().getStatusCode();
            
            if (status == ConnectionsStatusCodes.STATUS_OK) {
                markAsUpContact(nearbyEndpointID);
                peerDiscoverer2Daemon.notifyPeerListChanged();
            } else {
                Log.e(LOG_TAG, "Connection was not OK");
            }
        }
    
        @Override
        public void onDisconnected(@NonNull String nearbyEndpointID) {
            markAsDownContact(nearbyEndpointID);
            peerDiscoverer2Daemon.notifyPeerListChanged();
        }
    };
    private final PayloadCallback payloadCallback = new PayloadCallback() {
        private final SimpleArrayMap<Long, Payload> inboundPayloads = new SimpleArrayMap<>();
        
        @Override
        public void onPayloadReceived(@NonNull String nearbyEndpointID, @NonNull Payload payload) {
            if (payload.getType() == Payload.Type.STREAM) {
                inboundPayloads.put(payload.getId(), payload);
            }
        }
    
        @Override
        public void onPayloadTransferUpdate(
            @NonNull String nearbyEndpointID,
            @NonNull PayloadTransferUpdate payloadTransferUpdate
        ) {
            int status = payloadTransferUpdate.getStatus();
            long payloadId = payloadTransferUpdate.getPayloadId();
            Payload payload = inboundPayloads.remove(payloadId);
            
            if (status == PayloadTransferUpdate.Status.SUCCESS) {
                if (payload != null) {
                    try (ObjectInputStream in = new ObjectInputStream(
                            Objects.requireNonNull(payload.asStream()).asInputStream()
                    )) {
                        DTNBundle receivedBundle = (DTNBundle) in.readObject();
                        prophetCLA2Daemon.calculateDPTransitivity(receivedBundle);
                        cla2Daemon.onBundleReceived(receivedBundle);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Could not open or read from input stream.", e);
                    }
                }
            }/* else if (status == PayloadTransferUpdate.Status.FAILURE) {
                Log.e(LOG_TAG, "bundle sending failed");
            }*/
        }
    };
    
    private RadNearby() {}
    
    RadNearby(
        @NonNull PeerDiscoverer2Daemon peerDiscoverer2Daemon,
        @NonNull CLA2Daemon cla2Daemon,
        @NonNull PRoPHETCLA2Daemon prophetCLA2Daemon,
        @NonNull PRoPHETPeerDiscoverer2Daemon prophetPeerDiscoverer2Daemon,
        @NonNull NECTARPeerDiscoverer2Daemon nectarPeerDiscoverer2Daemon,
        @NonNull Context context
    ) {
        connectionsClient = Nearby.getConnectionsClient(context);
        this.cla2Daemon = cla2Daemon;
        this.prophetCLA2Daemon = prophetCLA2Daemon;
        this.peerDiscoverer2Daemon = peerDiscoverer2Daemon;
        this.nectarPeerDiscoverer2Daemon = nectarPeerDiscoverer2Daemon;
        this.prophetPeerDiscoverer2Daemon = prophetPeerDiscoverer2Daemon;
        discoveredNodes = new HashSet<>();
        connectedNodes = new HashSet<>();
    }
    
    @Override
    public synchronized int transmit(DTNBundle bundle, Set<DTNBundleNode> destinations) {
        if (destinations.isEmpty()) return 0;
        
        List<String> nextHops = new ArrayList<>();
        for (DTNBundleNode node : destinations) {
            String addr = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
            nextHops.add(addr);
        }
    
        int numSent = 0;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(bundle);
            out.flush();
        
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            Payload p = Payload.fromStream(bis);
        
            if (connectionsClient.sendPayload(nextHops, p)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "bundle sending failed", e);
                    }
                })
                .isSuccessful()
            ) numSent++;
        } catch (IOException e) {
            Log.e(LOG_TAG, "error creating output stream");
        }
        
        return numSent;
    }
    
    @Override
    public synchronized boolean transmit(DTNBundle bundle, DTNBundleNode destination) {
        String nextHop = destination.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
        assert nextHop != null;
        
        boolean sent = false;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(bundle);
            out.flush();
        
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            Payload p = Payload.fromStream(bis);
        
            sent = connectionsClient.sendPayload(nextHop, p)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "bundle sending failed", e);
                    }
                })
                .isSuccessful();
        } catch (IOException e) {
            Log.e(LOG_TAG, "error creating output stream");
        }
        
        return sent;
    }
    
    private void advertise() {
        connectionsClient.startAdvertising(
            cla2Daemon.getThisNodezEID().toString(),
            DTN_SERVICE_ID,
            connectionLifecycleCallback,
            ADVERTISING_OPTIONS
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(LOG_TAG, "Advertising failed.", e);
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
                Log.e(LOG_TAG, "Discovery failed.", e);
            }
        });
    }
    
    @Override
    public boolean start() {
        connectedNodes.clear();
        discoveredNodes.clear();
        peerDiscoverer2Daemon.notifyPeerListChanged();
        
        advertise();
        discover();
        
        return true;
    }
    
    @Override
    public boolean stop() {
        connectedNodes.clear();
        discoveredNodes.clear();
        peerDiscoverer2Daemon.notifyPeerListChanged();
    
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
        
        return true;
    }
    
    private Set<DTNBundleNode> discoveredNodes;
    private Set<DTNBundleNode> connectedNodes;
    
    @Override
    public synchronized Set<DTNBundleNode> getPeerList() {
        return Collections.unmodifiableSet(connectedNodes);
    }

    private void markAsUpContact(String claAddress) {
        for (DTNBundleNode node : discoveredNodes) {
            String address = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
            if (claAddress.equals(address)) {
                connectedNodes.add(node);
                break;
            }
        }
    }

    private void markAsDownContact(String claAddress) {
        for (DTNBundleNode node : connectedNodes) {
            String address = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
            if (claAddress.equals(address)) {
                connectedNodes.remove(node);
                break;
            }
        }
    }
}
