package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
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
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.ConvergenceLayerAdapter;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery.PeerDiscovery;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

final class RadP2PService implements PeerDiscovery, ConvergenceLayerAdapter {

    private static final String LOG_TAG
            = DConstants.MAIN_LOG_TAG + "_" + RadP2PService.class.getSimpleName();
    private static final String DTN_SERVICE_ID
            = DTNUtils.DTN_REGISTRATION_TYPE + BuildConfig.APPLICATION_ID;
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;
    private static final AdvertisingOptions ADVERTISING_OPTIONS
            = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
    private static final DiscoveryOptions DISCOVERY_OPTIONS
            = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();

    private DTNEndpointID thisBundleNodezEndpointId;
    private DTNBundle bundleToSend;
    private boolean sent;
    private boolean isIncoming;
    private CLAToRouter router;
    private HashSet<DTNBundleNode> potentialContacts;
    private ConnectionsClient connectionsClient;
    private int bundleNodeCounter;

    private final PayloadCallback payloadCallback
            = new PayloadCallback() {
        private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();
        
        @Override
        public void onPayloadReceived(
                @NonNull String nearbyEndpointID,
                @NonNull Payload payload
        ) {
            if (payload.getType() == Payload.Type.STREAM) {
                Log.i(LOG_TAG, "Incoming bundle from " + getBundleNodeEID(nearbyEndpointID));
                incomingPayloads.put(payload.getId(), payload);
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
                    String message = "Bundle receiving succeeded";
                    if (isIncoming) {
                        Payload payload = incomingPayloads.remove(payloadId);
                        if (payload != null) {
                            try (
                                ObjectInputStream in = new ObjectInputStream(
                                    Objects.requireNonNull(payload.asStream()).asInputStream()
                                )
                            ) {
                                DTNBundle receivedBundle = (DTNBundle) in.readObject();
                                router.deliver(receivedBundle);
                            } catch (Exception e) {
                                Log.e(LOG_TAG, "Bundle could not be read", e);
                            }
                        }
                    }
                    else message = "Bundle sending succeeded";
                    Log.i(LOG_TAG, message);
                    isIncoming = false;
                    connectionsClient.disconnectFromEndpoint(nearbyEndpointID);
                    break;
                case PayloadTransferUpdate.Status.IN_PROGRESS:
                    long transferred = payloadTransferUpdate.getBytesTransferred(); // cumulative
                    message = payloadId + ": " + transferred + " bytes ";
                    if (isIncoming) message += "downloaded";
                    else message += "uploaded";
                    Log.i(LOG_TAG, message);
                    break;
                case PayloadTransferUpdate.Status.FAILURE:
                    Log.e(LOG_TAG, "Bundle " + payloadId + " failed");
                    connectionsClient.disconnectFromEndpoint(nearbyEndpointID);
                    break;
                default:
                    break;
            }
        }
    };

    private final ConnectionLifecycleCallback connectionLifecycleCallback
            = new ConnectionLifecycleCallback() {

        @Override
        public void onConnectionInitiated(
                @NonNull String nearbyEndpointID,
                @NonNull ConnectionInfo connectionInfo
        ) {
            String bundleNodeEid = connectionInfo.getEndpointName();
            DTNBundleNode peerNode = DTNBundleNode.from(bundleNodeEid, nearbyEndpointID);
            
            isIncoming = connectionInfo.isIncomingConnection();

            if (isIncoming) {
                if (isWellKnown(peerNode)) {
                    connectionsClient.acceptConnection(nearbyEndpointID, payloadCallback);
                    Log.i(LOG_TAG, "Accepting incoming connection from wellknown node "
                            + peerNode.dtnEndpointID);
                } else {
                    connectionsClient.rejectConnection(nearbyEndpointID);
                    Log.i(LOG_TAG, "Rejecting incoming connection from unknown node "
                            + peerNode.dtnEndpointID);
                }
            } else {
                connectionsClient.acceptConnection(nearbyEndpointID, payloadCallback);
                Log.i(LOG_TAG, "Accepting outgoing connection to "
                        + peerNode.dtnEndpointID);
            }
        }

        @Override
        public void onConnectionResult(
                @NonNull String nearbyEndpointID,
                @NonNull ConnectionResolution connectionResolution
        ) {
            int statusCode = connectionResolution.getStatus().getStatusCode();
            String eid = getBundleNodeEID(nearbyEndpointID);

            if (eid != null) {
                switch (statusCode) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        // contact established
                        Log.i(LOG_TAG, "Successfully connected to " + eid);

                        if (!isIncoming) // is outgoing connection
                            forwardBundle(nearbyEndpointID, bundleToSend);
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        Log.i(LOG_TAG, "Connection to " + eid + " rejected");
                        break;
                    default:
                        Log.i(LOG_TAG, "Something went seriously wrong! :(");
                        break;
                }
            }
        }

        @Override
        public void onDisconnected(@NonNull String nearbyEndpointID) {
            Log.i(LOG_TAG, "Disconnected from " + getBundleNodeEID(nearbyEndpointID));
            if (sent) {
                router.onBundleForwardingCompleted(++bundleNodeCounter);
                sent = false;
            }
        }
    };

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
                Log.i(LOG_TAG, "Currently discovered nodes: " + potentialContacts);
            }
        }

        @Override
        public void onEndpointLost(@NonNull String nearbyEndpointID) {
            forgetDTNNode(nearbyEndpointID);
            Log.i(LOG_TAG, "Currently discovered nodes: " + potentialContacts);
        }
    };

    RadP2PService(Context context) {
        this.potentialContacts = new HashSet<>();
        this.connectionsClient = Nearby.getConnectionsClient(context);
    }

    @Override
    public void setThisBundleNodezEndpointId(DTNEndpointID thisBundleNodezEndpointId) {
        if (this.thisBundleNodezEndpointId == null) // to persist the first one
            this.thisBundleNodezEndpointId = thisBundleNodezEndpointId;
    }

    @Override
    public void setRouter(CLAToRouter router) {
        this.router = router;
    }

    @Override
    public Set<DTNBundleNode> getPeerList() {
        return potentialContacts;
    }

    @Override
    public void init() {
        if (thisBundleNodezEndpointId != null) {
            Log.i(LOG_TAG, "Starting P2P Service");
            advertise();
            discover();
            
            bundleNodeCounter = 0;
            sent = false;
            isIncoming = false;
        }
    }

    @Override
    public void cleanUp() {
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
        
        potentialContacts.clear(); // forget everyone
        bundleNodeCounter = 0;
        sent = false;
        isIncoming = false;
        Log.i(LOG_TAG, "Stopped P2P Service");
    }

    private void advertise() {
        Log.i(LOG_TAG, "Requesting to advertise this device as a bundle node");

        connectionsClient.startAdvertising(
                thisBundleNodezEndpointId.toString(),
                DTN_SERVICE_ID,
                connectionLifecycleCallback,
                ADVERTISING_OPTIONS
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // We're advertising!
                Log.i(LOG_TAG, "Advertise request succeeded. Device Bundle EID: "
                        + thisBundleNodezEndpointId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // We were unable to start advertising.
                Log.e(LOG_TAG, "Advertise request failed. Retrying...", e);
                connectionsClient.stopAllEndpoints();
                advertise();
            }
        });
    }

    private void discover() {
        Log.i(LOG_TAG, "Requesting to discover other bundle nodes");

        connectionsClient.startDiscovery(
                DTN_SERVICE_ID,
                endpointDiscoveryCallback,
                DISCOVERY_OPTIONS
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // We're discovering!
                Log.i(LOG_TAG, "Discovery request succeeded");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // We were unable to start discovering.
                Log.e(LOG_TAG, "Discovery request failed. Retrying...", e);
                connectionsClient.stopAllEndpoints();
                discover();
            }
        });
    }

    @Override
    public void transmit(DTNBundle bundle, DTNBundleNode destination) {
        bundleToSend = bundle;
        sent = false;
        final DTNEndpointID eid = destination.dtnEndpointID;
        Log.i(LOG_TAG, "Requesting a connection for bundle forwarding");
        String claAddress = destination.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
        
        assert claAddress != null;
        // initiating contact
        connectionsClient.requestConnection(
            thisBundleNodezEndpointId.toString(),
            claAddress,
            connectionLifecycleCallback
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // We successfully requested a connection.
                Log.i(LOG_TAG, "Connection request to " + eid + " succeeded");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Nearby Connections failed to request the connection.
                Log.e(LOG_TAG, "Connection request to " + eid + " failed", e);
            }
        });
    }

    private void forwardBundle(final String CLAAddress, final DTNBundle bundleToSend) {
        final String eid = getBundleNodeEID(CLAAddress);
        int readingSide = 0;
        final int writingSide = 1;

        if (eid != null) {
            try {
                final ParcelFileDescriptor[] payloadPipe = ParcelFileDescriptor.createPipe();

                connectionsClient.sendPayload(
                        CLAAddress,
                        Payload.fromStream(payloadPipe[readingSide])
                ).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(LOG_TAG, "Reading side of pipe successfully sent to " + eid);
                        try (
                            ObjectOutputStream out = new ObjectOutputStream(
                                new ParcelFileDescriptor
                                    .AutoCloseOutputStream(payloadPipe[writingSide])
                            )
                        ) {
                            out.writeObject(bundleToSend);
                            sent = true;
                            Log.i(LOG_TAG, "Bundle forwarded successfully to " + eid);
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Failed to forward bundle to " + eid, e);
                            disconnectAndNotify(CLAAddress);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Sending reading side of pipe to "
                                + eid + " failed", e);
//                        disconnectAndNotify(CLAAddress);
                    }
                });
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to forward bundle to " + eid, e);
                disconnectAndNotify(CLAAddress);
            }
        }
    }

    private void disconnectAndNotify(String CLAAddress) {
        connectionsClient.disconnectFromEndpoint(CLAAddress);
        router.onBundleForwardingCompleted(++bundleNodeCounter);
    }

    private boolean isWellKnown(DTNBundleNode newNode) {
        if (potentialContacts.isEmpty()) {
            return false;
        } else {
            return potentialContacts.contains(newNode);
        }
    }

    private void updateWellKnownDTNNodezCLAAddress(String newCLAAddress, DTNBundleNode staleNode) {
        String updatedCLAAddress = null;
        
        for (DTNBundleNode node : potentialContacts) {
            if (node.equals(staleNode)) {
                node.CLAAddresses.remove(DTNBundleNode.CLAKey.NEARBY);
                node.CLAAddresses.put(DTNBundleNode.CLAKey.NEARBY, newCLAAddress);
                updatedCLAAddress = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
                break;
            }
        }

        Log.d(LOG_TAG, "Updated node " + staleNode.dtnEndpointID
                + "\'s CLA address to " + updatedCLAAddress);
    }

    private void makeDTNNodeWellKnown(DTNBundleNode newNode) {
        potentialContacts.add(newNode);
        Log.i(LOG_TAG, "Newly discovered node: " + newNode);
    }

    private void forgetDTNNode(String CLAAddress) {
        for (DTNBundleNode node : potentialContacts) {
            String claAddress = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
            if (CLAAddress.equals(claAddress)) {
                Log.i(LOG_TAG, node.dtnEndpointID + " has gone away");
                potentialContacts.remove(node);
                break;
            }
        }
    }

    private String getBundleNodeEID(String CLAAddress) {
        for (DTNBundleNode node : potentialContacts) {
            String claAddress = node.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
            if (CLAAddress.equals(claAddress)) {
                return node.dtnEndpointID.toString();
            }
        }
        return null;
    }

    private boolean isDTNNode(String serviceId) {
        return serviceId.equals(DTN_SERVICE_ID);
    }
}

