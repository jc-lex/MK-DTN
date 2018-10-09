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
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.ConvergenceLayerAdapter;
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
    private ConnectionsClient client;

    private final PayloadCallback payloadCallback
            = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String p2pEndpointId, Payload payload) {
            if (payload != null && payload.getType() == Payload.Type.BYTES) { // must be stream
                Log.d(LOG_TAG, "Message from " + discoveredBundleNodes.get(p2pEndpointId));
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
                if (isWellKnownBundleNode(p2pEndPointId, bundleNodeEID)) {
                    client.acceptConnection(p2pEndPointId, payloadCallback);
                    Log.i(LOG_TAG, "Accepting incoming connection to well-known node "
                            + bundleNodeEID);
                } else {
                    client.rejectConnection(p2pEndPointId);
                    Log.i(LOG_TAG, "Rejecting incoming connection to unknown node "
                            + bundleNodeEID);
                }
            } else {
                client.acceptConnection(p2pEndPointId, payloadCallback);
                Log.i(LOG_TAG, "Accepting outgoing connection to " + bundleNodeEID);
            }
        }

        private boolean isWellKnownBundleNode(String p2pEndpointName, String bundleNodeEID) {
            String knownBundleNode = discoveredBundleNodes.get(p2pEndpointName);

            return discoveredBundleNodes.containsKey(p2pEndpointName) &&
                    discoveredBundleNodes.containsValue(bundleNodeEID) &&
                    bundleNodeEID.equals(knownBundleNode);
        }

        @Override
        public void onConnectionResult(String p2pEndpointId,
                                       ConnectionResolution connectionResolution) {
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    // We're connected! Can now start sending and receiving data.
                    Log.i(LOG_TAG, "Connection succeeded. =)");
                    client.sendPayload(p2pEndpointId, payloadToSend); // bundle forwarding
                    Log.d(LOG_TAG, "Message sent to "
                            + discoveredBundleNodes.get(p2pEndpointId));
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    // The connection was rejected by one or both sides.
                    Log.i(LOG_TAG, "Connection rejected. :(");
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    // The connection broke before it was able to be accepted.
                    Log.e(LOG_TAG, "Connection error.");
                    break;
            }

        }

        @Override
        public void onDisconnected(String p2pEndpointId) {
            // We've been disconnected from this endpoint. No more data can be
            // sent or received.
            Log.i(LOG_TAG, "Disconnected from " + discoveredBundleNodes.get(p2pEndpointId));
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

            if (isBundleNode(foundNodesServiceId)) {
                discoveredBundleNodes.put(p2pEndpointId, foundNodesBundleEID);
                Log.d(LOG_TAG, "Currently discovered bundle nodes: "
                        + discoveredBundleNodes.toString());
            }
        }

        private boolean isBundleNode(String foundEndpointsServiceId) {
            return foundEndpointsServiceId.equals(SERVICE_ID);
        }

        @Override
        public void onEndpointLost(String p2pEndpointId) {
            // A previously discovered endpoint has gone away.
            Log.i(LOG_TAG, discoveredBundleNodes.remove(p2pEndpointId) + " has gone away.");
            Log.d(LOG_TAG, "Currently discovered bundle nodes: "
                    + discoveredBundleNodes.toString());
        }
    };

    RadP2PService(Context context) {
        this.discoveredBundleNodes = new HashMap<>();
        this.client = Nearby.getConnectionsClient(context);
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
        Log.i(LOG_TAG, "Starting P2P Service");
        if (thisBundleNodezEndpointId != null) {
            advertise();
            discover();
        }
    }

    @Override
    public void cleanUp() {
        client.stopAdvertising();
        client.stopDiscovery();
        client.stopAllEndpoints();
        Log.i(LOG_TAG, "Stopped P2P Service");
    }

    private void advertise() {
        client.startAdvertising(
                thisBundleNodezEndpointId,
                SERVICE_ID,
                connectionLifecycleCallback,
                new AdvertisingOptions(STRATEGY))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // We're advertising!
                        Log.i(LOG_TAG, "Advertise request succeeded.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // We were unable to start advertising.
                        Log.e(LOG_TAG, "Advertise request failed.", e);
                        client.stopAllEndpoints();
                        advertise();
                    }
                });
    }

    private void discover() {
        client.startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                new DiscoveryOptions(STRATEGY))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unusedResult) {
                        // We're discovering!
                        Log.i(LOG_TAG, "Discovery request succeeded.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // We were unable to start discovering.
                        Log.e(LOG_TAG, "Discovery request failed.", e);
                        client.stopAllEndpoints();
                        discover();
                    }
                });
    }

    @Override
    public void transmitBundle(DTNBundle dtnBundleToSend, String p2pEndpointId) {
        payloadToSend = toPayload(dtnBundleToSend);

        client.requestConnection(
                thisBundleNodezEndpointId,
                p2pEndpointId,
                connectionLifecycleCallback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // We successfully requested a connection. Now both sides
                        // must accept before the connection is established.
                        Log.i(LOG_TAG, "Connection request succeeded.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Nearby Connections failed to request the connection.
                        Log.e(LOG_TAG, "Connection request failed.", e);
                    }
                });
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
}

