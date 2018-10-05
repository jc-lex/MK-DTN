package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DoPeerDiscoveryService extends Service {
//    private PeerDiscoverer discoverer;

    public DoPeerDiscoveryService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        discoverer = new RadPeerDiscoverer(this, getMainLooper());
//        discoverer.initWifiP2p();
//        discoverer.startDTNServiceRegistration();
//        discoverer.requestDTNServiceDiscovery();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // TODO find out how to make discovery and connection continuous, if even needed
////                discoverer.requestDTNServiceDiscovery();
//                discoverer.discoverDTNServicePeers();
//                discoverer.connectToPeers();
//            }
//        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        discoverer.cleanUpWifiP2P();
    }
}
