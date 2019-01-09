package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.tasks.OnSuccessListener;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.Daemon2CLA;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.CLA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.PRoPHETCLA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.Daemon2Managable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

final class RadCLA implements Daemon2CLA, RadDiscoverer.CLCProvider, Daemon2Managable {
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + RadCLA.class.getSimpleName();
    
    private final PayloadCallback payloadCallback
        = new PayloadCallback() {
        private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();
        
        @Override
        public void onPayloadReceived(
            @NonNull String nearbyEndpointID,
            @NonNull Payload payload
        ) {
            if (payload.getType() == Payload.Type.STREAM) {
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
                    if (isIncoming) {
                        Payload payload = incomingPayloads.remove(payloadId);
                        assert payload != null;
                        try (
                            ObjectInputStream in = new ObjectInputStream(
                                Objects.requireNonNull(payload.asStream()).asInputStream()
                            )
                        ) {
                            DTNBundle receivedBundle = (DTNBundle) in.readObject();
                            prophetDaemon.calculateDPTransitivity(receivedBundle);
                            daemon.onBundleReceived(receivedBundle);
                        } catch (Exception e) {
                            break;
                        }
                        isIncoming = false;
                        connectionsClient.disconnectFromEndpoint(nearbyEndpointID);
                    }
                    break;
                case PayloadTransferUpdate.Status.FAILURE:
                    if (isIncoming) {
                        isIncoming = false;
                        connectionsClient.disconnectFromEndpoint(nearbyEndpointID);
                    }
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
            isIncoming = connectionInfo.isIncomingConnection();
            connectionsClient.acceptConnection(nearbyEndpointID, payloadCallback);
        }
        
        @Override
        public void onConnectionResult(
            @NonNull String nearbyEndpointID,
            @NonNull ConnectionResolution connectionResolution
        ) {
            int statusCode = connectionResolution.getStatus().getStatusCode();
    
            switch (statusCode) {
                case ConnectionsStatusCodes.STATUS_OK:
                    if (!isIncoming) // is outgoing connection
                        forward(bundleToSend, nearbyEndpointID);
                    break;
                default:
                    break;
            }
        }
        
        @Override
        public void onDisconnected(@NonNull String nearbyEndpointID) {
            if (sent) {
                daemon.onTransmissionComplete(++bundleNodeCounter);
                sent = false;
            }
        }
    };
    
    private DTNBundle bundleToSend;
    private boolean sent;
    private boolean isIncoming;
    private int bundleNodeCounter;
    
    private CLA2Daemon daemon;
    private PRoPHETCLA2Daemon prophetDaemon;
    private ConnectionsClient connectionsClient;
    
    private RadCLA() {}
    
    RadCLA(@NonNull CLA2Daemon daemon, @NonNull PRoPHETCLA2Daemon prophetDaemon,
           @NonNull Context context) {
        this.daemon = daemon;
        this.prophetDaemon = prophetDaemon;
        connectionsClient = Nearby.getConnectionsClient(context);
        reset();
    }
    
    @Override
    public ConnectionLifecycleCallback getCLC() {
        return connectionLifecycleCallback;
    }
    
    @Override
    public void transmit(DTNBundle bundle, DTNBundleNode destination) {
        Log.i(LOG_TAG, "Sending bundle:\n" + bundle);
        bundleToSend = bundle;
        sent = false;
        String claAddress = destination.CLAAddresses.get(DTNBundleNode.CLAKey.NEARBY);
    
        assert claAddress != null;
        connectionsClient.requestConnection(
            daemon.getThisNodezEID().toString(),
            claAddress,
            connectionLifecycleCallback
        );
    }
    
    @Override
    public void reset() {
        bundleNodeCounter = 0;
    }
    
    private void forward(final DTNBundle bundleToSend, final String CLAAddress) {
        try {
            final ParcelFileDescriptor[] payloadPipe = ParcelFileDescriptor.createPipe();
        
            connectionsClient.sendPayload(
                CLAAddress,
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
                        out.writeObject(bundleToSend);
                        sent = true;
                    } catch (IOException e) {
                        disconnectAndNotify(CLAAddress);
                    }
                    Log.i(LOG_TAG, "Bundle sent = " + sent);
                }
            });
        } catch (IOException e) {
            disconnectAndNotify(CLAAddress);
        }
    }
    
    private void disconnectAndNotify(String CLAAddress) {
        connectionsClient.disconnectFromEndpoint(CLAAddress);
        daemon.onTransmissionComplete(++bundleNodeCounter);
    }
    
    @Override
    public boolean start() {
        return true; //RadDiscoverer is doing this for us
    }
    
    @Override
    public boolean stop() {
        return true; //RadDiscoverer is doing this for us
    }
}
