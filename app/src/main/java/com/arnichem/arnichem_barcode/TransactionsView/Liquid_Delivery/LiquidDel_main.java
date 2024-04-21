package com.arnichem.arnichem_barcode.TransactionsView.Liquid_Delivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.FullRecipt.FullReciptMainActivity;
import com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardMain;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.Delivery_type_liquid_Handler;
import com.arnichem.arnichem_barcode.view.LocationHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LiquidDel_main extends AppCompatActivity {
    ProgressDialog dialog;
    com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;
    RecyclerView recyclerView;
    ImageView empty_imageview;
    DatabaseHandler databaseHandlercustomer;
    Delivery_type_liquid_Handler delivery_type_liquid_handler;
    TextView no_data, vehiclevalue, usernamevalue, date, totalscanval;
    Spinner spinner, customerspinnerdelivery,delivery_type_spinner;
    String from_warehouse, to_warehouse,delivery_type,delivery_type_code,cust_code, srno, from_code,latitude="",logitude="",address="",unit,con_f,digitalSignPath="";
    Button button, delprint,Calculateammonia;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    ArrayAdapter<String> delivery_type_Adapter;
    public int poslocfixdel, poscustfixdel,posdeliverytype;
    static JSONObject object = null;
    syncHelper synchelper;
    Button uploadSign;
    String digital_sign = "" ;
    ConstraintLayout constraintSigned;
    ImageView closeImg,signedImg;
    boolean status = false;
    FloatingActionButton scanBtn;
    public String net_wt="";
    AutoCompleteTextView deliveryammoniafullwt,getDeliveryammoniaemptywt,getDeliveryammonianetwt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liquid_del_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Liquid Delivery");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        spinner = findViewById(R.id.deliveryloc);
        delivery_type_spinner=findViewById(R.id.delivery_type_spinner);
        totalscanval = findViewById(R.id.totalscanval);
        Calculateammonia=findViewById(R.id.Calculateammonia);
        customerspinnerdelivery = findViewById(R.id.cutomerdelivery);
        poslocfixdel = Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel = Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        databaseHandlercustomer = new DatabaseHandler(LiquidDel_main.this);
        delivery_type_liquid_handler = new Delivery_type_liquid_Handler(LiquidDel_main.this);
        fromloccodehandler = new fromloccodehandler(LiquidDel_main.this);
        synchelper = new syncHelper(LiquidDel_main.this);
        deliveryammoniafullwt=findViewById(R.id.deliveryammoniafullwt);
        getDeliveryammoniaemptywt=findViewById(R.id.deliveryammoniaemptywt);
        getDeliveryammonianetwt=findViewById(R.id.deliveryammonianetwt);
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);
        closeImg = findViewById(R.id.closeImg);
        signedImg = findViewById(R.id.signedImg);
        scanBtn =findViewById(R.id.scanBtn);


        fetchData();
        loadSpinnerData();
        loadSpinner_delivery_type();
        vehiclevalue = findViewById(R.id.vno);
        usernamevalue = findViewById(R.id.usernametxtvalue);
        button = findViewById(R.id.delMainPost);
        button.setEnabled(true);
        delprint = findViewById(R.id.delprintbtn);
        delprint.setVisibility(View.GONE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        vehiclevalue.setText(SharedPref.getInstance(this).getVehicleNo());
        date = findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        recyclerView = findViewById(R.id.recyclerView);
//        add_button = findViewById(R.id.deliveryscan);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = false;
                Intent intent = new Intent(LiquidDel_main.this, NewScanner.class);
                startActivity(intent);
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                from_warehouse = dataAdapter.getItem(position);
                poslocfixdel = position;
                SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(poslocfixdel));
                Cursor cursor = fromloccodehandler.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);

                        if (col.contentEquals(from_warehouse)) {
                            from_code = col1;


                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        delivery_type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                delivery_type = delivery_type_Adapter.getItem(position);
                posdeliverytype = position;
                SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(poscustfixdel));
                Cursor cursor = delivery_type_liquid_handler.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        String unit_t = cursor.getString(3);
                        String conv_factor_t= cursor.getString(4);

                        if (col.contentEquals(delivery_type)) {
                            delivery_type_code = col1;
                            con_f=conv_factor_t;
                            unit=unit_t;

                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        customerspinnerdelivery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                to_warehouse = customerdataAdapter.getItem(position);
                poscustfixdel = position;
                SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(poscustfixdel));
                Cursor cursor = databaseHandlercustomer.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        if (col.contentEquals(to_warehouse)) {
                            cust_code = col1;

                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                postUsingVolley();
            }
        });

        Calculateammonia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 if (posdeliverytype == 0) {
                    MDToast.makeText(LiquidDel_main.this, "कृपया Delivery type निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                 }
                 else  if (deliveryammoniafullwt.getText().toString().isEmpty()) {
                    MDToast.makeText(LiquidDel_main.this, "कृपया टँकर फुल वेट टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else if (getDeliveryammoniaemptywt.getText().toString().isEmpty())
                {
                    MDToast.makeText(LiquidDel_main.this, "कृपया टँकर एमटी वेट टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else {
                    double fullwt= Double.parseDouble(deliveryammoniafullwt.getText().toString());
                    double empwt= Double.parseDouble(getDeliveryammoniaemptywt.getText().toString());
                    double netWt=fullwt-empwt;
                    net_wt = String.valueOf(netWt);
                    String finalnetwt;
                    if(con_f.equals("1"))
                    {
                        finalnetwt= String.valueOf(netWt);
                    }
                    else {
                        finalnetwt=String.valueOf(netWt*Double.parseDouble(con_f));
                        finalnetwt=finalnetwt+" "+unit;
                    }
                    getDeliveryammonianetwt.setText(finalnetwt);

                }
            }

        });

        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LiquidDel_main.this, ActivityDigitalSignature.class);
                intent.putExtra("type","delivery");
                startActivity(intent);
            }
        });
        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPref.getInstance(getApplicationContext()).setSign("");
                constraintSigned.setVisibility(View.GONE);
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
                new IntentFilter("digital_sign"));



    }

    private void fetchData() {
        fromloccodehandler db = new fromloccodehandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        if (poslocfixdel != 0) {
            spinner.setSelection(poslocfixdel);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void loadSpinnerData() {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        customerdataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        customerdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //  data adapter to spinner
        customerspinnerdelivery.setAdapter(customerdataAdapter);
        if (poscustfixdel != 0) {
            customerspinnerdelivery.setSelection(poscustfixdel);
        }
    }




    private void loadSpinner_delivery_type() {
        Delivery_type_liquid_Handler db = new Delivery_type_liquid_Handler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        delivery_type_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        delivery_type_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //  data adapter to spinner
        delivery_type_spinner.setAdapter(delivery_type_Adapter);
        if (posdeliverytype != 0) {
            delivery_type_spinner.setSelection(posdeliverytype);
        }
    }







    private void postUsingVolley() {

            dialog = new ProgressDialog(LiquidDel_main.this);
            dialog.setTitle("Data Inserting");
            dialog.setMessage("Please wait....");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
            if (poslocfixdel == 0) {
                dialog.dismiss();
                button.setEnabled(true);
                MDToast.makeText(LiquidDel_main.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

            } else if (poscustfixdel == 0) {
                dialog.dismiss();
                button.setEnabled(true);
                MDToast.makeText(LiquidDel_main.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


            }
            else if (posdeliverytype == 0) {
                dialog.dismiss();
                button.setEnabled(true);
                MDToast.makeText(LiquidDel_main.this, "कृपया Delivery type निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


            }
            else if (getDeliveryammonianetwt.getText().toString().isEmpty()) {
                dialog.dismiss();
                button.setEnabled(true);
                MDToast.makeText(LiquidDel_main.this, "कृपया Net Weight Calculate करा  !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


            }
            else {

                Toast.makeText(this, "unit"+unit, Toast.LENGTH_SHORT).show();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.Liquid_delivery_entry,
                        new Response.Listener<String>() {
                            @SuppressLint("WrongConstant")
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONArray array = new JSONArray(response);
                                    for (int i = 0; i < array.length(); i++) {
                                        object = array.getJSONObject(i);
                                        String status = object.getString("status");
                                        String msg = object.getString("msg");

                                        if (status.equals("success")) {
                                            MDToast.makeText(LiquidDel_main.this, "Delivey Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                            MDToast.makeText(LiquidDel_main.this, "आता प्रिंट बटण दाबा !", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                                            button.setVisibility(View.GONE);
                                            delprint.setVisibility(View.VISIBLE);
                                            srno = object.getString("srno");

//                                        //  SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));
                                            dialog.dismiss();
                                            Intent intent = new Intent(LiquidDel_main.this, LiqourPrint.class);
                                            intent.putExtra("custname", to_warehouse);
                                            intent.putExtra("custcode", cust_code);
                                            intent.putExtra("empb", srno);
                                            intent.putExtra("delivery_type", delivery_type);
                                            intent.putExtra("sign_path",digitalSignPath);
                                            intent.putExtra("delivery_type_code", delivery_type_code);
                                            intent.putExtra("fullwt", deliveryammoniafullwt.getText().toString());
                                            intent.putExtra("emptywt",getDeliveryammoniaemptywt.getText().toString());
                                            intent.putExtra("netwt", getDeliveryammonianetwt.getText().toString());
                                            button.setEnabled(true);
                                            startActivity(intent);

                                        } else {
                                            button.setEnabled(true);
                                            dialog.dismiss();

                                        }

                                        Log.e("JSON", "> " + status + msg);
                                    }

                                } catch (JSONException e) {
                                    button.setEnabled(true);
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @SuppressLint("WrongConstant")
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                button.setEnabled(true);
                                error.printStackTrace();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("from_warehouse", from_warehouse);
                        params.put("to_warehouse", to_warehouse);
                        params.put("delivery_type", delivery_type_code);
                        params.put("unit",unit);
                        params.put("transport_type", "ARNICHEM");
                        params.put("cust_code", cust_code);
                        params.put("from_code", from_code);
                        params.put("full_wt", deliveryammoniafullwt.getText().toString());
                        params.put("empty_wt", getDeliveryammoniaemptywt.getText().toString());
                        params.put("conv_unit", getDeliveryammonianetwt.getText().toString());
                        params.put("net_wt", net_wt);
                        params.put("lati", latitude);
                        params.put("logi", logitude);
                        params.put("addr",address);
                        params.put("sign", digital_sign);
                        params.put("transport_no", SharedPref.getInstance(LiquidDel_main.this).getVehicleNo());
                        params.put("driver", SharedPref.getInstance(LiquidDel_main.this).getID());
                        params.put("email", SharedPref.getInstance(LiquidDel_main.this).getEmail());
                        params.put("db_host",SharedPref.mInstance.getDBHost());
                        params.put("db_username",SharedPref.mInstance.getDBUsername());
                        params.put("db_password",SharedPref.mInstance.getDBPassword());
                        params.put("db_name",SharedPref.mInstance.getDBName());
                        return params;
                    }
                };
                VolleySingleton.getInstance(LiquidDel_main.this).addToRequestQueue(stringRequest);
            }
        }




    private BroadcastReceiver mServiceReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().equalsIgnoreCase("digital_sign")){
                //Extract your data - better to use constants...
                String Signed=intent.getStringExtra("Signed");
                digitalSignPath=intent.getStringExtra("path");
                if (Signed.equalsIgnoreCase("true"))
                {
                    constraintSigned.setVisibility(View.VISIBLE);
                    File imgFile = new  File(digitalSignPath);
                    if(imgFile.exists()){
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        digital_sign = Util.getImage(myBitmap);
                        signedImg.setImageBitmap(myBitmap);
                    }
                }
            }

        }
    };


    @Override
    protected void onResume() {
        if(status){
            status = false;
            startActivity(getIntent());
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Transactions.class);
        startActivity(intent);
    }
}