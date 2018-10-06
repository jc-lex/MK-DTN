package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class MyPeerDiscoveryBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = Constants.MAIN_LOG_TAG + "_"
            + MyPeerDiscoveryBroadcastReceiver.class.getSimpleName();

    public MyPeerDiscoveryBroadcastReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        RadPeerDiscoverer discoverer = (RadPeerDiscoverer)
                DependencyInjection.getPeerDiscoverer(context);
        String action = intent.getAction();
        if (action != null && action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION))
            handleWifiStateChange(discoverer, intent);
    }

    private void handleWifiStateChange(RadPeerDiscoverer discoverer, Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            Log.i(LOG_TAG, "Wifi / WifiDirect ON (enabled)");
            discoverer.discover();
        }
        else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
            Log.i(LOG_TAG, "Wifi / WifiDirect OFF (disabled)");
            discoverer.cleanUp();
        }
    }
}
