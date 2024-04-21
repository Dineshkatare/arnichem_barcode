package com.arnichem.arnichem_barcode.view;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import com.arnichem.arnichem_barcode.Company.SelectCompanyActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.attendance.Attendance_log;
import com.arnichem.arnichem_barcode.util.SharedPref;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
   // Uri customSoundUri;
    @Override
    public void onReceive(Context context, Intent intent) {
        // Create a notification
    //    customSoundUri  = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert);

        createNotification(context, "Alarm", "Have you marked your attendance in the app?");

        // You can perform additional actions here if needed
    }

    private void createNotification(Context context, String title, String content) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default_channel_id";
            String channelName = "Default Channel";
            NotificationChannel channel = new NotificationChannel(channelId, channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Specify the target activity
        Intent intent;
        String status = SharedPref.getInstance(context).LoggedInUser();
        if (status.equals("success"))
        {
             intent = new Intent(context, Attendance_log.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }else {
             intent = new Intent(context, SelectCompanyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }




        int flags;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        // Create a PendingIntent for the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                flags);
        Uri customSoundUri = Uri.parse("android.resource://" + context.getPackageName() + "/raw/alert");

        // Create a Ringtone object using the custom sound Uri
        Ringtone ringtone = RingtoneManager.getRingtone(context, customSoundUri);

        // Play the custom sound
        ringtone.play();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "default_channel_id")
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent) // Set the PendingIntent for the notification
                        .setAutoCancel(true)
                                .setSound(customSoundUri);
         // Automatically removes the notification when clicked

        // Show the notification
        notificationManager.notify(1, builder.build());
    }

    public static void setAlarm(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        int flags;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        // Use a unique request code for each PendingIntent
        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        // Set the alarm to trigger at the specified time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long triggerTime = calendar.getTimeInMillis();

        // Set a repeating alarm that goes off every day
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            // For versions prior to Android 13, use the old method
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime,pendingIntent);
        }
    }

    public static void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // Cancel the alarm
        alarmManager.cancel(pendingIntent);
    }
}
