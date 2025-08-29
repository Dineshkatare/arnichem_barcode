package com.arnichem.arnichem_barcode.PaymentReceipt;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.arnichem.arnichem_barcode.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.data.response.PaymentVoucherResponse;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CashVoucherPrintHistoryActivity extends AppCompatActivity {
    Button payPrint;
    String paymentmodestr, custname, date, srno, description, count, amountstr, remarkStr, transactionStr, series, vch_no, username;
    TextView docSeries, srnoId, dateid, custnameid, amount, transactionTv, paymentModeId, remarkTv, descriptionTv, cdarnichemdignprint;
    Bitmap printLogoDr, phoneNumberDr, digital_sign;
    ImageView printImg, phoneImg, signedImg;
    TextView arnichemsignTxt, termsTxt;
    private BluetoothConnection selectedDevice;
    private APIInterface apiInterface;
    private ProgressDialog dialog;
    private DatabaseHandler databaseHandlercustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_voucher_print);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Driver Instructions");

        // Initialize views
        srnoId = findViewById(R.id.srnoId);
        dateid = findViewById(R.id.cddateid);
        docSeries = findViewById(R.id.docSeries);
        custnameid = findViewById(R.id.cdcustnameid);
        descriptionTv = findViewById(R.id.descriptionTv);
        amount = findViewById(R.id.amount);
        paymentModeId = findViewById(R.id.paymentModeId);
        transactionTv = findViewById(R.id.transactionId);
        remarkTv = findViewById(R.id.remarkTv);
        payPrint = findViewById(R.id.payPrint);
        cdarnichemdignprint = findViewById(R.id.cdarnichemdignprint);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        termsTxt = findViewById(R.id.termsTxt);
        signedImg = findViewById(R.id.custnamesign); // Assuming you add this ImageView to your layout

        // Initialize database and API
        databaseHandlercustomer = new DatabaseHandler(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        // Get vch_no from Intent
        Intent i = getIntent();
        vch_no = i.getStringExtra("vch_no");

        // Set static data
        String print_logo = SharedPref.mInstance.getPrintLogo();
        File imgFile = new File(print_logo);
        if (imgFile.exists()) {
            printLogoDr = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            printImg.setImageBitmap(printLogoDr);
        }

        String phoneNumber = SharedPref.mInstance.getPhoneNumber();
        File imgFilePhoneNumber = new File(phoneNumber);
        if (imgFilePhoneNumber.exists()) {
            phoneNumberDr = BitmapFactory.decodeFile(imgFilePhoneNumber.getAbsolutePath());
            phoneImg.setImageBitmap(phoneNumberDr);
        }
        arnichemsignTxt.setText(SharedPref.mInstance.getOwnCode());
        // termsTxt.setText(SharedPref.mInstance.getTermsText());

        // Fetch data from API
        fetchPaymentVoucher(vch_no);

        // Set print button listener
        payPrint.setOnClickListener(v -> printBluetooth());
    }

    private void fetchPaymentVoucher(String vchNo) {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Data Fetching");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        String dbHost = SharedPref.mInstance.getDBHost();
        String dbUsername = SharedPref.mInstance.getDBUsername();
        String dbPassword = SharedPref.mInstance.getDBPassword();
        String dbName = SharedPref.mInstance.getDBName();

        Call<PaymentVoucherResponse> call = apiInterface.getPaymentVoucher(dbHost, dbUsername, dbPassword, dbName, vchNo);
        call.enqueue(new Callback<PaymentVoucherResponse>() {
            @Override
            public void onResponse(Call<PaymentVoucherResponse> call, Response<PaymentVoucherResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getData().isEmpty()) {
                    PaymentVoucherResponse.PaymentVoucher voucher = response.body().getData().get(0);

                    // Assign values to variables
                    srno = String.valueOf(voucher.getSrno());
                    series = voucher.getSeries();
                    date = parseDateToddMMyyyy(voucher.getDate());
                    custname = fetchCustomerName(voucher.getCustCode());
                    paymentmodestr = voucher.getMode();
                    amountstr = voucher.getAmount();
                    description = voucher.getDescription();
                    transactionStr = voucher.getTransactionId();
                    remarkStr = voucher.getRemarks();
                    username = voucher.getUser();
                    setImage(voucher.getFilePath());

                    // Update UI
                    srnoId.setText(srno);
                    docSeries.setText(series + "/" + vch_no);
                    dateid.setText(date);
                    custnameid.setText(custname);
                    descriptionTv.setText(description);
                    amount.setText(amountstr);
                    paymentModeId.setText(paymentmodestr);
                    transactionTv.setText(transactionStr);
                    remarkTv.setText(remarkStr);
                    cdarnichemdignprint.setText(username);
                } else {
                    Toast.makeText(CashVoucherPrintHistoryActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<PaymentVoucherResponse> call, Throwable t) {
                Toast.makeText(CashVoucherPrintHistoryActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private String fetchCustomerName(String cid) {
        String cust_name = null;
        Cursor cursor = databaseHandlercustomer.readAllData();
        if (cursor.getCount() == 0) {
            // Handle empty database
        } else {
            while (cursor.moveToNext()) {
                String col = cursor.getString(1);
                String col1 = cursor.getString(2);
                if (col1.contentEquals(cid)) {
                    cust_name = col;
                }
            }
        }
        return cust_name;
    }

    private void setImage(String path) {
        String base_url = "/public_html/arnichem.co.in/intranet";
        String new_base_url = base_url.replace("/public_html/", "");
        StringBuilder logoUrl = new StringBuilder("http://" + new_base_url);
        logoUrl.append("/barcode/APP/images/digital_sign/").append(path);
        String finalLogoUrl = logoUrl.toString();
        digital_sign = Util.getBitmapFromURL(finalLogoUrl);
        if (digital_sign != null) {
            digital_sign = Bitmap.createScaledBitmap(digital_sign, 200, 200, true);
            signedImg.setImageBitmap(digital_sign);
        } else {
            digital_sign = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
            digital_sign.eraseColor(android.graphics.Color.WHITE);
        }
    }

    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static final int PERMISSION_BLUETOOTH = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == finalprint.PERMISSION_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
            } else {
                Toast.makeText(this, "Bluetooth permission denied. Cannot print.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectBluetoothDevice();
            } else {
                Toast.makeText(this, "Bluetooth connect permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void printBluetooth() {
        if (selectedDevice == null) {
            selectBluetoothDevice();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        finalprint.PERMISSION_BLUETOOTH);
            } else {
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, finalprint.PERMISSION_BLUETOOTH);
            } else {
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
            }
        }
    }

    public void selectBluetoothDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
            builder.setItems(deviceNames, (dialog, which) -> {
                BluetoothDevice device = deviceList.get(which);
                selectedDevice = new BluetoothConnection(device);
                printBluetooth();
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
                "[C-]Payment Voucher [R]\n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, phoneNumberDr) + "</img>\n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, printLogoDr) + "</img>\n\n" +
                        "[C]<font size='small'>Doc Series -  " + series + "/" + vch_no + "</font>\n" +
                        "[C]<font size='small'>Date -  " + date + "</font>\n" +
                        "[C]<font size='small'>Name -  " + custname + "</font>\n" +
                        "[C]<font size='small'>       Payment Details </font>\n" +
                        "[C]<font size='small'>Payment Mode :  " + paymentmodestr + "</font>\n" +
                        "[C]<font size='small'>Description :  " + description + "</font>\n" +
                        "[C]<font size='small'>Amount       :  " + amountstr + "</font>\n" +
                        "[C]<font size='small'>Transactions ID  :  " + transactionStr + "</font>\n" +
                        "[C]<font size='small'>Remarks   : " + remarkStr + "</font>\n" +
                        "[L]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, digital_sign) + "</img>\n" +
                        "[R]               [R]" + username + "\n" +
                        "[R]Receiver  [R]" + SharedPref.getInstance(this).getOwnCode() + "\n\n"
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CashVoucherPrintHistoryActivity.this, Transactions.class));
    }
}