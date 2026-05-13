package com.arnichem.arnichem_barcode.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.view.SplashScreen;

public class NotificationHelper {

    public static final String CHANNEL_ID   = "arnichem_notifications";
    public static final String CHANNEL_NAME = "Arnichem Notifications";
    public static final String CHANNEL_DESC = "General notifications from Arnichem App";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(Context context, String title, String message,
                                        java.util.Map<String, String> data) {
        createNotificationChannel(context);

        Log.d("NotificationHelper", "Showing notification: " + title + " | Data: " + data);

        String eventType = (data != null) ? data.get("event_type") : null;
        String orderId   = (data != null) ? data.get("order_id")   : null;
        String pickId    = (data != null) ? data.get("pick_id")    : null;
        String leaveId   = (data != null) ? data.get("leave_id")   : null;

        Intent intent;

        // Deep-link routing based on ID present in payload
        if (orderId != null && !orderId.isEmpty()) {
            Log.d("NotificationHelper", "Routing to OrderViewActivity for order: " + orderId);
            intent = new Intent(context, com.arnichem.arnichem_barcode.order.OrderViewActivity.class);
            intent.putExtra("order_id", orderId);
        } else if (pickId != null && !pickId.isEmpty()) {
            Log.d("NotificationHelper", "Routing to PickViewActivity for pick: " + pickId);
            intent = new Intent(context, com.arnichem.arnichem_barcode.order.PickViewActivity.class);
            intent.putExtra("pick_id", pickId);
        } else if (leaveId != null && !leaveId.isEmpty()) {
            Log.d("NotificationHelper", "Routing to LeaveViewActivity for leave: " + leaveId);
            intent = new Intent(context, com.arnichem.arnichem_barcode.leave.LeaveViewActivity.class);
            intent.putExtra("leave_id", leaveId);
        } else if (data != null && data.containsKey("log_id")) {
            String logId = data.get("log_id");
            Log.d("NotificationHelper", "Routing to AttendanceViewActivity for log: " + logId);
            intent = new Intent(context, com.arnichem.arnichem_barcode.attendance.AttendanceViewActivity.class);
            intent.putExtra("log_id", logId);
        } else {
            Log.d("NotificationHelper", "No specific ID, routing to SplashScreen");
            intent = new Intent(context, SplashScreen.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        // Forward all data payload extras into the intent
        if (data != null) {
            for (java.util.Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        // Use unique request code so each notification has its own PendingIntent
        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.logo)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(requestCode, notificationBuilder.build());
        }
    }
}
