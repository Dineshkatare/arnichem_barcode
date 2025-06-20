package com.arnichem.arnichem_barcode.util;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothPrinterUtil {

    private BluetoothConnection selectedDevice;

    public void printBluetooth(Activity activity) {
        if (selectedDevice == null) {
            selectBluetoothDevice(activity);
            return;
        }

        if (checkBluetoothPermissions(activity)) {
            new AsyncBluetoothEscPosPrint(activity).execute(getAsyncEscPosPrinter(selectedDevice));
        }
    }

    private boolean checkBluetoothPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 13 and above
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        finalprint.PERMISSION_BLUETOOTH);
                return false;
            }
        } else {
            // Below Android 13
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH}, finalprint.PERMISSION_BLUETOOTH);
                return false;
            }
        }
        return true;
    }

    public void selectBluetoothDevice(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;
        }

        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (pairedDevices.size() > 0) {
            final List<BluetoothDevice> deviceList = new ArrayList<>(pairedDevices);
            final CharSequence[] deviceNames = new CharSequence[deviceList.size()];

            for (int i = 0; i < deviceList.size(); i++) {
                deviceNames[i] = deviceList.get(i).getName();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Select a Bluetooth Device");
            builder.setItems(deviceNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BluetoothDevice device = deviceList.get(which);
                    selectedDevice = new BluetoothConnection(device);
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        printBluetooth(activity);
                        Toast.makeText(activity.getApplicationContext(), "Selected Device: " + device.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.show();
        } else {
            Toast.makeText(activity, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
        }
    }

    // Example method, replace with your actual implementation
    private AsyncEscPosPrinter getAsyncEscPosPrinter(BluetoothConnection device) {
        // Implement the logic to return AsyncEscPosPrinter based on the device
        return null; // replace with actual logic
    }
}
