package com.arnichem.arnichem_barcode.other_entries;

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
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class CustomerEmptyPrintActivity extends AppCompatActivity {

    TextView txtDate, txtCustomer, txtLocation, txtDescription, txtRemarks, txtVehicle, signTxt;
    Button btnPrint;
    ImageView printImg, phoneImg;
    Bitmap printLogoDr, phoneNumberDr;

    String customer, location, description, remarks, vehicle, dateAdded;
    private BluetoothConnection selectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_empty_print);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Print Receipt");
        }

        txtDate = findViewById(R.id.txtDate);
        txtCustomer = findViewById(R.id.txtCustomer);
        txtLocation = findViewById(R.id.txtLocation);
        txtDescription = findViewById(R.id.txtDescription);
        txtRemarks = findViewById(R.id.txtRemarks);
        txtVehicle = findViewById(R.id.txtVehicle);
        signTxt = findViewById(R.id.signTxt);
        btnPrint = findViewById(R.id.btnPrint);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);

        // Get Data
        Intent i = getIntent();
        customer = i.getStringExtra("customer");
        location = i.getStringExtra("location");
        description = i.getStringExtra("description");
        remarks = i.getStringExtra("remarks");
        vehicle = i.getStringExtra("vehicle");
        dateAdded = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());

        // Set Data
        txtDate.setText(dateAdded);
        txtCustomer.setText(customer);
        txtLocation.setText(location);
        txtDescription.setText(description);
        txtRemarks.setText(remarks);
        txtVehicle.setText(vehicle);

        signTxt.setText("For " + SharedPref.mInstance.getOwnCode());

        // Load Images
        String print_logo = SharedPref.mInstance.getPrintLogo();
        File imgFile = new File(print_logo);
        if (imgFile.exists()) {
            printLogoDr = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            printImg.setImageBitmap(printLogoDr);
        }

        String phoneNumber = SharedPref.mInstance.getPhoneNumber();
        File imgFilePhoneNumber = new File(phoneNumber);
        if (imgFile.exists()) {
            phoneNumberDr = BitmapFactory.decodeFile(imgFilePhoneNumber.getAbsolutePath());
            phoneImg.setImageBitmap(phoneNumberDr);
        }

        btnPrint.setOnClickListener(v -> printBluetooth());
    }

    // Bluetooth Printing Logic (Copied from Empty_Print)

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == finalprint.PERMISSION_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
            } else {
                Toast.makeText(this, "Bluetooth permission denied.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectBluetoothDevice();
            } else {
                Toast.makeText(this, "Bluetooth connect permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void printBluetooth() {
        if (selectedDevice == null) {
            selectBluetoothDevice();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT },
                        finalprint.PERMISSION_BLUETOOTH);
            } else {
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
            }
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.BLUETOOTH },
                        finalprint.PERMISSION_BLUETOOTH);
            } else {
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
            }
        }
    }

    public void selectBluetoothDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.BLUETOOTH_CONNECT }, 1);
                return;
            }
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            final List<BluetoothDevice> deviceList = new ArrayList<>(pairedDevices);
            final CharSequence[] deviceNames = new CharSequence[deviceList.size()];
            for (int i = 0; i < deviceList.size(); i++) {
                deviceNames[i] = deviceList.get(i).getName();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a Bluetooth Device");
            builder.setItems(deviceNames, (dialog, which) -> {
                BluetoothDevice device = deviceList.get(which);
                selectedDevice = new BluetoothConnection(device);
                printBluetooth();
            });
            builder.show();
        } else {
            Toast.makeText(this, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[C]        Customer Empty Receipt  \n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, phoneNumberDr) + "</img>\n"
                        +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, printLogoDr) + "</img>\n\n"
                        +
                        "[L]<font size='small'>Date : " + dateAdded + "</font>\n" +
                        "[L]<font size='small'>Customer : " + customer + "</font>\n" +
                        "[L]<font size='small'>Location : " + location + "</font>\n" +
                        "[L]<font size='small'>Vehicle : " + vehicle + "</font>\n" +
                        "[C]--------------------------------\n" +
                        "[L]<font size='small'><b>Description:</b></font>\n" +
                        "[L]<font size='small'>" + description + "</font>\n\n" +
                        "[L]<font size='small'><b>Remarks:</b></font>\n" +
                        "[L]<font size='small'>" + remarks + "</font>\n" +
                        "[C]--------------------------------\n" +
                        "[R]" + SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName()
                        + "\n" +
                        "[R]Customer " + SharedPref.getInstance(this).getOwnCode() + "\n\n" +
                        "[C]" + SharedPref.getInstance(this).getTermsText() + "\n");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
