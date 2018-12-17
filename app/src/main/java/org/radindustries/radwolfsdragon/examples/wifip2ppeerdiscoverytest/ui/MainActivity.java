package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.DependencyInjection;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.ConvergenceLayerAdapter;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AgeBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PayloadADU;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery.PeerDiscovery;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CLAToRouter /*, Daemon*/ {

    private static final int USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 66;
    private static final String LOG_TAG
            = DConstants.MAIN_LOG_TAG + "_" + MainActivity.class.getSimpleName();

    private ConvergenceLayerAdapter cla;
    private PeerDiscovery discoverer;
    
    // TODO EID must be persistent. Use storage
    private DTNEndpointID bundleNodeEndpointId;
    private ArrayList<DTNBundleNode> chosenPeers;
    private HashSet<DTNBundleID> deliveredFragments;
    private DTNBundle bundleToTransmit;
    private Button sendBtn;
    
    private static final long TEST_CPU_SPEED = 2_000_000L;
    private static final String TEST_SHORT_TEXT_MESSAGE = "William + Phoebe = <3";
    private static final int TEST_FRAGMENT_OFFSET = 0;
    private static final int TEST_FRAGMENT_LENGTH = TEST_SHORT_TEXT_MESSAGE.length();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        bundleToTransmit = requestNextBundle();
        chosenPeers = new ArrayList<>();
        deliveredFragments = new HashSet<>();

        // requestForPermissions for permissions first
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getDependencies();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //explain why you need it
                new AlertDialog.Builder(this)
                        .setTitle("Permission Request")
                        .setMessage("This app needs to know your location" +
                                " to send your DTN data")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestForPermissions();
                            }
                        })
                        .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                requestForPermissions();
            }
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        initUI();
    }
    
    private void initUI() {
        Button startBtn = findViewById(R.id.start_service_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverer.init();
            }
        });
    
        sendBtn = findViewById(R.id.send_button);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO make routing decision prior to bundle transmission
                chosenPeers.addAll(chooseDTNNodes(discoverer.getPeerList()));
    
                // TODO make sure to fragment this bundle first before sending
                cla.transmit(bundleToTransmit, setDestination(0));
                
                // so that successive button presses do not trigger transmissions,
                sendBtn.setEnabled(false);
            }
        });
    
        Button stopBtn = findViewById(R.id.stop_service_button);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenPeers.clear();
                deliveredFragments.clear();
                discoverer.cleanUp();
            }
        });
    }
    
    private DTNBundleNode setDestination(int theChosenOne) {
        DTNBundleNode destination = chosenPeers.get(theChosenOne);
        bundleToTransmit.primaryBlock.destinationEID = destination.dtnEndpointID;
        return destination;
    }
    
    private void requestForPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDependencies();
            } else {
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void deliver(DTNBundle bundle) {
        // TODO send to daemon for processing
        // TODO do asynch task here
        Log.d(LOG_TAG, "Bundle received:\n" + bundle);
        
        collectFragmentBundleID(bundle);
    }
    
    private void collectFragmentBundleID(DTNBundle deliveredBundle) {
        // this set of fragment IDs is collected by the Daemon, not router
        if (deliveredBundle.primaryBlock.destinationEID.equals(bundleNodeEndpointId)) {
            if (deliveredBundle.primaryBlock.bundleProcessingControlFlags
                .testBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT)) {
                DTNBundleID fragmentID = deliveredBundle.primaryBlock.bundleID;
                deliveredFragments.add(fragmentID); //step 1
                Log.i(LOG_TAG, "Delivered fragment: " + fragmentID);
                Log.d(LOG_TAG, "Accumulated fragments: " + deliveredFragments);
                 /*
                 2. daemon will store the "deliveredBundle" in the delivered queue in storage.
                 3. daemon will then use the "deliveredFragments" set to query the delivered queue
                 for all fragments having a common bundle ID.
                 4. for each set of related fragments (fragments with a common bundleID),
                 daemon will call the fragment manager to check for their defragmentability.
                 5. if defragmentable, daemon will tell fragment manager to defragment them.
                 6. the returned bundle is immediately sent to the user via App AA.
                 
                 This means the delivered queue in storage will only contain fragments
                 were this node is the destination.
                 */
            } else {
                // daemon sends delivered bundle to app aa immediately.
                Log.i(LOG_TAG, "Bundle not a fragment. Sending to App AA...");
            }
        }
    }

    @Override
    public void onBundleForwardingCompleted(int bundleNodeCount) {
         /*NOTE in case of Nearby multi-cast (P2P_CLUSTER),
         do something to trigger another transmission.*/
        if (bundleNodeCount < chosenPeers.size()) {
            cla.transmit(bundleToTransmit, setDestination(bundleNodeCount));
        } else {
            sendBtn.setEnabled(true);
        }
    }

    private Set<DTNBundleNode> chooseDTNNodes(Set<DTNBundleNode> nodes) {
        // NOTE do one random selection, for now
        DTNBundleNode[] nodesArray = nodes.toArray(new DTNBundleNode[]{});
        int randomNumber = (int) (Math.random() * nodesArray.length); // Z : [0, len)

        Set<DTNBundleNode> tmpNodes = new HashSet<>();
        tmpNodes.add(nodesArray[randomNumber]);

        return tmpNodes;
    }

    private DTNBundle requestNextBundle() {
        // TODO get bundle from Daemon
        return createTestUserBundle(TEST_SHORT_TEXT_MESSAGE.getBytes());
    }
    
    private DTNBundle createTestUserBundle(byte[] message) {
        // NOTE user bundles are made by the App AA
        
        PrimaryBlock primaryBlock = makePrimaryBlockForUserBundle();
        
        
        CanonicalBlock ageCBlock = makeAgeCBlock(primaryBlock.bundleID.creationTimestamp);
        
        
        CanonicalBlock payloadCBlock = new CanonicalBlock();
        payloadCBlock.blockTypeSpecificDataFields = makePayloadForUserBundle(message);
        payloadCBlock.blockType = CanonicalBlock.BlockType.PAYLOAD;
        payloadCBlock.blockProcessingControlFlags = BigInteger.ZERO.setBit(
            CanonicalBlock.BlockPCF.BLOCK_MUST_BE_REPLICATED_IN_ALL_FRAGMENTS
        );
        
        
        DTNBundle userBundle = new DTNBundle();
        userBundle.primaryBlock = primaryBlock;
        userBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.PAYLOAD, payloadCBlock);
        userBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.AGE, ageCBlock);
        
        return userBundle;
    }
    
    private PayloadADU makePayloadForUserBundle(byte[] message) {
        
        PayloadADU payload = new PayloadADU();
        payload.ADU = Arrays.copyOf(message, message.length);
        
        return payload;
    }
    
    private CanonicalBlock makeAgeCBlock(long creationTimestamp) {
        CanonicalBlock ageCBlock = new CanonicalBlock();
        
        ageCBlock.blockTypeSpecificDataFields
            = makeAgeBlockForBundle(creationTimestamp);
        ageCBlock.blockType = CanonicalBlock.BlockType.AGE;
        ageCBlock.blockProcessingControlFlags = generateBlockPCFsForAgeBlock();
        
        return ageCBlock;
    }
    
    private AgeBlock makeAgeBlockForBundle(long bundleCreationTimestamp) {
        // at the source or sender,
        AgeBlock ageBlock = new AgeBlock();
        
        ageBlock.sourceCPUSpeedInKHz = TEST_CPU_SPEED;
//        try {
//            ageBlock.sourceCPUSpeedInKHz = SystemUtils.getMaxCPUFrequencyInKHz();
//        } catch (Exception e) {
//            Log.e(LOG_TAG, "Could not get "
//                + bundleNodeEndpointId + "\'s MAX clock speed. Assuming default...", e);
////            finish();
//            // we can't do anything without this clock speed. for now, lets assume a default.
//            ageBlock.sourceCPUSpeedInKHz = TEST_CPU_SPEED;
//        }
    
        /*because ppl live in different timezones, there is need to have them all use
        * a common time reference (timezone). Therefore we use the standard UTC time for
        * everyone. This simplifies the process for determining the bundle's age.*/
        ageBlock.sendingTimestamp = System.currentTimeMillis();
    
        ageBlock.age = ageBlock.sendingTimestamp - bundleCreationTimestamp;
        ageBlock.agePrime = 0;
        ageBlock.T = bundleCreationTimestamp;
        
        /*
        at the receiver,
        ageBlock.agePrime = ageBlock.age.plus(TA); // where TA -> transmission age
        ageBlock.T = bundleCreationTimestamp.plus(ageBlock.agePrime);

         when we are gonna forward the bundle or at any other time
        ageBlock.sendingTimestamp = Instant.now();
        ageBlock.age = ageBlock.agePrime.plus(
            Duration.between(ageBlock.T, ageBlock.sendingTimestamp)
        );
        */
        
        return ageBlock;
    }
    
    private BigInteger generateBlockPCFsForAgeBlock() {
        
        return BigInteger.ZERO
            .setBit(CanonicalBlock.BlockPCF.BLOCK_MUST_BE_REPLICATED_IN_ALL_FRAGMENTS)
            .setBit(CanonicalBlock.BlockPCF.TRANSMIT_STATUS_REPORT_IF_BLOCK_CANNOT_BE_PROCESSED)
            .setBit(CanonicalBlock.BlockPCF.DELETE_BUNDLE_IF_BLOCK_CANNOT_BE_PROCESSED)
            .setBit(CanonicalBlock.BlockPCF.LAST_BLOCK)
            .setBit(CanonicalBlock.BlockPCF.DISCARD_BLOCK_IF_IT_CANNOT_BE_PROCESSED);
    }
    
    private PrimaryBlock makePrimaryBlockForUserBundle() {
        PrimaryBlock primaryBlock = new PrimaryBlock();
        
        primaryBlock.bundleProcessingControlFlags
            = generateBundlePCFsForUserBundle();
        primaryBlock.priorityClass = PrimaryBlock.PriorityClass.NORMAL;
        primaryBlock.bundleID
            = DTNBundleID.from(bundleNodeEndpointId, System.currentTimeMillis());
        primaryBlock.lifeTime = PrimaryBlock.LifeTime.THREE_DAYS.getPeriod();
        primaryBlock.custodianEID = DTNEndpointID.from(primaryBlock.bundleID.sourceEID);
        primaryBlock.reportToEID = DTNEndpointID.from(primaryBlock.bundleID.sourceEID);
        
        primaryBlock.detailsIfFragment = new HashMap<>();
        if (primaryBlock.bundleProcessingControlFlags
            .testBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT)) {
            primaryBlock.detailsIfFragment.put(
                PrimaryBlock.FragmentField.FRAGMENT_OFFSET,
                Integer.toString(TEST_FRAGMENT_OFFSET)
            );
            primaryBlock.detailsIfFragment.put(
                PrimaryBlock.FragmentField.TOTAL_ADU_LENGTH,
                Integer.toString(TEST_FRAGMENT_LENGTH)
            );
        }
        
        return primaryBlock;
    }
    
    private BigInteger generateBundlePCFsForUserBundle() {
        
        return BigInteger.ZERO
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT)
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_CUSTODY_TRANSFER_REQUESTED)
            .setBit(PrimaryBlock.BundlePCF.DESTINATION_ENDPOINT_IS_A_SINGLETON)
             /*request for a SINGLE bundle delivery report a.k.a return-receipt
             (from destination only)
             here, report-to dtnEndpointID == source dtnEndpointID*/
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_DELIVERY_REPORT_REQUESTED);
    }
    
    private void getDependencies() {
        // as the acting router and daemon...
        discoverer = DependencyInjection.getPeerDiscoverer(this);

        // short numbers for debugging purposes only
        String eid = Long.toHexString(UUID.randomUUID().getMostSignificantBits());
        //for the first time, do the next line and store it in storage DB.
        bundleNodeEndpointId = DTNEndpointID.from(DTNEndpointID.DTN_SCHEME, eid.substring(0, 8));
        //the next time, get the EID from storage DB
        
        discoverer.setThisBundleNodezEndpointId(bundleNodeEndpointId);

        cla = DependencyInjection.getConvergenceLayerAdapter(this);
        cla.setRouter(this);
    }
}
