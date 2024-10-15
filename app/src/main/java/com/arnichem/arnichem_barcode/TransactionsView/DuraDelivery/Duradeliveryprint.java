package com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery;


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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.PrintReceipt.DuraDeliveyPrint.DuraDeliveryPrintDB;
import com.arnichem.arnichem_barcode.PrintReceipt.GodownDeliveryPrintActivity.GodownDeliveryPrintView;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliveryprint;
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
import java.util.List;
import java.util.Set;

public class Duradeliveryprint extends AppCompatActivity {
    Button duradelprint;
    String gastype,Fullwt,tarewt,NetWt,cubic,durano,custname,pressure,dcno,cust_code,sign_path;
    TextView customername,duranumber,fullwt,gasType,Tarewt,Netwt,gas,vehicleno,arnichemdignprint,dcnoid,dtid,tvcode;
    DuraDeliveryPrintDB duraDeliveryPrintDB;
    Bitmap printLogoDr,phoneNumberDr,digital_sign;
    ImageView printImg,phoneImg,custnamesign;
    TextView arnichemsignTxt,termsTxt;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duradeliveryprint);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dcnoid=findViewById(R.id.dcnoid);
        dtid=findViewById(R.id.dtid);
        customername=findViewById(R.id.customername);
        duranumber=findViewById(R.id.duranumber);
        fullwt=findViewById(R.id.fullwt);
        gasType =findViewById(R.id.gasType);
        tvcode=findViewById(R.id.codeid);
        Tarewt=findViewById(R.id.Tarewt);
        Netwt=findViewById(R.id.Netwt);
        gas=findViewById(R.id.gas);
        vehicleno=findViewById(R.id.vehicleno);
        custnamesign = findViewById(R.id.custnamesign);
        arnichemdignprint=findViewById(R.id.cdarnichemdignprint);
        duradelprint=findViewById(R.id.duradelprint);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        termsTxt = findViewById(R.id.termsTxt);

        duraDeliveryPrintDB=new DuraDeliveryPrintDB(this);
        Intent i=getIntent();
        gastype=i.getStringExtra("gastype");
        Fullwt=i.getStringExtra("Fullwt");
        tarewt=i.getStringExtra("tarewt");
        NetWt=i.getStringExtra("NetWt");
        cubic=i.getStringExtra("cubic");
        durano=i.getStringExtra("durano");
        custname=i.getStringExtra("custname");
        pressure=i.getStringExtra("pressure");
        dcno=i.getStringExtra("dcno");
        cust_code=i.getStringExtra("custcode");
        sign_path=i.getExtras().getString("sign_path","");

        dcnoid.setText(dcno);
        dtid.setText(DateFormat.getDateTimeInstance().format(new Date()));
        fullwt.setText(Fullwt);
        duranumber.setText(durano);
        tvcode.setText(cust_code);
        gasType.setText(gastype);
        customername.setText(custname);
        Tarewt.setText(tarewt);
        Netwt.setText(NetWt);
        gas.setText(cubic);
        vehicleno.setText(SharedPref.getInstance(this).getVehicleNo());
        arnichemdignprint.setText(SharedPref.getInstance(this).FirstName()+SharedPref.getInstance(this).LastName());

        duradelprint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {

                printBluetooth();



            }
        });
        duraDeliveryPrintDB.addBook(durano,DateFormat.getDateTimeInstance().format(new Date()),cust_code,   custname,Fullwt,tarewt,NetWt,cubic,dcno);
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


    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[C]        Delivery challan  \n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,phoneNumberDr)+"</img>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,printLogoDr)+"</img>\n\n" +
                        "[C]<font size='small'>DC NO - "+dcno+"</font>\n" +
                        "[C]<font size='small'>Date  - "+DateFormat.getDateTimeInstance().format(new Date())+"</font>\n" +
                        "[C]<font size='small'>Code -  "+cust_code+"</font>\n" +
                        "[C]<font size='small'>Name -  "+custname+"</font>\n" +
                        "[C]<font size='small'>       Cylinder Details </font>\n" +
                        "[C]<font size='small'><b>Dura Number   :  "+durano+"</b></font>\n" +
                        "[C]<font size='small'>Gas type   :  "+gastype+"</font>\n" +
                        "[C]<font size='small'>Full Weight   :  "+Fullwt+"</font>\n" +
                        "[C]<font size='small'>Tare Weight   :  "+tarewt+"</font>\n" +
                        "[C]<font size='small'>Net Weight    :  "+NetWt+"</font>\n" +
                        "[C]<font size='small'>Gas           :  "+cubic+"</font>\n" +
                        "[C]<font size='small'>Vehicle Number:  "+ SharedPref.getInstance(this).getVehicleNo()+"</font>\n\n" +
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
        startActivity(new Intent(Duradeliveryprint.this, Transactions.class));
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

}