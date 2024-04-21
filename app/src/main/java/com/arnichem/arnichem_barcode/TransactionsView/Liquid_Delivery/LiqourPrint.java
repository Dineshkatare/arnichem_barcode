package com.arnichem.arnichem_barcode.TransactionsView.Liquid_Delivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.AddClyHelper;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LiqourPrint extends AppCompatActivity {
    Button duradelprint;
    String durano,custname,empb,cust_code,delivery_type_code,fullwt,emptywt,netwt,delivery_type,sign_path;
    TextView empbid,dateid,productNameTv,productCodeTv,cylindernumberempty,vehicleno,arnichemdignprint,custnameTv,tvcode,fullwttv,emptywttv,netwttv;
    AddClyHelper addClymyDB;
    ArrayList<String> newlist;
    Bitmap printLogoDr,phoneNumberDr,digital_sign;
    ImageView printImg,phoneImg,custnamesign;
    TextView arnichemsignTxt,termsTxt;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liqour_print);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Liquor Ammonia Delivery Print");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        empbid=findViewById(R.id.empbid);
        dateid=findViewById(R.id.dateid);
        productNameTv=findViewById(R.id.productNameTv);
        productCodeTv=findViewById(R.id.ProductCode);
        tvcode=findViewById(R.id.codeid);
        cylindernumberempty=findViewById(R.id.cylindernumberempty);
        arnichemdignprint=findViewById(R.id.cdarnichemdignprint);
        vehicleno=findViewById(R.id.vehicleno);
        custnamesign = findViewById(R.id.custnamesign);
        duradelprint=findViewById(R.id.duradelemptyprint);
        fullwttv=findViewById(R.id.fullwtval);
        custnameTv=findViewById(R.id.custnameTV);
        emptywttv=findViewById(R.id.emptwtval);
        netwttv=findViewById(R.id.netwtval);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        termsTxt = findViewById(R.id.termsTxt);

        Intent i=getIntent();
        custname=i.getStringExtra("custname");
        empb=i.getStringExtra("empb");
        cust_code=i.getStringExtra("custcode");
        durano= i.getStringExtra("cylinder");
        fullwt=i.getStringExtra("fullwt");
        delivery_type_code=i.getStringExtra("delivery_type_code");
        delivery_type=i.getStringExtra("delivery_type");
        emptywt=i.getStringExtra("emptywt");
        netwt=i.getStringExtra("netwt");
        sign_path=i.getExtras().getString("sign_path","");

        newlist=new ArrayList<String>();

        empbid.setText(empb);
        tvcode.setText(cust_code);
        productNameTv.setText(delivery_type);
        productCodeTv.setText(delivery_type_code);
        custnameTv.setText(custname);
        fullwttv.setText(fullwt);
        emptywttv.setText(emptywt);
        netwttv.setText(netwt);

        dateid.setText(DateFormat.getDateTimeInstance().format(new Date()));
        vehicleno.setText(SharedPref.getInstance(this).getVehicleNo());
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



    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[C]        Delivery challan  \n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,phoneNumberDr)+"</img>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,printLogoDr)+"</img>\n\n" +
                        "[C]<font size='small'>Date :  "+DateFormat.getDateTimeInstance().format(new Date())+"</font>\n" +
                        "[C]<font size='small'>Code :  "+cust_code+"</font>\n" +
                        "[C]<font size='small'>Name :  "+custname+"</font>\n" +
                        "[C]<font size='small'>Vehicle No  :"+ SharedPref.getInstance(this).getVehicleNo()+"</font>\n" +
                        "[C]<font size='small'>Product Name  : "+delivery_type+"</font>\n" +
                        "[C]<font size='small'>Product Code  : "+delivery_type_code+"</font>\n" +
                        "[C]<font size='small'>DC No -  "+empb+"</font>\n" +
                        "[C]<font size='small'>       Tanker Details </font>\n" +
                        "[C]<font size='small'>Full Weight  = "+fullwt+"</font>\n" +
                        "[C]<font size='small'>Empty Weight = "+emptywt+"</font>\n" +
                        "[C]<font size='small'>Net Weight   = "+netwt+"</font>\n" +
                        "[C]<font size='small'>Invoice No    :  "+"  "+"</font>\n\n\n" +
                        "[L]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,digital_sign)+"</img>\n" +
                        "[R]               [R]"+SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName()+"\n" +
                        "[R]Customer Sign [R]"+"For "+SharedPref.getInstance(this).getCompanyFullName()+"\n\n"+
                        "[R]"+SharedPref.getInstance(this).getTermsText()+"\n"
        );
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(LiqourPrint.this, Transactions.class));
    }
}