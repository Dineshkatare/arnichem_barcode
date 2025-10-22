package com.arnichem.arnichem_barcode.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestPrinter extends AppCompatActivity {

    private static final int PERMISSION_BLUETOOTH = 1001;
    private BluetoothConnection selectedDevice;
    private Button print;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_printer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Test Printer");

        print = findViewById(R.id.button2);

        // Check permissions at start
        checkBluetoothPermissions();

        print.setOnClickListener(v -> {
            if (selectedDevice == null) {
                selectBluetoothDevice();
            } else {
                printBluetooth();
            }
        });
    }

    /** -----------------------------
     *   PERMISSIONS HANDLING
     *  ----------------------------- */
    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        },
                        PERMISSION_BLUETOOTH);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH},
                        PERMISSION_BLUETOOTH);
            }
        }
    }

    /** -----------------------------
     *   DEVICE SELECTION
     *  ----------------------------- */
    private void selectBluetoothDevice() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_BLUETOOTH);
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
            return;
        }

        final List<BluetoothDevice> deviceList = new ArrayList<>(pairedDevices);
        final CharSequence[] deviceNames = new CharSequence[deviceList.size()];

        for (int i = 0; i < deviceList.size(); i++) {
            deviceNames[i] = deviceList.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Bluetooth Printer");
        builder.setItems(deviceNames, (dialog, which) -> {
            BluetoothDevice device = deviceList.get(which);
            selectedDevice = new BluetoothConnection(device);
            Toast.makeText(this, "Selected: " + device.getName(), Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    /** -----------------------------
     *   PRINT LOGIC
     *  ----------------------------- */
    private void printBluetooth() {
        if (selectedDevice == null) {
            Toast.makeText(this, "No printer selected", Toast.LENGTH_SHORT).show();
            selectBluetoothDevice();
            return;
        }

        try {
            // Check if connection works before printing
            selectedDevice.connect();
            selectedDevice.disconnect();

            new AsyncBluetoothEscPosPrint(this)
                    .execute(getAsyncEscPosPrinter(selectedDevice));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Unable to connect to printer: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /** -----------------------------
     *   PRINTER TEMPLATE
     *  ----------------------------- */
    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);
        return printer.setTextToPrint(
                "[C]<font size='normal'>====== Test Print ======</font>\n" +
                        "[L]<font size='small'>Printer Connected Successfully!</font>\n" +
                        "[C]-----------------------------\n" +
                        "[C]<font size='small'>Thank you for testing :)</font>\n\n"
        );
    }

    /** -----------------------------
     *   PERMISSION RESULT HANDLER
     *  ----------------------------- */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** -----------------------------
     *   NAVIGATION
     *  ----------------------------- */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(TestPrinter.this, MainSettings.class));
        finish();
    }
}
