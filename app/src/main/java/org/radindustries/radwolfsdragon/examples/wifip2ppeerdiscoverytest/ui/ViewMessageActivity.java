package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNTextMessage;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ViewMessageActivity extends AppCompatActivity implements View.OnClickListener {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        
        initUI();
    }
    
    private String senderEID;
    
    private void initUI() {
        TextView senderTV = findViewById(R.id.sender_eid_textview);
        TextView messageTV = findViewById(R.id.message_textview);
        TextView createdTSTV = findViewById(R.id.created_timestamp_tv);
        TextView deliveredTSTV = findViewById(R.id.delivered_timestamp_tv);
        TextView sentTSTV = findViewById(R.id.sent_timestamp_tv);
        TextView recvdTSTV = findViewById(R.id.recvd_timestamp_tv);
        
        if (getIntent() != null) {
            Intent intent = getIntent();
            if (intent.getSerializableExtra(MessagesActivity.DTN_MESSAGE_TAG) != null) {
                DTNTextMessage msg = (DTNTextMessage)
                    intent.getSerializableExtra(MessagesActivity.DTN_MESSAGE_TAG);
                
                senderEID = msg.sender;
                senderTV.setText(senderEID);
                createdTSTV.setText(msg.creationTimestamp);
                deliveredTSTV.setText(msg.deliveryTimestamp);
                sentTSTV.setText(msg.sendingTimestamp);
                recvdTSTV.setText(msg.receivedTimestamp);
                messageTV.setText(msg.textMessage);
            }
        }
        
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }
    
    
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, NewMessageActivity.class);
        intent.putExtra(PeersActivity.TO_EID_TAG, senderEID);
        startActivity(intent);
    }
}
