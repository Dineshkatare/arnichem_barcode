package com.arnichem.arnichem_barcode.GodownView.godownempty;

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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.GodownView.Closing_stock.closing_stock_print;
import com.arnichem.arnichem_barcode.GodownView.GOdownMainActivity;
import com.arnichem.arnichem_barcode.GodownView.GodownFullRecipt.GodownFullRecpPrint;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CylinderNamePrintAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.arnichem.arnichem_barcode.PrintReceipt.GodownEmptyPrintActivity.GodownEmpPrintDB;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
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

public class GOdownEmptyprint extends AppCompatActivity {
    Button duradelprint;
    String durano, custname, empb, custcode, count, sign_path;
    TextView empbid, dateid, custnameid, cylindernumberempty, arnichemdignprint, counttxt, tvcode;
    GodownEmptyHelper addClymyDB;
    ArrayList<String> newlist;
    GodownEmpPrintDB godownEmpPrintDB;
    Bitmap printLogoDr, phoneNumberDr, digital_sign;
    ImageView printImg, phoneImg, custnamesign;
    TextView arnichemsignTxt, termsTxt;
    RecyclerView fillwithrec, cyclinderNames;
    FilledWithAdapter filledWithAdapter;
    CylinderNamePrintAdapter cylinderNamePrintAdapter;
    ArrayList<String> name, tot, fillWithList, cylIdList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_godown_emptyprint);
        empbid = findViewById(R.id.empbid);
        dateid = findViewById(R.id.dateid);
        addClymyDB = new GodownEmptyHelper(GOdownEmptyprint.this);
        custnameid = findViewById(R.id.custnameid);
        counttxt = findViewById(R.id.totalq);
        custnamesign = findViewById(R.id.custnamesign);
        tvcode = findViewById(R.id.codeid);
        cylindernumberempty = findViewById(R.id.cylindernumberempty);
        arnichemdignprint = findViewById(R.id.cdarnichemdignprint);
        duradelprint = findViewById(R.id.duradelemptyprint);
        godownEmpPrintDB = new GodownEmpPrintDB(this);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        termsTxt = findViewById(R.id.termsTxt);
        cyclinderNames = findViewById(R.id.cyclinderNames);

        Intent i = getIntent();
        custname = i.getStringExtra("custname");
        empb = i.getStringExtra("empb");
        custcode = i.getStringExtra("custcode");
        durano = i.getStringExtra("cylinder");
        count = i.getStringExtra("count");
        sign_path = i.getExtras().getString("sign_path", "");

        newlist = new ArrayList<String>();
        fillWithList = new ArrayList<>();
        cylIdList = new ArrayList<>();
        cylinder();

        fillwithrec = findViewById(R.id.fillwithrec);
        name = new ArrayList<>();
        tot = new ArrayList<>();
        for (java.util.Map.Entry<String, List<String>> entry : groupedData.entrySet()) {
            name.add(entry.getKey());
            tot.add(String.valueOf(entry.getValue().size()));
        }
        filledWithAdapter = new FilledWithAdapter(GOdownEmptyprint.this, this, name, tot);
        fillwithrec.setAdapter(filledWithAdapter);
        cylinderNamePrintAdapter = new CylinderNamePrintAdapter(GOdownEmptyprint.this, this, cylIdList, newlist,
                fillWithList);
        cyclinderNames.setAdapter(cylinderNamePrintAdapter);
        cyclinderNames.setLayoutManager(new LinearLayoutManager(GOdownEmptyprint.this));
        fillwithrec.setLayoutManager(new LinearLayoutManager(GOdownEmptyprint.this));
        empbid.setText(empb);
        addClymyDB.deleteAllData();
        tvcode.setText(custcode);
        counttxt.setText(count);
        custnameid.setText(custname);
        // String joined = TextUtils.join(",", newlist);
        // cylindernumberempty.setText(joined);
        // cylindernumberempty.setText(foreaching().toString());
        dateid.setText(DateFormat.getDateTimeInstance().format(new Date()));
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

        if (!sign_path.isEmpty()) {
            File signFile = new File(sign_path);
            if (signFile.exists()) {
                digital_sign = BitmapFactory.decodeFile(signFile.getAbsolutePath());
                digital_sign = Bitmap.createScaledBitmap(digital_sign, 200, 200, true);

                custnamesign.setImageBitmap(digital_sign);
            }
        } else {
            digital_sign = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
            digital_sign.eraseColor(Color.WHITE);
            custnamesign.setImageBitmap(digital_sign);
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
            // This handles the Bluetooth device selection permission for Android 12 and
            // above
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
            // Below Android 12
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) and above
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
            builder.setItems(deviceNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BluetoothDevice device = deviceList.get(which);
                    selectedDevice = new BluetoothConnection(device);

                    printBluetooth();
                    // Toast.makeText(getApplicationContext(), "Selected Device: " +
                    // device.getName(), Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        } else {
            Toast.makeText(this, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
        }
    }

    java.util.Map<String, List<String>> groupedData = new java.util.HashMap<>();

    public void cylinder() {

        Cursor cursor = addClymyDB.readAllData();
        if (cursor.getCount() == 0) {

        } else {

            while (cursor.moveToNext()) {
                String cyl = cursor.getString(1);
                String fw = cursor.getString(5);
                if (fw == null || fw.isEmpty() || fw.equals("null"))
                    fw = "Other";

                if (!groupedData.containsKey(fw)) {
                    groupedData.put(fw, new ArrayList<>());
                }
                groupedData.get(fw).add(cyl);

                cylIdList.add(cursor.getString(0));
                newlist.add(cursor.getString(1));
                fillWithList.add(cursor.getString(5));
                godownEmpPrintDB.addBook(cursor.getString(1), DateFormat.getDateTimeInstance().format(new Date()),
                        custcode, custname, count, empb);

            }

        }
    }

    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[C]        Empty  Receipt  \n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, phoneNumberDr) + "</img>\n"
                        +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, printLogoDr) + "</img>\n\n"
                        +
                        "[C]<font size='small'>EMPB -  " + empb + "</font>\n" +
                        "[C]<font size='small'>Date -  " + DateFormat.getDateTimeInstance().format(new Date())
                        + "</font>\n" +
                        "[C]<font size='small'>Code -  " + custcode + "</font>\n" +
                        "[C]<font size='small'>Name -  " + custname + "</font>\n" +
                        "[C]<font size='small'><b>       Cylinder Numbers </b></font>\n" +
                        "[C]<font size='small'><b>       Cylinder Numbers </b></font>\n" +
                        "[C]<font size='small'><b>            " + foreaching() + "</b></font>\n" +
                        "[C]--------------------------------\n" +
                        "[L]\n" +
                        foreachname() +
                        "[C]--------------------------------\n" +
                        "[C]<font size='small'>Total Quantity : " + count + "</font>\n" +
                        "[L]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, digital_sign) + "</img>\n"
                        +
                        "[R]               [R]" + SharedPref.getInstance(this).FirstName() + " "
                        + SharedPref.getInstance(this).LastName() + "\n" +
                        "[R]Customer Sign [R]" + "For " + SharedPref.getInstance(this).getOwnCode() + "\n\n" +
                        "[R]" + SharedPref.getInstance(this).getTermsText() + "\n");
    }

    public Serializable foreachname() {
        StringBuffer text = new StringBuffer();
        for (java.util.Map.Entry<String, List<String>> entry : groupedData.entrySet()) {
            text.append("[L]<font size='small'>" + entry.getKey() + " : " + entry.getValue().size() + "</font>\n");
        }
        return text;
    }

    public Serializable foreaching() {
        StringBuffer text = new StringBuffer();
        for (java.util.Map.Entry<String, List<String>> entry : groupedData.entrySet()) {
            text.append(entry.getKey()).append(":\n");
            int i = 0;
            for (String cyl : entry.getValue()) {
                text.append(cyl);
                if (i < entry.getValue().size() - 1) {
                    text.append(", ");
                }
                i++;
            }
            text.append("\n\n");
        }
        return text;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(GOdownEmptyprint.this, GOdownMainActivity.class));
    }
}