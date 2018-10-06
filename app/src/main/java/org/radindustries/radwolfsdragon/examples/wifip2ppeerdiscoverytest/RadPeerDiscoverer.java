package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RadPeerDiscoverer
        implements PeerDiscoverer {

    private static final String LOG_TAG = Constants.MAIN_LOG_TAG + "_"
            + RadPeerDiscoverer.class.getSimpleName();
    private static final String SERVICE_ID
            = Constants.DTN_REGISTRATION_TYPE + BuildConfig.APPLICATION_ID;
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private static final String thisNodezEID = UUID.randomUUID().toString();

    private List<String> peerList;
    private Context context;

    private final PayloadCallback payloadCallback
            = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            if (payload != null && payload.getType() == Payload.Type.BYTES)
                Log.d(LOG_TAG, "Received message from " + endpointId + ": "
                        + new String(payload.asBytes()));
        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {
            // no UI updates
        }
    };

    private final ConnectionLifecycleCallback connectionLifecycleCallback
            = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endPointId, ConnectionInfo connectionInfo) {
            // Automatically accept the connection on both sides.
            Nearby.getConnectionsClient(context).acceptConnection(endPointId, payloadCallback);
        }

        @Override
        public void onConnectionResult(String endpointId,
                                       ConnectionResolution connectionResolution) {
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    // We're connected! Can now start sending and receiving data.
                    peerList.add(endpointId);
                    Log.i(LOG_TAG, "Connection succeeded. Endpoint: \""
                            + endpointId + "\" added.");
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    // The connection was rejected by one or both sides.
                    Log.i(LOG_TAG, "Connection rejected.");
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    // The connection broke before it was able to be accepted.
                    Log.i(LOG_TAG, "Connection error.");
                    break;
            }

        }

        @Override
        public void onDisconnected(String endpointId) {
            // We've been disconnected from this endpoint. No more data can be
            // sent or received.
            peerList.remove(endpointId);
            Log.i(LOG_TAG, "Disconnected from Endpoint: \""
                    + endpointId + "\".");
        }
    };

    private final EndpointDiscoveryCallback endpointDiscoveryCallback
            = new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(
                        String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    // An endpoint was found!
                    Nearby.getConnectionsClient(context).requestConnection(
                            thisNodezEID,
                            endpointId,
                            connectionLifecycleCallback
                    ).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // We successfully requested a connection. Now both sides
                            // must accept before the connection is established.
                            Log.i(LOG_TAG, "Connection request succeeded.");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Nearby Connections failed to request the connection.
                            Log.i(LOG_TAG, "Connection request failed.");
                        }
                    });
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                    peerList.remove(endpointId);
                    Log.i(LOG_TAG, "Endpoint: \""
                            + endpointId + "\" has gone away.");
                }
            };


    public RadPeerDiscoverer(Context context) {
        this.context = context;
    }

    @Override
    public List<String> getPeerList() {
        return this.peerList;
    }

    @Override
    public void discover() {
        this.peerList = new ArrayList<>();
        Log.d(LOG_TAG, "DTN node EID: " + thisNodezEID);
        Log.d(LOG_TAG, "DTN node service ID: " + SERVICE_ID);

        Nearby.getConnectionsClient(this.context)
                .startAdvertising(
                        thisNodezEID,
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
                        Log.i(LOG_TAG, "Advertise request failed.");
                    }
                });

        Nearby.getConnectionsClient(context).startDiscovery(
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
                        Log.i(LOG_TAG, "Discovery request succeeded.");
                    }
                });

    }

    @Override
    public void cleanUp() {
        Nearby.getConnectionsClient(context).stopAdvertising();
        Nearby.getConnectionsClient(context).stopDiscovery();
        peerList = null;
    }
}
