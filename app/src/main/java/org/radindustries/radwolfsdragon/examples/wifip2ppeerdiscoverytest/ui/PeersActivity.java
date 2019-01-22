package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PeersActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + PeersActivity.class.getSimpleName();
    
    private static ArrayAdapter<String> peersAdapter;
    private static String dtnClientID = "dtn:null";
    
    private static ArrayList<String> dataSet = new ArrayList<>();
    
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
                case MKDTNService.MSG_GET_DTN_CLIENT_ID: setClientID(data); break;
                case MKDTNService.MSG_GET_PEER_LIST: setPeers(data); break;
                default: break;
            }
        }
        
        private void setClientID(Bundle data) {
            dtnClientID = data.getString(MKDTNService.DTN_CLIENT_ID_KEY, "dtn:null");
            Log.i(LOG_TAG, "id = " + dtnClientID);
            
            String myEID = String.format("MY DTN EID: %s", dtnClientID);
            if (dataSet.isEmpty()) dataSet.add(0, myEID);
            else dataSet.set(0, myEID);
            
            peersAdapter.clear();
            peersAdapter.addAll(dataSet);
        }
        
        private void setPeers(Bundle data) {
            String[] peers = data.getStringArray(MKDTNService.PEER_LIST_KEY);
            Log.i(LOG_TAG, "peers = " + Arrays.toString(peers));
            
            dataSet.clear();
            dataSet.add(0, String.format("MY DTN EID: %s", dtnClientID));
            if (peers != null) dataSet.addAll(new ArrayList<>(Arrays.asList(peers)));
            peersAdapter.clear();
            peersAdapter.addAll(dataSet);
        }
    }
    
    private final ServiceConnection serviceConnection = new MKDTNServiceConnection();
    private class MKDTNServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dtnServiceMessenger = new Messenger(service);
            registerWithDTNService();
            
            getClientID();
            getPeers();
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            dtnServiceMessenger = null;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }
    
    private void initUI() {
        ListView peersList = findViewById(R.id.peers_list);
        peersAdapter = new ArrayAdapter<>(
            this, R.layout.peers_list_item, R.id.peer_list_item_textview
        );
        peersList.setAdapter(peersAdapter);
        peersList.setOnItemClickListener(this);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
            TextView peerName = view.findViewById(R.id.peer_list_item_textview);
            String peerEID = peerName.getText().toString();
            
            Intent intent = new Intent(this, NewMessageActivity.class);
            intent.putExtra(TO_EID_TAG, peerEID);
            startActivity(intent);
        }
    }
    
    static final String TO_EID_TAG = "to_eid";
    
    @Override
    protected void onStart() {
        super.onStart();
        initUI();
        bind();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        unbind();
    }
    
    private void bind() {
        if (MKDTNService.isRunning(this)) {
            Intent intent = new Intent(this, MKDTNService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }
    
    private void unbind() {
        if (MKDTNService.isRunning(this)) {
            unregisterWithDTNService();
            unbindService(serviceConnection);
        }
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
    
    private void sendMessageToDTNService(Message msg) {
        if(dtnServiceMessenger != null) {
            try {
                dtnServiceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "Service down", e);
            }
        }
    }
}
