package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.BuildConfig;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.ConvergenceLayerAdapter;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery.PeerDiscovery;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

final class RadP2PService implements PeerDiscovery, ConvergenceLayerAdapter {

    private static final String LOG_TAG
            = DTNConstants.MAIN_LOG_TAG + "_" + RadP2PService.class.getSimpleName();
    private static final String DTN_SERVICE_ID
            = DTNConstants.DTN_REGISTRATION_TYPE + BuildConfig.APPLICATION_ID;
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;
    private static final AdvertisingOptions ADVERTISING_OPTIONS
            = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
    private static final DiscoveryOptions DISCOVERY_OPTIONS
            = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();

    private String thisBundleNodezEndpointId;
    private Payload payloadToSend;
    private CLAToRouter router;
    private Set<DTNNode> potentialContacts;
    private ConnectionsClient connectionsClient;
    private int bundleNodeCounter;

    private final PayloadCallback payloadCallback
            = new PayloadCallback() {
        @Override
        public void onPayloadReceived(
                @NonNull String nearbyEndpointID,
                @NonNull Payload payload
        ) {
            DTNBundle bundle = toDTNBundle(payload);
            router.deliverDTNBundle(bundle);

            String senderBundleNodeEID = getBundleNodeEID(nearbyEndpointID);
            Log.i(LOG_TAG, "Message received from " + senderBundleNodeEID);
        }

        @Override
        public void onPayloadTransferUpdate(
                @NonNull String nearbyEndpointID,
                @NonNull PayloadTransferUpdate payloadTransferUpdate
        ) {
//            int status = payloadTransferUpdate.getStatus();
//
//            switch (status) {
//                case PayloadTransferUpdate.Status.SUCCESS:
//                    Log.i(LOG_TAG, "Bundle receiving succeeded");
//                    connectionsClient.disconnectFromEndpoint(nearbyEndpointID);
//                    break;
//                case PayloadTransferUpdate.Status.FAILURE:
//                    Log.e(LOG_TAG, "Bundle receiving failed");
//                    connectionsClient.disconnectFromEndpoint(nearbyEndpointID);
//                    break;
//                default:
//                    break;
//            }
        }
    };

    private final ConnectionLifecycleCallback connectionLifecycleCallback
            = new ConnectionLifecycleCallback() {
        private boolean isIncoming;

        @Override
        public void onConnectionInitiated(
                @NonNull String nearbyEndpointID,
                @NonNull ConnectionInfo connectionInfo
        ) {
            String bundleNodeEid = connectionInfo.getEndpointName();

            DTNNode peerNode = new DTNNode();
            peerNode.eid = bundleNodeEid;
            peerNode.CLAAddress = nearbyEndpointID;
            isIncoming = connectionInfo.isIncomingConnection();

            if (isIncoming) {
                if (isWellKnown(peerNode)) {
                    connectionsClient.acceptConnection(peerNode.CLAAddress, payloadCallback);
                    Log.i(LOG_TAG, "Accepting incoming connection from wellknown node "
                            + peerNode.eid);
                } else {
                    connectionsClient.rejectConnection(peerNode.CLAAddress);
                    Log.i(LOG_TAG, "Rejecting incoming connection from unknown node "
                            + peerNode.eid);
                }
            } else {
                connectionsClient.acceptConnection(nearbyEndpointID, payloadCallback);
                Log.i(LOG_TAG, "Accepting outgoing connection to "
                        + peerNode.eid);
            }
        }

        @Override
        public void onConnectionResult(
                @NonNull String nearbyEndpointID,
                @NonNull ConnectionResolution connectionResolution
        ) {
            int statusCode = connectionResolution.getStatus().getStatusCode();
            String eid = getBundleNodeEID(nearbyEndpointID);

            if (eid != null)
                switch (statusCode) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        // contact established
                        Log.i(LOG_TAG, "Successfully connected to " + eid);

                        if (!isIncoming) // is outgoing connection
                            forwardBundle(nearbyEndpointID, payloadToSend);
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        Log.i(LOG_TAG, "Connection to " + eid + " rejected");
                        break;
                    default:
                        Log.i(LOG_TAG, "Something went seriously wrong! :(");
                        break;
                }
        }

        @Override
        public void onDisconnected(@NonNull String nearbyEndpointID) {
            Log.i(LOG_TAG, "Disconnected from " + getBundleNodeEID(nearbyEndpointID));
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

            DTNNode foundNode = new DTNNode();
            foundNode.eid = bundleNodeEID;
            foundNode.CLAAddress = nearbyEndpointID;

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
    public void setThisBundleNodezEndpointId(String thisBundleNodezEndpointId) {
        if (this.thisBundleNodezEndpointId == null)
            this.thisBundleNodezEndpointId = thisBundleNodezEndpointId;
    }

    @Override
    public void setRouter(CLAToRouter router) {
        this.router = router;
    }

    @Override
    public Set<DTNNode> getPeerList() {
        return potentialContacts;
    }

    @Override
    public void init() {
        if (thisBundleNodezEndpointId != null) {
            Log.i(LOG_TAG, "Starting P2P Service");
            advertise();
            discover();
            bundleNodeCounter = 0;
        }
    }

    @Override
    public void cleanUp() {
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
        potentialContacts.clear(); // forget everyone
        bundleNodeCounter = 0;
        Log.i(LOG_TAG, "Stopped P2P Service");
    }

    private void advertise() {
        Log.i(LOG_TAG, "Requesting to advertise this device as a bundle node");

        connectionsClient.startAdvertising(
                thisBundleNodezEndpointId,
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
    public void transmitBundle(DTNBundle dtnBundleToSend, final DTNNode node) {
        payloadToSend = toPayload(dtnBundleToSend);

        Log.i(LOG_TAG, "Requesting a connection for bundle forwarding");

        // initiating contact
        connectionsClient.requestConnection(
                thisBundleNodezEndpointId,
                node.CLAAddress,
                connectionLifecycleCallback
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // We successfully requested a connection. Now both sides
                // must accept before the connection is established.
                Log.i(LOG_TAG, "Connection request to " + node.eid + " succeeded");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Nearby Connections failed to request the connection.
                Log.e(LOG_TAG, "Connection request to " + node.eid + " failed", e);
            }
        });
    }

    private void forwardBundle(final String CLAAddress, Payload payloadToSend) {
        final String eid = getBundleNodeEID(CLAAddress);

        if (eid != null) {
            connectionsClient.sendPayload(
                    CLAAddress,
                    payloadToSend
            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.i(LOG_TAG, "Bundle successfully sent to " + eid);
                    disconnectAndNotify(CLAAddress);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(LOG_TAG, "Bundle sending to " + eid + " failed", e);
                    disconnectAndNotify(CLAAddress);
                }
            });
        }
    }

    private void disconnectAndNotify(String CLAAddress) {
        connectionsClient.disconnectFromEndpoint(CLAAddress);
        router.notifyBundleForwardingComplete(++bundleNodeCounter);
    }

    private boolean isWellKnown(DTNNode newNode) {

        if (potentialContacts.isEmpty()) {
            return false;
        } else {
            return potentialContacts.contains(newNode);
        }
    }

    private void updateWellKnownDTNNodezCLAAddress(String newCLAAddress, DTNNode staleNode) {

        DTNNode updatedNode = new DTNNode();
        updatedNode.eid = staleNode.eid;
        updatedNode.CLAAddress = newCLAAddress;

        potentialContacts.remove(staleNode);

        potentialContacts.add(updatedNode);

        Log.i(LOG_TAG, "Updated node " + staleNode.eid
                + "\'s CLA address to " + updatedNode.CLAAddress);
    }

    private void makeDTNNodeWellKnown(DTNNode newNode) {

        potentialContacts.add(newNode);
        Log.i(LOG_TAG, "Newly discovered node: {"
                + newNode.CLAAddress + "=" + newNode.eid
                + "}");
    }

    private void forgetDTNNode(String CLAAddress) {
        for (DTNNode node : potentialContacts) {
            if (node.CLAAddress.equals(CLAAddress)) {
                Log.i(LOG_TAG, node.eid + " has gone away");
                potentialContacts.remove(node);
                break;
            }
        }
    }

    private String getBundleNodeEID(String CLAAddress) {
        for (DTNNode node : potentialContacts) {
            if (node.CLAAddress.equals(CLAAddress)) {
                return node.eid;
            }
        }
        return null;
    }

    private boolean isDTNNode(String serviceId) {
        return serviceId.equals(DTN_SERVICE_ID);
    }

    private DTNBundle toDTNBundle(Payload payload) {
        // TODO convert to normal bundle as appropriate
        DTNBundle bundle = new DTNBundle();
        bundle.data = new String(Objects.requireNonNull(payload.asBytes()));
        return bundle;
    }

    private Payload toPayload(DTNBundle dtnDTNBundle) {
        // TODO transform generic bundle to Nearby File or Stream Payload
        byte[] data = dtnDTNBundle.data.getBytes();
        return Payload.fromBytes(data);
    }
}

