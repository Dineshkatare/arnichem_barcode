package com.arnichem.arnichem_barcode.GodownView.Closing_stock;

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
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.GodownView.GOdownMainActivity;
import com.arnichem.arnichem_barcode.PrintReceipt.InwardPrint.InwardprintDB;
import com.arnichem.arnichem_barcode.R;

import com.arnichem.arnichem_barcode.TransactionsView.InWard.InwardPrint;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.OutwardPrint;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class closing_stock_print extends AppCompatActivity {
    Button duradelprint;
    String durano,warehouse,empb,count;
    TextView empbid,dateid,warehouseval,cylindernumberempty,arnichemdignprint,counttxt;
    closing_helper addClymyDB;
    ArrayList<String> newlist;
    Bitmap printLogoDr,phoneNumberDr;
    ImageView printImg,phoneImg;
    TextView arnichemsignTxt,termsTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closing_stock_print);
        empbid=findViewById(R.id.empbid);
        dateid=findViewById(R.id.dateid);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Stock Print");
        addClymyDB=new closing_helper(closing_stock_print.this);
        warehouseval=findViewById(R.id.warehouseval);
        cylindernumberempty=findViewById(R.id.cylindernumberempty);
        arnichemdignprint=findViewById(R.id.arnichemdignprint);
        counttxt=findViewById(R.id.totalq);
        duradelprint=findViewById(R.id.duradelemptyprint);
        Intent i=getIntent();
        empb=i.getStringExtra("empb");
        warehouse= i.getStringExtra("warehouse");
        count=i.getStringExtra("count");
        newlist=new ArrayList<String>();
        cylinder();
        addClymyDB.deleteAllData();
        empbid.setText(empb);
        counttxt.setText(count);
        warehouseval.setText(warehouse);
        String joined = TextUtils.join(",", newlist);
        cylindernumberempty.setText(joined);
        dateid.setText(DateFormat.getDateTimeInstance().format(new Date()));
        arnichemdignprint.setText(SharedPref.getInstance(this).FirstName()+SharedPref.getInstance(this).LastName());
        duradelprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printBluetooth();

            }
        });

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;


        }

        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Convert Set to List for easy handling
            final List<BluetoothDevice> deviceList = new ArrayList<>(pairedDevices);
            final CharSequence[] deviceNames = new CharSequence[deviceList.size()];

            for (int i = 0; i < deviceList.size(); i++) {
                deviceNames[i] = deviceList.get(i).getName();
            }

            // Show a dialog with paired devices
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a Bluetooth Device");
            builder.setItems(deviceNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BluetoothDevice device = deviceList.get(which);
                    selectedDevice = new BluetoothConnection(device);
                    if (ActivityCompat.checkSelfPermission(closing_stock_print
                            .this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    printBluetooth();
                    Toast.makeText(getApplicationContext(), "Selected Device: " + device.getName(), Toast.LENGTH_SHORT).show();
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
                newlist.add(cursor.getString(1));



            }


        }
    }

    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[C]        Stock     \n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.phone_number, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.printlogo, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n\n" +
                        "[C]<font size='small'>Srno -  "+empb+"</font>\n" +
                        "[C]<font size='small'>Date -  "+DateFormat.getDateTimeInstance().format(new Date())+"</font>\n" +
                        "[C]<font size='small'>warehouse -  "+warehouse+"</font>\n" +
                        "[C]<font size='small'>       Cylinder Details </font>\n" +
                        "[C]<font size='small'><b>       Cylinder Numbers </b></font>\n" +
                        "[C]<font size='small'><b>            "+foreaching()+"</b></font>\n" +
                        "[C]<font size='small'>Total Quantity : "+count+"</font>\n\n\n" +
                        "[R]                [R]"+SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName()+"\n" +
                        "[R]             [R]"+"For "+SharedPref.getInstance(this).getCompanyFullName()+"\n\n"+
                        "[R]"+SharedPref.getInstance(this).getTermsText()+"\n"
        );
    }

    public Serializable foreaching() {
        StringBuffer text = new StringBuffer();

        for (String mark: newlist) {
            text.append(mark.toString()).append('\n').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020');

        }
        return text;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(addClymyDB != null)
            addClymyDB.close();

        addClymyDB.deleteAllData();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(closing_stock_print.this, GOdownMainActivity.class));
    }
}