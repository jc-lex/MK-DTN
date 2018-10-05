package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest;

import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RadPeerDiscoverer
        implements PeerDiscoverer {

    private static final String LOG_TAG = Constants.MAIN_LOG_TAG + "_"
            + RadPeerDiscoverer.class.getSimpleName();

    private Context context;
    private Looper looper;

    private List<DTNNode> peerList;
    private List<WifiP2pDevice> availablePeerDevices;
    private List<DTNNode> discoveredDTNNodes;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    private WifiP2pManager.PeerListListener peerListListener
            = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            if (peers != null && availablePeerDevices != null) {
                availablePeerDevices.clear();
                availablePeerDevices.addAll(peers.getDeviceList());
                if (availablePeerDevices.size() == 0)
                    Log.i(LOG_TAG, "No devices found");
            }
        }
    };

    private WifiP2pManager.ConnectionInfoListener connectionInfoListener
            = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            Log.d(LOG_TAG, "Successfully connected to device(s): " + info.toString());
        }
    };

    private WifiP2pManager.GroupInfoListener groupInfoListener
            = new WifiP2pManager.GroupInfoListener() {
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            // VERY useful information, make good use of it
            Log.d(LOG_TAG, "Successfully connected to group: " + group.toString());
        }
    };

    public RadPeerDiscoverer(Context context, Looper looper) {
        this.context = context;
        this.looper = looper;
    }

    public WifiP2pManager.GroupInfoListener getGroupInfoListener() {
        return groupInfoListener;
    }

    public WifiP2pManager.PeerListListener getPeerListListener() {
        return this.peerListListener;
    }

    public WifiP2pManager.ConnectionInfoListener getConnectionInfoListener() {
        return this.connectionInfoListener;
    }

    public WifiP2pManager getManager() {
        return this.manager;
    }

    public WifiP2pManager.Channel getChannel() {
        return this.channel;
    }

    @Override
    public List<DTNNode> getPeerList() {
        return this.peerList;
    }

    @Override
    public void initWifiP2p() {
        // initialise Wifi P2P
        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        if (this.manager != null) {
            this.channel = this.manager.initialize(context, looper, null);
            if (this.channel != null)
                Log.i(LOG_TAG, "WIFI P2P manager and channel are okay :)");
            else
                Log.e(LOG_TAG, "WIFI P2P channel is null :(");
        }
        else {
            Log.e(LOG_TAG, "WIFI P2P manager is null :(");
            return;
        }

        this.peerList = new ArrayList<>();
        this.availablePeerDevices = new ArrayList<>();
        this.discoveredDTNNodes = new ArrayList<>();
    }

    @Override
    public void startDTNServiceRegistration() {
        HashMap<String, String> record = new HashMap<>();
        record.put(Constants.SERVER_PORT_KEY, String.valueOf(Constants.SERVER_PORT));
        record.put(Constants.DTN_ENABLED_KEY, "true");

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance(Constants.DTN_INSTANCE_NAME,
                        Constants.DTN_REGISTRATION_TYPE, record);

        this.manager.addLocalService(this.channel, serviceInfo,
                new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(LOG_TAG, "Local DTN service added successfully");
            }

            @Override
            public void onFailure(int arg0) {
                printErrorLogs("Service add failed", arg0);
            }
        });
    }

    @Override
    public void requestDTNServiceDiscovery() {
        WifiP2pManager.DnsSdTxtRecordListener txtRecordListener
                = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName,
                                                  Map<String, String> txtRecordMap,
                                                  WifiP2pDevice srcDevice) {
                DTNNode newlyFoundNode = new DTNNode();
                boolean dtnEnabled =
                        Boolean.parseBoolean(txtRecordMap.get(Constants.DTN_ENABLED_KEY));
                if (dtnEnabled) {
                    newlyFoundNode.serverPort =
                            Integer.parseInt(
                                    Objects.requireNonNull(
                                            txtRecordMap.get(Constants.SERVER_PORT_KEY)
                                    )
                            );
                    newlyFoundNode.device = srcDevice;
                    if (discoveredDTNNodes != null)
                        discoveredDTNNodes.add(newlyFoundNode); // this list is only growing
                }
                Log.i(LOG_TAG, "DNS Record available: "
                        + fullDomainName + ", "
                        + txtRecordMap.toString() + ", "
                        + srcDevice.deviceAddress
                );
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener
                = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName,
                                                String registrationType,
                                                WifiP2pDevice srcDevice) {
                // I don't know what to do here sincerely
                Log.i(LOG_TAG, "DNS Service available: "
                        + instanceName + ", "
                        + registrationType + ", "
                        + srcDevice.deviceAddress
                );
            }
        };

        this.manager.setDnsSdResponseListeners(this.channel,
                serviceResponseListener, txtRecordListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        this.manager.addServiceRequest(this.channel, serviceRequest,
                new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(LOG_TAG, "Service request added successfully");
            }

            @Override
            public void onFailure(int reason) {
                printErrorLogs("Service request add failed", reason);
            }
        });


    }

    @Override
    public void discoverDTNServicePeers() {
        this.manager.discoverServices(this.channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(LOG_TAG, "DTN service discovery initiated successfully");
            }

            @Override
            public void onFailure(int reason) {
                printErrorLogs("DTN service discovery failed", reason);
            }
        });
        this.manager.discoverPeers(this.channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(LOG_TAG, "Peer discovery initiated successfully");
            }

            @Override
            public void onFailure(int reason) {
                printErrorLogs("Peer discovery initiation failed", reason);
            }
        });
    }

    @Override
    public void connectToPeers() {
        createPeerList();
        if (peerList != null && !peerList.isEmpty())
            for (final DTNNode node : peerList) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = node.device.deviceAddress;
                config.wps.setup = WpsInfo.PBC; // is already set in the WifiP2pConfig() constructor
                Log.d(LOG_TAG, "Config info for connecting to "
                        + node.device.deviceAddress + " : " + config.toString());

                // connect to only Wifi Direct enabled devices... for now
                this.manager.connect(this.channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.i(LOG_TAG, "Successfully initiated connection with "
                                + node.device.deviceAddress);
                    }

                    @Override
                    public void onFailure(int reason) {
                        printErrorLogs("Connection request to "
                                + node.device.deviceAddress + " failed", reason);
                    }
                });
            }
    }

    @Override
    public void cleanUpWifiP2P() {
        this.manager.cancelConnect(this.channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(LOG_TAG, "connections closed successfully");
            }

            @Override
            public void onFailure(int reason) {
                printErrorLogs("Connections refused to cancel", reason);
            }
        });
        this.manager.clearServiceRequests(this.channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(LOG_TAG, "Service requests cleared successfully");
            }

            @Override
            public void onFailure(int reason) {
                printErrorLogs("Service requests failed to clear", reason);
            }
        });
        this.manager.clearLocalServices(this.channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(LOG_TAG, "Cleared local service");
            }

            @Override
            public void onFailure(int reason) {
                printErrorLogs("This device's service failed to clear", reason);
            }
        });
        this.manager.stopPeerDiscovery(this.channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(LOG_TAG, "Peer discovery stopped request successful");
            }

            @Override
            public void onFailure(int reason) {
                printErrorLogs("request to stop peer discovery failed", reason);
            }
        });
    }

    private void createPeerList() {
        if (discoveredDTNNodes != null && !discoveredDTNNodes.isEmpty())
            for (DTNNode discoveredDevice : discoveredDTNNodes) {
                if (peerList != null)
                    if(isAvailable(discoveredDevice.device))
                        peerList.add(discoveredDevice);
                    else
                        discoveredDTNNodes.remove(discoveredDevice); // to shrink this long list
            }
        if (peerList != null && !peerList.isEmpty())
            Log.d(LOG_TAG, "The peer list is: " + peerList.toString());
    }

    private boolean isAvailable(WifiP2pDevice discoveredDevice) {
        boolean available = false;
        if (availablePeerDevices != null && !availablePeerDevices.isEmpty()
                && discoveredDevice != null)
            for (WifiP2pDevice device : availablePeerDevices)
                if (discoveredDevice.deviceAddress.equals(device.deviceAddress)) {
                    available = true;
                    break;
                }
        return available;
    }

    private void printErrorLogs(String subject, int arg0) {
        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
        switch (arg0) {
            case WifiP2pManager.BUSY:
                Log.e(LOG_TAG, subject + ": device busy");
                break;
            case WifiP2pManager.ERROR:
                Log.e(LOG_TAG, subject + ": internal error");
                break;
            case WifiP2pManager.P2P_UNSUPPORTED:
                Log.e(LOG_TAG, subject + ": device does not support Wifi Direct");
                break;
            default:
                Log.e(LOG_TAG, subject + ": Error code: " + Integer.toString(arg0));
                break;
        }
    }
}
