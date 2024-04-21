package com.arnichem.arnichem_barcode.TransactionsView.FullRecipt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.PrintReceipt.FullReceipt.FullRecePrintDB;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CylinderNamePrintAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;
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

public class FullRecPrint extends AppCompatActivity {
    Button duradelprint;
    CylinderNamePrintAdapter cylinderNamePrintAdapter;
    FilledWithAdapter filledWithAdapter;
    ArrayList<String> name, tot, fillWithList;
    ArrayList<String> cylIdList;


    String durano, custname, empb, cust_code, count, sign_path;
    TextView empbid, dateid, custnameid, cylindernumberempty, vehicleno, arnichemdignprint, counttxt, tvcode;
    FullReciptHelper addClymyDB;
    ArrayList<String> newlist;
    FullRecePrintDB fullRecePrintDB;
    Bitmap printLogoDr, phoneNumberDr, digital_sign;
    ImageView printImg, phoneImg, custnamesign;
    TextView arnichemsignTxt, termsTxt;
    RecyclerView Filled_with_Recycle_View, cyclinderNames;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_rec_print);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        empbid = findViewById(R.id.empbid);
        dateid = findViewById(R.id.dateid);
        tvcode = findViewById(R.id.codeid);
        addClymyDB = new FullReciptHelper(FullRecPrint.this);
        custnameid = findViewById(R.id.custnameid);
        cylindernumberempty = findViewById(R.id.cylindernumberempty);
        arnichemdignprint = findViewById(R.id.cdarnichemdignprint);
        counttxt = findViewById(R.id.totalq);
        custnamesign = findViewById(R.id.custnamesign);
        vehicleno = findViewById(R.id.vehicleno);
        duradelprint = findViewById(R.id.duradelemptyprint);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        termsTxt = findViewById(R.id.termsTxt);
        Filled_with_Recycle_View = findViewById(R.id.fillwithrec);
        cyclinderNames = findViewById(R.id.cyclinderNames);
        fullRecePrintDB = new FullRecePrintDB(this);
        Intent i = getIntent();
        custname = i.getStringExtra("custname");
        empb = i.getStringExtra("empb");
        cust_code = i.getStringExtra("custcode");
        durano = i.getStringExtra("cylinder");
        count = i.getStringExtra("count");
        sign_path = i.getExtras().getString("sign_path", "");

        name = new ArrayList<>();
        tot = new ArrayList<>();
        newlist = new ArrayList<String>();
        fillWithList = new ArrayList<>();
        cylIdList = new ArrayList<>();

        check();

        cylinder();

        addClymyDB.deleteAllData();
        empbid.setText(empb);
        tvcode.setText(cust_code);
        counttxt.setText(count);
        custnameid.setText(custname);
//        String joined = TextUtils.join(",", newlist);
//        cylindernumberempty.setText(joined);
        dateid.setText(DateFormat.getDateTimeInstance().format(new Date()));
        vehicleno.setText(SharedPref.getInstance(this).getVehicleNo());
        arnichemdignprint.setText(SharedPref.getInstance(this).FirstName() + SharedPref.getInstance(this).LastName());
        duradelprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printBluetooth();

            }
        });
        Filled_with_Recycle_View = findViewById(R.id.fillwithrec);
        filledWithAdapter = new FilledWithAdapter(FullRecPrint.this, this, name, tot);
        Filled_with_Recycle_View.setAdapter(filledWithAdapter);
        cylinderNamePrintAdapter = new CylinderNamePrintAdapter(FullRecPrint.this, this, cylIdList, newlist, fillWithList);
        cyclinderNames.setAdapter(cylinderNamePrintAdapter);
        cyclinderNames.setLayoutManager(new LinearLayoutManager(FullRecPrint.this));
        Filled_with_Recycle_View.setLayoutManager(new LinearLayoutManager(FullRecPrint.this));

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
        arnichemsignTxt.setText("For " + SharedPref.mInstance.getCompanyFullName());
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
                cylIdList.add(cursor.getString(0));
                newlist.add(cursor.getString(1));
                fillWithList.add(cursor.getString(2));
                fullRecePrintDB.addBook(cursor.getString(1), DateFormat.getDateTimeInstance().format(new Date()), cust_code, custname, count, empb);


            }


        }
    }

    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[C]        FullReceipt  \n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, phoneNumberDr) + "</img>\n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, printLogoDr) + "</img>\n\n" +
                        "[C]<font size='small'>FULL -  " + empb + "</font>\n" +
                        "[C]<font size='small'>Date -  " + DateFormat.getDateTimeInstance().format(new Date()) + "</font>\n" +
                        "[C]<font size='small'>Code -  " + cust_code + "</font>\n" +
                        "[C]<font size='small'>Name -  " + custname + "</font>\n" +
                        "[C]<font size='small'>       Cylinder Details </font>\n" +
                        "[C]<font size='small'><b>       Cylinder Numbers </b></font>\n" +
                        "[C]<font size='small'><b>     " + foreaching() + "</b></font>\n" +
                        "[C]<font size='small'>--------------------------------</font>" +
                        "[C]<font size='small'>        " + foreachname() + "</font>\n" +
                        "[C]<font size='small'>Total Quantity : " + count + "</font>\n" +
                        "[C]<font size='small'>Vehicle No    :  " + SharedPref.getInstance(this).getVehicleNo() + "</font>\n" +
                        "[C]<font size='small'>Invoice No    :  " + "  " + "</font>\n\n\n" +
                        "[L]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, digital_sign) + "</img>\n" +
                        "[R]               [R]" + SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName() + "\n" +
                        "[R]Customer Sign [R]" + "For " + SharedPref.getInstance(this).getCompanyFullName() + "\n\n" +
                        "[R]" + SharedPref.getInstance(this).getTermsText() + "\n"
        );
    }

    public Serializable foreaching() {
        StringBuffer text = new StringBuffer();

        for (int i = 0; i < newlist.size(); i++) {
            text.append(newlist.get(i)).append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append(fillWithList.get(i)).append('\n').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020');

        }
        return text;
    }

    public Serializable foreachname() {
        StringBuffer text = new StringBuffer();

        for (int i = 0; i < name.size(); i++) {
            text.append(name.get(i)).append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append(tot.get(i)).append('\n').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020');
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(FullRecPrint.this, Transactions.class));
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

}