package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app;

public interface DTNUI {
    void onReceiveDTNMessage(byte[] message, String sender);
    void onBundleStatusReceived(String recipient, String msg);
    void onPeerListChanged(String[] peerList);
}
