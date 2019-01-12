package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;

import java.util.Arrays;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private static final int USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 66;
    private static final String LOG_TAG
            = DConstants.MAIN_LOG_TAG + "_" + MainActivity.class.getSimpleName();
    
    private static String dtnClientID;
    private static String[] peers;
    private static HashMap<String, String> messages;
    
    private String lifeTimeFromSettings;
    private String routingProtocolFromSettings;
    private String priorityClassFromSettings;
    
    private Messenger dtnServiceMessenger = null;
    private final Messenger ourMessenger = new Messenger(new ServiceMessageHandler());
    private static int regNum;
    
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dtnServiceMessenger = new Messenger(service);
//            Log.i(LOG_TAG, "dtnServiceMessenger = " + dtnServiceMessenger);
            bound = true;
            Log.i(LOG_TAG, "started = " + started + ", bound = " + bound);
            
            try {
                Message registrationRequest
                    = Message.obtain(null, MKDTNService.MSG_REGISTER_CLIENT);
                registrationRequest.replyTo = ourMessenger;
                dtnServiceMessenger.send(registrationRequest);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    
        @Override
        public void onServiceDisconnected(ComponentName name) {
            dtnServiceMessenger = null;
            Log.i(LOG_TAG, "dtnServiceMessenger = null");
            bound = false;
            Log.i(LOG_TAG, "started = " + started + ", bound = " + bound);
        }
    };
    
    private static class ServiceMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            
            switch (msg.what) {
                case MKDTNService.MSG_REGISTRATION_NUMBER:
                    regNum = msg.arg1;
                    break;
                case MKDTNService.MSG_GET_DTN_CLIENT_ID:
                    dtnClientID
                        = data.getString(MKDTNService.DTN_CLIENT_ID_KEY, "dtn:null");
                    Log.i(LOG_TAG, "MY ID: " + dtnClientID);
                    // TODO update UI
                    break;
                case MKDTNService.MSG_GET_PEER_LIST:
                    peers = data.getStringArray(MKDTNService.PEER_LIST_KEY);
                    Log.i(LOG_TAG, "peers = " + Arrays.toString(peers));
                    // TODO notify data set changed
                    break;
                case MKDTNService.MSG_GET_RECEIVED_DTN_MESSAGES:
                    messages
                        = (HashMap<String, String>) data.getSerializable(MKDTNService.MESSAGES_KEY);
                    Log.i(LOG_TAG, "messages = " + messages);
                    // TODO notify data set changed
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // set defaults only when app opens for the very first time
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//        getDTNSettings();

        initUI();
        
        getPermissions();
    }
    
    private void getPermissions() {
        // requestForPermissions for permissions first
        if (!(ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
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
                            handleRejection();
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
    protected void onResume() {
        super.onResume();
        NotificationManagerCompat.from(this)
            .cancel(MKDTNService.MKDTN_NOTIFICATION_TAG, MKDTNService.MKDTN_NOTIFICATION_ID);
        MKDTNService.myNotificationStyle = new NotificationCompat.InboxStyle();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        if (started && !bound) bind();
//        Log.i(LOG_TAG, "started = " + started + ", bound = " + bound);
    }
    
    private void bind() {
        Intent intent = new Intent(this, MKDTNService.class);
        bound = bindService(intent, serviceConnection, Context.BIND_IMPORTANT);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (started && bound) {
            unbindService(serviceConnection);
            bound = false;
        }
        Log.i(LOG_TAG, "started = " + started + ", bound = " + bound);
    }
    
    private void initUI() {
        Button sendBtn = findViewById(R.id.send_button);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPeers();
                if (peers == null || peers.length == 0) {
                    Log.i(LOG_TAG, "No peers");
                    return; // precaution
                }
                sendMessage(peers[0]);
            }
        });
    }
    
    private void getPeers() {
        Message peerRequest
            = Message.obtain(null, MKDTNService.MSG_GET_PEER_LIST, regNum, 0);
        if (dtnServiceMessenger != null) {
            try {
                dtnServiceMessenger.send(peerRequest);
                Thread.sleep(1000);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "Service down", e);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "waiting for peer list interrupted");
            }
        } else Log.i(LOG_TAG, "dtn messenger = null");
    }
    
    private void sendMessage(String destination) {
        getDTNSettings();
        
        Message msg = Message.obtain(null, MKDTNService.MSG_SEND);
        
        Bundle data = new Bundle();
        data.putString(MKDTNService.RECIPIENT_KEY, destination);
        data.putString(MKDTNService.TEXT_KEY, getString(R.string.mkdtn_hello_message));
        data.putString(MKDTNService.LIFETIME_KEY, lifeTimeFromSettings);
        data.putString(MKDTNService.PRIORITY_KEY, priorityClassFromSettings);
        data.putString(MKDTNService.PROTOCOL_KEY, routingProtocolFromSettings);
        
        msg.setData(data);
        
        if(dtnServiceMessenger != null) {
            try {
                dtnServiceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "Service down", e);
            }
        } else Log.i(LOG_TAG, "dtn messenger = null");
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
    
    private void getDTNSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    
        lifeTimeFromSettings = prefs.getString(
            getString(R.string.pref_lifetime_key),
            getString(R.string.pref_default_lifetime)
        );
    
        routingProtocolFromSettings = prefs.getString(
            getString(R.string.pref_routing_protocol_key),
            getString(R.string.pref_default_routing_protocol)
        );
        
        priorityClassFromSettings = prefs.getString(
            getString(R.string.pref_priority_class_key),
            getString(R.string.pref_default_priority_class)
        );
    }
    
    @Override
    public void onRequestPermissionsResult(
        int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                handleRejection();
            }
        }
    }
    
    private void handleRejection() {
        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        finish();
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
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.action_start_dtn) {
            if (!(started || bound)) {
                startService(new Intent(this, MKDTNService.class));
                started = true;
                bind();
            }
            Log.i(LOG_TAG, "started = " + started + ", bound = " + bound);
        } else if (id == R.id.action_stop_dtn) {
            if (started && bound) {
                unbindService(serviceConnection);
                bound = false;
                started = !stopService(new Intent(this, MKDTNService.class));
            }
            Log.i(LOG_TAG, "started = " + started + ", bound = " + bound);
        }
        return super.onOptionsItemSelected(item);
    }
    
    private static boolean started = false;
    private static boolean bound = false;
}
