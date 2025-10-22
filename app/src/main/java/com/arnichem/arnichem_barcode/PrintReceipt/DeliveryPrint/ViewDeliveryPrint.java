package com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.PaymentReceipt.GasTypeResponse;
import com.arnichem.arnichem_barcode.PrintReceipt.MainPrintActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.data.response.FetchItemAndQuantityVolume;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.util.Logger;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;

public class ViewDeliveryPrint extends AppCompatActivity {
    public static final int PERMISSION_BLUETOOTH = 1;
    static JSONObject object = null;
    Button duradelprint;
    APIInterface apiInterface;
    private List<String> gasTypes = new ArrayList<>();

    String pos, custname, dcno, type, custcode, username, totalQuan, delidate, strVehicleNo, itemName = "", quantity_vol = "";
    TextView empbid, dateid, custnameid, cylindernumberempty, vehicleno, arnichemdignprint, counttxt, tvcode;
    ArrayList<String> newlist;
    ProgressDialog dialog;
    DatabaseHandler databaseHandlercustomer;
    private BluetoothConnection selectedDevice;
    Bitmap printLogoDr, phoneNumberDr, digital_sign;
    ImageView printImg, phoneImg, signedImg;
    TextView arnichemsignTxt, termsTxt, cylinder_number_txt, total_quantity_txt;

    boolean isOxygen = true;
    private boolean isPrinting = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_delivery_print);
        Logger.info(this, "Activity ViewDeliveryPrint created");

        // Initialize views
        empbid = findViewById(R.id.cdcnoid);
        cylinder_number_txt = findViewById(R.id.cylinder_number_txt);
        total_quantity_txt = findViewById(R.id.total_quantity_txt);
        dateid = findViewById(R.id.cddateid);
        signedImg = findViewById(R.id.custnamesign);
        newlist = new ArrayList<>();
        databaseHandlercustomer = new DatabaseHandler(ViewDeliveryPrint.this);
        custnameid = findViewById(R.id.cdcustnameid);
        cylindernumberempty = findViewById(R.id.cylindernumberdel);
        arnichemdignprint = findViewById(R.id.cdarnichemdignprint);
        tvcode = findViewById(R.id.codeid);
        counttxt = findViewById(R.id.totalq);
        vehicleno = findViewById(R.id.cdvehicleno);
        duradelprint = findViewById(R.id.delyprint);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        termsTxt = findViewById(R.id.termsTxt);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        Logger.debug(this, "UI components initialized");

        Intent i = getIntent();
        dcno = i.getStringExtra("no");
        type = i.getStringExtra("type");
        empbid.setText(dcno);
        Logger.info(this, "Received intent data: dcno=" + dcno + ", type=" + type);

        // Start data fetching
        new FetchDataTask().execute();
        Logger.info(this, "Started FetchDataTask for data retrieval");

        duradelprint.setOnClickListener(v -> {
            Logger.info(this, "Print button clicked");
            if (isPrinting) {
                Logger.info(this, "Printing already in progress");
                Toast.makeText(ViewDeliveryPrint.this, "Printing in progress, please wait", Toast.LENGTH_SHORT).show();
                return;
            }
            isPrinting = true;
            duradelprint.setEnabled(false);
            Logger.debug(this, "Set isPrinting=true, disabled print button");
            if (digital_sign == null) {
                digital_sign = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
                digital_sign.eraseColor(Color.WHITE);
                Logger.debug(this, "Created default digital signature bitmap");
            }
            printBluetooth();
        });

        String print_logo = SharedPref.mInstance.getPrintLogo();
        File imgFile = new File(print_logo);
        if (imgFile.exists()) {
            printLogoDr = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            printImg.setImageBitmap(printLogoDr);
            Logger.debug(this, "Loaded print logo from: " + print_logo);
        } else {
            Logger.error(this, "Print logo file not found: " + print_logo, null);
        }

        String phoneNumber = SharedPref.mInstance.getPhoneNumber();
        File imgFilePhoneNumber = new File(phoneNumber);
        if (imgFilePhoneNumber.exists()) {
            phoneNumberDr = BitmapFactory.decodeFile(imgFilePhoneNumber.getAbsolutePath());
            phoneImg.setImageBitmap(phoneNumberDr);
            Logger.debug(this, "Loaded phone number image from: " + phoneNumber);
        } else {
            Logger.error(this, "Phone number image file not found: " + phoneNumber, null);
        }
        arnichemsignTxt.setText(SharedPref.mInstance.getOwnCode());
        termsTxt.setText(SharedPref.mInstance.getTermsText());
        Logger.debug(this, "Set signature and terms text");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logger.info(this, "Received permission result for requestCode=" + requestCode);
        if (requestCode == finalprint.PERMISSION_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Logger.debug(this, "Bluetooth permission granted");
                attemptBluetoothConnection(3);
            } else {
                Logger.error(this, "Bluetooth permission denied", null);
                Toast.makeText(this, "Bluetooth permission denied. Cannot print.", Toast.LENGTH_SHORT).show();
                isPrinting = false;
                duradelprint.setEnabled(true);
                Logger.debug(this, "Set isPrinting=false, enabled print button due to permission denial");
            }
        } else if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Logger.debug(this, "Bluetooth connect permission granted");
                selectBluetoothDevice();
            } else {
                Logger.error(this, "Bluetooth connect permission denied", null);
                Toast.makeText(this, "Bluetooth connect permission denied.", Toast.LENGTH_SHORT).show();
                isPrinting = false;
                duradelprint.setEnabled(true);
                Logger.debug(this, "Set isPrinting=false, enabled print button due to connect permission denial");
            }
        }
    }

    public void printBluetooth() {
        Logger.info(this, "Initiating printBluetooth");
//        if (isPrinting) {
//            Logger.info(this, "Print request ignored: printing already in progress");
//            Toast.makeText(this, "Printing in progress, please wait", Toast.LENGTH_SHORT).show();
//            return;
//        }

        if (selectedDevice == null) {
            Logger.info(this, "No Bluetooth device selected, prompting device selection");
            Toast.makeText(this, "No Bluetooth device selected", Toast.LENGTH_SHORT).show();
            selectBluetoothDevice();
            return;
        }

        isPrinting = true;
        duradelprint.setEnabled(false);
        Logger.debug(this, "Set isPrinting=true, disabled print button for Bluetooth printing");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Logger.info(this, "Requesting Bluetooth permissions for API >= 31");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        finalprint.PERMISSION_BLUETOOTH);
            } else {
                Logger.debug(this, "Bluetooth permissions granted, attempting connection");
                attemptBluetoothConnection(3);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Logger.info(this, "Requesting Bluetooth permission for API < 31");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, finalprint.PERMISSION_BLUETOOTH);
            } else {
                Logger.debug(this, "Bluetooth permission granted, attempting connection");
                attemptBluetoothConnection(3);
            }
        }
    }

    private void attemptBluetoothConnection(int maxRetries) {
        int retries = 0;
        while (retries < maxRetries) {
            try {
                Logger.info(this, "Attempting Bluetooth connection, attempt " + (retries + 1) + "/" + maxRetries);
                new AsyncBluetoothEscPosPrint(this) {
                    @Override
                    protected void onPostExecute(Integer result) {
                        super.onPostExecute(result);
                        isPrinting = false;
                        duradelprint.setEnabled(true);
                        if (result == AsyncBluetoothEscPosPrint.FINISH_SUCCESS) {
                            Logger.info(ViewDeliveryPrint.this, "Print job completed successfully");
                            Toast.makeText(ViewDeliveryPrint.this, "Print successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Logger.error(ViewDeliveryPrint.this, "Print job failed with result code: " + result, null);
                            Toast.makeText(ViewDeliveryPrint.this, "Print failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute(getAsyncEscPosPrinter(selectedDevice));
                Logger.debug(this, "Started AsyncBluetoothEscPosPrint task");
                return;
            } catch (Exception e) {
                retries++;
                Logger.error(this, "Bluetooth connection attempt " + retries + " failed", e);
                if (retries == maxRetries) {
                    Logger.error(this, "Failed to connect to printer after " + maxRetries + " attempts", null);
                    Toast.makeText(this, "Failed to connect to printer after " + maxRetries + " attempts", Toast.LENGTH_SHORT).show();
                    isPrinting = false;
                    duradelprint.setEnabled(true);
                    Logger.debug(this, "Set isPrinting=false, enabled print button after max retries");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Logger.error(this, "Retry interrupted", ie);
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        Logger.info(this, "Preparing print job for ESC/POS printer");
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        String printContent = isOxygen ?
                "[R]Delivery challan  [R]\n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, phoneNumberDr) + "</img>\n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, printLogoDr) + "</img>\n\n" +
                        "[C]<font size='small'>DCNO -  " + (dcno != null ? dcno : "") + "</font>\n" +
                        "[C]<font size='small'>Date -  " + (delidate != null ? delidate : "") + "</font>\n" +
                        "[C]<font size='small'>Code -  " + (custcode != null ? custcode : "") + "</font>\n" +
                        "[C]<font size='small'>Name -  " + (custname != null ? custname : "") + "</font>\n" +
                        "[C]<font size='small'><b>       Cylinder Numbers </b></font>\n" +
                        "[C]<font size='small'><b>            " + foreaching() + "</b></font>\n" +
                        "[C]<font size='small'>Total Quantity : " + (totalQuan != null ? totalQuan : "") + "</font>\n" +
                        "[C]<font size='small'>Vehicle No    :  " + (strVehicleNo != null ? strVehicleNo : "") + "</font>\n" +
                        "[L]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, digital_sign) + "</img>\n" +
                        "[R]               [R]" + (username != null ? username : "") + "\n" +
                        "[R]Customer  [R]" + SharedPref.getInstance(this).getOwnCode() + "\n\n" +
                        "[R]" + SharedPref.getInstance(this).getTermsText() + "\n" :
                "[R]Delivery challan  [R]\n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, phoneNumberDr) + "</img>\n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, printLogoDr) + "</img>\n\n" +
                        "[C]<font size='small'>DCNO -  " + (dcno != null ? dcno : "") + "</font>\n" +
                        "[C]<font size='small'>Date -  " + (delidate != null ? delidate : "") + "</font>\n" +
                        "[C]<font size='small'>Code -  " + (custcode != null ? custcode : "") + "</font>\n" +
                        "[C]<font size='small'>Name -  " + (custname != null ? custname : "") + "</font>\n" +
                        "[C]<font size='small'>       Cylinder Details </font>\n" +
                        "[C]<font size='small'>Item            : " + (itemName != null ? itemName : "") + "</font>\n" +
                        "[C]<font size='small'>Quantity Volume : " + (quantity_vol != null ? quantity_vol : "") + "</font>\n" +
                        "[C]<font size='small'>Vehicle No    :  " + (strVehicleNo != null ? strVehicleNo : "") + "</font>\n" +
                        "[L]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, digital_sign) + "</img>\n" +
                        "[R]               [R]" + (username != null ? username : "") + "\n" +
                        "[R]Customer  [R]" + SharedPref.getInstance(this).getOwnCode() + "\n\n" +
                        "[R]" + SharedPref.getInstance(this).getTermsText() + "\n";
        Logger.debug(this, "Print content prepared, isOxygen=" + isOxygen);
        return printer.setTextToPrint(printContent);
    }

    public Serializable foreaching() {
        Logger.debug(this, "Formatting cylinder numbers for print");
        StringBuffer text = new StringBuffer();
        for (String mark : newlist) {
            text.append(mark).append('\n').append("            ");
        }
        Logger.debug(this, "Formatted " + newlist.size() + " cylinder numbers");
        return text;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Logger.info(this, "Back button pressed, navigating to MainPrintActivity");
        startActivity(new Intent(ViewDeliveryPrint.this, MainPrintActivity.class));
    }

    private class FetchDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ViewDeliveryPrint.this);
            dialog.setTitle("Data Fetching");
            dialog.setMessage("Please wait....");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
            Logger.debug(ViewDeliveryPrint.this, "Showing progress dialog for data fetch");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Logger.info(ViewDeliveryPrint.this, "Executing FetchDataTask in background");
            postrequ();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                Logger.debug(ViewDeliveryPrint.this, "Dismissed progress dialog after data fetch");
            }
        }
    }

    private void postrequ() {
        Logger.info(this, "Starting postrequ to fetch data");

        // Fetch cylinder transactions
        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.fetch_print_data_cylinder_transactions,
                response -> {
                    Logger.info(this, "Received cylinder transactions response");
                    try {
                        JSONArray array = new JSONArray(response);
                        newlist.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.optJSONObject(i);
                            if (obj != null && obj.has("item_code")) {
                                newlist.add(obj.optString("item_code", ""));
                            }
                        }
                        Logger.debug(this, "Parsed " + newlist.size() + " cylinder items");
                        if (isOxygen) {
                            String joined = TextUtils.join(",", newlist);
                            totalQuan = String.valueOf(newlist.size());
                            runOnUiThread(() -> {
                                cylindernumberempty.setText(joined);
                                counttxt.setText(totalQuan);
                                Logger.debug(this, "Updated UI with cylinder data: " + joined);
                            });
                        }
                    } catch (JSONException e) {
                        Logger.error(this, "Error parsing cylinder transactions", e);
                        runOnUiThread(() -> Toast.makeText(ViewDeliveryPrint.this, "Error fetching cylinder data", Toast.LENGTH_SHORT).show());
                    }
                },
                error -> {
                    Logger.error(this, "Cylinder transactions API error", error);
                    runOnUiThread(() -> Toast.makeText(ViewDeliveryPrint.this, "Failed to fetch cylinder data", Toast.LENGTH_SHORT).show());
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("no", dcno != null ? dcno : "");
                params.put("type", type != null ? type : "");
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username", SharedPref.mInstance.getDBUsername());
                params.put("db_password", SharedPref.mInstance.getDBPassword());
                params.put("db_name", SharedPref.mInstance.getDBName());
                Logger.debug(ViewDeliveryPrint.this, "Sending cylinder transactions request with dcno=" + dcno + ", type=" + type);
                return params;
            }
        };
        VolleySingleton.getInstance(ViewDeliveryPrint.this).addToRequestQueue(stringRequest);
        Logger.info(this, "Enqueued cylinder transactions request");

        // Fetch delivery main data
        StringRequest stringRequest1 = new StringRequest(Request.Method.POST, APIClient.fetch_print_data_delivery_main,
                response -> {
                    Logger.info(this, "Received delivery main response");
                    try {
                        JSONArray array = new JSONArray(response);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.optJSONObject(i);
                            if (obj != null) {
                                custcode = obj.optString("ccode", "");
                                strVehicleNo = obj.optString("vehicle_no", "");
                                String driverId = obj.optString("driver_id", "");
                                String timestamp = obj.optString("timestamp", "");
                                custname = fetchCustomerName(custcode);
                                delidate = parseDateToddMMyyyy(timestamp);
                                Logger.debug(this, "Parsed delivery data: custcode=" + custcode + ", vehicle_no=" + strVehicleNo);
                                runOnUiThread(() -> {
                                    vehicleno.setText(strVehicleNo);
                                    custnameid.setText(custname != null ? custname : "");
                                    tvcode.setText(custcode);
                                    dateid.setText(delidate != null ? delidate : "");
                                    Logger.debug(this, "Updated UI with delivery data");
                                });
                                fetchUsername(driverId);
                            }
                        }
                    } catch (JSONException e) {
                        Logger.error(this, "Error parsing delivery main", e);
                        runOnUiThread(() -> Toast.makeText(ViewDeliveryPrint.this, "Error fetching delivery data", Toast.LENGTH_SHORT).show());
                    }
                },
                error -> {
                    Logger.error(this, "Delivery main API error", error);
                    runOnUiThread(() -> Toast.makeText(ViewDeliveryPrint.this, "Failed to fetch delivery data", Toast.LENGTH_SHORT).show());
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("no", dcno != null ? dcno : "");
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username", SharedPref.mInstance.getDBUsername());
                params.put("db_password", SharedPref.mInstance.getDBPassword());
                params.put("db_name", SharedPref.mInstance.getDBName());
                Logger.debug(ViewDeliveryPrint.this, "Sending delivery main request with dcno=" + dcno);
                return params;
            }
        };
        VolleySingleton.getInstance(ViewDeliveryPrint.this).addToRequestQueue(stringRequest1);
        Logger.info(this, "Enqueued delivery main request");

        // Fetch signature
        StringRequest stringRequest2 = new StringRequest(Request.Method.POST, APIClient.fetch_sign,
                response -> {
                    Logger.info(this, "Received signature response");
                    try {
                        JSONArray array = new JSONArray(response);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.optJSONObject(i);
                            if (obj != null && obj.has("path")) {
                                setImage(obj.optString("path", ""));
                                Logger.debug(this, "Signature path received: " + obj.optString("path", ""));
                            } else {
                                runOnUiThread(() -> {
                                    digital_sign = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
                                    digital_sign.eraseColor(Color.WHITE);
                                    signedImg.setImageBitmap(digital_sign);
                                    Logger.debug(this, "No signature path, set default bitmap");
                                });
                            }
                        }
                    } catch (JSONException e) {
                        Logger.error(this, "Error parsing signature", e);
                        runOnUiThread(() -> Toast.makeText(ViewDeliveryPrint.this, "Error fetching signature", Toast.LENGTH_SHORT).show());
                    }
                },
                error -> {
                    Logger.error(this, "Signature API error", error);
                    runOnUiThread(() -> {
                        Toast.makeText(ViewDeliveryPrint.this, "Failed to fetch signature", Toast.LENGTH_SHORT).show();
                        digital_sign = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
                        digital_sign.eraseColor(Color.WHITE);
                        signedImg.setImageBitmap(digital_sign);
                        Logger.debug(this, "Set default signature bitmap on API error");
                    });
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("no", dcno != null ? dcno : "");
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username", SharedPref.mInstance.getDBUsername());
                params.put("db_password", SharedPref.mInstance.getDBPassword());
                params.put("db_name", SharedPref.mInstance.getDBName());
                Logger.debug(ViewDeliveryPrint.this, "Sending signature request with dcno=" + dcno);
                return params;
            }
        };
        VolleySingleton.getInstance(ViewDeliveryPrint.this).addToRequestQueue(stringRequest2);
        Logger.info(this, "Enqueued signature request");
    }

    private void setImage(String path) {
        Logger.info(this, "Setting signature image, path=" + path);
        if (path == null || path.isEmpty()) {
            runOnUiThread(() -> {
                digital_sign = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
                digital_sign.eraseColor(Color.WHITE);
                signedImg.setImageBitmap(digital_sign);
                Logger.debug(this, "Set default signature bitmap due to empty path");
            });
            return;
        }

        String base_url = "/public_html/arnichem.co.in/intranet";
        String new_base_url = base_url.replace("/public_html/", "");
        StringBuilder logoUrl = new StringBuilder("http://" + new_base_url);
        logoUrl.append("/barcode/APP/images/digital_sign/").append(path);
        String finalLogoUrl = logoUrl.toString();
        Logger.debug(this, "Signature URL: " + finalLogoUrl);

        // Use AsyncTask to download image off the main thread
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                Logger.debug(ViewDeliveryPrint.this, "Downloading signature image from: " + finalLogoUrl);
                return Util.getBitmapFromURL(finalLogoUrl);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                digital_sign = bitmap;
                runOnUiThread(() -> {
                    if (digital_sign != null) {
                        digital_sign = Bitmap.createScaledBitmap(digital_sign, 200, 200, true);
                        signedImg.setImageBitmap(digital_sign);
                        Logger.debug(ViewDeliveryPrint.this, "Loaded and scaled signature bitmap");
                    } else {
                        digital_sign = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
                        digital_sign.eraseColor(Color.WHITE);
                        signedImg.setImageBitmap(digital_sign);
                        Logger.debug(ViewDeliveryPrint.this, "Set default signature bitmap due to null image");
                    }
                });
            }
        }.execute();
    }

    public String parseDateToddMMyyyy(String time) {
        Logger.debug(this, "Parsing date: " + time);
        if (time == null || time.isEmpty()) {
            Logger.debug(this, "Date parsing skipped: empty input");
            return "";
        }
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd-MMM-yyyy h:mm:ss a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        try {
            Date date = inputFormat.parse(time);
            String formattedDate = outputFormat.format(date);
            Logger.debug(this, "Date parsed successfully: " + formattedDate);
            return formattedDate;
        } catch (ParseException e) {
            Logger.error(this, "Date parse error", e);
            return "";
        }
    }

    private String fetchCustomerName(String cid) {
        Logger.debug(this, "Fetching customer name for cid=" + cid);
        if (cid == null || cid.isEmpty()) {
            Logger.debug(this, "Customer name fetch skipped: empty cid");
            return "";
        }
        String cust_name = "";
        Cursor cursor = databaseHandlercustomer.readAllData();
        if (cursor.getCount() == 0) {
            Logger.debug(this, "No customer data found in database");
        } else {
            while (cursor.moveToNext()) {
                String col = cursor.getString(1);
                String col1 = cursor.getString(2);
                if (col1 != null && col1.equals(cid)) {
                    cust_name = col != null ? col : "";
                }
            }
            Logger.debug(this, "Customer name found: " + cust_name);
        }
        cursor.close();
        return cust_name;
    }

    private void fetchUsername(String uid) {
        Logger.info(this, "Fetching username for uid=" + uid);
        if (uid == null || uid.isEmpty()) {
            runOnUiThread(() -> {
                arnichemdignprint.setText("");
                Logger.debug(this, "Set empty username due to empty uid");
            });
            return;
        }

        StringRequest stringRequest1 = new StringRequest(Request.Method.POST, APIClient.fetch_username,
                response -> {
                    Logger.info(this, "Received username response");
                    try {
                        JSONArray array = new JSONArray(response);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.optJSONObject(i);
                            if (obj != null) {
                                String fname = obj.optString("fname", "");
                                String lname = obj.optString("lname", "");
                                username = fname + " " + lname;
                                runOnUiThread(() -> {
                                    arnichemdignprint.setText(username);
                                    Logger.debug(this, "Updated UI with username: " + username);
                                });
                            }
                        }
                    } catch (JSONException e) {
                        Logger.error(this, "Error parsing username", e);
                        runOnUiThread(() -> Toast.makeText(ViewDeliveryPrint.this, "Error fetching username", Toast.LENGTH_SHORT).show());
                    }
                },
                error -> {
                    Logger.error(this, "Username API error", error);
                    runOnUiThread(() -> Toast.makeText(ViewDeliveryPrint.this, "Failed to fetch username", Toast.LENGTH_SHORT).show());
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", uid);
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username", SharedPref.mInstance.getDBUsername());
                params.put("db_password", SharedPref.mInstance.getDBPassword());
                params.put("db_name", SharedPref.mInstance.getDBName());
                Logger.debug(ViewDeliveryPrint.this, "Sending username request with uid=" + uid);
                return params;
            }
        };
        VolleySingleton.getInstance(ViewDeliveryPrint.this).addToRequestQueue(stringRequest1);
        Logger.info(this, "Enqueued username request");
    }

    public void selectBluetoothDevice() {
        Logger.info(this, "Starting Bluetooth device selection");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Logger.info(this, "Requesting BLUETOOTH_CONNECT permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                return;
            }
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Logger.error(this, "Bluetooth is not enabled", null);
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Logger.debug(this, "Found " + pairedDevices.size() + " paired Bluetooth devices");
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
                Logger.info(this, "Selected Bluetooth device: " + device.getName() + " (" + device.getAddress() + ")");
                printBluetooth();
            });
            builder.show();
            Logger.debug(this, "Displayed Bluetooth device selection dialog");
        } else {
            Logger.error(this, "No paired Bluetooth devices found", null);
            Toast.makeText(this, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchDeliveryItems(String dcno) {
        if (dcno == null || dcno.isEmpty()) {
            Logger.error(this, "Invalid dcno for fetchDeliveryItems: " + dcno, null);
            return;
        }
        Logger.info(this, "Fetching delivery items for dcno=" + dcno);
        Call<FetchItemAndQuantityVolume> call = apiInterface.getDeliveryItems(SharedPref.mInstance.getDBHost(), SharedPref.mInstance.getDBUsername(), SharedPref.mInstance.getDBPassword(), SharedPref.mInstance.getDBName(), dcno);
        call.enqueue(new Callback<FetchItemAndQuantityVolume>() {
            @Override
            public void onResponse(Call<FetchItemAndQuantityVolume> call, retrofit2.Response<FetchItemAndQuantityVolume> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FetchItemAndQuantityVolume apiResponse = response.body();
                    if (apiResponse.getItems().isEmpty()) {
                        Logger.info(ViewDeliveryPrint.this, "No items found for dcno=" + dcno);
                    } else {
                        boolean isQuantity = false;
                        for (FetchItemAndQuantityVolume.Item item : apiResponse.getItems()) {
                            Logger.debug(ViewDeliveryPrint.this, "Item: " + item.getItem() + ", Quantity Volume: " + item.getQuantity_volume());
                            if (quantity_vol.isEmpty()) {
                                quantity_vol = String.valueOf(item.getQuantity_volume());
                            }
                            if (itemName.isEmpty()) {
                                itemName = item.getItem();
                            }
                            if (gasTypes.contains(item.getItem())) {
                                isQuantity = true;
                            }
                        }
                        Logger.debug(ViewDeliveryPrint.this, "Processed " + apiResponse.getItems().size() + " items, isQuantity=" + isQuantity);
                        if (!isQuantity) {
                            isOxygen = true;
                            runOnUiThread(() -> {
                                cylinder_number_txt.setText("Cylinder No      :");
                                total_quantity_txt.setText("Total Quantity  :");
                                Logger.debug(ViewDeliveryPrint.this, "Set UI for oxygen cylinders");
                            });
                            postrequ();
                        } else {
                            isOxygen = false;
                            runOnUiThread(() -> {
                                cylinder_number_txt.setText("Item          :");
                                total_quantity_txt.setText("Quantity Volume :");
                                counttxt.setText(quantity_vol);
                                cylindernumberempty.setText(itemName);
                                Logger.debug(ViewDeliveryPrint.this, "Set UI for non-oxygen items: itemName=" + itemName + ", quantity_vol=" + quantity_vol);
                            });
                            postrequ();
                        }
                    }
                } else {
                    Logger.error(ViewDeliveryPrint.this, "Failed to fetch delivery items: " + response.message(), null);
                    runOnUiThread(() -> Toast.makeText(ViewDeliveryPrint.this, "Failed to fetch delivery items", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<FetchItemAndQuantityVolume> call, Throwable t) {
                Logger.error(ViewDeliveryPrint.this, "Network error fetching delivery items", t);
                runOnUiThread(() -> Toast.makeText(ViewDeliveryPrint.this, "Network error fetching delivery items", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchGasTypes() {
        Logger.info(this, "Fetching gas types");
        dialog = new ProgressDialog(ViewDeliveryPrint.this);
        dialog.setTitle("Data Fetching");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        Logger.debug(this, "Showing progress dialog for gas types fetch");

        Call<GasTypeResponse> call = apiInterface.fetchGasTypes(SharedPref.mInstance.getDBHost(), SharedPref.mInstance.getDBUsername(), SharedPref.mInstance.getDBPassword(), SharedPref.mInstance.getDBName());
        call.enqueue(new Callback<GasTypeResponse>() {
            @Override
            public void onResponse(Call<GasTypeResponse> call, retrofit2.Response<GasTypeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    gasTypes.clear();
                    gasTypes.addAll(response.body().getData().getGasTypes());
                    Logger.info(ViewDeliveryPrint.this, "Fetched " + gasTypes.size() + " gas types");
                    fetchDeliveryItems(dcno);
                } else {
                    Logger.error(ViewDeliveryPrint.this, "Failed to fetch gas types: " + response.message(), null);
                    runOnUiThread(() -> Toast.makeText(ViewDeliveryPrint.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show());
                }
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    Logger.debug(ViewDeliveryPrint.this, "Dismissed progress dialog for gas types");
                }
            }

            @Override
            public void onFailure(@NonNull Call<GasTypeResponse> call, @NonNull Throwable t) {
                Logger.error(ViewDeliveryPrint.this, "Network error fetching gas types", t);
                runOnUiThread(() -> Toast.makeText(ViewDeliveryPrint.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    Logger.debug(ViewDeliveryPrint.this, "Dismissed progress dialog on gas types failure");
                }
            }
        });
    }
}