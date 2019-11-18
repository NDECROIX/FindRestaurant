package com.decroix.nicolas.go4lunch.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.receiver.AlarmReceiver;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class NotificationHelper {

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
    }

    /**
     * Create a daily alarm that displays a notification.
     */
    public void createAlarmDaily() {

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        boolean notificationEnable = sharedPreferences
                .getBoolean(context.getString(R.string.setting_key_notification), true);

        if (!notificationEnable) {
            return;
        }

        String timeValue = sharedPreferences
                .getString(context.getString(R.string.setting_key_notification_time), context.getString(R.string.notification_default_time));
        timeValue = (timeValue == null) ? "12:00" : timeValue;
        String[] time = timeValue.split(":");
        int hours = Integer.parseInt(time[0]);
        int minutes = Integer.parseInt(time[1]);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // Set the alarm to start at approximately hours:minutes
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmMgr != null) {
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }

    public void disableNotification() {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}
