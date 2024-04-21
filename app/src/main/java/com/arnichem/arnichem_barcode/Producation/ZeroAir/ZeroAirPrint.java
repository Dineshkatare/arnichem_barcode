package com.arnichem.arnichem_barcode.Producation.ZeroAir;

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
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnichem.arnichem_barcode.Producation.Nitrogen.NitrogenPrint;
import com.arnichem.arnichem_barcode.Producation.Oxygen.oxygenprintadapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.DistributorHelper;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ZeroAirPrint extends AppCompatActivity {
    Button duradelprint;
    String batchID,starttimevolume,endtimevolume,manifoldval;
    TextView empbid,dateid,startvol,endvol,manifold;
    ZeroAirHelper addClymyDB;
    ArrayList<String> newlist;
    ArrayList<String> id, cylindername,dis,vol;
    RecyclerView oxyrecy;
    oxygenprintadapter oxygenAdapter;
    DistributorHelper distributorHelper;
    List<String> cylinder;
    List<String> cubic;
    String temp="",tempvol;
    List<String> Selected;
    Bitmap printLogoDr;
    ImageView printImg;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zero_air_print);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("ZeroAir Cylinder Fill Print");
        empbid=findViewById(R.id.batchidprint);
        dateid=findViewById(R.id.dateidprint);
        addClymyDB=new ZeroAirHelper(ZeroAirPrint.this);
        distributorHelper=new DistributorHelper(ZeroAirPrint.this);
        startvol=findViewById(R.id.starttankvol);
        endvol=findViewById(R.id.endtankvol);
        manifold=findViewById(R.id.manifoldval);
        oxyrecy=findViewById(R.id.oxygenfillprintrecyle);
        duradelprint=findViewById(R.id.oxygenFillPrint);
        printImg = findViewById(R.id.printImg);

        Intent i=getIntent();
        batchID=i.getStringExtra("batchDt");
        starttimevolume=i.getStringExtra("starttimevolume");
        endtimevolume= i.getStringExtra("endtimevolume");
        manifoldval= i.getStringExtra("manifoldval");
        newlist=new ArrayList<String>();
        id = new ArrayList<>();
        cylindername =new ArrayList<>();
        dis=new ArrayList<>();
        vol=new ArrayList<>();
        Selected=new ArrayList<>();
        storeDataInArrays();
        addClymyDB.deleteAllData();
        empbid.setText(batchID);
        startvol.setText(starttimevolume);
        endvol.setText(endtimevolume);
        manifold.setText(manifoldval);
        dateid.setText(DateFormat.getDateTimeInstance().format(new Date()));
        duradelprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printBluetooth();

            }
        });
        oxygenAdapter = new oxygenprintadapter(ZeroAirPrint.this,this, id, cylindername,dis,vol);
        oxyrecy.setAdapter(oxygenAdapter);
        oxyrecy.setLayoutManager(new LinearLayoutManager(ZeroAirPrint.this));
        String print_logo = SharedPref.mInstance.getPrintLogo();
        File imgFile = new  File(print_logo);
        if(imgFile.exists()){
            printLogoDr = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            printImg.setImageBitmap(printLogoDr);
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




    void storeDataInArrays(){
        Cursor cursor = addClymyDB.readAllData();
        if(cursor.getCount() == 0){
        }else{
            while (cursor.moveToNext()){
                id.add(cursor.getString(0));
                cylindername.add(cursor.getString(1));
                dis.add(cursor.getString(2));
                vol.add(cursor.getString(3));
                Cursor distcursor = distributorHelper.readAllData();
                if (cursor.getString(2).equalsIgnoreCase(SharedPref.getInstance( ZeroAirPrint.this).getOwnCode())) {
                    temp=SharedPref.getInstance(ZeroAirPrint.this).getOwnCode();
                } else {

                    while (distcursor.moveToNext()) {
                        String col=distcursor.getString(1);
                        String col1 =distcursor.getString(2);
                        if(col.contentEquals(cursor.getString(2)))
                        {
                            temp=col1;
                        }
                    }
                }
                Selected.add(temp);


            }


        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(addClymyDB != null)
            addClymyDB.close();

        addClymyDB.deleteAllData();


    }

    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[C]<font size='small'>ZeroAir Fill</font>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,printLogoDr)+"</img>\n\n" +
                        "[C]<font size='small'>Date -  "+DateFormat.getDateTimeInstance().format(new Date())+"</font>\n" +
                        "[C]<font size='small'>Batch ID          :"+batchID+"</font>\n" +
                        "[C]<font size='small'>Tank Start Volume :"+starttimevolume+"</font>\n" +
                        "[C]<font size='small'>Tank End Volume   :"+endtimevolume+"</font>\n" +
                        "[C]<font size='small'>Manifold No       :"+manifoldval+"</font>\n" +
                        "[C]<font size='small'><b>       Cylinder Numbers </b></font>\n" +
                        "[C]<font size='small'>Owner - Cylinder Number - Volume </font>\n" +
                        "[L]<font size='small'>"+forlooping()+"</font>\n" +
                        "[C]<font size='small'>"+ SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName()+"</font>\n"+
                        "[C]<font size='small'>Supervisor</font>\n"
        );
    }


    public Serializable forlooping() {
        StringBuffer text = new StringBuffer();
        for(int i=0; i<Selected.size();i++) {
            text.append(Selected.get(i)).append("    ").append(cylindername.get(i)).append("        ").append(vol.get(i)).append("m3").append('\n');
        }
        return text;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ZeroAirPrint.this, FirstZeroAir.class));
    }
}