package com.decroix.nicolas.go4lunch.controller.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.utils.NotificationHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private static final String KEY_NOTIFICATION_PREFERENCE = "notification";
    private static final String KEY_NOTIFICATION_TIME_PREFERENCE = "notification_time";

    private SwitchPreferenceCompat notificationEnabled;
    private Preference deleteAccount;
    private Preference notificationTime;
    private Context context;
    private NotificationHelper notificationHelper;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        notificationHelper = new NotificationHelper(getContext());
        getPreference();
        configListener();
    }

    /**
     * Initialize preference variables
     */
    private void getPreference() {
        notificationEnabled = findPreference(getString(R.string.setting_key_notification));
        deleteAccount = findPreference(getString(R.string.setting_key_delete_account));
        notificationTime = findPreference(getString(R.string.setting_key_notification_time));
    }

    /**
     * Configures listener on preference variables
     */
    private void configListener() {
        if (notificationEnabled != null) {
            notificationEnabled.setOnPreferenceChangeListener(this);
        }
        if (notificationTime != null) {
            notificationTime.setOnPreferenceClickListener(preference -> {
                //showTimeDialog(preference);
                return true;
            });
            notificationTime.setOnPreferenceChangeListener(this);
        }
        if (deleteAccount != null) {
            deleteAccount.setOnPreferenceClickListener(preference -> {
                //deleteAccountAlertDialog();
                return false;
            });
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
