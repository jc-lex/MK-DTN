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
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNTextMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private static final int USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 66;
    private static final String LOG_TAG
            = DConstants.MAIN_LOG_TAG + "_" + MainActivity.class.getSimpleName();
    
    private static String dtnClientID;
    private static String[] peers;
    private static List<DTNTextMessage> messages;
    
    private String lifeTimeFromSettings;
    private String routingProtocolFromSettings;
    private String priorityClassFromSettings;
    
    private Messenger dtnServiceMessenger = null;
    private static int regNum;
    private final Messenger ourMessenger = new Messenger(new MKDTNServiceMessageHandler());
    private static class MKDTNServiceMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            
            switch (msg.what) {
                case MKDTNService.MSG_REGISTRATION_NUMBER: regNum = msg.arg1; break;
                case MKDTNService.MSG_GET_DTN_CLIENT_ID:
                    dtnClientID
                        = data.getString(MKDTNService.DTN_CLIENT_ID_KEY, "dtn:null");
                    Log.i(LOG_TAG, "id = " + dtnClientID);
                    // TODO update UI
                    break;
                case MKDTNService.MSG_GET_PEER_LIST:
                    peers = data.getStringArray(MKDTNService.PEER_LIST_KEY);
                    Log.i(LOG_TAG, "peers = " + Arrays.toString(peers));
                    // TODO notify peers data set changed
                    break;
                case MKDTNService.MSG_GET_RECEIVED_DTN_MESSAGES:
                    messages = (ArrayList<DTNTextMessage>)
                        data.getSerializable(MKDTNService.MESSAGES_KEY);
                    Log.i(LOG_TAG, "messages = " + messages);
                    // TODO notify messages data set changed
                    break;
                default:
                    break;
            }
        }
    }
    
    private final ServiceConnection serviceConnection = new MKDTNServiceConnection();
    private class MKDTNServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dtnServiceMessenger = new Messenger(service);
            registerWithDTNService();
        }
    
        @Override
        public void onServiceDisconnected(ComponentName name) {
            dtnServiceMessenger = null;
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

        initUI();
        
        getPermissions();
    }
    
    private void initUI() {
        Button sendBtn = findViewById(R.id.send_button);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (peers == null || peers.length == 0) getPeers();
                else sendDTNMessage(peers[0]);
                
                getClientID();
                getDTNMessages();
            }
        });
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
    protected void onStart() {
        super.onStart();
        if (MKDTNService.isRunning(this)) bind();
    }
    
    private void bind() {
        Intent intent = new Intent(this, MKDTNService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void unbind() {
        unregisterWithDTNService();
        unbindService(serviceConnection);
    }
    
    private void registerWithDTNService() {
        Message registerMe = Message.obtain(null, MKDTNService.MSG_REGISTER_CLIENT);
        registerMe.replyTo = ourMessenger;
        sendMessageToDTNService(registerMe);
    }
    
    private void unregisterWithDTNService() {
        Message unregisterMe
            = Message.obtain(null, MKDTNService.MSG_UNREGISTER_CLIENT, regNum, 0);
        sendMessageToDTNService(unregisterMe);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (MKDTNService.isRunning(this)) unbind();
    }
    
    private void getPeers() {
        Message peerRequest
            = Message.obtain(null, MKDTNService.MSG_GET_PEER_LIST, regNum, 0);
        sendMessageToDTNService(peerRequest);
    }
    
    private void getClientID() {
        Message clientIDRequest
            = Message.obtain(null, MKDTNService.MSG_GET_DTN_CLIENT_ID, regNum, 0);
        sendMessageToDTNService(clientIDRequest);
    }
    
    private void getDTNMessages() {
        Message getMessagesRequest
            = Message.obtain(null, MKDTNService.MSG_GET_RECEIVED_DTN_MESSAGES, regNum, 0);
        sendMessageToDTNService(getMessagesRequest);
    }
    
    private void sendDTNMessage(String destination) {
        getDTNSettings();
        
        Message msg = Message.obtain(null, MKDTNService.MSG_SEND);
        
        Bundle data = new Bundle();
        data.putString(MKDTNService.RECIPIENT_KEY, destination);
        data.putString(MKDTNService.TEXT_KEY, getString(R.string.mkdtn_hello_message));
        data.putString(MKDTNService.LIFETIME_KEY, lifeTimeFromSettings);
        data.putString(MKDTNService.PRIORITY_KEY, priorityClassFromSettings);
        data.putString(MKDTNService.PROTOCOL_KEY, routingProtocolFromSettings);
        
        msg.setData(data);
        
        sendMessageToDTNService(msg);
    }
    
    private void sendMessageToDTNService(Message msg) {
        if(dtnServiceMessenger != null) {
            try {
                dtnServiceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "Service down", e);
            }
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_manager:
                startActivity(new Intent(this, ManagerActivity.class));
                break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }
}
