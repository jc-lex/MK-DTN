package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class MyPeerDiscoveryBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = Constants.MAIN_LOG_TAG + "_"
            + MyPeerDiscoveryBroadcastReceiver.class.getSimpleName();

    public MyPeerDiscoveryBroadcastReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        RadPeerDiscoverer discoverer = (RadPeerDiscoverer)
                DependencyInjection.getPeerDiscoverer(context, context.getMainLooper());
        String action = intent.getAction();
        if (action != null)
            switch (action) {
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    handleWifiStateChange(discoverer, intent);
                    break;
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    handlePeersChange(discoverer);
                    break;
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    handleConnectionChange(discoverer, intent);
                    break;
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    handleDeviceChange(intent);
                    break;
                default:
                    break;
            }
    }

    private void handleWifiStateChange(RadPeerDiscoverer discoverer, Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            Log.i(LOG_TAG, "Wifi / WifiDirect ON (enabled)");
            if (discoverer.getManager() == null) {
                discoverer.initWifiP2p();
                if (discoverer.getManager() == null) return;
                discoverer.startDTNServiceRegistration();
                discoverer.requestDTNServiceDiscovery();
            }

            discoverer.discoverDTNServicePeers();
            discoverer.connectToPeers();
        }
        else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
            Log.i(LOG_TAG, "Wifi / WifiDirect OFF (disabled)");
            if (discoverer.getManager() != null) discoverer.cleanUpWifiP2P();
        }
    }

    private void handlePeersChange(RadPeerDiscoverer discoverer) {
        if (discoverer.getManager() != null) {
            Log.i(LOG_TAG, "P2P peers changed");
            discoverer.getManager().requestPeers(discoverer.getChannel(),
                    discoverer.getPeerListListener());
//            discoverer.requestDTNServiceDiscovery();
//            discoverer.discoverDTNServicePeers();
            discoverer.connectToPeers();
        }
    }

    private void handleConnectionChange(RadPeerDiscoverer discoverer, Intent intent) {
        if (discoverer.getManager() != null) {
            NetworkInfo networkInfo
                    = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            Log.i(LOG_TAG, "Network info for connection change: "
                    + networkInfo.toString());

            if (networkInfo.isConnected()) {
                discoverer.getManager().requestConnectionInfo(discoverer.getChannel(),
                        discoverer.getConnectionInfoListener());
                discoverer.getManager().requestGroupInfo(discoverer.getChannel(),
                        discoverer.getGroupInfoListener());
            }
        }
    }

    private void handleDeviceChange(Intent intent) {
        WifiP2pDevice thisDevice
                = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        Log.i(LOG_TAG, "This device has changed somehow o_O: "
                + thisDevice.toString());
    }
}
