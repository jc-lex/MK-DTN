package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final int USE_ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 66;

    private final String[] items = new String[] {"Messages", "Peers"};
    
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
    
    @Override
    protected void onStart() {
        super.onStart();
        initSettings();
    }
    
    private void initSettings() {
        if (MKDTNService.configFileDoesNotExist(this)) {
            MKDTNService.writeDefaultConfig(this);
        }
        updateConfig();
    }
    
    private void updateConfig() {
        // SharedPreferences does not support use across multiple processes. :(
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        MKDTNService.DTNConfig config = MKDTNService.getConfig(this);
    
        config.lifetime = prefs.getString(
            getString(R.string.pref_lifetime_key),
            getString(R.string.pref_default_lifetime)
        );
    
        config.routingProtocol = prefs.getString(
            getString(R.string.pref_routing_protocol_key),
            getString(R.string.pref_default_routing_protocol)
        );
    
        config.priorityClass = prefs.getString(
            getString(R.string.pref_priority_class_key),
            getString(R.string.pref_default_priority_class)
        );
        
        config.enableManualMode = prefs.getBoolean(
            getString(R.string.pref_enable_manual_transmission_key),
            getResources().getBoolean(R.bool.pref_enable_manual_transmission_default)
        );
        
        config.transmissionMode = prefs.getString(
            getString(R.string.pref_transmission_mode_key),
            getString(R.string.pref_default_transmission_mode)
        );
        
        MKDTNService.updateConfig(this, config);
    }
    
    private void initUI() {
        ListView mainList = findViewById(R.id.main_list);
        ArrayAdapter<String> mainAdapter = new ArrayAdapter<>(
            this, R.layout.main_list_item, R.id.main_list_item_textview, items
        );
        mainList.setAdapter(mainAdapter);
        mainList.setOnItemClickListener(this);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0: startActivity(new Intent(this, MessagesActivity.class)); break;
            case 1: startActivity(new Intent(this, PeersActivity.class)); break;
        }
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
//            case R.id.action_start_dtn:
//                startService(new Intent(this, MKDTNService.class));
//                break;
            case R.id.action_manager:
                startActivity(new Intent(this, ManagerActivity.class));
                break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }
}
