package com.arnichem.arnichem_barcode.TransactionsView.DryIce;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.Producation.DryIce.DryIceFIrstScreen;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliDB;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliveryprint;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DryIceDelivery extends AppCompatActivity implements Listener, LocationData.AddressCallBack {
    static JSONObject object = null;
    public int poslocfixdel, poscustfixdel;
    ProgressDialog dialog;
    ArrayList<String> name, tot, volume;
    com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;

    ImageView empty_imageview, signedImg;
    DatabaseHandler databaseHandlercustomer;
    TextView no_data, vehiclevalue, usernamevalue, date, totalscanval;
    Spinner spinner, customerspinnerdelivery;
    String from_warehouse, to_warehouse, cust_code, srno, count, from_code, latitude = "0", logitude = "0", address = "0";
    SharedPreferences pref;
    Button button, delprint;
    deliAdapter deliadapter;
    ArrayAdapter<String> dataAdapter;
    ArrayList<String> book_id, book_title;
    RecyclerView recyclerView, Filled_with_Recycle_View;
    FilledWithAdapter filledWithAdapter;
    ArrayAdapter<String> customerdataAdapter;
    deliDB delidb;
      syncHelper synchelper;
    EditText deliverycylindersea;
    Boolean checkInvoice = false;
    boolean status = false;
    GetLocationDetail getLocationDetail;
    Button uploadSign;
    ConstraintLayout constraintSigned;
    ImageView closeImg;
    String digital_sign = "", digitalSignPath = "";
    private EasyWayLocation easyWayLocation;
    FloatingActionButton add_button;

    ProgressDialog progressDialog;
    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("digital_sign")) {
                //Extract your data - better to use constants...
                String Signed = intent.getStringExtra("Signed");
                digitalSignPath = intent.getStringExtra("path");
                if (Signed.equalsIgnoreCase("true")) {
                    constraintSigned.setVisibility(View.VISIBLE);
                    File imgFile = new File(digitalSignPath);
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        digital_sign = Util.getImage(myBitmap);
                        signedImg.setImageBitmap(myBitmap);
                    }
                }
            }

        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dry_ice_delivery);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPref.getInstance(DryIceDelivery.this).setDoubleEntry("false");
        getSupportActionBar().setTitle("Dry Ice Delivery");
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false, true, this);
        filledWithAdapter = new FilledWithAdapter(DryIceDelivery.this, this, name, tot);
        spinner = findViewById(R.id.deliveryloc);
        deliverycylindersea = findViewById(R.id.deliverycylindersea);
        totalscanval = findViewById(R.id.totalscanval);
        customerspinnerdelivery = findViewById(R.id.cutomerdelivery);
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);

        poslocfixdel = Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel = Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        databaseHandlercustomer = new DatabaseHandler(DryIceDelivery.this);
        fromloccodehandler = new fromloccodehandler(DryIceDelivery.this);
        synchelper = new syncHelper(DryIceDelivery.this);
        signedImg = findViewById(R.id.signedImg);
        fetchData();
        loadSpinnerData();
        vehiclevalue = findViewById(R.id.vno);
        usernamevalue = findViewById(R.id.usernametxtvalue);
        button = findViewById(R.id.delMainPost);
        button.setEnabled(true);

        delprint = findViewById(R.id.delprintbtn);
        delprint.setVisibility(View.GONE);
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        vehiclevalue.setText(SharedPref.getInstance(this).getVehicleNo());
        date = findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        recyclerView = findViewById(R.id.recyclerView);
        Filled_with_Recycle_View = findViewById(R.id.fillwithrec);
        add_button = findViewById(R.id.deliveryscan);
        empty_imageview = findViewById(R.id.empty_imageview);
        closeImg = findViewById(R.id.closeImg);
        no_data = findViewById(R.id.no_data);
        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DryIceDelivery.this, ActivityDigitalSignature.class);
                intent.putExtra("type", "delivery");
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
                        String invoice = cursor.getString(3);
                        if (col.contentEquals(to_warehouse)) {
                            if (invoice.equalsIgnoreCase("Y")) {
                                if (!checkInvoice) {
                                    showAlertDialogButtonClicked(view);
                                }
                            }
                            cust_code = col1;
                            checkdual(cust_code, view);
                        }
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status =true;
                Intent intent =new Intent(DryIceDelivery.this, NewScanner.class);
                intent.putExtra("type", "delivery");
                startActivity(intent);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // button.setEnabled(false);
                postUsingVolley();

            }
        });
        delprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delPrint();
            }
        });
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        deliadapter = new deliAdapter(DryIceDelivery.this, this, book_id, book_title);
        name = new ArrayList<>();
        tot = new ArrayList<>();
        volume = new ArrayList<>();


        totalscanval.setText(count);
        filledWithAdapter = new FilledWithAdapter(DryIceDelivery.this, this, name, tot);
        Filled_with_Recycle_View.setAdapter(filledWithAdapter);
        Filled_with_Recycle_View.setLayoutManager(new LinearLayoutManager(DryIceDelivery.this));
        recyclerView.setAdapter(deliadapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(DryIceDelivery.this));
        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
                new IntentFilter("digital_sign"));


    }

    private void delPrint() {
        Intent intent = new Intent(DryIceDelivery.this, deliveryprint.class);
        intent.putExtra("weight", deliverycylindersea.getText().toString());
        intent.putExtra("custname", to_warehouse);
        intent.putExtra("empb", srno);
        intent.putExtra("count", count);
        intent.putExtra("custcode", cust_code);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        if(status){
            status = false;
            startActivity(getIntent());
        }
        super.onResume();
        easyWayLocation.startLocation();

    }

    @Override
    protected void onPause() {
        super.onPause();
        easyWayLocation.endUpdates();
    }

    @Override
    public void locationOn() {
        Toast.makeText(this, "Location On", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void currentLocation(Location location) {
        latitude = String.valueOf(location.getLatitude());

        logitude = String.valueOf(location.getLongitude());

        getLocationDetail.getAddress(location.getLatitude(), location.getLongitude(), "xyz");
    }

    @Override
    public void locationCancelled() {
        Toast.makeText(this, "Location Cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void locationData(LocationData locationData) {
        address = locationData.getFull_address();


    }

    public void showAlertDialogButtonClicked(View view) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle("Alert!");
        builder.setMessage("Customer should be given an invoice during delivery\n ह्या गिरहिकासाठी डेलिवेरी चलन बनवताना इनवॉइस द्यायचे आहे");
        // add a button
        builder.setPositiveButton("OK", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        checkInvoice = true;
    }

    public void showAlertDialogDualDelivery(View view) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle("Alert!");
        builder.setMessage("काय तुह्मी खरचं या कस्टमर ला डिलिव्हरी करणार आहात का ? कारण आज या कस्टमर ला एकदा डेलिव्हवरी झाली आहे.");
        // add a button
        builder.setPositiveButton("हो", null);
        builder.setNegativeButton("नाही", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed();

            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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


    private void checkdual(String cust_code, View view) {
        dialog = new ProgressDialog(DryIceDelivery.this);
        dialog.setTitle("Loading");
        dialog.setMessage("Please wait....");
        dialog.setCancelable(false);

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.check_dual_delivery,
                new Response.Listener<String>() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        checkInvoice = true;
                        try {
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                object = array.getJSONObject(i);
                                String count = object.getString("data");

                                if (count.equals("0")) {
                                } else {
                                    showAlertDialogDualDelivery(view);
                                }
                            }
                        } catch (JSONException e) {
                            dialog.dismiss();
                            e.printStackTrace();
                            // dialog.dismiss();
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
                params.put("type", "DEL");
                params.put("cust_code", cust_code);
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username", SharedPref.mInstance.getDBUsername());
                params.put("db_password", SharedPref.mInstance.getDBPassword());
                params.put("db_name", SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(DryIceDelivery.this).addToRequestQueue(stringRequest);
    }

    private void postUsingVolley() {

        dialog = new ProgressDialog(DryIceDelivery.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poslocfixdel == 0) {
            button.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(DryIceDelivery.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        } else if (poscustfixdel == 0) {
            button.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(DryIceDelivery.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


        } else {
            DryICPost();
            // StringBuffer to String conversion
//            VolleySingleton.getInstance(DryIceDelivery.this).addToRequestQueue(stringRequest);
        }
    }


    private void DryICPost() {
        progressDialog = new ProgressDialog(DryIceDelivery.this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please wait....");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(DryIceDelivery.this);
        StringRequest request = new StringRequest(Request.Method.POST, APIClient.dry_ice_delivery_entry, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                progressDialog.dismiss();
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    // Assuming the response contains only one object in the array
                    JSONObject respObj = jsonArray.getJSONObject(0);

                    // Access the values from the JSON object
                    String status = respObj.getString("status");
                    String msg = respObj.getString("msg");

                    // Use the status and message as needed
                    if (status.equalsIgnoreCase("success")) {

                        MDToast.makeText(DryIceDelivery.this, "Delivey Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                       MDToast.makeText(DryIceDelivery.this, "आता प्रिंट बटण दाबा !", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                        button.setVisibility(View.GONE);
                        delprint.setVisibility(View.VISIBLE);
                        srno = respObj.getString("srno");

                        dialog.dismiss();
                        Intent intent = new Intent(DryIceDelivery.this, dry_ice_print.class);
                        intent.putExtra("custname", to_warehouse);
                        intent.putExtra("custcode", cust_code);
                        intent.putExtra("empb", srno);
                        intent.putExtra("weight", deliverycylindersea.getText().toString()
                        );
                        intent.putExtra("sign_path", digitalSignPath);
                        button.setEnabled(true);
                        startActivity(intent);
                        finish();


                        //  showAlert(true, msg);
                    } else {
                      //  showAlert(false, msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // method to handle errors.
                progressDialog.dismiss();
                Toast.makeText(DryIceDelivery.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("weight", deliverycylindersea.getText().toString());
                params.put("from_warehouse", from_warehouse);
                params.put("to_warehouse", to_warehouse);
                params.put("transport_type", "ARNICHEM");
                params.put("cust_code", cust_code);
                params.put("from_code", from_code);
                params.put("sign", digital_sign);
                params.put("lati", latitude);
                params.put("logi", logitude);
                params.put("addr", address);
                params.put("transport_no", SharedPref.getInstance(DryIceDelivery.this).getVehicleNo());
                params.put("driver", SharedPref.getInstance(DryIceDelivery.this).getID());
                params.put("email", SharedPref.getInstance(DryIceDelivery.this).getEmail());
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username", SharedPref.mInstance.getDBUsername());
                params.put("db_password", SharedPref.mInstance.getDBPassword());
                params.put("db_name", SharedPref.mInstance.getDBName());
                return params;
            }
        };
        // below line is to make
        // a json object request.
        queue.add(request);
    }


    private void showAlert(boolean val,String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DryIceDelivery.this);
        if(val){
            builder.setTitle("Success")
                    .setMessage(msg)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Handle OK button click
                            delprint.setVisibility(View.VISIBLE);
                        }
                    })
                    .setCancelable(false) // Set dialog to not cancelable

                    // Optional: Add more buttons or customize the dialog further

                    .show();

        }else {
            builder.setTitle("Failed")
                    .setMessage("This is an alert dialog")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Handle OK button click
                        }
                    })
                    // Optional: Add more buttons or customize the dialog further

                    .show();

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (delidb != null)
            delidb.close();


    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Transactions.class);
        startActivity(intent);
    }



}
