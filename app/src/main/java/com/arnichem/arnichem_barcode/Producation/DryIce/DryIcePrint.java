package com.arnichem.arnichem_barcode.Producation.DryIce;

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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.GodownView.Closing_stock.closing_stock_print;
import com.arnichem.arnichem_barcode.Producation.Co2.Co2Helper;
import com.arnichem.arnichem_barcode.Producation.Co2.Co2Print;
import com.arnichem.arnichem_barcode.Producation.Oxygen.FisrtPart;
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
import java.util.Set;

public class DryIcePrint extends AppCompatActivity {
    Button duradelprint;
    String batchID,starttimevolume,endtimevolume,manifoldval;
    TextView empbid,dateid,startvol,endvol,manifold;
    DryIceHelper addClymyDB;
    ArrayList<String> newlist;
    ArrayList<String> id, cylindername,dis,vol;
    RecyclerView oxyrecy;
    oxygenprintadapter oxygenAdapter;
    DistributorHelper distributorHelper;
    List<String> cylinder;
    List<String> cubic;
    String temp,tempvol;
    List<String> Selected;
    Bitmap printLogoDr;
    ImageView printImg;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dry_ice_print);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Dry Ice Print");
        empbid=findViewById(R.id.batchidprint);
        dateid=findViewById(R.id.dateidprint);
        addClymyDB=new DryIceHelper(DryIcePrint.this);
        distributorHelper=new DistributorHelper(DryIcePrint.this);
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
        oxygenAdapter = new oxygenprintadapter(DryIcePrint.this,this, id, cylindername,dis,vol);
        oxyrecy.setAdapter(oxygenAdapter);
        oxyrecy.setLayoutManager(new LinearLayoutManager(DryIcePrint.this));

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
        }else{
            while (cursor.moveToNext()){
                id.add(cursor.getString(0));
                cylindername.add(cursor.getString(1));
                dis.add(cursor.getString(2));
                vol.add(cursor.getString(3));
                Cursor distcursor = distributorHelper.readAllData();
                if (cursor.getString(2).equalsIgnoreCase(SharedPref.getInstance(DryIcePrint.this).getOwnCode())) {
                    temp=SharedPref.getInstance(DryIcePrint.this).getOwnCode();
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
                "[C]<font size='small'>CO2 Fill</font>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,printLogoDr)+"</img>\n\n" +
                        "[C]<font size='small'>Date -  "+DateFormat.getDateTimeInstance().format(new Date())+"</font>\n" +
                        "[C]<font size='small'>Batch ID          :"+batchID+"</font>\n" +
                        "[C]<font size='small'>Tank Start Volume :"+starttimevolume+"</font>\n" +
                        "[C]<font size='small'>Tank End Volume   :"+endtimevolume+"</font>\n" +
                        "[C]<font size='small'>Manifold No       :"+manifoldval+"</font>\n" +
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
        startActivity(new Intent(DryIcePrint.this, FisrtPart.class));
    }
}