package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.os.ParcelFileDescriptor;
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
import com.google.android.gms.tasks.OnSuccessListener;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.BuildConfig;
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;

final class RadNearby implements Daemon2CLA, Daemon2PeerDiscoverer, Daemon2Managable {
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + RadNearby.class.getSimpleName();
    
    private static final String DTN_SERVICE_ID = BuildConfig.APPLICATION_ID;
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;
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
    
    private boolean isInbound;
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
    
            if (serviceId.equals(DTN_SERVICE_ID)) {
                DTNEndpointID eid = DTNEndpointID.parse(bundleNodeEID);
                nectarPeerDiscoverer2Daemon.incrementMeetingCount(eid);
                prophetPeerDiscoverer2Daemon.updateDeliveryPredictability(eid);
        
                if (peers.contains(foundNode)) {
                    updateCLAAddress(foundNode, nearbyEndpointID);
                } else {
                    peers.add(foundNode);
                    peerDiscoverer2Daemon.notifyPeerListChanged();
                }
            }
        }
    
        private void updateCLAAddress(DTNBundleNode staleNode, String newCLAAddress) {
            for (DTNBundleNode node : peers) {
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
            peerDiscoverer2Daemon.notifyPeerListChanged();
        }
    
        private void forgetDTNNode(String CLAAddress) {
            for (DTNBundleNode node : peers) {
                String claAddress = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
                if (CLAAddress.equals(claAddress)) {
                    peers.remove(node);
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
            isInbound = connectionInfo.isIncomingConnection();
            connectionsClient.acceptConnection(nearbyEndpointID, payloadCallback);
        }
    
        @Override
        public void onConnectionResult(
            @NonNull String nearbyEndpointID, @NonNull ConnectionResolution connectionResolution
        ) {
            int status = connectionResolution.getStatus().getStatusCode();
            
            if (status == ConnectionsStatusCodes.STATUS_OK) {
                if (!isInbound) forward(bundle, nearbyEndpointID);
            } else {
                Log.e(LOG_TAG, "Connection was not OK");
                sent = true;
            }
        }
    
        private void forward(final DTNBundle bundle, String claAddress) {
            try {
                final ParcelFileDescriptor[] payloadPipe = ParcelFileDescriptor.createPipe();
            
                connectionsClient.sendPayload(
                    claAddress,
                    Payload.fromStream(payloadPipe[0]) // reading side
                ).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        try (
                            ObjectOutputStream out = new ObjectOutputStream(
                                new ParcelFileDescriptor
                                    .AutoCloseOutputStream(payloadPipe[1]) // writing side
                            )
                        ) {
                            out.writeObject(bundle);
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Could not create or write to output stream.", e);
                            sent = true;
                        }
                    }
                });
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not create pipe to transfer data.", e);
                sent = true;
            }
        }
    
        @Override
        public void onDisconnected(@NonNull String nearbyEndpointID) {
            if (isInbound) isInbound = false;
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
    
            switch (status) {
                case PayloadTransferUpdate.Status.SUCCESS:
                    if (isInbound) {
                        Payload payload = inboundPayloads.remove(payloadId);
                        assert payload != null;
                        
                        try (
                            ObjectInputStream in = new ObjectInputStream(
                                Objects.requireNonNull(payload.asStream()).asInputStream()
                            )
                        ) {
                            DTNBundle receivedBundle = (DTNBundle) in.readObject();
                            DTNUtils.setTimeReceived(receivedBundle);
                    
                            prophetCLA2Daemon.calculateDPTransitivity(receivedBundle);
                            cla2Daemon.onBundleReceived(receivedBundle);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Could not open or read from input stream.", e);
                        }
                    } else {
                        sent = true;
                        Log.i(LOG_TAG, "bundle sent = true");
                    }
                    break;
                case PayloadTransferUpdate.Status.FAILURE:
                    if (isInbound) inboundPayloads.remove(payloadId);
                    else {
                        sent = true;
                        Log.e(LOG_TAG, "bundle sending failed");
                    }
                    break;
                default:
                    break;
            }
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
    }
    
    private boolean sent;
    private DTNBundle bundle;
    
    @Override
    public void transmit(final DTNBundle bundle, Set<DTNBundleNode> destinations) {
        this.bundle = bundle;
        
        for (final DTNBundleNode node : destinations) {
            Log.i(LOG_TAG, "sending to " + node);
            
            sent = false;
            
            String claAddress = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
            assert claAddress != null;
            
            connectionsClient.requestConnection(
                cla2Daemon.getThisNodezEID().toString(),
                claAddress,
                connectionLifecycleCallback
            ).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sent = true;
                    Log.e(LOG_TAG, "connection request to " + node + " failed", e);
                }
            });
            
            while(!sent) {/*wait*/}
            connectionsClient.disconnectFromEndpoint(claAddress);
        }
    }
    
    @Override
    public boolean start() {
        peers = new HashSet<>();
        advertise();
        discover();
        return true;
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
                Log.e(LOG_TAG, "Advertising failed. Retrying...", e);
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
                Log.e(LOG_TAG, "Discovery failed. Retrying...", e);
                connectionsClient.stopAllEndpoints();
                discover();
            }
        });
    }
    
    @Override
    public boolean stop() {
        if (peers != null) peers.clear();
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
        return true;
    }
    
    private Set<DTNBundleNode> peers;
    
    @Override
    public Set<DTNBundleNode> getPeerList() {
        return Collections.unmodifiableSet(peers);
    }

//    private void markAsUpContact(String claAddress) {
//        for (DTNBundleNode node : discoveredContacts) {
//            String address = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
//            if (claAddress.equals(address)) {
//                connectedContacts.add(node);
//                break;
//            }
//        }
//    }
//
//    private void markAsDownContact(String claAddress) {
//        for (DTNBundleNode node : connectedContacts) {
//            String address = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
//            if (claAddress.equals(address)) {
//                connectedContacts.remove(node);
//                break;
//            }
//        }
//    }
}
