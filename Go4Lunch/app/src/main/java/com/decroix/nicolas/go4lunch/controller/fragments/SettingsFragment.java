package com.decroix.nicolas.go4lunch.controller.fragments;


import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.controller.activities.AuthActivity;
import com.decroix.nicolas.go4lunch.controller.activities.SettingsActivity;
import com.decroix.nicolas.go4lunch.utils.DeleteAccountHelper;
import com.decroix.nicolas.go4lunch.utils.NotificationHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;
import java.util.Objects;

import static java.lang.String.format;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener,
        TimePickerDialog.OnTimeSetListener, DeleteAccountHelper.UserDeleteListener {

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
                deleteAccountAlertDialog();
                return false;
            });
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case KEY_NOTIFICATION_PREFERENCE:
                updateStatusNotification(newValue);
                break;
            case KEY_NOTIFICATION_TIME_PREFERENCE:
                updateNotificationTime(newValue);
                break;
        }
        return true;
    }

    /**
     * Change the time at which the notification should be displayed
     *
     * @param newValue New time
     */
    private void updateNotificationTime(Object newValue) {
        if (newValue instanceof String) {
            notificationHelper.createAlarmDaily();
            Toast.makeText(getContext(), R.string.notification_time_update, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle the activation of the notification
     *
     * @param activation false to disable
     */
    private void updateStatusNotification(Object activation) {
        if (activation instanceof Boolean && getContext() != null) {
            boolean status = (boolean) activation;
            if (status) {
                notificationHelper.createAlarmDaily();
            } else {
                notificationHelper.disableNotification();
            }
        }
    }

    /**
     * Display a time dialog
     *
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

    /**
     * Delete the user from the registered restaurant and delete him/her from the database
     */
    @SuppressLint("InflateParams")
    // Pass null as the parent view because its going in the dialog layout
    private void deleteAccountAlertDialog() {
        Objects.requireNonNull(getActivity(), getString(R.string.rnn_context_cannot_be_null));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_settings_delete_account_dialog, null);
        final TextView summary = view.findViewById(R.id.alert_dialog_summary);
        summary.setText(R.string.alert_dialog_delete_account_re_authenticate);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(getString(R.string.alert_dialog_re_authenticate_button), (dialogInterface, i) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(AuthActivity.newIntent(getActivity(), SettingsActivity.class.getName()));
                })
                .setNegativeButton(getString(R.string.alert_dialog_delete_account_btn_return), null);
        alertDialog.create().show();
    }

    @Override
    public void userDeleted() {
        Toast.makeText(getContext(), R.string.msg_account_deleted, Toast.LENGTH_LONG).show();
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public OnFailureListener failureToDeleteUser(String text) {
        return e -> Toast.makeText(context, text + e, Toast.LENGTH_SHORT).show();
    }
}
