package de.twoid.spotifystreamer.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import de.twoid.spotifystreamer.R;

/**
 * Created by Johannes on 14.06.2015.
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    public static final String TAG = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        if(key.equals(Preferences.KEY_COUNTRY)){
            ListPreference countryPreference = (ListPreference) findPreference(key);
            countryPreference.setSummary(countryPreference.getEntry());
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause(){
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
