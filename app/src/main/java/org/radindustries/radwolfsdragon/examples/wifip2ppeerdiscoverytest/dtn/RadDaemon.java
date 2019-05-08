package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.admin.Daemon2AdminAA;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.Daemon2AppAA;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.Daemon2CLA;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.AdminAA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.AppAA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.CLA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.DTNManager2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.NECTARPeerDiscoverer2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.NECTARRouter2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.PRoPHETCLA2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.PRoPHETPeerDiscoverer2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.PRoPHETRouter2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.PeerDiscoverer2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.daemon.Router2Daemon;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CustodySignal;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.NECTARRoutingInfo;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PRoPHETRoutingInfo;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.StatusReport;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.fragmentmanager.Daemon2FragmentManager;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.Daemon2Managable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscoverer.Daemon2PeerDiscoverer;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2NECTARRoutingTable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2PRoPHETRoutingTable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui.MKDTNService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

final class RadDaemon
    implements AppAA2Daemon, AdminAA2Daemon, CLA2Daemon, PeerDiscoverer2Daemon, DTNManager2Daemon,
        Router2Daemon, NECTARRouter2Daemon, NECTARPeerDiscoverer2Daemon,
        PRoPHETRouter2Daemon, PRoPHETPeerDiscoverer2Daemon, PRoPHETCLA2Daemon {
    
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + RadDaemon.class.getSimpleName();
    
    private Daemon2CLA cla;
    private Daemon2PeerDiscoverer discoverer;
    private Daemon2AppAA appAA;
    private Daemon2AdminAA adminAA;
    private Daemon2FragmentManager fragmentManager;
    private Daemon2Router router;
    private Daemon2NECTARRoutingTable nectarRoutingTable;
    private Daemon2PRoPHETRoutingTable prophetRoutingTable;
    private Daemon2Managable[] managables;
    
    private Daemon2Router.RoutingProtocol currentProtocol;
    private Thread bundleTransmitter;
//    private Thread bundleAger;
    private ExecutorService bundleProcessor;
    private Context context;
    
    RadDaemon(@NonNull Context context) {
        this.context = context;
        this.currentProtocol = Daemon2Router.RoutingProtocol.PROPHET;
    }
    
    void setCLA(@NonNull Daemon2CLA cla) {
        this.cla = cla;
    }
    
    void setDiscoverer(@NonNull Daemon2PeerDiscoverer discoverer) {
        this.discoverer = discoverer;
    }
    
    void setAppAA(@NonNull Daemon2AppAA appAA) {
        this.appAA = appAA;
    }
    
    void setManagables(@NonNull Daemon2Managable[] managables) {
        this.managables = managables;
    }
    
    void setFragmentManager(@NonNull Daemon2FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }
    
    void setAdminAA(@NonNull Daemon2AdminAA adminAA) {
        this.adminAA = adminAA;
    }
    
    void setRouter(@NonNull Daemon2Router router) {
        this.router = router;
    }
    
    void setNECTARRoutingTable(@NonNull Daemon2NECTARRoutingTable nectarRoutingTable) {
        this.nectarRoutingTable = nectarRoutingTable;
    }
    
    void setPRoPHETRoutingTable(@NonNull Daemon2PRoPHETRoutingTable prophetRoutingTable) {
        this.prophetRoutingTable = prophetRoutingTable;
    }
    
    @Override
    public synchronized void transmit(DTNBundle bundle) {
        transmit(bundle, currentProtocol);
    }
    
    @Override
    public synchronized void transmit(
        DTNBundle bundle, Daemon2Router.RoutingProtocol routingProtocol
    ) {
        currentProtocol = routingProtocol;
        
        DTNBundle[] fragments = fragmentManager.fragment(bundle, getPrefMaxFragmentSize());
        
        MiBStorage.OBQ.addAll(Arrays.asList(fragments));
        MiBStorage.writeQueue(context, MiBStorage.OUTBOUND_BUNDLES_QUEUE);
    }
    
    private int getPrefMaxFragmentSize() {
        if (MKDTNService.configFileDoesNotExist(context)) {
            MKDTNService.writeDefaultConfig(context);
        }
        MKDTNService.DTNConfig config = MKDTNService.getConfig(context);
        
        String sizeStr = config.maxFragmentPayloadSize;
        int size;
        if (sizeStr.equals(Daemon2FragmentManager.MAXIMUM_FRAGMENT_PAYLOAD_SIZES[0]))
            size = Daemon2FragmentManager.KIBI_BYTE;
        else if (sizeStr.equals(Daemon2FragmentManager.MAXIMUM_FRAGMENT_PAYLOAD_SIZES[1]))
            size = 250 * Daemon2FragmentManager.KIBI_BYTE;
        else size = Daemon2FragmentManager.DEFAULT_FRAGMENT_PAYLOAD_SIZE_IN_BYTES;
        
        return size;
    }
    
    private class TransmitOutboundBundlesTask implements Runnable {
        private static final long SLEEP_TIME_MILLIS = 5_000L;
        
        private Set<DTNBundleNode> nextHops;
        private int head;
        
        TransmitOutboundBundlesTask() {
            nextHops = new HashSet<>();
            head = 0;
        }
    
        @Override
        public void run() {
            Log.i(LOG_TAG, "transmission started");
            try {
                while (!Thread.interrupted()) {
                    if (!MiBStorage.OBQ.isEmpty()) {
                        transmit();
                    }
                    Log.d(LOG_TAG, "Outbound bundles: " + MiBStorage.OBQ.size());
                    Thread.sleep(SLEEP_TIME_MILLIS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Log.i(LOG_TAG, "transmission stopped");
        }
        
        private void transmit() {
            DTNBundle bundle = MiBStorage.OBQ.remove(head);
            MiBStorage.writeQueue(context, MiBStorage.OUTBOUND_BUNDLES_QUEUE);
    
            if (DTNUtils.isValid(bundle)) {
                nextHops.clear();
                nextHops.addAll(
                    router.chooseNextHop(discoverer.getPeerList(),
                        currentProtocol, bundle
                    )
                );
                bundle.primaryBlock.custodianEID = getThisNodezEID();
                if (cla.transmit(bundle, nextHops) > 0) {
                    Log.i(LOG_TAG, "bundle sent");
                    MiBStorage.TBQ.add(bundle);
                    MiBStorage.writeQueue(context, MiBStorage.TRANSMITTED_BUNDLES_QUEUE);
                } else {
                    MiBStorage.OBQ.add(bundle);
                    MiBStorage.writeQueue(context, MiBStorage.OUTBOUND_BUNDLES_QUEUE);
                }
            }
        }
    }
    
    @Override
    public void notifyPeerListChanged() {
        appAA.notifyPeerListChanged(getPeerList());
    }
    
    @Override
    public Set<DTNEndpointID> getPeerList() {
        Set<DTNBundleNode> nodes = discoverer.getPeerList();
        Set<DTNEndpointID> peerEIDs = new HashSet<>();
        for (DTNBundleNode node : nodes) {
            peerEIDs.add(node.dtnEndpointID);
        }
        return peerEIDs;
    }
    
    @Override
    public void onBundleReceived(DTNBundle bundle) {
        Log.i(LOG_TAG, "bundle received");
        bundleProcessor.submit(new ProcessInboundBundleTask(bundle));
    }
    
    private class ProcessInboundBundleTask implements Runnable {
        private DTNBundle bundle;
        private ProcessInboundBundleTask(DTNBundle bundle) {
            this.bundle = bundle;
        }
    
        // NOTE save the bundle as it is. Don't update the custodian EID on custody acceptance.
        @Override
        public void run() {
            if (!DTNUtils.forSingletonDestination(bundle)) return;
            else if (unintelligibleDestinationEID(bundle)) {
                if (DTNUtils.isBundleDeletionReportRequested(bundle)) {
                    DTNBundle statusReport = adminAA.makeStatusReport(
                        bundle, StatusReport.StatusFlags.BUNDLE_DELETED,
                        StatusReport.Reason.DESTINATION_EID_UNINTELLIGIBLE
                    );
                    transmit(statusReport);
                }
                return;
            }
            
            if (!(keepNECTARBundle(bundle) | keepPRoPHETBundle(bundle))) return;
            
            if (!isForUs(bundle) && DTNUtils.isExpired(bundle)) {
                if (DTNUtils.isBundleDeletionReportRequested(bundle)) {
                    DTNBundle statusReport = adminAA.makeStatusReport(
                        bundle, StatusReport.StatusFlags.BUNDLE_DELETED,
                        StatusReport.Reason.LIFETIME_EXPIRED
                    );
                    transmit(statusReport);
                }
                return;
            }
            
            if (DTNUtils.isAdminRecord(bundle)) adminAA.processAdminRecord(bundle);
            
            else if (DTNUtils.isUserData(bundle)) {
                
                if (isForUs(bundle)) {
                    if (DTNUtils.isFragment(bundle)) {
                        MiBStorage.DFQ.add(bundle);
                        MiBStorage.writeQueue(context, MiBStorage.DELIVERED_FRAGMENTS_QUEUE);
                        
                        DTNBundle[] similarFragments
                            = getSimilarFragments(bundle.primaryBlock.bundleID);
                        
                        if (fragmentManager.defragmentable(similarFragments)) {
                            DTNBundle wholeBundle
                                = fragmentManager.defragment(similarFragments);
                            insertNewMessage(wholeBundle);
                            MiBStorage.DFQ.removeAll(Arrays.asList(similarFragments));
                            MiBStorage.writeQueue(context, MiBStorage.DELIVERED_FRAGMENTS_QUEUE);
                        }
                    } else {
                        insertNewMessage(bundle);
                    }
                } else {
                    boolean weCan = canAcceptCustody(bundle);
                    if (weCan) transmit(bundle);
                    makeCustodySignal(bundle, weCan);
                }
            }
        }
        
        private void insertNewMessage(DTNBundle bundle) {
            boolean weCan = canAcceptCustody(bundle);
            if (weCan) {
                bundle.timeOfDelivery = System.currentTimeMillis();
                MiBStorage.DBQ.add(bundle);
                MiBStorage.writeQueue(context, MiBStorage.DELIVERED_BUNDLES_QUEUE);
                
                appAA.deliver(bundle);
                makeDeliveryReport(bundle);
            }
            makeCustodySignal(bundle, weCan);
        }
        
        private void makeDeliveryReport(DTNBundle bundle) {
            if (DTNUtils.isBundleDeliveryReportRequested(bundle)) {
                DTNBundle statusReport = adminAA.makeStatusReport(
                    bundle, StatusReport.StatusFlags.BUNDLE_DELIVERED,
                    StatusReport.Reason.NO_OTHER_INFO
                );
                
                transmit(statusReport);
            }
        }
        
        private void makeCustodySignal(DTNBundle bundle, boolean canAcceptCustody) {
            if (DTNUtils.isCustodyTransferRequested(bundle)) {
                DTNBundle custodySignal;
                if (canAcceptCustody) {
                    custodySignal = adminAA.makeCustodySignal(
                        bundle, true, CustodySignal.Reason.NO_OTHER_INFO
                    );
                } else {
                    custodySignal = adminAA.makeCustodySignal(
                        bundle, false, getCustodySignalReason()
                    );
                }
                
                transmit(custodySignal);
            }
        }
        
        private DTNBundle[] getSimilarFragments(DTNBundleID id) {
            List<DTNBundle> similarFragments = new ArrayList<>();
            
            synchronized (MiBStorage.DFQ) {
                for (DTNBundle bundle : MiBStorage.DFQ) {
                    if (bundle.primaryBlock.bundleID.equals(id)) similarFragments.add(bundle);
                }
            }
            
            return similarFragments.toArray(new DTNBundle[0]);
        }
    }
    
    @Override
    public List<DTNBundle> getDeliveredMessages() {
        List<DTNBundle> messages = new ArrayList<>();
        int head = 0;
    
        synchronized (MiBStorage.DBQ) {
            for (DTNBundle bundle : MiBStorage.DBQ) messages.add(head, bundle);
        }
    
        return Collections.unmodifiableList(messages);
    }
    
    private static final float MKDTN_MIN_BATTERY_LEVEL_PERCENTAGE = 15.0F;
    private static final float MKDTN_MIN_FREE_SPACE_PERCENTAGE = 5.0F;
    
    private synchronized boolean unintelligibleDestinationEID(DTNBundle bundle) {
        if (DTNUtils.isValid(bundle)) {
            DTNEndpointID destEID = bundle.primaryBlock.destinationEID;
            return destEID == null ||
                destEID.scheme == null ||
                !destEID.scheme.equals(DTNEndpointID.DTN_SCHEME) ||
                destEID.ssp == null ||
                destEID.ssp.length() != getThisNodezEID().ssp.length();
        }
        else return false;
    }
    
    private synchronized boolean canAcceptCustody(DTNBundle bundle) {
        
        if (insufficientBatteryPower()) {
            setCustodySignalReason(CustodySignal.Reason.DEPLETED_POWER);
            return false;
        }
        
        if (insufficientStorageSpace()) {
            setCustodySignalReason(CustodySignal.Reason.DEPLETED_STORAGE);
            return false;
        }
        
        if (alreadyHas(bundle)) {
            Log.i(LOG_TAG, "redundant reception");
            setCustodySignalReason(CustodySignal.Reason.REDUNDANT_RECEPTION);
            return false;
        }
        
        setCustodySignalReason(CustodySignal.Reason.NO_OTHER_INFO);
        return true;
    }
    
    private synchronized boolean insufficientBatteryPower() {
        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, batteryIntentFilter);
        if (batteryStatus != null) {
            int current = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int maximum = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        
            float batteryLevelPercentage = (current / (float) maximum) * 100;
            Log.i(LOG_TAG, "Battery level = " + batteryLevelPercentage);
            
            return batteryLevelPercentage < MKDTN_MIN_BATTERY_LEVEL_PERCENTAGE;
        }
        return false;
    }
    
    private synchronized boolean insufficientStorageSpace() {
        String ourAppsDir = context.getFilesDir().getAbsoluteFile().toString();
        File dir = new File(ourAppsDir);
//        Log.i(LOG_TAG, "our app's directory = " + ourAppsDir);
        
        long freeSpace = dir.getFreeSpace();
        long totalSpace = dir.getTotalSpace();
    
        float freeSpacePercentage = (freeSpace / (float) totalSpace) * 100;
        Log.i(LOG_TAG, "Free space = " + freeSpacePercentage);
        
        return freeSpacePercentage < MKDTN_MIN_FREE_SPACE_PERCENTAGE;
    }
    
    private synchronized boolean alreadyHas(DTNBundle bundle) {
        return MiBStorage.OBQ.contains(bundle) || MiBStorage.DBQ.contains(bundle) ||
            MiBStorage.DFQ.contains(bundle) || MiBStorage.TBQ.contains(bundle);
    }
    
    private CustodySignal.Reason custodySignalReason;
    
    private synchronized CustodySignal.Reason getCustodySignalReason() {
        return custodySignalReason;
    }
    
    private synchronized void setCustodySignalReason(CustodySignal.Reason reason) {
        custodySignalReason = reason;
    }
    
    @Override
    public boolean start() {
        MiBStorage.readDB(context);
        startExecutors();
        for (Daemon2Managable managable : managables) managable.start();
        return true;
    }
    
    private void startExecutors() {
        if (bundleTransmitter == null) {
            bundleTransmitter = new Thread(new TransmitOutboundBundlesTask());
            bundleTransmitter.setName("Bundle Transmission Task");
            bundleTransmitter.start();
        }
        
        if (bundleProcessor == null) {
            bundleProcessor = Executors.newSingleThreadExecutor();
        }
        
//        if (bundleAger == null) {
//            bundleAger = new Thread(new BundleAgingTask());
//            bundleAger.setName("Bundle Aging Task");
//            bundleAger.start();
//        }
    }
    
    @Override
    public boolean stop() {
        stopExecutors();
        for (Daemon2Managable managable : managables) managable.stop();
        MiBStorage.writeDB(context);
        return true;
    }
    
    private void stopExecutors() {
        if (bundleTransmitter != null) {
            bundleTransmitter.interrupt();
            bundleTransmitter = null;
        }
    
        if (bundleProcessor != null) {
            bundleProcessor.shutdown();
        
            try {
                bundleProcessor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "processor shutdown interrupted", e);
            }
        
            bundleProcessor = null;
        }
        
//        if (bundleAger != null) {
//            bundleAger.interrupt();
//            bundleAger = null;
//        }
    }
    
    @Override
    public DTNEndpointID getThisNodezEID() {
        return MiBStorage.getNodeEID(context);
    }
    
    @Override
    public boolean isForUs(DTNBundle bundle) {
        if (DTNUtils.isValid(bundle)) {
            DTNEndpointID dest = bundle.primaryBlock.destinationEID;
            return dest != null && dest.equals(getThisNodezEID());
        }
        return false;
    }
    
    @Override
    public boolean isFromUs(DTNBundle bundle) {
        if (DTNUtils.isValid(bundle)) {
            DTNEndpointID src = bundle.primaryBlock.bundleID.sourceEID;
            return src != null && src.equals(getThisNodezEID());
        }
        return false;
    }
    
    @Override
    public boolean isUs(DTNEndpointID eid) {
        return getThisNodezEID().equals(eid);
    }
    
    @Override
    public void incrementMeetingCount(DTNEndpointID nodeEID) {
        nectarRoutingTable.incrementMeetingCount(nodeEID);
    }
    
    @Override
    public float getMeetingFrequency(DTNEndpointID nodeEID) {
        return nectarRoutingTable.getMeetingFrequency(nodeEID);
    }
    
    private synchronized boolean keepNECTARBundle(DTNBundle bundle) {
        if (!DTNUtils.isValid(bundle)) return false;
    
        CanonicalBlock nectarCBlock
            = bundle.canonicalBlocks.remove(DTNBundle.CBlockNumber.NECTAR_ROUTING_INFO);
    
        if (DTNUtils.isValidNECTARCBlock(nectarCBlock)) {
        
            NECTARRoutingInfo info = (NECTARRoutingInfo) nectarCBlock.blockTypeSpecificDataFields;
            float freqCustodian2Destination = info.meetingFrequency;
        
            float freqMe2Destination = getMeetingFrequency(bundle.primaryBlock.destinationEID);
        
            return Float.compare(freqMe2Destination, freqCustodian2Destination) >= 0;
        }
        return true;
    }
    
    private synchronized boolean keepPRoPHETBundle(DTNBundle bundle) {
        if (!DTNUtils.isValid(bundle)) return false;
        
        CanonicalBlock prophetCBlock
            = bundle.canonicalBlocks.remove(DTNBundle.CBlockNumber.PROPHET_ROUTING_INFO);
        
        if (DTNUtils.isValidPRoPHETCBlock(prophetCBlock)) {
    
            PRoPHETRoutingInfo info = (PRoPHETRoutingInfo) prophetCBlock.blockTypeSpecificDataFields;
            float probCustodian2Destination = info.deliveryPredictability;
            
            float probMe2Destination = getDeliveryPredictability(bundle.primaryBlock.destinationEID);
            
            return Float.compare(probMe2Destination, probCustodian2Destination) >= 0;
        }
        return true;
    }
    
    @Override
    public void updateDeliveryPredictability(DTNEndpointID nodeEID) {
        prophetRoutingTable.updateDeliveryPredictability(nodeEID);
    }
    
    @Override
    public void calculateDPTransitivity(DTNBundle bundle) {
        prophetRoutingTable.calculateDPTransitivity(bundle);
    }
    
    @Override
    public float getDeliveryPredictability(DTNEndpointID nodeEID) {
        return prophetRoutingTable.getDeliveryPredictability(nodeEID);
    }
    
    @Override
    public void notifyBundleStatus(String recipient, String msg) {
        appAA.notifyBundleStatus(recipient, msg);
    }
    
    @Override
    public void delete(DTNBundleID bundleID) {
        if (currentProtocol != Daemon2Router.RoutingProtocol.EPIDEMIC) {
            synchronized (MiBStorage.TBQ) {
                for (DTNBundle bundle : MiBStorage.TBQ) {
                    if (bundle.primaryBlock.bundleID.equals(bundleID))
                        MiBStorage.TBQ.remove(bundle);
                }
                MiBStorage.writeQueue(context, MiBStorage.TRANSMITTED_BUNDLES_QUEUE);
            }
        }
    }
    
    @Override
    public void delete(DTNBundleID bundleID, int fragmentOffset) {
        if (currentProtocol != Daemon2Router.RoutingProtocol.EPIDEMIC) {
            synchronized (MiBStorage.TBQ) {
                for (DTNBundle bundle : MiBStorage.TBQ) {
                    if (DTNUtils.isFragment(bundle)) {
                        
                        String fragOffsetStr
                            = bundle.primaryBlock.detailsIfFragment
                            .get(PrimaryBlock.FragmentField.FRAGMENT_OFFSET);
                        
                        int fragOffset;
                        if (fragOffsetStr != null) fragOffset = Integer.parseInt(fragOffsetStr);
                        else return;
                        
                        if (bundle.primaryBlock.bundleID.equals(bundleID) &&
                            fragmentOffset == fragOffset)
                            MiBStorage.TBQ.remove(bundle);
                    }
                }
                MiBStorage.writeQueue(context, MiBStorage.TRANSMITTED_BUNDLES_QUEUE);
            }
        }
    }
    
//    private void age() {
//        synchronized (MiBStorage.OBQ) {
//            for (DTNBundle bundle : MiBStorage.OBQ) {
//                int i = MiBStorage.OBQ.indexOf(bundle);
//
//                if (DTNUtils.isExpired(bundle)) {
//                    MiBStorage.OBQ.remove(i);
//
//                    if (!isFromUs(bundle) &&
//                        DTNUtils.isBundleDeletionReportRequested(bundle)) {
//                        DTNBundle statusReport = adminAA.makeStatusReport(
//                            bundle, StatusReport.StatusFlags.BUNDLE_DELETED,
//                            StatusReport.Reason.LIFETIME_EXPIRED
//                        );
//                        transmit(statusReport);
//                    }
//                }
//            }
//            MiBStorage.writeQueue(context, MiBStorage.OUTBOUND_BUNDLES_QUEUE);
//        }
//    }
    
//    private class BundleAgingTask implements Runnable {
//        @Override
//        public void run() {
//            Log.i(LOG_TAG, "bundle aging started");
//            try {
//                while (!Thread.interrupted()) {
//                    if (!MiBStorage.OBQ.isEmpty()) {
//                        age();
//                    }
//                    Thread.sleep(637_000); // 10.6167 mins
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//            Log.i(LOG_TAG, "bundle aging stopped");
//        }
//    }
}
