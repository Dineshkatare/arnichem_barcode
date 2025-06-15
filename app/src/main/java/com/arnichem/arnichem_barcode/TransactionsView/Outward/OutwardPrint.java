package com.arnichem.arnichem_barcode.TransactionsView.Outward;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.PrintReceipt.OutwardPrint.outwardPrintDB;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.util.BluetoothPrinterUtil;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class OutwardPrint extends AppCompatActivity {
    public static final int PERMISSION_BLUETOOTH = 1;
    Button duradelprint;
    String durano, custname, empb, count;
    TextView empbid, dateid, custnameid, cylindernumberempty, vehicleno, arnichemdignprint, counttxt;
    MyDatabaseHelper addClymyDB;
    ArrayList<String> newlist;
    ArrayList<String> fillWithList;
    ArrayList<String> cylIdList;
    outwardPrintDB outwardPrintDB;
    FilledWithAdapter filledWithAdapter;
    ArrayList<String> name, tot;
    RecyclerView Filled_with_Recycle_View, cyclinderNames;
    CylinderNamePrintAdapter cylinderNamePrintAdapter;
    private BluetoothConnection selectedDevice;
    Bitmap printLogoDr, phoneNumberDr;
    ImageView printImg, phoneImg;
    TextView arnichemsignTxt, termsTxt;
    private BluetoothPrinterUtil bluetoothPrinterUtil;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outward_print);
        empbid = findViewById(R.id.empbid);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        bluetoothPrinterUtil = new BluetoothPrinterUtil();
        dateid = findViewById(R.id.dateid);
        addClymyDB = new MyDatabaseHelper(OutwardPrint.this);
        custnameid = findViewById(R.id.custnameid);
        //     cylindernumberempty = findViewById(R.id.cylindernumberempty);
        arnichemdignprint = findViewById(R.id.arnichemdignprint);
        vehicleno = findViewById(R.id.vehicleno);
        Filled_with_Recycle_View = findViewById(R.id.fillwithrec);
        cyclinderNames = findViewById(R.id.cyclinderNames);
        duradelprint = findViewById(R.id.duradelemptyprint);
        counttxt = findViewById(R.id.totalq);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        termsTxt = findViewById(R.id.termsTxt);

        outwardPrintDB = new outwardPrintDB(this);
        Intent i = getIntent();
        custname = i.getStringExtra("custname");
        empb = i.getStringExtra("empb");
        durano = i.getStringExtra("cylinder");
        count = i.getStringExtra("count");
        newlist = new ArrayList<String>();
        fillWithList = new ArrayList<>();
        cylIdList = new ArrayList<>();
        cylinder();
        name = new ArrayList<>();
        tot = new ArrayList<>();
        check();
        addClymyDB.deleteAllData();
        empbid.setText(empb);
        custnameid.setText(custname);
        counttxt.setText(count);
        String joined = TextUtils.join(",", newlist);
        //       cylindernumberempty.setText(joined);
        dateid.setText(DateFormat.getDateTimeInstance().format(new Date()));
        vehicleno.setText(SharedPref.getInstance(this).getVehicleNo());
        filledWithAdapter = new FilledWithAdapter(OutwardPrint.this, this, name, tot);
        Filled_with_Recycle_View.setAdapter(filledWithAdapter);
        cylinderNamePrintAdapter = new CylinderNamePrintAdapter(OutwardPrint.this, this, cylIdList, newlist, fillWithList);
        cyclinderNames.setAdapter(cylinderNamePrintAdapter);
        cyclinderNames.setLayoutManager(new LinearLayoutManager(OutwardPrint.this));
        Filled_with_Recycle_View.setLayoutManager(new LinearLayoutManager(OutwardPrint.this));
        arnichemdignprint.setText(SharedPref.getInstance(this).FirstName() + SharedPref.getInstance(this).LastName());
        duradelprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printBluetooth();

            }
        });
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
        arnichemsignTxt.setText("For " + SharedPref.mInstance.getOwnCode());
        termsTxt.setText(SharedPref.mInstance.getTermsText());
       // selectBluetoothDevice();

    }

//  2222222222222222222222222222222
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == finalprint.PERMISSION_BLUETOOTH) {
            boolean allPermissionsGranted = true;

            // Check if all requested permissions were granted
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permissions granted, proceed with Bluetooth printing
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
            } else {
                // Permissions denied, handle the case (show a message, etc.)
                Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


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

    public void cylinder() {
        Cursor cursor = addClymyDB.readAllData();
        if (cursor.getCount() == 0) {
        } else {
            while (cursor.moveToNext()) {
                cylIdList.add(cursor.getString(0));
                newlist.add(cursor.getString(1));
                fillWithList.add(cursor.getString(2));
                outwardPrintDB.addBook(cursor.getString(1), DateFormat.getDateTimeInstance().format(new Date()), custname, count, empb);
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[C]        Outward Receipt  \n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,phoneNumberDr)+"</img>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,printLogoDr)+"</img>\n\n" +
                        "[C]<font size='small'>Outward No-  " + empb + "</font>\n" +
                        "[C]<font size='small'>Date -  " + DateFormat.getDateTimeInstance().format(new Date()) + "</font>\n" +
                        "[C]<font size='small'>To -  " + custname + "</font>\n" +
                        "[C]<font size='small'><b>       Cylinder Numbers </b></font>\n" +
                        "[C]<font size='small'><b>      " + foreaching() + "</b></font>\n" +
                        "[C]<font size='small'>        " + foreachname() + "</font>\n" +
                        "[C]<font size='small'>Total Quantity : " + count + "</font>\n" +
                        "[C]<font size='small'>Vehicle No    :  " + SharedPref.getInstance(this).getVehicleNo() + "</font>\n" +
                        "[R]             [R]" + SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName() + "\n" +
                        "[R]Customer Sign  [R]"+"For "+SharedPref.getInstance(this).getOwnCode()+"\n\n"+
                        "[R]"+SharedPref.getInstance(this).getTermsText()+"\n"
        );
    }

    public Serializable foreaching() {
        StringBuffer text = new StringBuffer();

        for (int i = 0; i < newlist.size(); i++) {
            text.append(newlist.get(i)).append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append(fillWithList.get(i)).append('\n').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020');

        }
        return text;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addClymyDB != null)
            addClymyDB.close();

        addClymyDB.deleteAllData();


    }

    void check() {
        Cursor cursor = addClymyDB.readcount();
        if (cursor.getCount() == 0) {
        } else {
            while (cursor.moveToNext()) {
                name.add(cursor.getString(3));
                tot.add(cursor.getString(2));
            }
        }
    }


    public Serializable foreachname() {
        StringBuffer text = new StringBuffer();

        for(int i=0;i<name.size();i++) {
            text.append(name.get(i).toString()).append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append(fillWithList.get(i).toString()).append('\n').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020');
        }

        return text;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(OutwardPrint.this, Transactions.class));
    }
}