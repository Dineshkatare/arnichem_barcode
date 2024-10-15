package com.arnichem.arnichem_barcode.PrintReceipt.DuraDeliveyPrint;

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
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.GodownView.Closing_stock.closing_stock_print;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery.Duradeliveryprint;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class DuraDeliPrintView extends AppCompatActivity {

    Button duradelprint;
    String Fullwt,tarewt,NetWt,cubic,durano,custname,pos,dcno,cust_code,delidate;
    TextView customername,duranumber,fullwt,Tarewt,Netwt,gas,vehicleno,arnichemdignprint,dcnoid,dtid,tvcode;
    DuraDeliveryPrintDB duraDeliveryPrintDBl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duradeliveryprint);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dcnoid=findViewById(R.id.dcnoid);
        dtid=findViewById(R.id.dtid);
        customername=findViewById(R.id.customername);
        duranumber=findViewById(R.id.duranumber);
        fullwt=findViewById(R.id.fullwt);
        tvcode=findViewById(R.id.codeid);
        Tarewt=findViewById(R.id.Tarewt);
        Netwt=findViewById(R.id.Netwt);
        gas=findViewById(R.id.gas);
        vehicleno=findViewById(R.id.vehicleno);
        arnichemdignprint=findViewById(R.id.arnichemdignprint);
        duradelprint=findViewById(R.id.duradelprint);
        duraDeliveryPrintDBl=new DuraDeliveryPrintDB(this);
        Intent i=getIntent();
        pos=i.getStringExtra("pos");
        cylinder();
        dcnoid.setText(dcno);
        dtid.setText(delidate);
        fullwt.setText(Fullwt);
        duranumber.setText(durano);
        tvcode.setText(cust_code);
        customername.setText(custname);
        Tarewt.setText(tarewt);
        Netwt.setText(NetWt);
        gas.setText(cubic);
        vehicleno.setText(SharedPref.getInstance(this).getVehicleNo());
        arnichemdignprint.setText(SharedPref.getInstance(this).FirstName()+SharedPref.getInstance(this).LastName());

        duradelprint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {

                printBluetooth();



            }
        });
    }
    public void cylinder() {

        Cursor cursor = duraDeliveryPrintDBl.readAllData();
        if (cursor.getCount() == 0) {


        } else {

            while (cursor.moveToNext()) {
                if (pos.equals(cursor.getString(9))) {
                    durano=(cursor.getString(1));
                    delidate = cursor.getString(2);
                    cust_code = cursor.getString(3);
                    custname = cursor.getString(4);
                Fullwt = cursor.getString(5);
                    tarewt = cursor.getString(6);
                    NetWt  = cursor.getString(7);
                     cubic = cursor.getString(8);
                    dcno = cursor.getString(9);
                }

            }
        }
    }
    public static final int PERMISSION_BLUETOOTH = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
           if (requestCode == finalprint.PERMISSION_BLUETOOTH) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted for Bluetooth, continue with the Bluetooth operation
            new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
        } else {
            // Permission denied, inform the user
            Toast.makeText(this, "Bluetooth permission denied. Cannot print.", Toast.LENGTH_SHORT).show();
        }
    } else if (requestCode == 1) {
        // This handles the Bluetooth device selection permission for Android 12 and above
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed to select Bluetooth device
            selectBluetoothDevice();
        } else {
            // Permission denied, inform the user
            Toast.makeText(this, "Bluetooth connect permission denied.", Toast.LENGTH_SHORT).show();
        }
    }
    }

    private BluetoothConnection selectedDevice;


        public void printBluetooth() {
        if (selectedDevice == null) {
            selectBluetoothDevice();
            
            return;
        }

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // Android 12 (API 31) and above
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                finalprint.PERMISSION_BLUETOOTH);
    } else {
        new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
    }
} else {
    // Below Android 12
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, finalprint.PERMISSION_BLUETOOTH);
    } else {
        new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
    }
}

    }
    public void selectBluetoothDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) and above
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
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
            builder.setItems(deviceNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BluetoothDevice device = deviceList.get(which);
                    selectedDevice = new BluetoothConnection(device);

                    printBluetooth();
                    // Toast.makeText(getApplicationContext(), "Selected Device: " + device.getName(), Toast.LENGTH_SHORT).show();
                }
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
                "[R]Delivery challan  [R]\n" +
                        "[C]<font size='small'>7776823823/8446823823/8378823823</font>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.printlogo, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n" +
                        "[C]<font size='small'>DC NO - "+dcno+"</font>\n" +
                        "[C]<font size='small'>Date  - "+delidate+"</font>\n" +
                        "[C]<font size='small'>Code -  "+cust_code+"</font>\n" +
                        "[C]<font size='small'>Name -  "+custname+"</font>\n" +
                        "[C]<font size='small'>       Cylinder Details </font>\n" +
                        "[C]<font size='small'><b>Dura Number   :  "+durano+"</b></font>\n" +
                        "[C]<font size='small'>Full Weight   :  "+Fullwt+"</font>\n" +
                        "[C]<font size='small'>Tare Weight   :  "+tarewt+"</font>\n" +
                        "[C]<font size='small'>Net Weight    :  "+NetWt+"</font>\n" +
                        "[C]<font size='small'>Gas           :  "+cubic+"</font>\n" +
                        "[C]<font size='small'>Vehicle Number:  "+ SharedPref.getInstance(this).getVehicleNo()+"</font>\n\n" +
                        "[C]<font size='small'>Invoice No    :  "+"  "+"</font>\n\n\n" +
                        "[R]               [R]"+SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName()+"\n" +
                        "[R]Customer Sign [R]"+"For "+SharedPref.getInstance(this).getCompanyFullName()+"\n"
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(DuraDeliPrintView.this, DuraDeliPrintView.class));
    }
}