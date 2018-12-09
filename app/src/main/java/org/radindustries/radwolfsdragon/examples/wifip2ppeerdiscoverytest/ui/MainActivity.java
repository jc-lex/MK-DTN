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

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.DTNConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.DependencyInjection;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.ConvergenceLayerAdapter;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNNode;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery.PeerDiscovery;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CLAToRouter /*, Daemon*/ {

    private static final int USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 66;
    private static final String LOG_TAG
            = DTNConstants.MAIN_LOG_TAG + "_" + MainActivity.class.getSimpleName();

    private ConvergenceLayerAdapter cla;
    private PeerDiscovery discoverer;

    private ArrayList<DTNNode> chosenPeers;
    private DTNBundle bundleToTransmit;
    private Button sendBtn;

    // TODO EID must be persistent. Use storage
    private static final UUID bundleNodeEndpointId = UUID.randomUUID();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bundleToTransmit = requestNextBundle();

        Button startBtn = findViewById(R.id.start_service_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenPeers = new ArrayList<>();
                discoverer.init();
            }
        });

        sendBtn = findViewById(R.id.send_button);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO make routing decision prior to bundle transmission
                chosenPeers.addAll(chooseDTNNodes(discoverer.getPeerList()));

                cla.transmitBundle(
                        bundleToTransmit,
                        chosenPeers.get(0)
                );

                // so that successive button presses do not trigger transmissions,
                sendBtn.setEnabled(false);
            }
        });

        Button stopBtn = findViewById(R.id.stop_service_button);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenPeers = null;
                discoverer.cleanUp();
            }
        });

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
    public void deliverDTNBundle(DTNBundle bundle) {
        // TODO send to daemon for processing
        // use its toString() method when showing in logs
        Log.d(LOG_TAG, "Bundle received: " + bundle.data);
    }

    @Override
    public void notifyBundleForwardingComplete(int bundleNodeCount) {
        // TODO in case of multi-cast, do something to trigger another transmission
        if (bundleNodeCount < chosenPeers.size()) {
            cla.transmitBundle(
                    bundleToTransmit,
                    chosenPeers.get(bundleNodeCount)
            );
        } else {
            sendBtn.setEnabled(true);
        }
    }

    private Set<DTNNode> chooseDTNNodes(Set<DTNNode> nodes) {
        // one random selection, for now
        DTNNode[] nodesArray = nodes.toArray(new DTNNode[]{});
        int randomNumber = (int) (Math.random() * nodesArray.length); // Z : [0, len)

        Set<DTNNode> tmpNodes = new HashSet<>();
        tmpNodes.add(nodesArray[randomNumber]);

        return tmpNodes;
    }

    private DTNBundle requestNextBundle() {
        // TODO get bundle from Daemon
        DTNBundle bundle = new DTNBundle();
        bundle.data = "Yo homie, the time is " + System.currentTimeMillis();
        return bundle;
    }

    private void getDependencies() {
        // as the acting router and daemon...
        discoverer = DependencyInjection.getPeerDiscoverer(this);

        // short numbers for debugging purposes only
        String eid = Long.toHexString(bundleNodeEndpointId.getMostSignificantBits());
        discoverer.setThisBundleNodezEndpointId(eid.substring(0, 7));

        cla = DependencyInjection.getConvergenceLayerAdapter(this);
        cla.setRouter(this);
    }
}
