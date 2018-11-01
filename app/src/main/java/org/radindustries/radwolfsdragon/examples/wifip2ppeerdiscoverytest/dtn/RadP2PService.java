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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.BuildConfig;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.ConvergenceLayerAdapter;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery.PeerDiscovery;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;

import java.util.HashSet;
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
//    private Payload payloadToSend;
//    private CLAToRouter router;
//    private HashMap<String, DTNNode> discoveredDTNNodes;
//    private HashMap<String, DTNNode> connectedDTNNodes;
    private Set<DTNNode> potentialContacts;
//    private Set<DTNNode> upContacts;
    private ConnectionsClient connectionsClient;

    private final PayloadCallback payloadCallback
            = new PayloadCallback() {
        @Override
        public void onPayloadReceived(
                @NonNull String nearbyEndpointID,
                @NonNull Payload payload
        ) {
//            if (payload.getType() == Payload.Type.BYTES) {
//                Log.i(LOG_TAG, "Bundle received");
//                DTNBundle receivedBundle = toDTNBundle(payload);
//                router.deliverDTNBundle(receivedBundle);
//            }
        }

        @Override
        public void onPayloadTransferUpdate(
                @NonNull String nearbyEndpointID,
                @NonNull PayloadTransferUpdate payloadTransferUpdate
        ) {

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

            DTNNode peerNode = new DTNNode();
            peerNode.eid = bundleNodeEid;
            peerNode.CLAAddress = nearbyEndpointID;

            if (connectionInfo.isIncomingConnection()) {
                if (isWellKnown(peerNode)) {
                    connectionsClient.acceptConnection(peerNode.CLAAddress, payloadCallback);
                    Log.i(LOG_TAG, "Accepting incoming connection from wellknown node "
                            + peerNode.eid);
                } else {
                    connectionsClient.rejectConnection(peerNode.CLAAddress);
                    Log.i(LOG_TAG, "Rejecting incoming connection to unknown node "
                            + peerNode.eid);
                }
            } else {
                    connectionsClient.acceptConnection(nearbyEndpointID, payloadCallback);
                    Log.i(LOG_TAG, "Accepting outgoing connection to "
                            + peerNode.eid);
            }
//
//            if (isWellKnown(eid)) {
//                if (connectionInfo.isIncomingConnection()) {

//                } else {

//                }
//            } else {
//                if (connectionInfo.isIncomingConnection()) {

//                }
//            }
        }

        @Override
        public void onConnectionResult(
                @NonNull String nearbyEndpointID,
                @NonNull ConnectionResolution connectionResolution
        ) {
            int statusCode = connectionResolution.getStatus().getStatusCode();

            switch (statusCode) {
                case ConnectionsStatusCodes.STATUS_OK:
//                    markDTNNodeAsConnected(nearbyEndpointID);
//                    forwardBundle(nearbyEndpointID, payloadToSend);
                    Log.i(LOG_TAG, "Successfully connected to "
                            + getBundleNodeEID(nearbyEndpointID));
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
//                    handleRejection(nearbyEndpointID);
                    Log.i(LOG_TAG, "Connection to " + getBundleNodeEID(nearbyEndpointID)
                            + " rejected");
                    break;
                    default:
                        Log.i(LOG_TAG, "Something went seriously wrong! :(");
            }
        }

        @Override
        public void onDisconnected(@NonNull String nearbyEndpointID) {
//            DTNNode disconnectedNode = connectedDTNNodes.remove(nearbyEndpointID);
//
//            if (disconnectedNode != null) {
//                discoveredDTNNodes.put(nearbyEndpointID, disconnectedNode);
//                Log.i(LOG_TAG, "Disconnected from " + disconnectedNode.eid);
//                Log.i(LOG_TAG, "Currently connected nodes: " + connectedDTNNodes);
//            }
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
//        this.discoveredDTNNodes = new HashMap<>();
//        this.connectedDTNNodes = new HashMap<>();
        this.potentialContacts = new HashSet<>();
//        this.upContacts = new HashSet<>();
        this.connectionsClient = Nearby.getConnectionsClient(context);
    }

    @Override
    public void setThisBundleNodezEndpointId(String thisBundleNodezEndpointId) {
        if (this.thisBundleNodezEndpointId == null)
            this.thisBundleNodezEndpointId = thisBundleNodezEndpointId;
    }

    @Override
    public void setRouter(CLAToRouter router) {
//        this.router = router;
    }

    @Override
    public Set<DTNNode> getPeerList() {
//        return upContacts;
        return potentialContacts;
    }

    @Override
    public void init() {
        if (thisBundleNodezEndpointId != null) {
            Log.i(LOG_TAG, "Starting P2P Service");
            advertise();
            discover();
        }
    }

    @Override
    public void cleanUp() {
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
        potentialContacts.clear(); // forget everyone
//        connectedDTNNodes.clear();
//        discoveredDTNNodes.clear();
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
    public void transmitBundle(DTNBundle dtnBundleToSend, Set<DTNNode> nodes) {
//        payloadToSend = toPayload(dtnBundleToSend);
//
//        Log.i(LOG_TAG, "Transmitting bundle to connected bundle nodes");
//        for (final DTNNode node : nodes) {
//            if (isConnected(node)) {
//                forwardBundle(node.CLAAddress, payloadToSend);
//            } else {
//                Log.i(LOG_TAG, "Requesting connection to " + node.eid);
//
//                connectionsClient.requestConnection(
//                        thisBundleNodezEndpointId,
//                        node.CLAAddress,
//                        connectionLifecycleCallback
//                ).addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {

//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {

//                    }
//                });
//            }
//        }
        final DTNNode node = chooseDTNNode(nodes);

        Log.i(LOG_TAG, "Requesting a connection for bundle forwarding");

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

    private DTNNode chooseDTNNode(Set<DTNNode> nodes) {
        // random selection, for now
        DTNNode[] nodesArray = nodes.toArray(new DTNNode[]{});
        int randomNumber = (int) (Math.random() * nodesArray.length); // Z : [0, len)
        return nodesArray[randomNumber];
    }

//    private boolean isConnected(DTNNode node) {
//        return connectedDTNNodes.containsValue(node);
//    }
//
//    private DTNBundle toDTNBundle(Payload payload) {
//        // convert to normal bundle as appropriate
//        DTNBundle bundle = new DTNBundle();
//        bundle.data = new String(Objects.requireNonNull(payload.asBytes()));
//        return bundle;
//    }
//
//    private Payload toPayload(DTNBundle dtnDTNBundle) {
//        byte[] data = dtnDTNBundle.data.getBytes();
//        return Payload.fromBytes(data);
//    }
//
//    private void forwardBundle(String nearbyCLAAddress, Payload payloadToSend) {
//        DTNNode node = connectedDTNNodes.get(nearbyCLAAddress);
//
//        if (node != null) {
//            Log.i(LOG_TAG, "Sending bundle to " + node.eid);
//            connectionsClient.sendPayload(node.CLAAddress, payloadToSend);
//            Log.i(LOG_TAG, "Bundle sent to " + node.eid);
//        }
//    }
//
//    private void markDTNNodeAsConnected(String CLAAddress) {
//        DTNNode newlyConnectedNode = discoveredDTNNodes.remove(CLAAddress);
//
//        if (newlyConnectedNode != null) {
//            connectedDTNNodes.put(CLAAddress, newlyConnectedNode);
//            Log.i(LOG_TAG, "Connected to {"
//                    + newlyConnectedNode.CLAAddress + "=" + newlyConnectedNode.eid
//                    + "}");
//            Log.i(LOG_TAG, "Currently connected nodes: " + connectedDTNNodes);
//        }
//    }
//
//    private void handleRejection(String CLAAddress) {
//        DTNNode node = discoveredDTNNodes.get(CLAAddress);
//
//        if (node != null)
//            Log.i(LOG_TAG, "This node was rejected by " + node.eid);
//    }

    private boolean isWellKnown(DTNNode newNode) {
//        Set<Map.Entry<String, DTNNode>> nodes = discoveredDTNNodes.entrySet();
//        for (Map.Entry node : nodes) {
//            DTNNode dtnNode = (DTNNode) node.getValue();
//            if (dtnNode.eid.equals(eid)) {
//                return true;
//            }
//        }
//        return false;
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
}

