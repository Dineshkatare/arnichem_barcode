package com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryMainActivity;
import com.arnichem.arnichem_barcode.Producation.DuraScan;
import com.arnichem.arnichem_barcode.Producation.Oxygen.OxygenFilling;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.TransactionsView.Duraempty.duraemptymain;
import com.arnichem.arnichem_barcode.TransactionsView.Duraempty.duraemptyprint;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.attendance.MyResponseModel;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.CustomerSearchHandler;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.DurasyncHelper;
import com.arnichem.arnichem_barcode.view.LocationHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class DuraDeliveryMain extends AppCompatActivity implements Listener, LocationData.AddressCallBack{
    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    RecyclerView recyclerView;
    FloatingActionButton add_button;
    ImageView empty_imageview;
    TextView no_data,vehiclevalue,usernamevalue,date;
    Spinner spinner,customerspinner,cylinderspin;
    AutoCompleteTextView autoCompleteTextView;
    SharedPreferences pref;
    ProgressDialog dialog;
    String s,dcno,is_scan="";
    String from_warehouse,to_warehouse,cust_code,searchcylinder,from_code;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    public  int posloc,poscust;
    Button submit,print;
    DatabaseHandler databaseHandlercustomer;
    fromloccodehandler fromloccodehandler;
    static JSONObject object =null;
    String fullwt,gastype,emtywt,netwt,cubic,latitude="0",logitude="0",address="0",digitalSignPath = "";
    ArrayAdapter<String> cylinderAdapter;
    public  int poslocfix,poscustfix;
    syncHelper synchelper;
    List<String> item;
    List<String> itemq;
    List<String> item_volume;
    TextView fullwttxt,emptywttxt,netwttxt,gasType,gasTypeVal,cubictxt,fullwtval,emptywtval,netwtval,cubicval;
    Button uploadSign;
    ConstraintLayout constraintSigned;
    ImageView closeImg,signedImg;
    String digital_sign = "" ;
    APIInterface apiInterface;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dura_delivery_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Dura Delivery");
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false,true,this);
        pref = getSharedPreferences(constant.TAG,MODE_PRIVATE);
        spinner=findViewById(R.id.duradeliveryloc);
        synchelper = new syncHelper(DuraDeliveryMain.this);
        databaseHandlercustomer=new DatabaseHandler(DuraDeliveryMain.this);
        fromloccodehandler=new fromloccodehandler(DuraDeliveryMain.this);
        print=findViewById(R.id.duradeliprint);
        autoCompleteTextView=findViewById(R.id.duradelivercylinder);
        poslocfix= Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfix=Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        autoCompleteTextView.setVisibility(View.GONE);
        print.setVisibility(View.GONE);
        customerspinner=findViewById(R.id.cutomerduradelivery);
        cylinderspin=findViewById(R.id.spinnercylinderdelivery);
        submit=findViewById(R.id.DuraDeliverySubmit);
        submit.setEnabled(true);
        fullwttxt=findViewById(R.id.Fullwttxt);
        fullwttxt.setVisibility(View.GONE);
        emptywttxt=findViewById(R.id.empty_wttxt);
        emptywttxt.setVisibility(View.GONE);
        netwttxt=findViewById(R.id.net_wttxt);
        netwttxt.setVisibility(View.GONE);
        gasType = findViewById(R.id.gasType);
        gasTypeVal = findViewById(R.id.gasTypeVal);
        gasType.setVisibility(View.GONE);
        gasTypeVal.setVisibility(View.GONE);
        cubictxt=findViewById(R.id.cubictxt);
        cubictxt.setVisibility(View.GONE);
        fullwtval=findViewById(R.id.Fullwtval);
        fullwtval.setVisibility(View.GONE);
        emptywtval=findViewById(R.id.empty_wtval);
        emptywtval.setVisibility(View.GONE);
        netwtval=findViewById(R.id.net_wtval);
        netwtval.setVisibility(View.GONE);
        cubicval=findViewById(R.id.cubicval);
        cubicval.setVisibility(View.GONE);
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);
        closeImg = findViewById(R.id.closeImg);
        signedImg = findViewById(R.id.signedImg);

        apiInterface = APIClient.getClient().create(APIInterface.class);

        item=new ArrayList<>();
        itemq=new ArrayList<>();
        item_volume=new ArrayList<>();
        loadSpinnerData();
        fetchData();
        fetchserachcylinder();
        vehiclevalue=findViewById(R.id.vno);
        usernamevalue=findViewById(R.id.usernametxtvalue);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName());
        vehiclevalue.setText(SharedPref.getInstance(this).getVehicleNo());
        date=findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        recyclerView = findViewById(R.id.recyclerView);
        add_button = findViewById(R.id.duradeliveryscan);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);




        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DuraDeliveryMain.this,Duradeliveryprint.class);
                intent.putExtra("gastype",gastype);
                intent.putExtra("Fullwt",fullwt);
                intent.putExtra("tarewt",emtywt);
                intent.putExtra("NetWt",netwt);
                intent.putExtra("cubic",cubic);
                intent.putExtra("durano",s);
                intent.putExtra("custcode",cust_code);
                intent.putExtra("custname",to_warehouse);
                intent.putExtra("dcno",dcno);
                startActivity(intent);


            }
        });
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(DuraDeliveryMain.this, NewScanner.class);
                intent.putExtra("type", "dura_delivery");
                startActivity(intent);
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit.setEnabled(false);
                //postUsingVolley();
                postUsingRetrofit();
            }
        });






        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                from_warehouse = dataAdapter.getItem(position);
                posloc=position;
                SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(posloc));
                Cursor cursor = fromloccodehandler.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col=cursor.getString(1);
                        String col1 =cursor.getString(2);
                        if(col.contentEquals(from_warehouse))
                        {
                            from_code=col1;

                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cylinderspin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchcylinder = cylinderAdapter.getItem(position);
                s=searchcylinder;
                is_scan = "no";

                login();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        customerspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                to_warehouse = customerdataAdapter.getItem(position);
                poscust=position;
                SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(poscust));
                Cursor cursor = databaseHandlercustomer.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col=cursor.getString(1);
                        String col1 =cursor.getString(2);
                        String invoice =cursor.getString(3);



                        if(col.contentEquals(to_warehouse))
                        {
                            if(invoice.equalsIgnoreCase(
                                    "Y"))
                            {
                                showAlertDialogButtonClicked(view);
                           }
                            cust_code=col1;

                            checkdual(cust_code,view);

                            if (cursor.getString(4) != null && !cursor.getString(4).isEmpty()) {
                                String message = cursor.getString(4).replace("\\n", "\n"); // Replace literal "\n" with a newline
                                showCustomMsg( message);
                                Log.d("chech",""+message);
                            }

                        }
                    }
                }
                poscust=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DuraDeliveryMain.this, ActivityDigitalSignature.class);
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
        LocalBroadcastManager.getInstance(this).registerReceiver(dura_no_receiver,
                new IntentFilter("dura_delivery"));



    }
    @Override
    protected void onResume() {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        builder.setTitle("Alert!");
        builder.setMessage("Customer should be given an invoice during delivery\n ह्या गिरहिकासाठी डेलिवेरी चलन बनवताना इनवॉइस द्यायचे आहे");
        // add a button
        builder.setPositiveButton("OK", null);
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void loadSpinnerData() {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        customerdataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        customerdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        customerspinner.setAdapter(customerdataAdapter);
        if(poscustfix!=0)
        {
            customerspinner.setSelection(poscustfix);
        }
    }

    private void checkdual(String cust_code,View view)
    {
        dialog = new ProgressDialog(DuraDeliveryMain.this);
        dialog.setTitle("Loading");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,APIClient.check_dual_delivery,
                new Response.Listener<String>() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                object = array.getJSONObject(i);
                                String count = object.getString("data");

                                dialog.dismiss();
                                if(count.equals("0"))
                                {

                                }
                                else {
                                    showAlertDialogDualDelivery(view);
                                }


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
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
                params.put("type","DEL");
                params.put("cust_code",cust_code);
                params.put("db_host",SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(DuraDeliveryMain.this).addToRequestQueue(stringRequest);
    }


    public void showAlertDialogDualDelivery(View view) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
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
        dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        if(poslocfix!=0)
        {
            spinner.setSelection(poslocfix);
        }

    }
    private void fetchserachcylinder() {
        DurasyncHelper db = new DurasyncHelper(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        cylinderAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        cylinderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        cylinderspin.setAdapter(cylinderAdapter);



    }

    private void postUsingRetrofit() {
        dialog = new ProgressDialog(DuraDeliveryMain.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        if (posloc == 0) {
            submit.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(DuraDeliveryMain.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else if (poscust == 0) {
            submit.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(DuraDeliveryMain.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else {
            // Create a File instance from your image file path
            MultipartBody.Part signPart = null;
            // Create a File instance from your image file path
            if(!digitalSignPath.isEmpty()){
                File file = new File(digitalSignPath); // Replace with the actual path to your image file
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                signPart = MultipartBody.Part.createFormData("sign", file.getName(), requestFile);
            }

            RequestBody duraCode = RequestBody.create(s, MediaType.parse("text/plain"));
            RequestBody isScan = RequestBody.create(is_scan, MediaType.parse("text/plain"));
            RequestBody item_request = RequestBody.create(String.valueOf(item), MediaType.parse("text/plain"));
            RequestBody itemq_request = RequestBody.create(String.valueOf(itemq), MediaType.parse("text/plain"));
            RequestBody itemVolume = RequestBody.create(String.valueOf(item_volume), MediaType.parse("text/plain"));
            RequestBody fromWarehouse = RequestBody.create(from_warehouse, MediaType.parse("text/plain"));
            RequestBody fromCode = RequestBody.create(from_code, MediaType.parse("text/plain"));
            RequestBody toWarehouse = RequestBody.create(to_warehouse, MediaType.parse("text/plain"));
            RequestBody transportType = RequestBody.create("ARNICHEM", MediaType.parse("text/plain"));
            RequestBody custCode = RequestBody.create(cust_code, MediaType.parse("text/plain"));
            RequestBody lati = RequestBody.create(latitude, MediaType.parse("text/plain"));
            RequestBody logi = RequestBody.create(logitude, MediaType.parse("text/plain"));
            RequestBody addr = RequestBody.create(address, MediaType.parse("text/plain"));
            RequestBody transportNo = RequestBody.create(SharedPref.getInstance(DuraDeliveryMain.this).getVehicleNo(), MediaType.parse("text/plain"));
            RequestBody driver = RequestBody.create(SharedPref.getInstance(DuraDeliveryMain.this).getID(), MediaType.parse("text/plain"));
            RequestBody email = RequestBody.create(SharedPref.getInstance(DuraDeliveryMain.this).getEmail(), MediaType.parse("text/plain"));
            RequestBody dbHost = RequestBody.create(SharedPref.mInstance.getDBHost(), MediaType.parse("text/plain"));
            RequestBody dbUsername = RequestBody.create(SharedPref.mInstance.getDBUsername(), MediaType.parse("text/plain"));
            RequestBody dbPassword = RequestBody.create(SharedPref.mInstance.getDBPassword(), MediaType.parse("text/plain"));
            RequestBody dbName = RequestBody.create(SharedPref.mInstance.getDBName(), MediaType.parse("text/plain"));

            // Create Retrofit service
            Call<MyResponseModel> call = apiInterface.uploadDeliveryData(
                    duraCode,
                    isScan,
                    item_request,
                    itemq_request,
                    itemVolume,
                    fromWarehouse,
                    fromCode,
                    toWarehouse,
                    transportType,
                    custCode,
                    lati,
                    logi,
                    addr,
                    transportNo,
                    driver,
                    email,
                    dbHost,
                    dbUsername,
                    dbPassword,
                    dbName,
                    signPart
            );

            call.enqueue(new Callback<MyResponseModel>() {

                @Override
                public void onResponse(Call<MyResponseModel> call, retrofit2.Response<MyResponseModel> response) {
                    {
                        if (response.isSuccessful()) {
                            MyResponseModel myResponseModel = response.body();
                            if (myResponseModel != null && myResponseModel.getStatus().equals("success")) {
                                // Handle success
                                MDToast.makeText(DuraDeliveryMain.this, s + " Dura Delivey Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                MDToast.makeText(DuraDeliveryMain.this, "आता प्रिंट बटण दाबा !", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                dcno = myResponseModel.getSrno();
                                print.setVisibility(View.VISIBLE);
                                submit.setVisibility(View.GONE);
                                dialog.dismiss();
                                Intent intent = new Intent(DuraDeliveryMain.this, Duradeliveryprint.class);
                                intent.putExtra("Fullwt", fullwt);
                                intent.putExtra("tarewt", emtywt);
                                intent.putExtra("gastype", gastype);
                                intent.putExtra("NetWt", netwt);
                                intent.putExtra("cubic", cubic);
                                intent.putExtra("sign_path", digitalSignPath);
                                intent.putExtra("custcode", cust_code);
                                intent.putExtra("durano", s);
                                intent.putExtra("custname", to_warehouse);
                                intent.putExtra("dcno", dcno);
                                submit.setEnabled(true);
                                startActivity(intent);
                            } else {
                                // Handle error
                                submit.setEnabled(true);
                                dialog.dismiss();
                            }
                        } else {
                            // Handle other responses (e.g., HTTP error)
                            submit.setEnabled(true);
                            dialog.dismiss();
                        }
                    }
                }

                @Override
                public void onFailure(Call<MyResponseModel> call, Throwable t) {
                    // Handle the failure
                }
            });
        }
    }



        private void login () {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.get_dura_fill_details,
                    new Response.Listener<String>() {
                        @SuppressLint("WrongConstant")
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray array = new JSONArray(response);
                                for (int i = 0; i < array.length(); i++) {
                                    object = array.getJSONObject(i);
                                    fullwt = object.getString("fullwt");
                                    emtywt = object.getString("emtywt");
                                    netwt = object.getString("netwt");
                                    cubic = object.getString("cubic");
                                    fullwttxt.setVisibility(View.VISIBLE);
                                    emptywttxt.setVisibility(View.VISIBLE);
                                    netwttxt.setVisibility(View.VISIBLE);
                                    gasType.setVisibility(View.VISIBLE);
                                    gasTypeVal.setVisibility(View.VISIBLE);
                                    cubictxt.setVisibility(View.VISIBLE);
                                    fullwtval.setVisibility(View.VISIBLE);
                                    emptywtval.setVisibility(View.VISIBLE);
                                    netwtval.setVisibility(View.VISIBLE);
                                    cubicval.setVisibility(View.VISIBLE);
                                    fullwtval.setText(fullwt);
                                    emptywtval.setText(emtywt);
                                    netwtval.setText(netwt);
                                    cubicval.setText(cubic);
                                    int TMEDOXCOUNT=0;
                                    String  TMEDOXVOLUME="";
                                    Cursor cursor = synchelper.readAllData();
                                    if (cursor.getCount() == 0) {
                                        //      empty_imageview.setVisibility(View.VISIBLE);
                                        //      no_data.setVisibility(View.VISIBLE);
                                    } else {
                                        while (cursor.moveToNext()) {
                                            String volume=cursor.getString(4);
                                            String Fillwith=cursor.getString(5);
                                            String col1 =cursor.getString(1);
                                            if(col1.contentEquals(s))
                                            {

                                                if(cursor.getString(5).equals("MEDOXDURA"))
                                                {
                                                    TMEDOXVOLUME = cursor.getString(3);

                                                    TMEDOXCOUNT = 1;
                                                }
                                                gastype =Fillwith;
                                                item.add(Fillwith);
                                                gasTypeVal.setText(Fillwith);
                                                item_volume.add(cubic);
                                                itemq.add("1");
                                            }
                                        }
                                        item.add("TRANSPORT");
                                        item_volume.add("1");
                                        itemq.add("1");
                                        if(TMEDOXCOUNT!=0)
                                        {
                                            item.add("TMEDOXDURA");
                                            item_volume.add(cubic);
                                            itemq.add(String.valueOf(TMEDOXCOUNT));
                                        }
                                    }
                                }

//                            JSONObject obj = new JSONObject(response);
//                            if (obj.getBoolean("error")) {
//                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
//                            } else {
//
//
//                                //starting the login activity
//                                startActivity(new Intent(getApplicationContext(),login.class));
//
//                            }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @SuppressLint("WrongConstant")
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            MDToast.makeText(DuraDeliveryMain.this, "कृपया इंटरनेट तपासा "+error, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                            error.printStackTrace();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("duracode", s);
                    params.put("db_host",SharedPref.mInstance.getDBHost());
                    params.put("db_username",SharedPref.mInstance.getDBUsername());
                    params.put("db_password",SharedPref.mInstance.getDBPassword());
                    params.put("db_name",SharedPref.mInstance.getDBName());
                    return params;
                }
            };
            VolleySingleton.getInstance(DuraDeliveryMain.this).addToRequestQueue(stringRequest);


        }





        @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){

        }
    }



    private void startCameraPreviewActivity(){
        //   startActivity(new Intent(this, CameraPreviewActivity.class));
    }

    /** Request permission and check */



    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Transactions.class);
        startActivity(intent);
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Destroy", "onDestroy: ");

    }

    private final BroadcastReceiver dura_no_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("dura_delivery")) {
                //Extract your data - better to use constants...
                s=intent.getStringExtra("dura_no");

                if(s!=null)
                {
                    cylinderspin.setVisibility(View.GONE);
                    autoCompleteTextView.setVisibility(View.VISIBLE);
                    autoCompleteTextView.setText(s);
                    login();
                }
            }

        }
    };

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
                    File imgFile = new File(digitalSignPath);
                    if(imgFile.exists()){
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        digital_sign = Util.getImage(myBitmap);
                        signedImg.setImageBitmap(myBitmap);
                    }
                }
            }

        }
    };

    public void showCustomMsg(String msg) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle("Instructions / सूचना");
        builder.setMessage(msg);
        // add a button
        builder.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}