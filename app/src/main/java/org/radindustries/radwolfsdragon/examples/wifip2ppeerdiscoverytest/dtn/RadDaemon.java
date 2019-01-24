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
    
    private static final DTNEndpointID BUNDLE_NODE_EID = makeEID(); //from persistent storage
    
    private Daemon2Router.RoutingProtocol currentProtocol;
    private Thread bundleTransmitter;
    private ExecutorService bundleProcessor;
    
    RadDaemon(@NonNull Context context) {
        this.context = context;
//        this.deliveredMessages = new ArrayList<>();
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
//        bundleTransmitter.submit(new TransmitOutboundBundleTask(bundle));
        
//        // proactive fragmentation
//        DTNBundle[] fragments = fragmentManager.fragment(bundle);
//
//        for (DTNBundle fragment : fragments)
//            bundleTransmitter.submit(new TransmitOutboundBundleTask(fragment));
    }
    
    private class TransmitOutboundBundlesTask implements Runnable {
        private DTNBundle bundle;
        private Set<DTNBundleNode> nextHops;
    
        private TransmitOutboundBundlesTask() {
            bundle = null;
            nextHops = new HashSet<>();
        }
    
        @Override
        public void run() {
            int head = 0;
            Log.i(LOG_TAG, "transmitting...");
            while (!Thread.interrupted()) {
                // get what to send
                if (!DummyStorage.OUTBOUND_BUNDLES_QUEUE.isEmpty()) {
                    bundle = DummyStorage.OUTBOUND_BUNDLES_QUEUE.remove(head);
                } else if (!DummyStorage.INTERMEDIATE_BUNDLES_QUEUE.isEmpty()) {
                    bundle = DummyStorage.INTERMEDIATE_BUNDLES_QUEUE.remove(head);
                } else continue;
        
                // get who to send it to
                do {
                    nextHops.addAll(
                        router.chooseNextHop(discoverer.getPeerList(), currentProtocol, bundle)
                    );
                } while (nextHops.isEmpty());
        
                // update the custodian EID prior to transmission
                bundle.primaryBlock.custodianEID = getThisNodezEID();
        
                cla.transmit(bundle, nextHops);
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
//            storeTextMessage(bundle);
//            if (deliveredMessages.add(bundle)) appAA.deliver(bundle);
            if (!DTNUtils.forSingletonDestination(bundle)) return;
            
            if (!(keepNECTARBundle(bundle) | keepPRoPHETBundle(bundle))) return;
            
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
                        }
                    } else {
                        insertNewMessage(bundle);
                    }
                } else {
                    boolean weCan = canAcceptCustody(bundle);
                    if (weCan) DummyStorage.INTERMEDIATE_BUNDLES_QUEUE.add(bundle);
                    makeCustodySignal(bundle, weCan);
                }
            }
        }
        
        private void insertNewMessage(DTNBundle bundle) {
            DummyStorage.DELIVERED_BUNDLES_QUEUE.add(bundle);
            appAA.deliver(bundle);
    
            makeStatusReport(bundle);
        }
        
        private void makeStatusReport(DTNBundle bundle) {
            if (DTNUtils.isBundleDeliveryReportRequested(bundle)) {
                DTNBundle statusReport = adminAA.makeStatusReport(
                    bundle, true, StatusReport.Reason.NO_OTHER_INFO
                );
                
                DummyStorage.OUTBOUND_BUNDLES_QUEUE.add(statusReport);
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
                
                DummyStorage.OUTBOUND_BUNDLES_QUEUE.add(custodySignal);
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
    
    // temporary storage, for both payloadADUs and status reports
//    private List<DTNBundle> deliveredMessages;
    
    @Override
    public List<DTNBundle> getDeliveredMessages() {
        // TODO get them from storage, sort by received timestamp DESC
        return Collections.unmodifiableList(DummyStorage.DELIVERED_BUNDLES_QUEUE);
    }
    
    private Context context;
    private static final float MKDTN_MIN_BATTERY_LEVEL_PERCENTAGE = 35.0F;
    private static final float MKDTN_MIN_FREE_SPACE_PERCENTAGE = 35.0F;
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + RadDaemon.class.getSimpleName();
    private synchronized boolean canAcceptCustody(DTNBundle bundle) {
        
        // BATTERY LEVEL
        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, batteryIntentFilter);
        if (batteryStatus != null) {
            int current = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int maximum = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            
            float batteryLevelPercentage = (current / (float) maximum) * 100;
            Log.i(LOG_TAG, "Battery level = " + batteryLevelPercentage);
            if (batteryLevelPercentage < MKDTN_MIN_BATTERY_LEVEL_PERCENTAGE) {
                setCustodySignalReason(CustodySignal.Reason.DEPLETED_POWER);
                return false;
            }
        }
        
        // DISK SPACE
        long freeSpace // getFilesDir() == internal storage directory for your app
            = new File(context.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        long totalSpace
            = new File(context.getFilesDir().getAbsoluteFile().toString()).getTotalSpace();
        
        float freeSpacePercentage = (freeSpace / (float) totalSpace) * 100;
        Log.i(LOG_TAG, "Free space = " + freeSpacePercentage);
        if (freeSpacePercentage < MKDTN_MIN_FREE_SPACE_PERCENTAGE) {
            setCustodySignalReason(CustodySignal.Reason.DEPLETED_STORAGE);
            return false;
        }
        
        // REDUNDANT RECEPTION
        if (DummyStorage.OUTBOUND_BUNDLES_QUEUE.contains(bundle) ||
            DummyStorage.INTERMEDIATE_BUNDLES_QUEUE.contains(bundle) ||
            DummyStorage.DELIVERED_BUNDLES_QUEUE.contains(bundle) ||
            DummyStorage.DELIVERED_FRAGMENTS_QUEUE.contains(bundle)
        ) {
            Log.i(LOG_TAG, "redundant reception: " + bundle);
            setCustodySignalReason(CustodySignal.Reason.REDUNDANT_RECEPTION);
            return false;
        }
        
        setCustodySignalReason(CustodySignal.Reason.NO_OTHER_INFO);
        return true;
    }
    
    private CustodySignal.Reason custodySignalReason;
    
    private synchronized CustodySignal.Reason getCustodySignalReason() {
        return custodySignalReason;
    }
    
    private synchronized void setCustodySignalReason(CustodySignal.Reason reason) {
        custodySignalReason = reason;
    }
    
//    private void storeTextMessage(DTNBundle bundle) {
//        if (bundle.primaryBlock.bundleProcessingControlFlags
//            .testBit(PrimaryBlock.BundlePCF.ADU_IS_AN_ADMIN_RECORD)) {
//
//            CanonicalBlock adminRecordCBlock
//                = bundle.canonicalBlocks.get(DTNBundle.CBlockNumber.ADMIN_RECORD);
//            assert adminRecordCBlock != null;
//
//            AdminRecord adminRecord = (AdminRecord) adminRecordCBlock.blockTypeSpecificDataFields;
//
//            if (adminRecord.recordType.equals(AdminRecord.RecordType.CUSTODY_SIGNAL)) return;
//        }
//        deliveredMessages.add(bundle);
//    }
//
//    private void collectFragmentBundleID(DTNBundle deliveredBundle) {
//        // this set of fragment IDs is collected by the Daemon, not router
//        if (deliveredBundle.primaryBlock.destinationEID.equals(BUNDLE_NODE_EID)) {
//            if (deliveredBundle.primaryBlock.bundleProcessingControlFlags
//                .testBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT)) {
//                DTNBundleID fragmentID = deliveredBundle.primaryBlock.bundleID;
//                deliveredFragments.add(fragmentID); //step 1
////                Log.i(LOG_TAG, "Delivered fragment: " + fragmentID);
////                Log.d(LOG_TAG, "Accumulated fragments: " + deliveredFragments);
//                 /*
//                 2. daemon will store the "deliveredBundle" in the delivered queue in storage.
//                 3. daemon will then use the "deliveredFragments" set to query the delivered queue
//                 for all fragments having a common bundle ID.
//                 4. for each set of related fragments (fragments with a common bundleID),
//                 daemon will call the fragment manager to check for their defragmentability.
//                 5. if defragmentable, daemon will tell fragment manager to defragment them.
//                 6. the returned bundle is immediately sent to the user via App AA.
//
//                 This means the delivered queue in storage will only contain fragments
//                 were this node is the destination.
//                 */
//            } /*else {
//                // daemon sends delivered bundle to app aa immediately.
////                Log.i(LOG_TAG, "Bundle not a fragment. Sending to App AA...");
//            }*/
//        }
//    }
    
//    @Override
//    public void onTransmissionComplete(int numNodesSentTo) {
//        if (numNodesSentTo < chosenPeers.size()) {
//            cla.transmit(bundleToTransmit, chosenPeers.get(numNodesSentTo));
//        } else {
//            cla.reset();
//        }
//    }
    
    @Override
    public boolean start() {
        boolean state = startExecutors();
        for (Daemon2Managable managable : managables) state &= managable.start();
        return state;
    }
    
    private boolean startExecutors() {
        bundleTransmitter = new Thread(new TransmitOutboundBundlesTask());
        bundleTransmitter.start();
//        if (waitingBundleTransmissionTasks != null)
//            for (Runnable task : waitingBundleTransmissionTasks) bundleTransmitter.submit(task);
        
        bundleProcessor = Executors.newCachedThreadPool();
//        if (waitingBundleProcessingTasks != null)
//            for (Runnable task : waitingBundleProcessingTasks) bundleProcessor.submit(task);
        
        return bundleTransmitter.isAlive();
    }
    
    @Override
    public boolean stop() {
        boolean state = stopExecutors();
        for (Daemon2Managable managable : managables) state &= managable.stop();
        return state;
    }
    
    private static final long WAITING_TIME_IN_SECONDS = 15L;
//    private List<Runnable> waitingBundleProcessingTasks;
//    private List<Runnable> waitingBundleTransmissionTasks;
    
    private boolean stopExecutors() {
        if (bundleTransmitter != null && bundleProcessor != null) {
            
//            bundleTransmitter.shutdown();
            bundleTransmitter.interrupt();
            bundleProcessor.shutdown();
    
//            bundleTransmitter.shutdownNow();
            bundleProcessor.shutdownNow();
            
//            waitingBundleTransmissionTasks = bundleTransmitter.shutdownNow();
//            waitingBundleProcessingTasks = bundleProcessor.shutdownNow();
            
            try {
                return bundleTransmitter.isInterrupted() &&
//                    bundleTransmitter.awaitTermination(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS) &&
                    bundleProcessor.awaitTermination(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                return false;
            }
            
        } else return true;
    }
    
    @Override
    public DTNEndpointID getThisNodezEID() {
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
    
    private static DTNEndpointID makeEID() {
        String eid = Long.toHexString(UUID.randomUUID().getMostSignificantBits());
        //for the first time, do the next line and store it in storage DB.
        return DTNEndpointID.from(DTNEndpointID.DTN_SCHEME, eid.substring(0, 10));
        //the next time, get the EID from storage DB
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
        
            return freqMe2Destination > freqCustodian2Destination;
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
            
            return probMe2Destination > probCustodian2Destination;
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
        // don't delete if currentProtocol == EPIDEMIC
        if (currentProtocol != Daemon2Router.RoutingProtocol.EPIDEMIC) {
            synchronized (DummyStorage.OUTBOUND_BUNDLES_QUEUE) {
                for (DTNBundle bundle : DummyStorage.OUTBOUND_BUNDLES_QUEUE) {
                    if (bundle.primaryBlock.bundleID.equals(bundleID))
                        DummyStorage.OUTBOUND_BUNDLES_QUEUE.remove(bundle);
                }
            }
            synchronized (DummyStorage.INTERMEDIATE_BUNDLES_QUEUE) {
                for (DTNBundle bundle : DummyStorage.INTERMEDIATE_BUNDLES_QUEUE) {
                    if (bundle.primaryBlock.bundleID.equals(bundleID))
                        DummyStorage.INTERMEDIATE_BUNDLES_QUEUE.remove(bundle);
                }
            }
        }
    }
    
    @Override
    public void delete(DTNBundleID bundleID, int fragmentOffset) {
        // don't delete if currentProtocol == EPIDEMIC
        if (currentProtocol != Daemon2Router.RoutingProtocol.EPIDEMIC) {
            synchronized (DummyStorage.OUTBOUND_BUNDLES_QUEUE) {
                for (DTNBundle bundle : DummyStorage.OUTBOUND_BUNDLES_QUEUE) {
                    if (DTNUtils.isFragment(bundle)) {
                        
                        String fragOffsetStr
                            = bundle.primaryBlock.detailsIfFragment
                            .get(PrimaryBlock.FragmentField.FRAGMENT_OFFSET);
                        
                        int fragOffset;
                        if (fragOffsetStr != null) fragOffset = Integer.parseInt(fragOffsetStr);
                        else return;
                        
                        if (bundle.primaryBlock.bundleID.equals(bundleID) &&
                            fragmentOffset == fragOffset)
                            
                            DummyStorage.OUTBOUND_BUNDLES_QUEUE.remove(bundle);
                    }
                }
            }
    
            synchronized (DummyStorage.INTERMEDIATE_BUNDLES_QUEUE) {
                for (DTNBundle bundle : DummyStorage.INTERMEDIATE_BUNDLES_QUEUE) {
                    if (DTNUtils.isFragment(bundle)) {
                
                        String fragOffsetStr
                            = bundle.primaryBlock.detailsIfFragment
                            .get(PrimaryBlock.FragmentField.FRAGMENT_OFFSET);
                
                        int fragOffset;
                        if (fragOffsetStr != null) fragOffset = Integer.parseInt(fragOffsetStr);
                        else return;
                
                        if (bundle.primaryBlock.bundleID.equals(bundleID) &&
                            fragmentOffset == fragOffset)
                    
                            DummyStorage.INTERMEDIATE_BUNDLES_QUEUE.remove(bundle);
                    }
                }
            }
        }
    }
}
