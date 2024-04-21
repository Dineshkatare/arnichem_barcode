package com.arnichem.arnichem_barcode;

import android.app.Service;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ClipboardMonitorService extends Service {

    private ClipboardManager clipboardManager;
    private static final String TAG = "ClipboardMonitor";

    @Override
    public void onCreate() {
        super.onCreate();
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(clipChangedListener);
        } else {
            Log.e(TAG, "ClipboardManager is null");
        }
    }

    private ClipboardManager.OnPrimaryClipChangedListener clipChangedListener =
            () -> {
                ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    String barcodeValue = clipData.getItemAt(0).getText().toString().trim();
                    if (!barcodeValue.isEmpty()) {
                        handleBarcodeData(barcodeValue);
                    }
                }
            };

    private void handleBarcodeData(String barcodeValue) {
        Toast.makeText(this, "Barcode: " + barcodeValue, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Barcode: " + barcodeValue);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (clipboardManager != null) {
            clipboardManager.removePrimaryClipChangedListener(clipChangedListener);
        }
        super.onDestroy();
    }
}
