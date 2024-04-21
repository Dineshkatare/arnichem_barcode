package com.arnichem.arnichem_barcode.GodownView.Closing_stock.NewClosingStock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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

import com.arnichem.arnichem_barcode.GodownView.Closing_stock.closing_helper;
import com.arnichem.arnichem_barcode.GodownView.Closing_stock.closing_stock_print;
import com.arnichem.arnichem_barcode.GodownView.GOdownMainActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.InventoryGases;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.connection.tcp.TcpConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClosingPrint extends AppCompatActivity {
    Button duradelprint;
    String warehouse,empb;
    TextView empbid,dateid,warehouseval,arnichemdignprint;
    InventoryGases inventoryGases;
    RecyclerView editableRecycle;
    ClosingPrintAdapter closingPrintAdapter;
    List<String> gasType;
    List<String> fullWt;
    List<String> empWt;
    Bitmap printLogoDr,phoneNumberDr;
    ImageView printImg,phoneImg;
    TextView arnichemsignTxt,termsTxt;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closing_print);
        empbid=findViewById(R.id.empbid);
        dateid=findViewById(R.id.dateid);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Closing Stock Print");
        inventoryGases = new InventoryGases(ClosingPrint.this);
        editableRecycle = findViewById(R.id.editableRecycle);

        warehouseval=findViewById(R.id.warehouseval);
        arnichemdignprint=findViewById(R.id.arnichemdignprint);
        duradelprint=findViewById(R.id.duradelemptyprint);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        termsTxt = findViewById(R.id.termsTxt);

        Intent i=getIntent();
        empb=i.getStringExtra("empb");
        warehouse= i.getStringExtra("warehouse");

        gasType = new ArrayList<>();
        fullWt = new ArrayList<>();
        empWt = new ArrayList<>();

        empbid.setText(empb);
        warehouseval.setText(warehouse);
        closingPrintAdapter = new ClosingPrintAdapter(ClosingPrint.this);
        editableRecycle.setAdapter(closingPrintAdapter);
        editableRecycle.setLayoutManager(new LinearLayoutManager(ClosingPrint.this));
        dateid.setText(DateFormat.getDateTimeInstance().format(new Date()));
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
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);
        return printer.setTextToPrint(
                "[C]      Closing  Stock  \n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,phoneNumberDr)+"</img>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,printLogoDr)+"</img>\n\n" +
                        "[L]<font size='small'>Srno -  "+empb+"</font>\n" +
                        "[L]<font size='small'>Date -  "+DateFormat.getDateTimeInstance().format(new Date())+"</font>\n" +
                        "[L]<font size='small'>warehouse -  "+warehouse+"</font>\n" +
                        "[C]<font size='small'><b>    Cylinder Details</b> </font>\n\n" +
                        "[C]<font size='small'><b>GasType  Total Full  Total Empty</b></font>\n\n"
                        +foreaching()+"\n\n"+
                        "[R]                [R]"+SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName()+"\n" +
                        "[R]               [R]"+"For "+SharedPref.getInstance(this).getCompanyFullName()+"\n\n"+
                        "[R]"+SharedPref.getInstance(this).getTermsText()+"\n"
        );
    }


    public Serializable foreaching() {
        StringBuffer text = new StringBuffer();
        for (ClosingModel closingModel: ClosingStockMain.closingModelList) {
            if(closingModel.getFull_Wt().isEmpty()||closingModel.getFull_Wt()==null)
            {
                closingModel.setFull_Wt("0");
            }
            if(closingModel.getEmp_wt().isEmpty()||closingModel.getEmp_wt()==null)
            {
                closingModel.setEmp_wt("0");
            }
            text.append("[L]<b>"+closingModel.getGasType().toString()+"</b>[C]"+closingModel.getFull_Wt().toString()+"[R]"+closingModel.getEmp_wt().toString()+"\n");
        }
        return text;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}