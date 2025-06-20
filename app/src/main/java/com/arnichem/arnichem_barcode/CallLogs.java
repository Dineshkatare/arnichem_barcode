package com.arnichem.arnichem_barcode;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.constant.LogsAdapter;
import com.arnichem.arnichem_barcode.view.LogsManager;
import com.wickerlabs.logmanager.LogObject;

import java.util.Date;
import java.util.List;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;

public class CallLogs extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_CALL_LOG = 100;
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_logs);

        if (checkPermissions()) {
            setupCallLogs();
        } else {
            requestPermissions();
        }
    }

    private void setupCallLogs() {
        RecyclerView logList = findViewById(R.id.recyclerView);
        LogsManager logsManager = new LogsManager(this);
        List<LogObject> callLogs = logsManager.getLogs(LogsManager.ALL_CALLS);

        LogsAdapter logsAdapter = new LogsAdapter(this, R.layout.log_layout, callLogs);
        logList.setAdapter(logsAdapter);
        logList.setLayoutManager(new LinearLayoutManager(this));

        // Print the call logs
        for (LogObject logObject : callLogs) {
            String phoneNumber = logObject.getContactName();
            String callType = getCallTypeString(logObject.getType());
            String callDate = formatDate(logObject.getDate());

            Log.d("CallLog", "Number: " + phoneNumber + ", Type: " + callType + ", Date: " + callDate);
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS},
                PERMISSION_REQUEST_READ_CALL_LOG);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_CALL_LOG || requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCallLogs(); // Permission granted, setup the call logs
            } else {
                Toast.makeText(this, "Permission denied to read call logs or contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String formatDate(long dateInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return formatter.format(new Date(dateInMillis));
    }

    private String getCallTypeString(int callType) {
        switch (callType) {
            case 1:
                return "Incoming";
            case 2:
                return "Outgoing";
            case 3:
                return "Missed";
            default:
                return "Unknown";
        }
    }
}
