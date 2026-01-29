package com.arnichem.arnichem_barcode;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.arnichem.arnichem_barcode.FileUpload.ApiResponse;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.google.gson.Gson;
import com.wickerlabs.logmanager.LogObject;
import com.wickerlabs.logmanager.LogsManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallLogSyncService extends Service {

    private static final String CHANNEL_ID = "CallLogSyncServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Not a bound service, return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the service as a foreground service
        startForegroundService();

        // Fetch and upload call logs in a background thread
        new Thread(this::fetchAndUploadCallLogs).start();

        // Continue running until manually stopped
        return START_STICKY;
    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundService() {
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call Log Sync Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Create a persistent notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call Log Sync")
                .setContentText("Syncing call logs in the background...")
                .setSmallIcon(R.drawable.logo) // Your app's icon
                .build();

        // Start the service in the foreground with the notification
        startForeground(NOTIFICATION_ID, notification);
    }

    private void fetchAndUploadCallLogs() {
        // Fetch call logs using LogsManager
        LogsManager logsManager = new LogsManager(getApplicationContext());
        List<LogObject> callLogs = logsManager.getLogs(LogsManager.ALL_CALLS);

        Log.d("CallLogSyncService", "Fetched call logs: " + callLogs.size());

        // Upload the call logs
        uploadCallLogs(callLogs); // Uncommented to enable upload
    }

    private void uploadCallLogs(List<LogObject> callLogs) {
        // Convert call logs to JSON
        Gson gson = new Gson();
        String callLogsJson = gson.toJson(callLogs);

        Log.d("CallLogSyncService", "API Request Data (Call Logs): " + callLogsJson); // Added log

        // Create API instance and make the call
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);

        String dbHost = SharedPref.mInstance.getDBHost();
        String dbUsername = SharedPref.mInstance.getDBUsername();
        String dbPassword = SharedPref.mInstance.getDBPassword();
        String dbName = SharedPref.mInstance.getDBName();

        Call<ApiResponse> call = apiInterface.uploadCallLogs(dbHost, dbUsername, dbPassword, dbName, callLogsJson);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("CallLogSyncService", "Call logs uploaded successfully");
                } else {
                    Log.e("CallLogSyncService", "Failed to upload call logs. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("CallLogSyncService", "Error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        // Cleanup resources when the service is destroyed
        super.onDestroy();
        Log.d("CallLogSyncService", "Service destroyed");
    }
}
