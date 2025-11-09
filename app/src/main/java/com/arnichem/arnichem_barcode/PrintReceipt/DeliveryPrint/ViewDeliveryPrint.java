package com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.PaymentReceipt.GasTypeResponse;
import com.arnichem.arnichem_barcode.PrintReceipt.EmptyPrint.ViewEmptyPrint;
import com.arnichem.arnichem_barcode.PrintReceipt.MainAdapter;
import com.arnichem.arnichem_barcode.PrintReceipt.MainPrintActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.data.response.FetchItemAndQuantityVolume;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.print.Utils;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.loading;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


public class ViewDeliveryPrint extends AppCompatActivity {
    public static final int PERMISSION_BLUETOOTH = 1;
    static JSONObject object = null;
    Button duradelprint;
    APIInterface apiInterface;
    private List<String> gasTypes = new ArrayList<>();

    String pos,custname,dcno,type,custcode,username,totalQuan,delidate,strVehicleNo,itemName="",quantity_vol="";
    TextView empbid,dateid,custnameid,cylindernumberempty,vehicleno,arnichemdignprint,counttxt,tvcode;
    ArrayList<String> newlist;
    ProgressDialog dialog;
    DatabaseHandler databaseHandlercustomer;
    private BluetoothConnection selectedDevice;
    Bitmap printLogoDr,phoneNumberDr,digital_sign;
    ImageView printImg,phoneImg,signedImg;
    TextView arnichemsignTxt,termsTxt,cylinder_number_txt,total_quantity_txt;

    boolean isOxygen = true;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_delivery_print);
        empbid=findViewById(R.id.cdcnoid);
        cylinder_number_txt= findViewById(R.id.cylinder_number_txt);
        total_quantity_txt= findViewById(R.id.total_quantity_txt);
        dateid=findViewById(R.id.cddateid);
        signedImg = findViewById(R.id.custnamesign);
        newlist=new ArrayList<>();
        databaseHandlercustomer = new DatabaseHandler(ViewDeliveryPrint.this);
        custnameid=findViewById(R.id.cdcustnameid);
        cylindernumberempty=findViewById(R.id.cylindernumberdel);
        arnichemdignprint=findViewById(R.id.cdarnichemdignprint);
        tvcode=findViewById(R.id.codeid);
        counttxt=findViewById(R.id.totalq);
        vehicleno=findViewById(R.id.cdvehicleno);
        duradelprint=findViewById(R.id.delyprint);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        termsTxt = findViewById(R.id.termsTxt);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        apiInterface = APIClient.getClient().create(APIInterface.class);

        StrictMode.setThreadPolicy(policy);
        Intent i=getIntent();
        dcno = i.getStringExtra("no");
        type = i.getStringExtra("type");
        empbid.setText(dcno);
        //  postrequ();
        fetchGasTypes();
        duradelprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(digital_sign==null){
                    digital_sign = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
                    digital_sign.eraseColor(Color.WHITE);

                }
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
        arnichemsignTxt.setText(SharedPref.mInstance.getOwnCode());
        termsTxt.setText(SharedPref.mInstance.getTermsText());


    }

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
                //  secondPrint();
            }
        } else {
            // Below Android 12
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, finalprint.PERMISSION_BLUETOOTH);
            } else {
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
                //  secondPrint();
            }
        }

    }

    public void secondPrint() {
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

        // Check if isOxygen is true or false and set the text to print accordingly
        if (isOxygen) {
            // Set text to print for oxygen case
            return printer.setTextToPrint(
                    "[R]Delivery challan  [R]\n" +
                            "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, phoneNumberDr) + "</img>\n" +
                            "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, printLogoDr) + "</img>\n\n" +
                            "[C]<font size='small'>DCNO -  " + dcno + "</font>\n" +
                            "[C]<font size='small'>Date -  " + delidate + "</font>\n" +
                            "[C]<font size='small'>Code -  " + custcode + "</font>\n" +
                            "[C]<font size='small'>Name -  " + custname + "</font>\n" +
                            "[C]<font size='small'><b>       Cylinder Numbers </b></font>\n" +
                            "[C]<font size='small'><b>            " + foreaching() + "</b></font>\n" +
                            "[C]<font size='small'>Total Quantity : " + totalQuan + "</font>\n" +
                            "[C]<font size='small'>Vehicle No    :  " + strVehicleNo + "</font>\n" +
                            "[L]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, digital_sign) + "</img>\n" +
                            "[R]               [R]" + username + "\n" +
                            "[R]Customer  [R]" + SharedPref.getInstance(this).getOwnCode() + "\n\n" +
                            "[R]" + SharedPref.getInstance(this).getTermsText() + "\n"
            );
        } else {
            // Set text to print for non-oxygen case
            return printer.setTextToPrint(
                    "[R]Delivery challan  [R]\n" +
                            "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, phoneNumberDr) + "</img>\n" +
                            "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, printLogoDr) + "</img>\n\n" +
                            "[C]<font size='small'>DCNO -  " + dcno + "</font>\n" +
                            "[C]<font size='small'>Date -  " + delidate + "</font>\n" +
                            "[C]<font size='small'>Code -  " + custcode + "</font>\n" +
                            "[C]<font size='small'>Name -  " + custname + "</font>\n" +
                            "[C]<font size='small'>       Cylinder Details </font>\n" +
                            "[C]<font size='small'>Item            : " + itemName + "</font>\n" +
                            "[C]<font size='small'>Quantity Volume : " + quantity_vol + "</font>\n" +
                            "[C]<font size='small'>Vehicle No    :  " + strVehicleNo + "</font>\n" +
                            "[L]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, digital_sign) + "</img>\n" +
                            "[R]               [R]" + username + "\n" +
                            "[R]Customer  [R]" + SharedPref.getInstance(this).getOwnCode() + "\n\n" +
                            "[R]" + SharedPref.getInstance(this).getTermsText() + "\n"
            );
        }
    }

    public Serializable foreaching() {
        StringBuffer text = new StringBuffer();

        for (String mark: newlist) {
            text.append(mark.toString()).append('\n').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020');

        }
        return text;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ViewDeliveryPrint.this, MainPrintActivity.class));
    }
    private void postrequ()
    {


        dialog = new ProgressDialog(ViewDeliveryPrint.this);
        dialog.setTitle("Data Fetching");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();



        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.fetch_print_data_cylinder_transactions,
                new Response.Listener<String>() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onResponse(String response) {
                        JSONArray array = null;
                        try {
                            array = new JSONArray(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        for (int i = 0; i < array.length(); i++) {
                            try {
                                object = array.getJSONObject(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                newlist.add(object.getString("item_code"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if(isOxygen) {
                                String joined = TextUtils.join(",", newlist);
                                cylindernumberempty.setText(joined);
                            }
                            dialog.dismiss();

                        }
                        if(isOxygen) {

                            String joined = TextUtils.join(",", newlist);
                            totalQuan = String.valueOf(newlist.size());
                            cylindernumberempty.setText(joined);
                            counttxt.setText(totalQuan);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        dialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("no",dcno);
                params.put("type",type);
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(ViewDeliveryPrint.this).addToRequestQueue(stringRequest);


        StringRequest stringRequest1 = new StringRequest(Request.Method.POST, APIClient.fetch_print_data_delivery_main,
                new Response.Listener<String>() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onResponse(String response) {
                        JSONArray array = null;
                        try {
                            array = new JSONArray(response);
                        } catch (JSONException e) {
                            dialog.dismiss();
                            e.printStackTrace();
                        }
                        for (int i = 0; i < array.length(); i++) {
                            try {
                                object = array.getJSONObject(i);
                            } catch (JSONException e) {
                                dialog.dismiss();
                                e.printStackTrace();
                            }

                            try {
                                custcode = object.getString("ccode");
                                strVehicleNo = object.getString("vehicle_no");

                                vehicleno.setText(strVehicleNo);
                                custname = fetchCustomerName(custcode);
                                custnameid.setText(custname);
                                fetchUsername(object.getString("driver_id"));
                                tvcode.setText(custcode);
                                delidate=parseDateToddMMyyyy(object.getString("timestamp"));
                                dateid.setText(delidate);



//        counttxt.setText(totalQuan);

                            } catch (JSONException e) {
                                dialog.dismiss();
                                e.printStackTrace();
                            }
                            dialog.dismiss();


                        }


                    }
                },
                new Response.ErrorListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        dialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("no",dcno);
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(ViewDeliveryPrint.this).addToRequestQueue(stringRequest1);




        StringRequest stringRequest2 = new StringRequest(Request.Method.POST, APIClient.fetch_sign,
                new Response.Listener<String>() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onResponse(String response) {
                        JSONArray array = null;
                        try {
                            array = new JSONArray(response);
                        } catch (JSONException e) {
                            dialog.dismiss();

                            e.printStackTrace();
                        }
                        for (int i = 0; i < array.length(); i++) {
                            try {
                                object = array.getJSONObject(i);
                            } catch (JSONException e) {
                                dialog.dismiss();
                                e.printStackTrace();
                            }

                            try {
                                setImage(object.getString("path"));
                                //   Toast.makeText(ViewDeliveryPrint.this, ""+object.getString("path"), Toast.LENGTH_SHORT).show();

                            } catch (JSONException e) {
                                dialog.dismiss();
                                e.printStackTrace();
                            }
                            dialog.dismiss();


                        }


                    }
                },
                new Response.ErrorListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        dialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("no",dcno);
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(ViewDeliveryPrint.this).addToRequestQueue(stringRequest2);
    }

    private void setImage(String path) {

        String base_url =  "/public_html/arnichem.co.in/intranet";
        String new_base_url = base_url.replace("/public_html/","");// this will contain "Fruit"


        StringBuilder logoUrl = new StringBuilder("http://"+new_base_url);
        logoUrl.append("/barcode/APP/images/digital_sign/").append(path);

        String finalLogoUrl = logoUrl.toString();
        //  Toast.makeText(this, ""+finalLogoUrl, Toast.LENGTH_SHORT).show();
        digital_sign = Util.getBitmapFromURL(finalLogoUrl);
        if(digital_sign!=null){
            digital_sign = Bitmap.createScaledBitmap(digital_sign,200, 200, true);

            signedImg.setImageBitmap(digital_sign);
        }else {
            digital_sign = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
            digital_sign.eraseColor(Color.WHITE);

        }

    }

    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd-MMM-yyyy h:mm:ss a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    private String fetchCustomerName(String cid) {
        String cust_name = null;
        Cursor cursor = databaseHandlercustomer.readAllData();
        if (cursor.getCount() == 0) {
            //      empty_imageview.setVisibility(View.VISIBLE);
            //      no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                String col = cursor.getString(1);
                String col1 = cursor.getString(2);
                if (col1.contentEquals(cid)) {

                    cust_name = col;

                }
            }
        }
        return  cust_name;
    }
    private void fetchUsername(String uid) {


        StringRequest stringRequest1 = new StringRequest(Request.Method.POST, APIClient.fetch_username,
                new Response.Listener<String>() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onResponse(String response) {
                        JSONArray array = null;


                        try {
                            array = new JSONArray(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        for (int i = 0; i < array.length(); i++) {
                            try {
                                object = array.getJSONObject(i);
                            } catch (JSONException e) {
                                dialog.dismiss();
                                e.printStackTrace();
                            }

                            try {

                                arnichemdignprint.setText(object.getString("fname")+object.getString("lname"));
                                username=object.getString("fname")+" "+object.getString("lname");

                            } catch (JSONException e) {
                                dialog.dismiss();
                                e.printStackTrace();
                            }
                            dialog.dismiss();


                        }


                    }
                },
                new Response.ErrorListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        dialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id",uid);
                params.put("db_host",SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(ViewDeliveryPrint.this).addToRequestQueue(stringRequest1);
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

    private void fetchDeliveryItems(String dcno) {
        // Replace with your actual values
        String dbHost = SharedPref.mInstance.getDBHost();
        String dbUsername = SharedPref.mInstance.getDBUsername();
        String dbPassword = SharedPref.mInstance.getDBPassword();
        String dbName = SharedPref.mInstance.getDBName();

        Call<FetchItemAndQuantityVolume> call = apiInterface.getDeliveryItems(dbHost, dbUsername, dbPassword, dbName, dcno);
        call.enqueue(new Callback<FetchItemAndQuantityVolume>() {

            @Override
            public void onResponse(Call<FetchItemAndQuantityVolume> call, retrofit2.Response<FetchItemAndQuantityVolume> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FetchItemAndQuantityVolume apiResponse = response.body();

                    // Handle the response data
                    if (apiResponse.getItems().isEmpty()) {
                        Log.d("API", "No items found for this dcno");
                    } else {
                        boolean isQuantity = false;

                        for (FetchItemAndQuantityVolume.Item item : apiResponse.getItems()) {
                            Log.d("API", "Item: " + item.getItem() + ", Quantity Volume: " + item.getQuantity_volume());
                            if(quantity_vol.isEmpty()) {
                                quantity_vol = String.valueOf(item.getQuantity_volume());
                            }

                            if(itemName.isEmpty()) {
                                itemName = item.getItem();
                            }
                            if (gasTypes.contains(item.getItem())) {
                                isQuantity = true;
                            }

                        }

                        // Log the result
                        if (!isQuantity) {
                            isOxygen = true;
                            cylinder_number_txt.setText("Cylinder No      :");
                            total_quantity_txt.setText("Total Quantity  :");
                            postrequ();
                            Log.d("API", "MEDOX7 is present in the response");
                        }else {
                            postrequ();
                            isOxygen = false;
                            cylinder_number_txt.setText("Item          :");
                            total_quantity_txt.setText("Quantity Volume :");
                            Log.d("API", "not isIndox7Present");
                            counttxt.setText(quantity_vol);
                            cylindernumberempty.setText(itemName);

                        }
                        saveFullScrollViewImage();
                    }
                } else {
                    Log.d("API", "Failed to fetch data");
                }
            }

            @Override
            public void onFailure(Call<FetchItemAndQuantityVolume> call, Throwable t) {
                // Handle failure
                Toast.makeText(ViewDeliveryPrint.this, "API Request Failed", Toast.LENGTH_SHORT).show();
                Log.e("API", t.getMessage(), t);
            }
        });
    }


    private void fetchGasTypes() {

        dialog = new ProgressDialog(ViewDeliveryPrint.this);
        dialog.setTitle("Data Fetching");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();


        Call<GasTypeResponse> call = apiInterface.fetchGasTypes(SharedPref.mInstance.getDBHost(), SharedPref.mInstance.getDBUsername(), SharedPref.mInstance.getDBPassword(), SharedPref.mInstance.getDBName());
        call.enqueue(new Callback<GasTypeResponse>() {

            @Override
            public void onResponse(Call<GasTypeResponse> call, retrofit2.Response<GasTypeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    gasTypes.clear();
                    gasTypes.addAll(response.body().getData().getGasTypes());
                    fetchDeliveryItems(dcno);

                    //  response.body().getData().getGasTypes()
                } else {
                    Toast.makeText(ViewDeliveryPrint.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<GasTypeResponse> call, @NonNull Throwable t) {
                Toast.makeText(ViewDeliveryPrint.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });
    }
    // New: Main method to capture, save, upload, and insert to DB

    private void saveFullScrollViewImage() {
        // 🌀 Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Preparing receipt for upload...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // ⏳ Delay for 1 second before capturing
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                ScrollView scrollView = findViewById(R.id.receipt_scroll_view);
                View childView = scrollView.getChildAt(0);

                // ✅ Measure and layout view properly
                int widthSpec = View.MeasureSpec.makeMeasureSpec(scrollView.getWidth(), View.MeasureSpec.EXACTLY);
                int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                childView.measure(widthSpec, heightSpec);
                childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());

                // ✅ Create bitmap and draw background
                Bitmap bitmap = Bitmap.createBitmap(
                        childView.getMeasuredWidth(),
                        childView.getMeasuredHeight(),
                        Bitmap.Config.ARGB_8888
                );
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                childView.draw(canvas);

                // ✅ Save bitmap to storage
                File dir = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "ArnichemReceipts"
                );
                if (!dir.exists()) dir.mkdirs();

                String fileName = "ScrollView_" + dcno + "_" + System.currentTimeMillis() + ".jpg";
                File imageFile = new File(dir, fileName);

                FileOutputStream out = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                // ✅ Make visible in gallery
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));

                // ✅ Upload automatically
                uploadAndInsertPod(imageFile.getAbsolutePath(), fileName);

                // ✅ Dismiss loader and show message
                progressDialog.dismiss();
                Toast.makeText(this, "Receipt saved and uploaded successfully!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, 1000); // 1 second delay
    }




    private void uploadAndInsertPod(String localPath, String fileName) {
        ProgressDialog uploadDialog = new ProgressDialog(this);
        uploadDialog.setTitle("Uploading POD");
        uploadDialog.setMessage("Please wait...");
        uploadDialog.setCancelable(false);
        uploadDialog.show();

        File podFile = new File(localPath);
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), podFile);

        // ✅ Server expects "print_image"
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("print_image", fileName, fileBody);

        // ✅ Text form data
        RequestBody dcnoBody   = RequestBody.create(MediaType.parse("text/plain"), dcno);
        RequestBody emailBody  = RequestBody.create(MediaType.parse("text/plain"), SharedPref.getInstance(this).getEmail());
        RequestBody dbHost     = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBHost());
        RequestBody dbUser     = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBUsername());
        RequestBody dbPass     = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBPassword());
        RequestBody dbName     = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBName());
        RequestBody transType  = RequestBody.create(MediaType.parse("text/plain"), "DC"); // or dynamic if available
        RequestBody vehicleNo  = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getVehicleNo()); // optional

        // ✅ Ensure the same parameter order as your PHP expects
        Call<ResponseBody> call = apiInterface.uploadPod(
                filePart,
                dcnoBody,
                emailBody,
                dbHost,
                dbUser,
                dbPass,
                dbName,
                transType,
                vehicleNo
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                uploadDialog.dismiss();

                try {
                    String resp = "";
                    if (response.body() != null)
                        resp = response.body().string();
                    else if (response.errorBody() != null)
                        resp = response.errorBody().string();

                    if (resp.isEmpty()) {
                        Toast.makeText(ViewDeliveryPrint.this, "Empty server response", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONObject obj = new JSONObject(resp);
                    String status = obj.optString("status", "error");
                    String msg = obj.optString("msg", "Unknown response");

                    if (status.equalsIgnoreCase("success")) {
                        Toast.makeText(ViewDeliveryPrint.this, msg, Toast.LENGTH_SHORT).show();
                        Log.d("POD_UPLOAD", "Server OK: " + msg);
                    } else {
                        Toast.makeText(ViewDeliveryPrint.this, "Upload failed: " + msg, Toast.LENGTH_LONG).show();
                        Log.e("POD_UPLOAD", "Error response: " + resp);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ViewDeliveryPrint.this, "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                uploadDialog.dismiss();
                Toast.makeText(ViewDeliveryPrint.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("POD_UPLOAD", "Upload failed", t);
            }
        });
    }

}