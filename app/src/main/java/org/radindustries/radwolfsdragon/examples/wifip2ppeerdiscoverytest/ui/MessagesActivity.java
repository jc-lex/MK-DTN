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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNTextMessage;

import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MessagesActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + MessagesActivity.class.getSimpleName();
    
    private static ArrayAdapter<String> senderAdapter;
    private static ArrayList<DTNTextMessage> messages = new ArrayList<>();
    
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
                case MKDTNService.MSG_GET_RECEIVED_DTN_MESSAGES: setMessages(data); break;
                default: break;
            }
        }
        
        private void setMessages(Bundle data) {
            messages = (ArrayList<DTNTextMessage>) data.getSerializable(MKDTNService.MESSAGES_KEY);
            Log.i(LOG_TAG, "messages = " + messages);
            
            ArrayList<String> senderDisplayInfo = new ArrayList<>();
            senderAdapter.clear();
            if (messages != null)
                for (DTNTextMessage msg : messages)
                    senderDisplayInfo.add("From " + msg.sender + " @ " + msg.deliveryTimestamp);
            senderAdapter.addAll(senderDisplayInfo);
        }
    }
    
    private final ServiceConnection serviceConnection = new MKDTNServiceConnection();
    private class MKDTNServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dtnServiceMessenger = new Messenger(service);
            registerWithDTNService();
            
            getDTNMessages();
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            dtnServiceMessenger = null;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }
    
    private void initUI() {
        ListView messagesList = findViewById(R.id.message_list);
        senderAdapter = new ArrayAdapter<>(
            this, R.layout.messages_list_item, R.id.message_list_item_textview
        );
        messagesList.setAdapter(senderAdapter);
        messagesList.setOnItemClickListener(this);
        
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, NewMessageActivity.class));
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DTNTextMessage msg = messages.get(position);
        
        Intent intent = new Intent(this, ViewMessageActivity.class);
        intent.putExtra(DTN_MESSAGE_TAG, msg);
        startActivity(intent);
    }
    
    static final String DTN_MESSAGE_TAG = "dtn_msg_tag";
    
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
    
    private void getDTNMessages() {
        Message getMessagesRequest
            = Message.obtain(null, MKDTNService.MSG_GET_RECEIVED_DTN_MESSAGES, regNum, 0);
        sendMessageToDTNService(getMessagesRequest);
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
