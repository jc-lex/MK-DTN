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
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AgeBlock;
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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

final class RadDaemon
    implements AppAA2Daemon, AdminAA2Daemon, CLA2Daemon, PeerDiscoverer2Daemon, DTNManager2Daemon,
        Router2Daemon, NECTARRouter2Daemon, NECTARPeerDiscoverer2Daemon,
        PRoPHETRouter2Daemon, PRoPHETPeerDiscoverer2Daemon, PRoPHETCLA2Daemon {
    
    private Daemon2CLA cla;
    private Daemon2PeerDiscoverer discoverer;
    private Daemon2AppAA appAA;
    private Daemon2AdminAA adminAA;
    private Daemon2FragmentManager fragmentManager;
    private Daemon2Router router;
    private Daemon2NECTARRoutingTable nectarRoutingTable;
    private Daemon2PRoPHETRoutingTable prophetRoutingTable;
    private Daemon2Managable[] managables;
    
    private static final DTNEndpointID BUNDLE_NODE_EID = makeEID();
    private static DTNEndpointID makeEID() {
        String eid = Long.toHexString(UUID.randomUUID().getMostSignificantBits());
        return DTNEndpointID.from(DTNEndpointID.DTN_SCHEME, eid.substring(0, 4));
    }
    
    private Daemon2Router.RoutingProtocol currentProtocol;
    private Thread bundleTransmitter;
    private ExecutorService bundleProcessor;
    
    RadDaemon(@NonNull Context context) {
        this.context = context;
        this.currentProtocol = Daemon2Router.RoutingProtocol.PER_HOP;
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
    public void transmit(DTNBundle bundle) {
        transmit(bundle, currentProtocol);
    }
    
    @Override
    public void transmit(DTNBundle bundle, Daemon2Router.RoutingProtocol routingProtocol) {
        currentProtocol = routingProtocol;
        
        DTNBundle[] fragments = fragmentManager.fragment(bundle);
        DummyStorage.OUTBOUND_BUNDLES_QUEUE.addAll(Arrays.asList(fragments));
    }
    
    private abstract class TransmitOutboundBundlesTask implements Runnable {
        Set<DTNBundleNode> nextHops;
        TransmitOutboundBundlesTask() {
            nextHops = new HashSet<>();
        }
        
        static final long ONE_MINUTE_MILLIS = 60_000L;
        
        void srcPhase(long onCycleDurationMillis, long offCycleDurationMillis)
            throws InterruptedException {
            Log.i(LOG_TAG, "entering ON cycle");
            doSrcMode(onCycleDurationMillis);
            Log.i(LOG_TAG, "leaving ON cycle");
    
            doSleep(offCycleDurationMillis);
        }
        
        void sinkPhase(long onCycleDurationMillis, long offCycleDurationMillis)
            throws InterruptedException {
            Log.i(LOG_TAG, "entering ON cycle");
            doSinkMode(onCycleDurationMillis);
            Log.i(LOG_TAG, "leaving ON cycle");
            
            doSleep(offCycleDurationMillis);
        }
        
        private void doSleep(long offCycleDurationMillis) throws InterruptedException {
            Log.i(LOG_TAG, "entering OFF cycle");
            Thread.sleep(offCycleDurationMillis);
            Log.i(LOG_TAG, "leaving OFF cycle");
        }
        
        private void doSrcMode(long onCycleDurationMillis) throws InterruptedException {
            if (insufficientBatteryPower()) return;
            
            Log.i(LOG_TAG, "entering SOURCE mode");
            discoverer.start(Daemon2PeerDiscoverer.ServiceMode.SOURCE);
            long stopTime = System.currentTimeMillis() + onCycleDurationMillis;
            int head = 0;
    
            while (System.currentTimeMillis() < stopTime) {
                // get what to send
                DTNBundle bundle = null;
                do {
                    if (!DummyStorage.OUTBOUND_BUNDLES_QUEUE.isEmpty())
                        bundle = DummyStorage.OUTBOUND_BUNDLES_QUEUE.remove(head);
                } while (bundle == null && System.currentTimeMillis() < stopTime);
                if (System.currentTimeMillis() >= stopTime) break;
                
                assert bundle != null;
                
                // get who to send to
                nextHops.clear();
                do {
                    nextHops.addAll(
                        router.chooseNextHop(discoverer.getPeerList(), currentProtocol, bundle)
                    );
                } while (nextHops.isEmpty() && System.currentTimeMillis() < stopTime);
                if (System.currentTimeMillis() >= stopTime) break;
                
                // update bundle details
                bundle.primaryBlock.custodianEID = getThisNodezEID();
                setSendingTime(bundle);

                // send bundle
                if (cla.transmit(bundle, nextHops) == 0) {
                    DummyStorage.OUTBOUND_BUNDLES_QUEUE.add(head, bundle);
                } else {
                    DummyStorage.TRANSMITTED_BUNDLES_QUEUE.add(bundle);
                }
            }
    
            Log.i(LOG_TAG, "leaving SOURCE mode");
            discoverer.stop();
        }
        
        private void doSinkMode(long onCycleDurationMillis) throws InterruptedException {
            if (insufficientBatteryPower() && insufficientStorageSpace()) return;
            
            Log.i(LOG_TAG, "entering SINK mode");
            discoverer.start(Daemon2PeerDiscoverer.ServiceMode.SINK);
    
            Thread.sleep(onCycleDurationMillis);
    
            Log.i(LOG_TAG, "leaving SINK mode");
            discoverer.stop();
        }
        
        private void setSendingTime(DTNBundle bundle) {
            long bundleCreationTimestamp
                = bundle.primaryBlock.bundleID.creationTimestamp;
    
            CanonicalBlock ageCBlock
                = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.AGE);
            assert ageCBlock != null;
    
            AgeBlock ageBlock = (AgeBlock) ageCBlock.blockTypeSpecificDataFields;
    
            ageBlock.sendingTimestamp = System.currentTimeMillis();
    
            if (isFromUs(bundle)) { // initial conditions from bundle's src
                ageBlock.agePrime = 0;
                ageBlock.T = bundleCreationTimestamp;
            }
    
            // NOTE general equation for bundle aging, where now == sendingTimestamp
            ageBlock.age = ageBlock.agePrime + (long)
                ((DTNUtils.getMaxCPUFrequencyInKHz() / (float) ageBlock.sourceCPUSpeedInKHz)
                    * (ageBlock.sendingTimestamp - ageBlock.T));
        }
    }
    
    private class AutomaticTransmitTask extends TransmitOutboundBundlesTask {
        private AutomaticTransmitTask() {
            super();
        }
        private static final long ON_CYCLE_DURATION_MILLIS = 3 * ONE_MINUTE_MILLIS;
        private static final long OFF_CYCLE_DURATION_MILLIS = 4 * ONE_MINUTE_MILLIS;
        
        @Override
        public void run() {
            Log.i(LOG_TAG, "transmission AUTO");
            Log.i(LOG_TAG, "transmission started");
            try {
                while (!Thread.interrupted()) {
                    srcPhase(ON_CYCLE_DURATION_MILLIS, OFF_CYCLE_DURATION_MILLIS);
                    sinkPhase(ON_CYCLE_DURATION_MILLIS, OFF_CYCLE_DURATION_MILLIS);
                }
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "transmission interrupted", e);
                discoverer.stop();
            }
            Log.i(LOG_TAG, "transmission stopped");
        }
    }
    
    private class ManualTransmitTask extends TransmitOutboundBundlesTask {
        private Daemon2PeerDiscoverer.ServiceMode mode;
        private ManualTransmitTask(Daemon2PeerDiscoverer.ServiceMode mode) {
            super();
            this.mode = mode;
        }
        private static final long ON_CYCLE_DURATION_MILLIS = 10 * ONE_MINUTE_MILLIS;
        private static final long OFF_CYCLE_DURATION_MILLIS = 5 * ONE_MINUTE_MILLIS;
    
        @Override
        public void run() {
            Log.i(LOG_TAG, "transmission MANUAL");
            Log.i(LOG_TAG, "transmission started");
            try {
                while (!Thread.interrupted()) {
                    switch (mode) {
                        case SOURCE:
                            srcPhase(ON_CYCLE_DURATION_MILLIS, OFF_CYCLE_DURATION_MILLIS);
                            break;
                        case SINK:
                            sinkPhase(ON_CYCLE_DURATION_MILLIS, OFF_CYCLE_DURATION_MILLIS);
                            break;
                        default: break;
                    }
                }
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "transmission interrupted", e);
                discoverer.stop();
            }
            Log.i(LOG_TAG, "transmission stopped");
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
            
            if (!(keepNECTARBundle(bundle) | keepPRoPHETBundle(bundle))) return;
            
            processAgeAtReceipt(bundle);
            
            if (DTNUtils.isAdminRecord(bundle)) adminAA.processAdminRecord(bundle);
            
            else if (DTNUtils.isUserData(bundle)) {
                
                if (isForUs(bundle)) {
                    if (DTNUtils.isFragment(bundle)) {
                        DummyStorage.DELIVERED_FRAGMENTS_QUEUE.add(bundle);
                        
                        DTNBundle[] similarFragments
                            = getSimilarFragments(bundle.primaryBlock.bundleID);
                        
                        if (fragmentManager.defragmentable(similarFragments)) {
                            DTNBundle wholeBundle
                                = fragmentManager.defragment(similarFragments);
                            insertNewMessage(wholeBundle);
                            DummyStorage.DELIVERED_FRAGMENTS_QUEUE
                                .removeAll(Arrays.asList(similarFragments));
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
        
        private void processAgeAtReceipt(DTNBundle bundle) {
            if (DTNUtils.isValid(bundle)) {
                long cts = bundle.primaryBlock.bundleID.creationTimestamp;
                CanonicalBlock ageCBlock
                    = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.AGE);
                assert ageCBlock != null;
    
                AgeBlock ageBlock = (AgeBlock) ageCBlock.blockTypeSpecificDataFields;
    
                float A1 = (float) ageBlock.sourceCPUSpeedInKHz * ageBlock.sendingTimestamp;
                float A2 = (DTNUtils.getMaxCPUFrequencyInKHz() * ageBlock.receivingTimestamp) /
                    (float) ageBlock.sourceCPUSpeedInKHz;
                long transmissionAge = (long) (Math.abs(A2 - A1) / ageBlock.sourceCPUSpeedInKHz);
    
                ageBlock.agePrime = ageBlock.age + transmissionAge;
                ageBlock.T = cts + ageBlock.agePrime;
            }
        }
        
        private void insertNewMessage(DTNBundle bundle) {
            boolean weCan = canAcceptCustody(bundle);
            if (weCan) {
                DummyStorage.DELIVERED_BUNDLES_QUEUE.add(bundle);
                appAA.deliver(bundle);
                
                // TODO make status report reasons useful
                makeStatusReport(bundle);
            }
            makeCustodySignal(bundle, weCan);
        }
        
        private void makeStatusReport(DTNBundle bundle) {
            if (DTNUtils.isBundleDeliveryReportRequested(bundle)) {
                DTNBundle statusReport = adminAA.makeStatusReport(
                    bundle, true, StatusReport.Reason.NO_OTHER_INFO
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
            
            synchronized (DummyStorage.DELIVERED_FRAGMENTS_QUEUE) {
                for (DTNBundle bundle : DummyStorage.DELIVERED_FRAGMENTS_QUEUE) {
                    if (bundle.primaryBlock.bundleID.equals(id)) similarFragments.add(bundle);
                }
            }
            
            return similarFragments.toArray(new DTNBundle[0]);
        }
    }
    
    @Override
    public List<DTNBundle> getDeliveredMessages() {
        // TODO get them from storage, sort by received timestamp DESC
        return Collections.unmodifiableList(DummyStorage.DELIVERED_BUNDLES_QUEUE);
    }
    
    private Context context;
    private static final float MKDTN_MIN_BATTERY_LEVEL_PERCENTAGE = 15.0F;
    private static final float MKDTN_MIN_FREE_SPACE_PERCENTAGE = 5.0F;
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + RadDaemon.class.getSimpleName();
    
    private synchronized boolean canAcceptCustody(DTNBundle bundle) {
        
        if (insufficientBatteryPower()) {
            setCustodySignalReason(CustodySignal.Reason.DEPLETED_POWER);
            return false;
        }
        
        if (insufficientStorageSpace()) {
            setCustodySignalReason(CustodySignal.Reason.DEPLETED_STORAGE);
            return false;
        }
        
        // REDUNDANT RECEPTION
        if (DummyStorage.OUTBOUND_BUNDLES_QUEUE.contains(bundle) ||
            DummyStorage.DELIVERED_BUNDLES_QUEUE.contains(bundle) ||
            DummyStorage.DELIVERED_FRAGMENTS_QUEUE.contains(bundle) ||
            DummyStorage.TRANSMITTED_BUNDLES_QUEUE.contains(bundle)
        ) {
//            Log.i(LOG_TAG, "redundant reception");
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
//            Log.i(LOG_TAG, "Battery level = " + batteryLevelPercentage);
            
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
//        Log.i(LOG_TAG, "Free space = " + freeSpacePercentage);
        
        return freeSpacePercentage < MKDTN_MIN_FREE_SPACE_PERCENTAGE;
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
        boolean state = startExecutors();
        for (Daemon2Managable managable : managables) state &= managable.start();
        return state;
    }
    
    private boolean startExecutors() {
        if (bundleTransmitter == null && bundleProcessor == null) {
            if (MKDTNService.configFileDoesNotExist(context)) {
                MKDTNService.writeDefaultConfig(context);
            }
            MKDTNService.DTNConfig config = MKDTNService.getConfig(context);
            
            if (config.enableManualMode) {
                Daemon2PeerDiscoverer.ServiceMode transmissionMode
                    = Daemon2PeerDiscoverer.ServiceMode.valueOf(config.transmissionMode);
                
                bundleTransmitter = new Thread(new ManualTransmitTask(transmissionMode));
            } else {
                bundleTransmitter = new Thread(new AutomaticTransmitTask());
            }
            bundleTransmitter.start();
    
//            bundleProcessor = Executors.newCachedThreadPool();
            bundleProcessor = Executors.newSingleThreadExecutor();
    
            return bundleTransmitter.isAlive();
        } else return true;
    }
    
    @Override
    public boolean stop() {
        boolean state = stopExecutors();
        for (Daemon2Managable managable : managables) state &= managable.stop();
        return state;
    }
    
    private boolean stopExecutors() {
        if (bundleTransmitter != null && bundleProcessor != null) {
            bundleTransmitter.interrupt();
            bundleProcessor.shutdown();
            
            boolean done;
            try {
                done = bundleTransmitter.isInterrupted() &&
                    bundleProcessor.awaitTermination(5L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                done = false;
            }
            
            bundleTransmitter = null;
            bundleProcessor = null;
            
            return done;
        } else return true;
    }
    
    @Override
    public DTNEndpointID getThisNodezEID() {
        // TODO if we have one, get it. else, make one and keep it, then get it.
        return BUNDLE_NODE_EID;
    }
    
    @Override
    public boolean isForUs(DTNBundle bundle) {
        if (bundle != null) {
            PrimaryBlock primaryBlock = bundle.primaryBlock;
            if (primaryBlock != null) {
                DTNEndpointID dest = primaryBlock.destinationEID;
                return dest != null && dest.equals(BUNDLE_NODE_EID);
            }
        }
        return false;
    }
    
    @Override
    public boolean isFromUs(DTNBundle bundle) {
        if (bundle != null) {
            PrimaryBlock primaryBlock = bundle.primaryBlock;
            if (primaryBlock != null) {
                DTNBundleID bundleID = primaryBlock.bundleID;
                if (bundleID != null) {
                    DTNEndpointID src = bundleID.sourceEID;
                    return src != null && src.equals(BUNDLE_NODE_EID);
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean isUs(DTNEndpointID eid) {
        return eid != null && eid.equals(BUNDLE_NODE_EID);
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
    public void notifyOutboundBundleDelivered(String recipient) {
        appAA.notifyOutboundBundleReceived(recipient);
    }
    
    @Override
    public void notifyOutboundBundleDeliveryFailed(String recipient, String reason) {
        appAA.notifyOutboundBundleDeliveryFailed(recipient, reason);
    }
    
    @Override
    public void delete(DTNBundleID bundleID) {
        if (currentProtocol != Daemon2Router.RoutingProtocol.EPIDEMIC) {
            synchronized (DummyStorage.TRANSMITTED_BUNDLES_QUEUE) {
                for (DTNBundle bundle : DummyStorage.TRANSMITTED_BUNDLES_QUEUE) {
                    if (bundle.primaryBlock.bundleID.equals(bundleID))
                        DummyStorage.TRANSMITTED_BUNDLES_QUEUE.remove(bundle);
                }
            }
        }
    }
    
    @Override
    public void delete(DTNBundleID bundleID, int fragmentOffset) {
        if (currentProtocol != Daemon2Router.RoutingProtocol.EPIDEMIC) {
            synchronized (DummyStorage.TRANSMITTED_BUNDLES_QUEUE) {
                for (DTNBundle bundle : DummyStorage.TRANSMITTED_BUNDLES_QUEUE) {
                    if (DTNUtils.isFragment(bundle)) {
                        
                        String fragOffsetStr
                            = bundle.primaryBlock.detailsIfFragment
                            .get(PrimaryBlock.FragmentField.FRAGMENT_OFFSET);
                        
                        int fragOffset;
                        if (fragOffsetStr != null) fragOffset = Integer.parseInt(fragOffsetStr);
                        else return;
                        
                        if (bundle.primaryBlock.bundleID.equals(bundleID) &&
                            fragmentOffset == fragOffset)
                            
                            DummyStorage.TRANSMITTED_BUNDLES_QUEUE.remove(bundle);
                    }
                }
            }
        }
    }
}
