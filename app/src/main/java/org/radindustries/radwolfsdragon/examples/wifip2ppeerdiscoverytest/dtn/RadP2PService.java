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
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery.PeerDiscovery;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;

import java.util.HashMap;
import java.util.Objects;

final class RadP2PService implements PeerDiscovery, ConvergenceLayerAdapter {

    private static final String LOG_TAG
            = DTNConstants.MAIN_LOG_TAG + "_" + RadP2PService.class.getSimpleName();
    private static final String SERVICE_ID
            = DTNConstants.DTN_REGISTRATION_TYPE + BuildConfig.APPLICATION_ID;
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;

    private String thisBundleNodezEndpointId;
    private Payload payloadToSend;
    private CLAToRouter router;
    private HashMap<String, String> discoveredBundleNodes;
    private HashMap<String, String> connectedBundleNodes;
    private ConnectionsClient connectionsClient;

    private final PayloadCallback payloadCallback
            = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String p2pEndpointId, Payload payload) {
            if (payload != null && payload.getType() == Payload.Type.BYTES) {
                Log.i(LOG_TAG, "Bundle received from "
                        + connectedBundleNodes.get(p2pEndpointId));
                DTNBundle receivedBundle = toDTNBundle(payload);
                if (router != null) router.deliverDTNBundle(receivedBundle);
            }
        }

        @Override
        public void onPayloadTransferUpdate(String p2pEndpointId,
                                            PayloadTransferUpdate payloadTransferUpdate) {
            // no UI updates
        }
    };

    private final ConnectionLifecycleCallback connectionLifecycleCallback
            = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String p2pEndPointId, ConnectionInfo connectionInfo) {
            String bundleNodeEID = connectionInfo.getEndpointName();
            if (connectionInfo.isIncomingConnection()) {
                if (discoveredBundleNodes.containsValue(bundleNodeEID)) { // is well-known
                    connectionsClient.acceptConnection(p2pEndPointId, payloadCallback);
                    Log.i(LOG_TAG, "Accepting incoming connection to well-known node "
                            + bundleNodeEID);
                } else {
                    connectionsClient.rejectConnection(p2pEndPointId);
                    Log.i(LOG_TAG, "Rejecting incoming connection to unknown node "
                            + bundleNodeEID);
                }
            } else {
                connectionsClient.acceptConnection(p2pEndPointId, payloadCallback);
                Log.i(LOG_TAG, "Accepting outgoing connection to " + bundleNodeEID);
            }
        }

        @Override
        public void onConnectionResult(String p2pEndpointId,
                                       ConnectionResolution connectionResolution) {
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    // We're connected! Can now start sending and receiving data.
                    String newlyConnectedNode = discoveredBundleNodes.get(p2pEndpointId);
                    Log.i(LOG_TAG, "Connection to " + newlyConnectedNode + " succeeded. =)");
                    connectedBundleNodes.put(p2pEndpointId, newlyConnectedNode);
                    Log.d(LOG_TAG, "Currently connected bundle nodes: "
                            + connectedBundleNodes.toString());
                    forwardBundle(p2pEndpointId, payloadToSend);
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    // The connection was rejected by one side
                    Log.e(LOG_TAG, "Connection rejected :(");
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    // The connection broke before it was able to be accepted.
                    Log.e(LOG_TAG, "Connection error");
                    break;
                case ConnectionsStatusCodes.STATUS_BLUETOOTH_ERROR:
                    Log.e(LOG_TAG, "Bluetooth is not working well");
                    break;
                case ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT:
                    // We're already connected! Just send.
                    Log.i(LOG_TAG, "Already connected to "
                            + connectedBundleNodes.get(p2pEndpointId));
                    forwardBundle(p2pEndpointId, payloadToSend);
                    break;
            }

        }

        @Override
        public void onDisconnected(String p2pEndpointId) {
            // We've been disconnected from this endpoint. No more data can be
            // sent or received.
            Log.i(LOG_TAG, "Disconnected from " + connectedBundleNodes.remove(p2pEndpointId));
            Log.d(LOG_TAG, "Currently connected bundle nodes: "
                    + connectedBundleNodes.toString());
        }
    };

    private final EndpointDiscoveryCallback endpointDiscoveryCallback
            = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(
                final String p2pEndpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
            // An endpoint was found!
            String foundNodesServiceId = discoveredEndpointInfo.getServiceId();
            String foundNodesBundleEID = discoveredEndpointInfo.getEndpointName();

            if (foundNodesServiceId.equals(SERVICE_ID) && // supports DTN
                    !discoveredBundleNodes.containsValue(foundNodesBundleEID)) { // not well-known
                discoveredBundleNodes.put(p2pEndpointId, foundNodesBundleEID);
                Log.d(LOG_TAG, "Currently discovered bundle nodes: "
                        + discoveredBundleNodes.toString());
            }
        }

        @Override
        public void onEndpointLost(String p2pEndpointId) {
            // A previously discovered endpoint has gone away.
            Log.i(LOG_TAG, discoveredBundleNodes.remove(p2pEndpointId) + " has gone away");
            Log.d(LOG_TAG, "Currently discovered bundle nodes: "
                    + discoveredBundleNodes.toString());
        }
    };

    RadP2PService(Context context) {
        this.discoveredBundleNodes = new HashMap<>();
        this.connectedBundleNodes = new HashMap<>();
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
    public HashMap<String, String> getDiscoveredBundleNodes() {
        return this.discoveredBundleNodes;
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
        connectedBundleNodes.clear();
        discoveredBundleNodes.clear();
        Log.i(LOG_TAG, "Stopped P2P Service");
    }

    private void advertise() {
        Log.i(LOG_TAG, "Requesting to advertise this device as a bundle node");
        connectionsClient.startAdvertising(
                thisBundleNodezEndpointId,
                SERVICE_ID,
                connectionLifecycleCallback,
                new AdvertisingOptions(STRATEGY))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // We're advertising!
                        Log.i(LOG_TAG, "Advertise request succeeded. Device Bundle EID: "
                                + thisBundleNodezEndpointId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
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
                SERVICE_ID,
                endpointDiscoveryCallback,
                new DiscoveryOptions(STRATEGY))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unusedResult) {
                        // We're discovering!
                        Log.i(LOG_TAG, "Discovery request succeeded");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
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
    public void transmitBundle(DTNBundle dtnBundleToSend, String... p2pEndpointIds) {
        payloadToSend = toPayload(dtnBundleToSend);

        Log.i(LOG_TAG, "Transmitting bundle to connected bundle nodes");
        for (final String p2pEID : p2pEndpointIds) {
            if (connectedBundleNodes.containsKey(p2pEID)) { // is connected
                forwardBundle(p2pEID, payloadToSend);
            } else {
                Log.i(LOG_TAG, "Requesting connection to "
                        + discoveredBundleNodes.get(p2pEID));
                connectionsClient.requestConnection(
                        thisBundleNodezEndpointId,
                        p2pEID,
                        connectionLifecycleCallback)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // We successfully requested a connection. Now both sides
                                // must accept before the connection is established.
                                Log.i(LOG_TAG, "Connection request to "
                                        + discoveredBundleNodes.get(p2pEID)
                                        + " succeeded");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Nearby Connections failed to request the connection.
                                Log.e(LOG_TAG, "Connection request to "
                                        + discoveredBundleNodes.get(p2pEID)
                                        + " failed", e);
                            }
                        });
            }
        }
    }

    private DTNBundle toDTNBundle(Payload payload) {
        // convert to normal bundle as appropriate
        DTNBundle bundle = new DTNBundle();
        bundle.data = new String(Objects.requireNonNull(payload.asBytes()));
        return bundle;
    }

    private Payload toPayload(DTNBundle dtnDTNBundle) {
        // payload should be a stream
        byte[] data = dtnDTNBundle.data.getBytes();
        return Payload.fromBytes(data);
    }

    private void forwardBundle(String p2pEndpointId, Payload payloadToSend) {
        Log.i(LOG_TAG, "Sending bundle to " + connectedBundleNodes.get(p2pEndpointId));
        connectionsClient.sendPayload(p2pEndpointId, payloadToSend);
        Log.i(LOG_TAG, "Bundle sent to " + connectedBundleNodes.get(p2pEndpointId));
    }
}

