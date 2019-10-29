package com.decroix.nicolas.go4lunch.controller.fragments;


import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.utils.NotificationHelper;

import java.util.Locale;

import static java.lang.String.format;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener,
        TimePickerDialog.OnTimeSetListener {

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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        notificationHelper = new NotificationHelper(context);
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
                showTimeDialog(preference);
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

    /**
     * Display a time dialog
     * @param preference time to display
     */
    private void showTimeDialog(Preference preference) {
        String value = preference.getSharedPreferences().getString(getString(R.string.setting_key_notification_time), "12:00");
        String[] time = value.split(":");
        int hours = Integer.parseInt(time[0]);
        int minutes = Integer.parseInt(time[1]);
        if (getFragmentManager() != null) {
            new TimePickerFragment(this, hours, minutes)
                    .show(getFragmentManager(), getString(R.string.tag_time_picker));
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int h, int m) {
        String time = format(Locale.getDefault(), "%02d", h) + ":" + format(Locale.getDefault(), "%02d", m);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(getString(R.string.setting_key_notification_time), time).apply();
        notificationTime.callChangeListener(time);
    }
}
