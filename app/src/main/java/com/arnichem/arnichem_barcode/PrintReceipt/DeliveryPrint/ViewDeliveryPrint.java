package com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.arnichem.arnichem_barcode.PrintReceipt.EmptyPrint.ViewEmptyPrint;
import com.arnichem.arnichem_barcode.PrintReceipt.MainAdapter;
import com.arnichem.arnichem_barcode.PrintReceipt.MainPrintActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
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
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;


public class ViewDeliveryPrint extends AppCompatActivity {
    public static final int PERMISSION_BLUETOOTH = 1;
    static JSONObject object = null;
    Button duradelprint;
    String pos,custname,dcno,type,custcode,username,totalQuan,delidate,strVehicleNo;
    TextView empbid,dateid,custnameid,cylindernumberempty,vehicleno,arnichemdignprint,counttxt,tvcode;
    ArrayList<String> newlist;
    ProgressDialog dialog;
    DatabaseHandler databaseHandlercustomer;
    private BluetoothConnection selectedDevice;
    Bitmap printLogoDr,phoneNumberDr,digital_sign;
    ImageView printImg,phoneImg,signedImg;
    TextView arnichemsignTxt,termsTxt;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_delivery_print);
        empbid=findViewById(R.id.cdcnoid);
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

        StrictMode.setThreadPolicy(policy);
        Intent i=getIntent();
        dcno = i.getStringExtra("no");
        type = i.getStringExtra("type");
        empbid.setText(dcno);
        postrequ();

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
        arnichemsignTxt.setText("For "+SharedPref.mInstance.getCompanyFullName());
        termsTxt.setText(SharedPref.mInstance.getTermsText());


    }

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

    public void printBluetooth() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, finalprint.PERMISSION_BLUETOOTH);
        } else {
            new AsyncBluetoothEscPosPrint(this).execute(this.getAsyncEscPosPrinter(selectedDevice));
            secondPrint();
        }
    }
    public void secondPrint() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, finalprint.PERMISSION_BLUETOOTH);
        } else {
            new AsyncBluetoothEscPosPrint(this).execute(this.getAsyncEscPosPrinter(selectedDevice));
        }
    }


    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                "[R]Delivery challan  [R]\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,phoneNumberDr)+"</img>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,printLogoDr)+"</img>\n\n" +
                        "[C]<font size='small'>DCNO -  "+dcno+"</font>\n" +
                        "[C]<font size='small'>Date -  "+delidate+"</font>\n" +
                        "[C]<font size='small'>Code -  "+custcode+"</font>\n" +
                        "[C]<font size='small'>Name -  "+custname+"</font>\n" +
                        "[C]<font size='small'>       Cylinder Details </font>\n" +
                        "[C]<font size='small'><b>       Cylinder Numbers </b></font>\n" +
                        "[C]<font size='small'><b>            "+foreaching()+"</b></font>\n" +
                        "[C]<font size='small'>Total Quantity : "+totalQuan+"</font>\n" +
                        "[C]<font size='small'>Vehicle No    :  "+ strVehicleNo+"</font>\n" +
                        "[C]<font size='small'>Invoice No    :  "+"  "+"</font>\n\n\n" +
                        "[L]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,digital_sign)+"</img>\n" +
                        "[R]               [R]"+username+"\n" +
                        "[R]Customer Sign [R]"+"For "+SharedPref.getInstance(this).getCompanyFullName()+"\n\n"+
                        "[R]"+SharedPref.getInstance(this).getTermsText()+"\n"
        );
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
        dialog.setTitle("Data Inserting");
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
                            String joined = TextUtils.join(",", newlist);
                                cylindernumberempty.setText(joined);
                                dialog.dismiss();


                            }
                            String joined = TextUtils.join(",", newlist);
                            totalQuan = String.valueOf(newlist.size());
                            cylindernumberempty.setText(joined);
                            counttxt.setText(totalQuan);

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
                                Toast.makeText(ViewDeliveryPrint.this, ""+object.getString("path"), Toast.LENGTH_SHORT).show();

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

        String base_url =  SharedPref.getInstance(ViewDeliveryPrint.this).getBaseUrl();

        String new_base_url = base_url.replace("/public_html/","");// this will contain "Fruit"


        StringBuilder logoUrl = new StringBuilder("http://"+new_base_url);
        logoUrl.append("/barcode/APP/images/digital_sign/").append(path);

        String finalLogoUrl = logoUrl.toString();
        Toast.makeText(this, ""+finalLogoUrl, Toast.LENGTH_SHORT).show();
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




}