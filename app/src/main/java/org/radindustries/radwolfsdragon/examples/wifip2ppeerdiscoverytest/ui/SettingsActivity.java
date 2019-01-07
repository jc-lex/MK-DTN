package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;

public class SettingsActivity extends PreferenceActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
