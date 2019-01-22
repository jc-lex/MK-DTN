package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

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
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.fragmentmanager.Daemon2FragmentManager;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.Daemon2Managable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscoverer.Daemon2PeerDiscoverer;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2NECTARRoutingTable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2PRoPHETRoutingTable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;

import java.util.ArrayList;
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
    private ExecutorService bundleTransmitter;
    private ExecutorService bundleProcessor;
    
    RadDaemon() {
        this.deliveredMessages = new ArrayList<>();
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
    
        // TODO fragment & store bundle(s)
        
        bundleTransmitter.submit(new TransmitOutboundBundleTask(bundle));
        
//        // proactive fragmentation
//        DTNBundle[] fragments = fragmentManager.fragment(bundle);
//
//        for (DTNBundle fragment : fragments)
//            bundleTransmitter.submit(new TransmitOutboundBundleTask(fragment));
    }
    
    private class TransmitOutboundBundleTask implements Runnable {
        private DTNBundle bundle;
        private Set<DTNBundleNode> nextHops;
    
        private TransmitOutboundBundleTask(DTNBundle bundle) {
            this.bundle = bundle;
            nextHops = new HashSet<>();
        }
    
        @Override
        public void run() {
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
            if (deliveredMessages.add(bundle)) appAA.deliver(bundle);
        }
    }
    
    // temporary storage, for both payloadADUs and status reports
    private List<DTNBundle> deliveredMessages;
    
    @Override
    public List<DTNBundle> getDeliveredMessages() {
        // TODO get them from storage, sort by received timestamp DESC
        return Collections.unmodifiableList(deliveredMessages);
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
        bundleTransmitter = Executors.newSingleThreadExecutor();
//        if (waitingBundleTransmissionTasks != null)
//            for (Runnable task : waitingBundleTransmissionTasks) bundleTransmitter.submit(task);
        
        bundleProcessor = Executors.newSingleThreadExecutor();
//        if (waitingBundleProcessingTasks != null)
//            for (Runnable task : waitingBundleProcessingTasks) bundleProcessor.submit(task);
        
        return true;
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
            
            bundleTransmitter.shutdown();
            bundleProcessor.shutdown();
    
            bundleTransmitter.shutdownNow();
            bundleProcessor.shutdownNow();
            
//            waitingBundleTransmissionTasks = bundleTransmitter.shutdownNow();
//            waitingBundleProcessingTasks = bundleProcessor.shutdownNow();
            
            try {
                return
                    bundleTransmitter.awaitTermination(WAITING_TIME_IN_SECONDS, TimeUnit.SECONDS) &&
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
    
    private static DTNEndpointID makeEID() {
        // short numbers for debugging purposes only
        String eid = Long.toHexString(UUID.randomUUID().getMostSignificantBits());
        //for the first time, do the next line and store it in storage DB.
        return DTNEndpointID.from(DTNEndpointID.DTN_SCHEME, eid.substring(0, 8));
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
    public void delete(DTNBundleID bundleID) {
        // don't deleteIndex if currentProtocol == EPIDEMIC
    }
    
    @Override
    public void delete(DTNBundleID bundleID, int fragmentOffset) {
        // don't deleteIndex if currentProtocol == EPIDEMIC
    }
}
