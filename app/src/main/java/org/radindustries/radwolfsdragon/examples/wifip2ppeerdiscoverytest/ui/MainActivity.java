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
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery.PeerDiscovery;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CLAToRouter /*, Daemon*/ {

    private static final int USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 66;
    private static final String LOG_TAG
            = DTNConstants.MAIN_LOG_TAG + "_" + MainActivity.class.getSimpleName();

    private ConvergenceLayerAdapter cla;
    private PeerDiscovery discoverer;

    // TODO EID must be persistent. Use storage
    private static final String bundleNodeEndpointId = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button startBtn = findViewById(R.id.start_service_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverer.init();
            }
        });

        Button sendBtn = findViewById(R.id.send_button);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DTNBundle bundleToSend = generateDummyBundleToSend();
//                Log.i(LOG_TAG, "Bundle to send: " + bundleToSend.data);
//                HashMap<String, String> recipientBundleNodes
//                        = discoverer.getPeerList();
//                Set<String> keySet = recipientBundleNodes.keySet();
//                String[] nodes = keySet.toArray(new String[]{}); // NB: never use "set.toArray()"
//                if (nodes.length > 0)
//                    cla.transmitBundle(bundleToSend, nodes);
                cla.transmitBundle(null, discoverer.getPeerList());
            }
        });

        Button stopBtn = findViewById(R.id.stop_service_button);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverer.cleanUp();
            }
        });

        // request for permissions first
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //explain why you need it
                new AlertDialog.Builder(this)
                        .setTitle("Permission Request")
                        .setMessage("This app needs to know your location" +
                                " to send your DTN text messages")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //request for the permission
                                ActivityCompat.requestPermissions(getParent(),
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE);
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
                //request for the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            getDependencies();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            } else {
                getDependencies();
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
        // use its toString() method when showing in logs
        Log.d(LOG_TAG, "Bundle received: " + bundle.data);
    }

//    private DTNBundle generateDummyBundleToSend() {
//        DTNBundle bundle = new DTNBundle();
//        bundle.data = "Yo, Wassap homie? B-)";
//        return bundle;
//    }

    private void getDependencies() {
        // as the acting router and daemon...
        discoverer = DependencyInjection.getPeerDiscoverer(this);
        discoverer.setThisBundleNodezEndpointId(bundleNodeEndpointId);
        cla = DependencyInjection.getConvergenceLayerAdapter(this);
        cla.setRouter(this);
    }
}
