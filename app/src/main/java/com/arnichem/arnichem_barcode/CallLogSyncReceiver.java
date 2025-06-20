package com.arnichem.arnichem_barcode;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import java.util.List;

public class CallLogSyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                // Call ended, fetch and upload call logs
                fetchAndUploadCallLogs(context);
            }
        }
    }

    private void fetchAndUploadCallLogs(Context context) {
        CallLogManager callLogManager = new CallLogManager(context);
        List<CallLogManager.CallLogEntry> callLogs = callLogManager.getCallLogs();

        if (!callLogs.isEmpty()) {
         //   callLogManager.sendCallLogsToServer(callLogs);
        }
    }

}
