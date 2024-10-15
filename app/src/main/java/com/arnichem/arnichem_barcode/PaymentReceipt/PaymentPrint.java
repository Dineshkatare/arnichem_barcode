package com.arnichem.arnichem_barcode.PaymentReceipt;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.GodownView.Closing_stock.closing_stock_print;
import com.arnichem.arnichem_barcode.PrintReceipt.EmptyPrint.EmptyPrintDB;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.AddClyHelper;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.Empty_Print;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
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
import java.util.List;
import java.util.Set;

public class PaymentPrint extends AppCompatActivity {
    Button payPrint;
    String paymentmodestr,custname,srno,cust_code,count,amountstr,inwords,particularStr;
    TextView srnoId,dateid,custnameid,amount,particularTv,vehicleno,paymentModeId,inWordsId,tvcode,cdarnichemdignprint;
    EmptyPrintDB emptyPrintDB;
    Bitmap printLogoDr,phoneNumberDr;
    ImageView printImg,phoneImg;
    TextView arnichemsignTxt,termsTxt;

    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_print);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        srnoId = findViewById(R.id.srnoId);
        dateid = findViewById(R.id.cddateid);
        custnameid = findViewById(R.id.cdcustnameid);
        tvcode = findViewById(R.id.cust_code_id);
        amount = findViewById(R.id.amount);
        paymentModeId = findViewById(R.id.paymentModeId);
        particularTv= findViewById(R.id.particularTv);
        vehicleno = findViewById(R.id.cdvehicleno);
        inWordsId = findViewById(R.id.inWordsId);
        payPrint = findViewById(R.id.payPrint);
        cdarnichemdignprint=findViewById(R.id.cdarnichemdignprint);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        termsTxt = findViewById(R.id.termsTxt);

        emptyPrintDB = new EmptyPrintDB(this);
        Intent i = getIntent();
        custname = i.getStringExtra("custname");
        srno = i.getStringExtra("srno");
        cust_code = i.getStringExtra("custcode");
        amountstr = i.getStringExtra("amountstr");
        paymentmodestr = i.getStringExtra("paymentstr");
        particularStr = i.getStringExtra("particularStr");
        inwords=Currency.convertToIndianCurrency(amountstr);
        particularTv.setText(particularStr);
        srnoId.setText(srno);
        custnameid.setText(custname);
        tvcode.setText(cust_code);
        amount.setText(amountstr);
        inWordsId.setText(inwords);
        paymentModeId.setText(paymentmodestr);
        dateid.setText(DateFormat.getDateTimeInstance().format(new Date()));
//        vehicleno.setText(SharedPref.getInstance(this).getVehicleNo());
        cdarnichemdignprint.setText(SharedPref.getInstance(this).FirstName() + SharedPref.getInstance(this).LastName());
        payPrint.setOnClickListener(new View.OnClickListener() {
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

    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[C-]Payment  Receipt  [R]\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,phoneNumberDr)+"</img>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,printLogoDr)+"</img>\n\n" +
                        "[C]<font size='small'>Sr No -  " + srno + "</font>\n" +
                        "[C]<font size='small'>Date -  " + DateFormat.getDateTimeInstance().format(new Date()) + "</font>\n" +
                        "[C]<font size='small'>Code -  " + cust_code + "</font>\n" +
                        "[C]<font size='small'>Name -  " + custname + "</font>\n" +
                        "[C]<font size='small'>       Payment Details </font>\n" +
                        "[C]<font size='small'>Payment Mode :  " + paymentmodestr + "</font>\n" +
                        "[C]<font size='small'>Particular   :  " + particularStr + "</font>\n" +
                        "[C]<font size='small'>Amount       :  " + amountstr + "</font>\n" +
                        "[C]<font size='small'>In Words     : " + inwords + "</font>\n" +
//                        "[C]<font size='small'>Vehicle No    :  " + SharedPref.getInstance(this).getVehicleNo() + "</font>\n" +
                        "[C]<font size='small'>Invoice No   :  " + "  " + "</font>\n\n\n" +
                        "[R]               [R]" + SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName() + "\n" +
                        "[R]Customer Sign [R]"+"For "+SharedPref.getInstance(this).getCompanyFullName()+"\n\n"+
                        "[R]"+SharedPref.getInstance(this).getTermsText()+"\n"

        );
    }





    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PaymentPrint.this, Transactions.class));
    }
}