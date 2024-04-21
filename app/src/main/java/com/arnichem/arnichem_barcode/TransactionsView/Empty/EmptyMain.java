package com.arnichem.arnichem_barcode.TransactionsView.Empty;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
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
import android.view.MenuItem;
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
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery.DuraDeliveryMain;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliveryprint;
import com.arnichem.arnichem_barcode.attendance.MyResponseModel;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.CustomerSearchHandler;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.LocationHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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

public class EmptyMain extends AppCompatActivity implements Listener, LocationData.AddressCallBack{
    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    ProgressDialog dialog;
    ArrayList<String> book_id, book_title;
    RecyclerView recyclerView;
    FloatingActionButton add_button;
    ImageView empty_imageview;
    DatabaseHandler databaseHandlercustomer;
    fromloccodehandler fromloccodehandler;
    TextView no_data,vehiclevalue,usernamevalue,date,totalscanval;
    Spinner spinner,customerspinnerdelivery;
    String from_warehouse,to_warehouse,cust_code,from_code,srno,count,latitude="0",logitude="0",address="0",digitalSignPath = "";
    SharedPreferences pref;
    boolean status = false;
    public static final int checks=1001;
    Button button,print;
    String digital_sign = "" ;
    emptyadpter emptyadpter;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    AddClyHelper addClyHelper;
    public  int poslocfixdel,poscustfixdel;
    static JSONObject object =null;
    List<String> cylinder;

    List<String> is_scan;
    AutoCompleteTextView emptycylindernumber;
    Button uploadSign;
    ConstraintLayout constraintSigned;
    ImageView closeImg,signedImg;
    APIInterface apiInterface;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        getSupportActionBar().setTitle("Empty");
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false,true,this);
        print=findViewById(R.id.emptyprintbtn);
        totalscanval=findViewById(R.id.Totalscanvalue);
        print.setVisibility(View.GONE);
        cylinder=new ArrayList<String>();
        is_scan=new ArrayList<>();
        addClyHelper=new AddClyHelper(EmptyMain.this);
        emptyadpter=new emptyadpter(EmptyMain.this,this, book_id, book_title);
        spinner=findViewById(R.id.spinfromemp);
        emptycylindernumber=findViewById(R.id.emptycylindersea);
        customerspinnerdelivery=findViewById(R.id.custnamespinemp);
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);
        closeImg = findViewById(R.id.closeImg);
        signedImg = findViewById(R.id.signedImg);

        poslocfixdel= Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel=Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        databaseHandlercustomer=new DatabaseHandler(EmptyMain.this);
        fromloccodehandler=new fromloccodehandler(EmptyMain.this);
        loadata();
        fetchData();
        loadSpinnerData();
        vehiclevalue=findViewById(R.id.vno);
        usernamevalue=findViewById(R.id.usernametxtvalue);
        button=findViewById(R.id.EmptyMainPost);
        button.setEnabled(true);
        pref = getSharedPreferences(constant.TAG,MODE_PRIVATE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName());
        vehiclevalue.setText(SharedPref.getInstance(this).getVehicleNo());
        date=findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        recyclerView = findViewById(R.id.recyclerView);
        add_button = findViewById(R.id.emptyscan);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                from_warehouse = dataAdapter.getItem(position);
                poslocfixdel=position;
                SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(poslocfixdel));
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

        customerspinnerdelivery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                to_warehouse = customerdataAdapter.getItem(position);
                poscustfixdel=position;
                SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(poscustfixdel));
                Cursor cursor = databaseHandlercustomer.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col=cursor.getString(1);
                        String col1 =cursor.getString(2);
                        if(col.contentEquals(to_warehouse))
                        {
                            cust_code=col1;
                            checkdual(cust_code,view);
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
                //postUsingVolley();
                postUsingRetrofit();
            }
        });
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        emptyadpter = new emptyadpter(EmptyMain.this, this, book_id, book_title);
        storeDataInArrays();
        totalscanval.setText(count);
        recyclerView.setAdapter(emptyadpter);
        recyclerView.setLayoutManager(new LinearLayoutManager(EmptyMain.this));
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(EmptyMain.this, Empty_Print.class);
                intent.putExtra("durano", String.valueOf(cylinder));
                intent.putExtra("custname",to_warehouse);
                intent.putExtra("empb",srno);
                intent.putExtra("count", count);
                intent.putExtra("custcode",cust_code);
                intent.putExtra("cylinder", String.valueOf(cylinder));
                startActivity(intent);
            }
        });
        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmptyMain.this, ActivityDigitalSignature.class);
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
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status =true;
                Intent intent =new Intent(EmptyMain.this, NewScanner.class);
                intent.putExtra("type", "empty");
                startActivity(intent);
            }
        });



        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
                new IntentFilter("digital_sign"));



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
    private void checkdual(String cust_code,View view)
    {
        dialog = new ProgressDialog(EmptyMain.this);
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
                params.put("type","EMP");
                params.put("cust_code",cust_code);
                params.put("db_host",SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(EmptyMain.this).addToRequestQueue(stringRequest);
    }

    public void showAlertDialogDualDelivery(View view) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        builder.setTitle("Alert!");
        builder.setMessage("काय तुह्मी खरचं या कस्टमर ला एम्पटी सिलेंडर घेणार आहात का ? कारण आज या कस्टमर ला एकदा एम्पटी सिलेंडर घेतले आहेत.");
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
        if(poslocfixdel!=0)
        {
            spinner.setSelection(poslocfixdel);
        }
    }

    private void loadSpinnerData() {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        customerdataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        customerdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //  data adapter to spinner
        customerspinnerdelivery.setAdapter(customerdataAdapter);
        if(poscustfixdel!=0)
        {
            customerspinnerdelivery.setSelection(poscustfixdel);
        }

    }

    private  void  loadata()
    {
        List<ItemCode>  itemCodes=new ArrayList<>();
        SearchAdapter searchAdapter=new SearchAdapter(getApplicationContext(),itemCodes);
        emptycylindernumber.setThreshold(1);
        emptycylindernumber.setAdapter(searchAdapter);
        emptycylindernumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addClyHelper.addBook(emptycylindernumber.getText().toString(),"no");
                finish();
                startActivity(getIntent());
            }
        });
    }








    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
//            recreate();
        }
        if(requestCode == checks)
        {
            switch (resultCode)
            {
                case Activity.RESULT_OK:
                    Toast.makeText(EmptyMain.this, "GPS On", Toast.LENGTH_SHORT).show();
                    break;

                case Activity.RESULT_CANCELED:
                    Toast.makeText(EmptyMain.this, "GPS OFF", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }

    void storeDataInArrays(){
        Cursor cursor = addClyHelper.readAllData();
        if(cursor.getCount() == 0){
//            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        }else{
            while (cursor.moveToNext()){
                book_id.add(cursor.getString(0));
                book_title.add(cursor.getString(1));
                cylinder.add(cursor.getString(1));
                is_scan.add(cursor.getString(4));
//                book_author.add(cursor.getString(2));
//                book_pages.add(cursor.getString(3));
            }
            int cou = cursor.getCount();
            count= String.valueOf(cou);
            //         empty_imageview.setVisibility(View.GONE);
            no_data.setVisibility(View.GONE);
        }
    }
    private void postUsingVolley() {

        dialog = new ProgressDialog(EmptyMain.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poslocfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(EmptyMain.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        } else if (poscustfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(EmptyMain.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


        }  else {


            StringBuilder str = new StringBuilder("");
            for (String eachstring : cylinder) {
                str.append(eachstring).append(",");
            }


//            // StringBuffer to String conversion
            String commaseparatedlist = str.toString();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.empty_entry,
                    new Response.Listener<String>() {
                        @SuppressLint("WrongConstant")
                        @Override
                        public void onResponse(String response) {
                            try {
                                // Split the response to separate the location and the JSON array
                                String[] responseParts = response.split("\\[", 2);
                                String location = responseParts[0].trim();
                                String jsonArrayPart = "[" + responseParts[1];

                                // Handle the JSON array
                                JSONArray jsonArray = new JSONArray(jsonArrayPart);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String status = jsonObject.getString("status");
                                    String msg = jsonObject.getString("msg");

                                    if (status.equals("success")) {
                                        MDToast.makeText(EmptyMain.this, "Empty Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                        button.setVisibility(View.GONE);
                                        print.setVisibility(View.VISIBLE);
                                        srno = jsonObject.getString("srno");
                                        dialog.dismiss();
                                        Intent intent = new Intent(EmptyMain.this, Empty_Print.class);
                                        intent.putExtra("durano", String.valueOf(cylinder));
                                        intent.putExtra("custname", to_warehouse);
                                        intent.putExtra("empb", srno);
                                        intent.putExtra("sign_path", digitalSignPath);
                                        intent.putExtra("custcode", cust_code);
                                        intent.putExtra("count", count);
                                        intent.putExtra("cylinder", String.valueOf(cylinder));
                                        button.setEnabled(true);
                                        startActivity(intent);
                                    } else {
                                        button.setEnabled(true);
                                        dialog.dismiss();
                                    }

                                    Log.e("JSON", "> " + status + msg);
                                }
                            } catch (JSONException e) {
                                dialog.dismiss();
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
                            dialog.dismiss();
                            error.printStackTrace();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("dura_code", String.valueOf(cylinder));
                    params.put("is_scan", String.valueOf(is_scan));
                    params.put("from_warehouse", from_warehouse);
                    params.put("to_warehouse", to_warehouse);
                    params.put("transport_type", "ARNICHEM");
                    params.put("cust_code", cust_code);
                    params.put("from_code",from_code);
                    params.put("lati", latitude);
                    params.put("logi", logitude);
                    params.put("addr",address);
                    params.put("sign", digital_sign);
                    params.put("transport_no", SharedPref.getInstance(EmptyMain.this).getVehicleNo());
                    params.put("driver", SharedPref.getInstance(EmptyMain.this).getID());
                    params.put("email", SharedPref.getInstance(EmptyMain.this).getEmail());
                    params.put("count", count);
                    params.put("db_host",SharedPref.mInstance.getDBHost());
                    params.put("db_username",SharedPref.mInstance.getDBUsername());
                    params.put("db_password",SharedPref.mInstance.getDBPassword());
                    params.put("db_name",SharedPref.mInstance.getDBName());
                    return params;
                }
            };
            VolleySingleton.getInstance(EmptyMain.this).addToRequestQueue(stringRequest);


        }
        }

    private void postUsingRetrofit() {

        dialog = new ProgressDialog(EmptyMain.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        if (poslocfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(EmptyMain.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else if (poscustfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(EmptyMain.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else {
//            StringBuilder str = new StringBuilder();
//            for (String eachstring : cylinder) {
//                str.append(eachstring).append(",");
//            }

            MultipartBody.Part signPart = null;
            // Create a File instance from your image file path
            if(!digitalSignPath.isEmpty()){
                File file = new File(digitalSignPath); // Replace with the actual path to your image file
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                signPart = MultipartBody.Part.createFormData("sign", file.getName(), requestFile);
            }

//            String commaseparatedlist = str.toString();

            // Create MultipartBody.Part for image file

            // Create RequestBody for other parameters
            RequestBody duraCode = RequestBody.create(String.valueOf(cylinder), MediaType.parse("text/plain"));
            RequestBody isScan = RequestBody.create(String.valueOf(is_scan), MediaType.parse("text/plain"));
            RequestBody fromWarehouse = RequestBody.create(from_warehouse, MediaType.parse("text/plain"));
            RequestBody toWarehouse = RequestBody.create(to_warehouse, MediaType.parse("text/plain"));
            RequestBody transportType = RequestBody.create("ARNICHEM", MediaType.parse("text/plain"));
            RequestBody custCode = RequestBody.create(cust_code, MediaType.parse("text/plain"));
            RequestBody fromCode = RequestBody.create(from_code, MediaType.parse("text/plain"));
            RequestBody lati = RequestBody.create(latitude, MediaType.parse("text/plain"));
            RequestBody logi = RequestBody.create(logitude, MediaType.parse("text/plain"));
            RequestBody addr = RequestBody.create(address, MediaType.parse("text/plain"));
            RequestBody transportNo = RequestBody.create(SharedPref.getInstance(EmptyMain.this).getVehicleNo(), MediaType.parse("text/plain"));
            RequestBody driver = RequestBody.create(SharedPref.getInstance(EmptyMain.this).getID(), MediaType.parse("text/plain"));
            RequestBody email = RequestBody.create(SharedPref.getInstance(EmptyMain.this).getEmail(), MediaType.parse("text/plain"));
            RequestBody countRequest = RequestBody.create(count, MediaType.parse("text/plain"));
            RequestBody dbHost = RequestBody.create(SharedPref.mInstance.getDBHost(), MediaType.parse("text/plain"));
            RequestBody dbUsername = RequestBody.create(SharedPref.mInstance.getDBUsername(), MediaType.parse("text/plain"));
            RequestBody dbPassword = RequestBody.create(SharedPref.mInstance.getDBPassword(), MediaType.parse("text/plain"));
            RequestBody dbName = RequestBody.create(SharedPref.mInstance.getDBName(), MediaType.parse("text/plain"));

            // Create Retrofit service
            Call<MyResponseModel> call = apiInterface.uploadEmptyData(
                    duraCode,
                    isScan,
                    fromWarehouse,
                    toWarehouse,
                    transportType,
                    custCode,
                    fromCode,
                    lati,
                    logi,
                    addr,
                    transportNo,
                    driver,
                    email,
                    countRequest,
                    dbHost,
                    dbUsername,
                    dbPassword,
                    dbName,
                    signPart
            );

            call.enqueue(new Callback<MyResponseModel>() {

                @Override
                public void onResponse(Call<MyResponseModel> call, retrofit2.Response<MyResponseModel> response) {
                    if (response.isSuccessful()) {
                        MyResponseModel myResponseModel = response.body();
                        if (myResponseModel != null && myResponseModel.getStatus().equals("success")) {
                            // Handle success
                            MDToast.makeText(EmptyMain.this, "Empty Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                            button.setVisibility(View.GONE);
                            print.setVisibility(View.VISIBLE);
                            srno = myResponseModel.getSrno();
                            dialog.dismiss();
                            Intent intent = new Intent(EmptyMain.this, Empty_Print.class);
                            intent.putExtra("durano", String.valueOf(cylinder));
                            intent.putExtra("custname", to_warehouse);
                            intent.putExtra("empb", srno);
                            intent.putExtra("sign_path", digitalSignPath);
                            intent.putExtra("custcode", cust_code);
                            intent.putExtra("count", count);
                            intent.putExtra("cylinder", String.valueOf(cylinder));
                            button.setEnabled(true);
                            startActivity(intent);
                        } else {
                            // Handle error
                            button.setEnabled(true);
                            dialog.dismiss();
                        }
                    } else {
                        // Handle other responses (e.g., HTTP error)
                        button.setEnabled(true);
                        dialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<MyResponseModel> call, Throwable t) {
                    // Handle failure
                    button.setEnabled(true);
                    dialog.dismiss();

                }
            });
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
        if (item.getItemId() == R.id.delete_all) {
            confirmDialog();
        }
        return super.onOptionsItemSelected(item);
    }
    void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All?");
        builder.setMessage("Are you sure you want to delete all Data?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                addClyHelper.deleteAllData();
                //Refresh Activity
                finish();
                startActivity(getIntent());
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(addClyHelper != null)
            addClyHelper.close();



    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Transactions.class);
        startActivity(intent);
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

}
