package com.onets.wallet.ui;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;

import com.onets.wallet.R;

/**
 * @author Yu K.Q.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.preferences);
    }
}
