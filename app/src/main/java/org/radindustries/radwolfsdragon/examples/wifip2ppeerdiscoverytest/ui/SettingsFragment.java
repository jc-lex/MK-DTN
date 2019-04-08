package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public final class SettingsFragment extends PreferenceFragmentCompat {
    // ListPreferences
    private static final String PREF_ROUTING_PROTOCOL_KEY
        = "dtn_routing_protocol";
    private static final String PREF_PRIORITY_CLASS_KEY
        = "bundle_priority_class";
    private static final String PREF_LIFETIME_KEY
        = "bundle_lifetime";
    private static final String PREF_MAX_FRAGMENT_PAYLOAD_SIZE_KEY
        = "maximum_fragment_payload_size";
    private static final String PREF_TRANSMISSION_MODE_KEY
        = "transmission_mode";
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        initUI();
    }
    
    private void initUI() {
        if (getActivity() != null) {
            SharedPreferences prefs
                = PreferenceManager.getDefaultSharedPreferences(getActivity());
            
            findPreference(PREF_ROUTING_PROTOCOL_KEY).setSummary(prefs.getString(
                PREF_ROUTING_PROTOCOL_KEY, getString(R.string.pref_default_routing_protocol)
            ));
            findPreference(PREF_PRIORITY_CLASS_KEY).setSummary(prefs.getString(
                PREF_PRIORITY_CLASS_KEY, getString(R.string.pref_default_priority_class)
            ));
            findPreference(PREF_LIFETIME_KEY).setSummary(prefs.getString(
                PREF_LIFETIME_KEY, getString(R.string.pref_default_lifetime)
            ));
            findPreference(PREF_MAX_FRAGMENT_PAYLOAD_SIZE_KEY).setSummary(prefs.getString(
                PREF_MAX_FRAGMENT_PAYLOAD_SIZE_KEY, getString(R.string.pref_default_max_fragment_payload_size)
            ));
            findPreference(PREF_TRANSMISSION_MODE_KEY).setSummary(prefs.getString(
                PREF_TRANSMISSION_MODE_KEY, getString(R.string.pref_default_transmission_mode)
            ));
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
    
    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener
        = new SharedPreferences.OnSharedPreferenceChangeListener() {
    
        // This method is called when a shared preference is changed, added, or removed.
        // This may be called even if a preference is set to its existing value. =D
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
    
            switch (key) {
                case PREF_ROUTING_PROTOCOL_KEY:
                    pref.setSummary(sharedPreferences.getString(
                        key, getString(R.string.pref_default_routing_protocol)
                    ));
                    break;
                case PREF_PRIORITY_CLASS_KEY:
                    pref.setSummary(sharedPreferences.getString(
                        key, getString(R.string.pref_default_priority_class)
                    ));
                    break;
                case PREF_LIFETIME_KEY:
                    pref.setSummary(sharedPreferences.getString(
                        key, getString(R.string.pref_default_lifetime)
                    ));
                    break;
                case PREF_MAX_FRAGMENT_PAYLOAD_SIZE_KEY:
                    pref.setSummary(sharedPreferences.getString(
                        key, getString(R.string.pref_default_max_fragment_payload_size)
                    ));
                    break;
                case PREF_TRANSMISSION_MODE_KEY:
                    pref.setSummary(sharedPreferences.getString(
                        key, getString(R.string.pref_default_transmission_mode)
                    ));
                    break;
                default: break;
            }
        }
    };
}
