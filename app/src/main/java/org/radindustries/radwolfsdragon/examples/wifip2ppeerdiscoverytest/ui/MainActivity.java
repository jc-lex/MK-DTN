package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.DTNConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.DependencyInjection;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.ConvergenceLayerAdapter;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscovery.PeerDiscovery;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.CLAToRouter;

import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CLAToRouter /*, Daemon*/ {

    private static final int USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 66;
    private static final String LOG_TAG
            = DTNConstants.MAIN_LOG_TAG + MainActivity.class.getSimpleName();

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
                DTNBundle bundleToSend = generateDummyBundleToSend();
                HashMap<String, String> recepientBundleNodes
                        = discoverer.getDiscoveredBundleNodes();
                String[] nodesAsArray = (String[]) recepientBundleNodes.keySet().toArray();
                if (nodesAsArray != null && nodesAsArray.length > 0) { // the first node discovered
                    cla.transmitBundle(bundleToSend, nodesAsArray[0]);
                    Log.i(LOG_TAG, "Sending message to " + nodesAsArray[0]);
                }
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
                Toast.makeText(this, "This app needs this permission to act " +
                        "as a DTN node to send your text message.", Toast.LENGTH_LONG).show();

                //request for the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void deliverDTNBundle(DTNBundle bundle) {
        // use its toString() method when showing in logs
        Log.d(LOG_TAG, "Received bundle: " + bundle.data);
    }

    private DTNBundle generateDummyBundleToSend() {
        // TODO ALOOOOTT of code here... Ayi maama
        DTNBundle bundle = new DTNBundle();
        bundle.data = "Yo, name's " + bundleNodeEndpointId + ". Wassap? B-)";
        return bundle;
    }

    private void getDependencies() {
        // as the acting router and daemon...
        discoverer = DependencyInjection.getPeerDiscoverer(this);
        discoverer.setThisBundleNodezEndpointId(bundleNodeEndpointId);
        cla = DependencyInjection.getConvergenceLayerAdapter(this);
        cla.setRouter(this);
    }
}
