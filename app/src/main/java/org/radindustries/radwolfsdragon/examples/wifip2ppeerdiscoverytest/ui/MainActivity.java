package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.BuildConfig;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.BWDTN;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNClient;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNUI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.DTNManager;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements DTNUI {

    private static final int USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 66;
    private static final String LOG_TAG
            = DConstants.MAIN_LOG_TAG + "_" + MainActivity.class.getSimpleName();
    
    private String[] peers;
    private DTNClient dtnClient;
    private DTNManager dtnManager;
    private String dtnClientID;
    private PrimaryBlock.LifeTime lifeTimeFromSettings;
    private Daemon2Router.RoutingProtocol routingProtocolFromSettings;
    private PrimaryBlock.PriorityClass priorityClassFromSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // set defaults only when app opens for the very first time
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        getDTNSettings();

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
    
    @Override
    protected void onResume() {
        super.onResume();
        if (IDs != null) {
            for (Integer id : IDs) {
                NotificationManagerCompat.from(this).cancel(MKDTN_NOTIFICATION_TAG, id);
            }
        }
        IDs = new ArrayList<>();
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
                peers = dtnClient.getPeerList();
                Log.i(LOG_TAG, "peers = " + Arrays.toString(peers));
                if (peers != null && peers.length > 0) {
                    getDTNSettings();
                    
                    // sending to the first one 4 now
                    dtnClient.send(
                        getString(R.string.mkdtn_hello_message).getBytes(),
                        peers[0],
                        priorityClassFromSettings,
                        lifeTimeFromSettings,
                        routingProtocolFromSettings
                    );
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
    }
    
    private void getDTNSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        String lifetime = prefs.getString(
            getString(R.string.pref_lifetime_key),
            getString(R.string.pref_default_lifetime)
        );
        lifeTimeFromSettings = PrimaryBlock.LifeTime.valueOf(lifetime);
        
        String routingProtocol = prefs.getString(
            getString(R.string.pref_routing_protocol_key),
            getString(R.string.pref_default_routing_protocol)
        );
        routingProtocolFromSettings = Daemon2Router.RoutingProtocol.valueOf(routingProtocol);
        
        String priorityClass = prefs.getString(
            getString(R.string.pref_priority_class_key),
            getString(R.string.pref_default_priority_class)
        );
        priorityClassFromSettings = PrimaryBlock.PriorityClass.valueOf(priorityClass);
    }
    
    @Override
    public void onReceiveDTNMessage(byte[] message, String sender) {
        String text = new String(message);
        Log.i(LOG_TAG, "Message from " + sender + " => " + text);
        
        notifyUser(sender, text);
    }
    
    @Override
    public void onOutboundBundleReceived(String recipient) {
        Log.i(LOG_TAG, recipient + " received our message.");
        
        notifyUser(
            getString(R.string.delivery_report_title),
            String.format(getString(R.string.delivery_report_message), recipient)
        );
    }
    
    private static final String MKDTN_NOTIFICATION_TAG = BuildConfig.APPLICATION_ID;
    private static final int NOTIFICATION_VISIBILITY = NotificationCompat.VISIBILITY_SECRET;
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                MKDTN_NOTIFICATION_TAG,
                getString(R.string.mkdtn_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.mkdtn_channel_description));
            channel.enableVibration(true);
            channel.setLockscreenVisibility(NOTIFICATION_VISIBILITY);
            
            NotificationManager notificationManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null &&
                notificationManager.getNotificationChannel(MKDTN_NOTIFICATION_TAG) == null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private NotificationCompat.Builder makeNotification(String title, String text) {
        return new NotificationCompat.Builder(this, MKDTN_NOTIFICATION_TAG)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(text)
            .setGroup(getString(R.string.notification_group_key))
            .setGroupSummary(true) // for weak KITKAT devices that can't show group stuff
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NOTIFICATION_VISIBILITY);
    }
    
    private static ArrayList<Integer> IDs;
    
    private void notifyUser(String title, String text) {
        NotificationManagerCompat notificationManagerCompat
            = NotificationManagerCompat.from(this);
        
        // check if notifications are enabled
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean(getString(R.string.pref_enable_dtn_notifications_key),
            getResources().getBoolean(R.bool.pref_enable_dtn_notification_default)) ||
            !notificationManagerCompat.areNotificationsEnabled()) return;
    
        // create and register channel with the system
        createNotificationChannel();
    
        // make notification builder
        NotificationCompat.Builder notificationBuilder
            = makeNotification(title, text);
        
        // create intent for opening main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        notificationBuilder.setContentIntent(pendingIntent);
    
        // for those that don't support grouping,
        int id = (int) (Math.random() * Integer.MAX_VALUE);
        IDs.add(id);
        notificationManagerCompat.notify(MKDTN_NOTIFICATION_TAG, id, notificationBuilder.build());
    }
    
    @Override
    public void onPeerListChanged(String[] peerList) {
        peers = peerList;
        Log.i(LOG_TAG, "peers = " + Arrays.toString(peers));
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
