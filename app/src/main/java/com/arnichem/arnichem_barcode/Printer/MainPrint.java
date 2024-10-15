package com.arnichem.arnichem_barcode.Printer;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arnichem.arnichem_barcode.GodownView.Closing_stock.closing_stock_print;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class MainPrint extends AppCompatActivity{
    protected static final String TAG = "TAG";
    Button mPrint;
    String batchDt,mfgDt,deliveryDt,grossWt,tareWt,netWt,Gas,pressure,Durano,gasType;
    TextView tvbatchDt,tvmfgDt,tvdduranovalue,tvgrossWt,tvtareWt,tvnetWt,tvGas,tvpressure,gas_type;
    @Override
    public void onCreate(Bundle mSavedInstanceState) {
        super.onCreate(mSavedInstanceState);
        setContentView(R.layout.activity_main_print);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Print Recipt");
        tvbatchDt=findViewById(R.id.batchno);
        tvmfgDt=findViewById(R.id.mfgdt);
        tvdduranovalue=findViewById(R.id.duranovalue);
        tvgrossWt=findViewById(R.id.Grosswt);
        tvtareWt=findViewById(R.id.Tarewt);
        tvnetWt=findViewById(R.id.Netwt);
        tvGas=findViewById(R.id.gas);
        tvpressure=findViewById(R.id.pressure);
        gas_type = findViewById(R.id.gas_type);
        Intent i=getIntent();
        batchDt=i.getStringExtra("batchDt");
        Durano=i.getStringExtra("durano");
        deliveryDt=i.getStringExtra("deliveryDt");
        grossWt=i.getStringExtra("grossWt");
        tareWt=i.getStringExtra("tareWt");
        netWt=i.getStringExtra("netWt");
        Gas=i.getStringExtra("Gas");
        pressure=i.getStringExtra("pressure");
        gasType = i.getStringExtra("gas_type");
        tvbatchDt.setText(batchDt);
        tvmfgDt.setText(DateFormat.getDateTimeInstance().format(new Date()));;
        tvdduranovalue.setText(Durano);
        tvgrossWt.setText(grossWt);
        tvtareWt.setText(tareWt);
        tvnetWt.setText(netWt);
        tvGas.setText(Gas);
        tvpressure.setText(pressure);
        gas_type.setText(gasType);
        mPrint = (Button) findViewById(R.id.mPrint);
        mPrint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {

                printBluetooth();

            }
        });




    }// onCreate
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
                "[C]<img>"+PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.printlogo, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n" +
                        "[C]<font size='small'>Mfg. Lic. No. PD/225            Dt.01/10/2019</font>\n"+
                        "[C]<font size='small'>Dist. Lic. No. MH/SOL/94474     Dt.26/12/2016</font>\n\n"+
                        "[C]<font size='small'>Batch No - "+batchDt+"</font>\n" +
                        "[C]<font size='small'><b>Dura No - "+Durano+"</b></font>\n" +
                        "[C]<font size='small'>Mfg Dt - "+DateFormat.getDateTimeInstance().format(new Date())+"</font>\n\n" +
                        "[C]<font size='small'>       Weighment Details </font>\n" +
                        "[C]<font size='small'>Gas Type   :   "+gasType+"</font>\n" +
                        "[C]<font size='small'>Gross Wt   :   "+grossWt+"</font>\n" +
                        "[C]<font size='small'>Tare Wt    :   "+tareWt+"</font>\n" +
                        "[C]<font size='small'>Net  Wt    :   "+netWt+"</font>\n" +
                        "[C]<font size='small'>Gas(\tm3)    :   "+Gas+"</font>\n" +
                        "[C]<font size='small'>Pressure   :  "+pressure+"</font>"
        );
    }





}
