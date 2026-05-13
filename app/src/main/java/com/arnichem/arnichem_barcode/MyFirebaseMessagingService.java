package com.arnichem.arnichem_barcode;

import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.util.NotificationHelper;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_SERVICE";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String title = null;
        String body  = null;

        // Prefer data payload (works even when app is in foreground/background)
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data payload: " + remoteMessage.getData());
            title = remoteMessage.getData().get("title");
            body  = remoteMessage.getData().get("body");
        }

        // Fallback to notification payload
        if (remoteMessage.getNotification() != null) {
            if (title == null) title = remoteMessage.getNotification().getTitle();
            if (body  == null) body  = remoteMessage.getNotification().getBody();
        }

        if (title != null && body != null) {
            NotificationHelper.showNotification(this, title, body, remoteMessage.getData());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed FCM token: " + token);
        SharedPref.getInstance(this).storeFcmToken(token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Use stored username (same key used in login.java)
        String appUsername = SharedPref.getInstance(this).UserName();
        if (appUsername == null || appUsername.isEmpty()) {
            Log.w(TAG, "sendRegistrationToServer: no username in SharedPref, skipping");
            return;
        }

        Log.d(TAG, "Re-registering token for username: " + appUsername);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.register_fcm_token,
                response -> Log.d(TAG, "Token re-registered: " + response),
                error  -> Log.e(TAG, "Token re-registration failed: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username",    appUsername);          // was user_id
                params.put("fcm_token",   token);
                params.put("device_type", "android");
                params.put("role_key",    SharedPref.getInstance(MyFirebaseMessagingService.this).getRoleKey());
                params.put("db_host",     SharedPref.mInstance.getDBHost());
                params.put("db_username", SharedPref.mInstance.getDBUsername());
                params.put("db_password", SharedPref.mInstance.getDBPassword());
                params.put("db_name",     SharedPref.mInstance.getDBName());
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
