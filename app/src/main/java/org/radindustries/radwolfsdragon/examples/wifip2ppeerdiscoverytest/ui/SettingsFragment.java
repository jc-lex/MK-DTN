package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.os.Bundle;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;

import androidx.preference.PreferenceFragmentCompat;

public final class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
