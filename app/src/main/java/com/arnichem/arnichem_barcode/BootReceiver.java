package com.arnichem.arnichem_barcode;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.arnichem.arnichem_barcode.view.Dashboard;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Reschedule the call log sync on device boot
            Intent serviceIntent = new Intent(context, Dashboard.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(serviceIntent);
        }
    }
}
