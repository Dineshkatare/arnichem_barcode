package com.arnichem.arnichem_barcode.Producation.NewAmmonia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.GodownView.Closing_stock.closing_stock_print;
import com.arnichem.arnichem_barcode.Producation.ZeroAir.ZeroAirPrint;
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
import java.util.Set;

public class ammoniaprint extends AppCompatActivity {
    Button duradelprint;
    String batchID,starttimevolume,endtimevolume,manifoldval,count;
    TextView empbid,dateid,startvol,endvol,manifold;
    ammoniaHelper addClymyDB;
    ArrayList<String> newlist;
    ArrayList<String> id, cylindername,dis,vol,disname,distot,iddist,distotvol,distfull,distnet,mani;
    RecyclerView oxyrecy;
    ammoniaprintAdapter oxygenAdapter;
    DistributorHelper distributorHelper;
    List<String> cylinder;
    List<String> cubic;
    List<String> fullwts;
    List<String> manifolds;
    List<String> netwts;
    List<String> Selected;
    String temp="",tempvol;
    Bitmap printLogoDr,phoneNumberDr;
    ImageView printImg;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ammoniaprint);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ammonia Cylinder Fill Print");
        empbid=findViewById(R.id.batchidprint);
        dateid=findViewById(R.id.dateidprint);
        addClymyDB=new ammoniaHelper(ammoniaprint.this);
        distributorHelper=new DistributorHelper(ammoniaprint.this);
        startvol=findViewById(R.id.starttankvol);
        endvol=findViewById(R.id.endtankvol);
        manifold=findViewById(R.id.manifoldval);
        oxyrecy=findViewById(R.id.oxygenfillprintrecyle);
        duradelprint=findViewById(R.id.oxygenFillPrint);
        printImg = findViewById(R.id.printImg);

        cylinder=new ArrayList<String>();
        cubic=new ArrayList<String>();
        fullwts=new ArrayList<String>();
        manifolds=new ArrayList<String>();
        netwts=new ArrayList<String>();
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
        mani=new ArrayList<>();
        iddist=new ArrayList<>();
        disname=new ArrayList<>();
        distot=new ArrayList<>();
        distotvol=new ArrayList<>();
        Selected=new ArrayList<>();
        distfull=new ArrayList<>();
        distnet=new ArrayList<>();
        storeDataInArrays();
        addClymyDB.deleteAllData();
        empbid.setText(batchID);
        startvol.setText(starttimevolume);
        endvol.setText(endtimevolume);
        dateid.setText(DateFormat.getDateTimeInstance().format(new Date()));
        duradelprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printBluetooth();

            }
        });
        oxygenAdapter = new ammoniaprintAdapter(ammoniaprint.this,this, id, cylindername,mani,vol,distfull,distnet);
        oxyrecy.setAdapter(oxygenAdapter);
        oxyrecy.setLayoutManager(new LinearLayoutManager(ammoniaprint.this));

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



    void storeDataInArrays(){
        Cursor cursor = addClymyDB.readAllData();
        if(cursor.getCount() == 0){
//            empty_imageview.setVisibility(View.VISIBLE);

        }else{
            while (cursor.moveToNext()){
                id.add(cursor.getString(0));
                cylindername.add(cursor.getString(1));
                cylinder.add(cursor.getString(1));
                dis.add(cursor.getString(2));
                vol.add(cursor.getString(4));
                cubic.add(cursor.getString(4));
                distfull.add(cursor.getString(3));
                fullwts.add(cursor.getString(3));
                mani.add(cursor.getString(5));
                manifolds.add(cursor.getString(5));
                distnet.add(cursor.getString(6));
                netwts.add(cursor.getString(6));
                Toast.makeText(ammoniaprint.this, ""+cursor.getString(5), Toast.LENGTH_SHORT).show();


                Cursor distcursor = distributorHelper.readAllData();
                if (cursor.getString(2).equalsIgnoreCase(SharedPref.getInstance( ammoniaprint.this).getOwnCode())) {
                    temp=SharedPref.getInstance(ammoniaprint.this).getOwnCode();
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
            int cou = cursor.getCount();
            count= String.valueOf(cou);

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
                "[C]<font size='small'>            Ammonia Fill</font>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,printLogoDr)+"</img>\n\n" +
                        "[C]<font size='small'>Date -  "+DateFormat.getDateTimeInstance().format(new Date())+"</font>\n" +
                        "[C]<font size='sma" +
                        "ll'>Batch ID  :"+batchID+"</font>\n" +
                        "[C]<font size='small'>Tank Start Volume :"+starttimevolume+"</font>\n" +
                        "[C]<font size='small'>Tank End Volume   :"+endtimevolume+"</font>\n" +
                        "[C]<font size='small'>cylinder No - Tare Wt - Net Wt </font>\n" +
                        "[L]<font size='small'>"+forlooping()+"</font>\n" +
                        "[C]<font size='small'>"+ SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName()+"</font>\n"+
                        "[C]<font size='small'>Supervisor</font>\n"
        );
    }


    public Serializable forlooping() {
        StringBuffer text = new StringBuffer();
        for(int i=0; i<Selected.size();i++) {
            text.append(" "+cylinder.get(i)).append("       ").append(cubic.get(i)).append("     ").append(netwts.get(i)).append('\n');
        }
        return text;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ammoniaprint.this, ammoniafirstpage.class));
    }
}