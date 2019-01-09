package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.os.Bundle;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.settings_container, new SettingsFragment())
            .commit();
    }
}
