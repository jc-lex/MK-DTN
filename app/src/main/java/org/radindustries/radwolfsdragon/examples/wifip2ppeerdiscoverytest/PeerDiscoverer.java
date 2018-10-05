package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import java.util.List;

public interface PeerDiscoverer {
    List<DTNNode> getPeerList(); // TODO find somewhere else to put this thing
    void initWifiP2p();
    void startDTNServiceRegistration();
    void requestDTNServiceDiscovery();
    void discoverDTNServicePeers();
    void connectToPeers();
    void cleanUpWifiP2P();
}
