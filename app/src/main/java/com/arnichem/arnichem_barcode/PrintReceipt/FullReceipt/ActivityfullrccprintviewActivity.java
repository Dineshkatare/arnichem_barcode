package com.arnichem.arnichem_barcode.PrintReceipt.FullReceipt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.FullRecipt.FullRecPrint;
import com.arnichem.arnichem_barcode.TransactionsView.FullRecipt.FullReciptHelper;
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

public class ActivityfullrccprintviewActivity extends AppCompatActivity {
    Button duradelprint;
    String pos,custname,dcno,custcode,totalQuan,delidate;
    TextView empbid,dateid,custnameid,cylindernumberempty,vehicleno,arnichemdignprint,counttxt,tvcode;
    FullRecePrintDB addClymyDB;
    ArrayList<String> newlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activityfullrccprintview);
        empbid=findViewById(R.id.empbid);
        dateid=findViewById(R.id.dateid);
        tvcode=findViewById(R.id.codeid);
        addClymyDB=new FullRecePrintDB(ActivityfullrccprintviewActivity.this);
        custnameid=findViewById(R.id.custnameid);
        cylindernumberempty=findViewById(R.id.cylindernumberempty);
        arnichemdignprint=findViewById(R.id.arnichemdignprint);
        counttxt=findViewById(R.id.totalq);
        vehicleno=findViewById(R.id.vehicleno);
        duradelprint=findViewById(R.id.duradelemptyprint);
        newlist=new ArrayList<String>();
        Intent i=getIntent();
        pos=i.getStringExtra("pos");
        cylinder();
        empbid.setText(dcno);
        tvcode.setText(custcode);
        counttxt.setText(totalQuan);
        custnameid.setText(custname);
        String joined = TextUtils.join(",", newlist);
        cylindernumberempty.setText(joined);
        dateid.setText(delidate);
        vehicleno.setText(SharedPref.getInstance(this).getVehicleNo());
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
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case finalprint.PERMISSION_BLUETOOTH:
                    this.printBluetooth();
                    break;
            }
        }
    }

    private BluetoothConnection selectedDevice;


    public void printBluetooth() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, finalprint.PERMISSION_BLUETOOTH);
        } else {
            new AsyncBluetoothEscPosPrint(this).execute(this.getAsyncEscPosPrinter(selectedDevice));
        }
    }

    public void cylinder() {

        Cursor cursor = addClymyDB.readAllData();
        if (cursor.getCount() == 0) {


        } else {

            while (cursor.moveToNext()) {
                if(pos.equals(cursor.getString(6))) {
                    newlist.add(cursor.getString(1));
                    delidate = cursor.getString(2);
                    custcode = cursor.getString(3);
                    custname = cursor.getString(4);
                    totalQuan = cursor.getString(5);
                    dcno = cursor.getString(6);
                }




            }


        }
    }
    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[R]FullReceipt  [R]\n" +
                        "[C]<font size='small'>77 76 823 823/84 46 823 823/83 78 823 823</font>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.printlogo, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n\n" +
                        "[C]<font size='small'>FULL -  "+dcno+"</font>\n" +
                        "[C]<font size='small'>Date -  "+delidate+"</font>\n" +
                        "[C]<font size='small'>Code -  "+custcode+"</font>\n" +
                        "[C]<font size='small'>Name -  "+custname+"</font>\n" +
                        "[C]<font size='small'>       Cylinder Details </font>\n" +
                        "[C]<font size='small'><b>       Cylinder Numbers </b></font>\n" +
                        "[C]<font size='small'><b>            "+foreaching()+"</b></font>\n" +
                        "[C]<font size='small'>Total Quantity : "+totalQuan+"</font>\n" +
                        "[C]<font size='small'>Vehicle No    :  "+ SharedPref.getInstance(this).getVehicleNo()+"</font>\n" +
                        "[C]<font size='small'>Invoice No    :  "+"  "+"</font>\n\n\n" +
                        "[R]               [R]"+SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName()+"\n" +
                        "[R]Customer Sign [R]"+"For "+SharedPref.getInstance(this).getCompanyFullName()+"\n"
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityfullrccprintviewActivity.this, Transactions.class));
    }
}