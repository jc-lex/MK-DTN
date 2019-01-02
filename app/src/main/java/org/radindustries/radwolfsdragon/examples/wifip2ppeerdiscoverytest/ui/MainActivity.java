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
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.BWDTN;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNClient;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNUI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.DTNManager;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements DTNUI {

    private static final int USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 66;
    private static final String LOG_TAG
            = DConstants.MAIN_LOG_TAG + "_" + MainActivity.class.getSimpleName();
    
    private static final String TEST_SHORT_TEXT_MESSAGE = "William + Phoebe = <3";
    
    private DTNClient dtnClient;
    private DTNManager dtnManager;
    private String[] peers;
    private String dtnClientID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initUI();
        
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
    
    private void initUI() {
        Button startBtn = findViewById(R.id.start_service_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dtnManager.start();
                Log.i(LOG_TAG, "MY ID: " + dtnClientID);
            }
        });
    
        Button sendBtn = findViewById(R.id.send_button);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (peers.length > 0) {
                    // using defaults & sending to the first one 4 now
                    dtnClient.send(TEST_SHORT_TEXT_MESSAGE.getBytes(), peers[0]);
                }
            }
        });
    
        Button stopBtn = findViewById(R.id.stop_service_button);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dtnManager.stop();
            }
        });
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
    
    private void getDependencies() {
        // should always be called first before anything BWDTN-related
        BWDTN.init(this, this);
        
        dtnClient = BWDTN.getDTNClient();
        dtnManager = BWDTN.getDTNManager();
        
        assert dtnClient != null;
        dtnClientID = dtnClient.getID();
        
        peers = new String[0];
    }
    
    @Override
    public void onReceiveDTNMessage(byte[] message, String sender) {
        String text = new String(message);
        Log.i(LOG_TAG, "Message from " + sender + " :- " + text);
    }
    
    @Override
    public void onOutboundBundleReceived(String recipient) {
        Log.i(LOG_TAG, recipient + " received our message.");
    }
    
    @Override
    public void onPeerListChanged(String[] peerList) {
        peers = peerList;
        Log.i(LOG_TAG, "Peers: " + Arrays.toString(peerList));
        // TODO notify UI of change in list
    }
    
    @Override
    public void onRequestPermissionsResult(
        int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults
    ) {
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
}
