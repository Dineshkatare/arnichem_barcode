package com.arnichem.arnichem_barcode.other_entries;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
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

import com.arnichem.arnichem_barcode.GodownView.GOdownMainActivity;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDelPrint;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryHelper;
import com.arnichem.arnichem_barcode.PrintReceipt.GodownDeliveryPrintActivity.GodownDelPrintDB;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CylinderNamePrintAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OtherEntryPrintActivity extends AppCompatActivity {
    CylinderNamePrintAdapter cylinderNamePrintAdapter;


    ArrayList<String> name, tot,fillWithList;
    ArrayList<String> cylIdList;

    RecyclerView cyclinderNames;
    Button duradelprint;
    String durano,custname,empb,custcode,count,sign_path;
    TextView empbid,dateid,custnameid,cylindernumberempty,arnichemdignprint,counttxt,tvcode;
    OtherEntryHelper deliDB;
    ArrayList<String> newlist;
    GodownDelPrintDB godownDelPrintDB;
    Bitmap printLogoDr,phoneNumberDr,digital_sign;
    ImageView printImg,phoneImg,custnamesign;
    TextView arnichemsignTxt,termsTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_entry_print);
        empbid=findViewById(R.id.cdcnoid);
        dateid=findViewById(R.id.cddateid);
        deliDB=new OtherEntryHelper(OtherEntryPrintActivity.this);
        custnameid=findViewById(R.id.cdcustnameid);
        counttxt=findViewById(R.id.totalq);
        custnamesign = findViewById(R.id.custnamesign);
        cyclinderNames = findViewById(R.id.cyclinderNames);
        // cylindernumberempty=findViewById(R.id.cylindernumberdel);
        arnichemdignprint=findViewById(R.id.cdarnichemdignprint);
        duradelprint=findViewById(R.id.delyprint);
        tvcode=findViewById(R.id.codeid);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        termsTxt = findViewById(R.id.termsTxt);

        godownDelPrintDB=new GodownDelPrintDB(this);
        Intent i=getIntent();
        custname=i.getStringExtra("custname");
        empb=i.getStringExtra("empb");
        custcode=i.getStringExtra("custcode");
        durano= i.getStringExtra("cylinder");
        count=i.getStringExtra("count");
        sign_path=i.getExtras().getString("sign_path","");

        newlist=new ArrayList<String>();
        name=new ArrayList<>();
        tot=new ArrayList<>();
        newlist = new ArrayList<String>();
        fillWithList = new ArrayList<>();
        cylIdList = new ArrayList<>();
        cylinder();
        empbid.setText(empb);
        deliDB.deleteAllData();
        tvcode.setText(custcode);
        counttxt.setText(count);
        custnameid.setText(custname);
//        String joined = TextUtils.join(",", newlist);
//        cylindernumberempty.setText(joined);
        dateid.setText(DateFormat.getDateTimeInstance().format(new Date()));

        cylinderNamePrintAdapter = new CylinderNamePrintAdapter(OtherEntryPrintActivity.this, this, cylIdList, newlist, fillWithList);
        cyclinderNames.setAdapter(cylinderNamePrintAdapter);
        cyclinderNames.setLayoutManager(new LinearLayoutManager(OtherEntryPrintActivity.this));
        arnichemdignprint.setText(SharedPref.getInstance(this).FirstName()+SharedPref.getInstance(this).LastName());
        duradelprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printBluetooth();

            }
        });
        String print_logo = SharedPref.mInstance.getPrintLogo();
        File imgFile = new  File(print_logo);
        if(imgFile.exists()){
            printLogoDr = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            printImg.setImageBitmap(printLogoDr);
        }

        String phoneNumber = SharedPref.mInstance.getPhoneNumber();
        File imgFilePhoneNumber = new  File(phoneNumber);
        if(imgFile.exists()){
            phoneNumberDr = BitmapFactory.decodeFile(imgFilePhoneNumber.getAbsolutePath());
            phoneImg.setImageBitmap(phoneNumberDr);
        }
        arnichemsignTxt.setText("For "+SharedPref.mInstance.getCompanyFullName());
        termsTxt.setText(SharedPref.mInstance.getTermsText());

        if(!sign_path.isEmpty()){
            File signFile = new File(sign_path);
            if(signFile.exists()){
                digital_sign = BitmapFactory.decodeFile(signFile.getAbsolutePath());
                digital_sign = Bitmap.createScaledBitmap(digital_sign,200, 200, true);

                custnamesign.setImageBitmap(digital_sign);
            }
        }else {
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

        Cursor cursor = deliDB.readAllData();
        if (cursor.getCount() == 0) {


        } else {

            while (cursor.moveToNext()) {
                cylIdList.add(cursor.getString(0));
                newlist.add(cursor.getString(1));
                fillWithList.add(cursor.getString(3));
                godownDelPrintDB.addBook(cursor.getString(1),DateFormat.getDateTimeInstance().format(new Date()),custcode,custname,count,empb);


            }


        }
    }


    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[C]        Other Delivery  \n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,phoneNumberDr)+"</img>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,printLogoDr)+"</img>\n\n" +
                        "[C]<font size='small'>DCNO -  "+empb+"</font>\n" +
                        "[C]<font size='small'>Date -  "+DateFormat.getDateTimeInstance().format(new Date())+"</font>\n" +
                        "[C]<font size='small'>Code -  "+custcode+"</font>\n" +
                        "[C]<font size='small'>Name -  "+custname+"</font>\n" +
                        "[C]<font size='small'>          Details </font>\n" +
                        "[C]<font size='small'><b>       "+foreaching()+"</b></font>\n" +
                        "[C]<font size='small'>--------------------------------</font>" +
                        "[C]<font size='small'>Total Quantity : "+count+"</font>\n" +
                        "[C]<font size='small'>Invoice No     :  "+"  "+"</font>\n\n\n" +
                        "[L]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,digital_sign)+"</img>\n" +
                        "[R]               [R]"+SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName()+"\n" +
                        "[R]Customer Sign [R]"+"For "+SharedPref.getInstance(this).getCompanyFullName()+"\n\n"+
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
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(OtherEntryPrintActivity.this, Dashboard.class));
    }
}