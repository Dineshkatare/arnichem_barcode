package com.arnichem.arnichem_barcode.TransactionsView.Duraempty;

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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery.DuraDeliveryMain;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
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

public class duraemptymain extends AppCompatActivity implements Listener, LocationData.AddressCallBack{
    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail; ProgressDialog dialog;
    ArrayList<String> book_id, book_title;
    RecyclerView recyclerView;
    FloatingActionButton add_button;
    ImageView empty_imageview;
    DatabaseHandler databaseHandlercustomer;
    com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;
    TextView no_data,vehiclevalue,usernamevalue,date,totalscanval;
    Spinner spinner,customerspinnerdelivery,cylinderspin;
    String from_warehouse,to_warehouse,cust_code,from_code,srno,count,selectcyli,latitude="0",logitude="0",address="0";
    SharedPreferences pref;
    Button button,print;
    boolean status = false;
    DuraemptyAdapter emptyadpter;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    DuraemptyHelper addClyHelper;
    public  int poslocfixdel,poscustfixdel;
    ArrayAdapter<String> cylinderAdapter;
    static JSONObject object =null;
    List<String> cylinder;
    Button uploadSign;
    ConstraintLayout constraintSigned;
    ImageView closeImg,signedImg;
    String digital_sign = "",digitalSignPath = "" ;
    APIInterface apiInterface;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duraemptymain);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Dura Empty");
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false,true,this);
        print=findViewById(R.id.emptyprintbtn);
        totalscanval=findViewById(R.id.Totalscanvalue);
        print.setVisibility(View.GONE);
        cylinder=new ArrayList<String>();
        addClyHelper=new DuraemptyHelper(duraemptymain.this);
        emptyadpter=new DuraemptyAdapter(duraemptymain.this,this, book_id, book_title);
        spinner=findViewById(R.id.spinfromemp);
        cylinderspin=findViewById(R.id.spinnercylinderempty);
        customerspinnerdelivery=findViewById(R.id.custnamespinemp);
        poslocfixdel= Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel=Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        databaseHandlercustomer=new DatabaseHandler(duraemptymain.this);
        fromloccodehandler=new fromloccodehandler(duraemptymain.this);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        fetchData();
        fetchserachcylinder();
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
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);
        closeImg = findViewById(R.id.closeImg);
        signedImg = findViewById(R.id.signedImg);



        cylinderspin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectcyli = cylinderAdapter.getItem(position);
             if(position!=0)
             {
                 addClyHelper.addBook(selectcyli);
                 finish();
                 startActivity(getIntent());

             }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                Intent intent =new Intent(duraemptymain.this, NewScanner.class);
                intent.putExtra("type", "dura_empty");
                startActivity(intent);
                //          startScan();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
               // postUsingVolley();
                postUsingRetrofit();
            }
        });
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        emptyadpter = new DuraemptyAdapter(duraemptymain.this, this, book_id, book_title);
        storeDataInArrays();
        totalscanval.setText(count);
        recyclerView.setAdapter(emptyadpter);
        recyclerView.setLayoutManager(new LinearLayoutManager(duraemptymain.this));
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(duraemptymain.this, duraemptyprint.class);
                intent.putExtra("durano", String.valueOf(cylinder));
                intent.putExtra("custname",to_warehouse);
                intent.putExtra("empb",srno);
                intent.putExtra("custcode",cust_code);
                intent.putExtra("count", count);
                intent.putExtra("cylinder", String.valueOf(cylinder));
                startActivity(intent);
            }
        });
        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(duraemptymain.this, ActivityDigitalSignature.class);
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











    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
//            recreate();
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
//                book_author.add(cursor.getString(2));
//                book_pages.add(cursor.getString(3));
            }
            int cou = cursor.getCount();
            count= String.valueOf(cou);
            //         empty_imageview.setVisibility(View.GONE);
            no_data.setVisibility(View.GONE);
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
    private void postUsingVolley() {

        dialog = new ProgressDialog(duraemptymain.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poslocfixdel == 0) {
            button.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(duraemptymain.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        } else if (poscustfixdel == 0) {
            button.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(duraemptymain.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


        }  else {
            StringBuilder str = new StringBuilder("");
            for (String eachstring : cylinder) {
                str.append(eachstring).append(",");
            }

            // StringBuffer to String conversion
            String commaseparatedlist = str.toString();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.dura_empty_entry,
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

                                        MDToast.makeText(duraemptymain.this, "Empty Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                        button.setVisibility(View.GONE);
                                        print.setVisibility(View.VISIBLE);
                                        srno = object.getString("srno");
//                                        //  SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));
                                        dialog.dismiss();
                                        Intent intent=new Intent(duraemptymain.this, duraemptyprint.class);
                                        intent.putExtra("durano", String.valueOf(cylinder));
                                        intent.putExtra("custname",to_warehouse);
                                        intent.putExtra("empb",srno);
                                        intent.putExtra("sign_path",digitalSignPath);
                                        intent.putExtra("count", count);
                                        intent.putExtra("custcode",cust_code);
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
                    params.put("from_warehouse", from_warehouse);
                    params.put("to_warehouse", to_warehouse);
                    params.put("transport_type", "ARNICHEM");
                    params.put("cust_code", cust_code);
                    params.put("lati", latitude);
                    params.put("logi", logitude);
                    params.put("addr",address);
                    params.put("sign", digital_sign);
                    params.put("from_code",from_code);
                    params.put("transport_no", SharedPref.getInstance(duraemptymain.this).getVehicleNo());
                    params.put("driver", SharedPref.getInstance(duraemptymain.this).getID());
                    params.put("email", SharedPref.getInstance(duraemptymain.this).getEmail());
                    params.put("count", count);
                    params.put("db_host",SharedPref.mInstance.getDBHost());
                    params.put("db_username",SharedPref.mInstance.getDBUsername());
                    params.put("db_password",SharedPref.mInstance.getDBPassword());
                    params.put("db_name",SharedPref.mInstance.getDBName());
                    return params;
                }
            };
            VolleySingleton.getInstance(duraemptymain.this).addToRequestQueue(stringRequest);


        }
    }


    private void postUsingRetrofit() {
        dialog = new ProgressDialog(duraemptymain.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        if (poslocfixdel == 0) {
            button.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(duraemptymain.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else if (poscustfixdel == 0) {
            button.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(duraemptymain.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else {
//            StringBuilder str = new StringBuilder();
//            for (String eachstring : cylinder) {
//                str.append(eachstring).append(",");
//            }

            // Create a Retrofit service

            // Create RequestBody instances for each parameter
            RequestBody duraCode = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(cylinder));
            RequestBody fromWarehouse = RequestBody.create(MediaType.parse("text/plain"), from_warehouse);
            RequestBody toWarehouse = RequestBody.create(MediaType.parse("text/plain"), to_warehouse);
            RequestBody transportType = RequestBody.create(MediaType.parse("text/plain"), "ARNICHEM");
            RequestBody custCode = RequestBody.create(MediaType.parse("text/plain"), cust_code);
            RequestBody latitude_request = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(latitude));
            RequestBody longitude = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(logitude));
            RequestBody address_request = RequestBody.create(MediaType.parse("text/plain"), address);
            RequestBody digitalSign = RequestBody.create(MediaType.parse("text/plain"), digital_sign);
            RequestBody fromCode = RequestBody.create(MediaType.parse("text/plain"), from_code);
            RequestBody transportNo = RequestBody.create(MediaType.parse("text/plain"), SharedPref.getInstance(duraemptymain.this).getVehicleNo());
            RequestBody driver = RequestBody.create(MediaType.parse("text/plain"), SharedPref.getInstance(duraemptymain.this).getID());
            RequestBody email = RequestBody.create(MediaType.parse("text/plain"), SharedPref.getInstance(duraemptymain.this).getEmail());
            RequestBody count_request = RequestBody.create(MediaType.parse("text/plain"), count);
            RequestBody dbHost = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBHost());
            RequestBody dbUsername = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBUsername());
            RequestBody dbPassword = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBPassword());
            RequestBody dbName = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBName());
            MultipartBody.Part signPart = null;
            // Create a File instance from your image file path
            if(!digitalSignPath.isEmpty()){
                File file = new File(digitalSignPath); // Replace with the actual path to your image file
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                signPart = MultipartBody.Part.createFormData("sign", file.getName(), requestFile);
            }

            // Make the API call
            Call<MyResponseModel> call = apiInterface.
                    uploadEmptyEntry(
                    duraCode, fromWarehouse, toWarehouse, transportType, custCode, latitude_request, longitude,
                    address_request, digitalSign, fromCode, transportNo, driver, email, count_request, dbHost, dbUsername,
                    dbPassword, dbName,signPart
            );

            call.enqueue(new Callback<MyResponseModel>() {

                @Override
                public void onResponse(Call<MyResponseModel> call, retrofit2.Response<MyResponseModel> response) {
                    if (response.isSuccessful()) {
                        MyResponseModel myResponseModel = response.body();
                        if (myResponseModel != null && myResponseModel.getStatus().equals("success")) {
                            // Handle success
                            MDToast.makeText(duraemptymain.this, "Empty Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                            button.setVisibility(View.GONE);
                            print.setVisibility(View.VISIBLE);
                            srno = myResponseModel.getSrno();
//                                        //  SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));
                            dialog.dismiss();
                            Intent intent=new Intent(duraemptymain.this, duraemptyprint.class);
                            intent.putExtra("durano", String.valueOf(cylinder));
                            intent.putExtra("custname",to_warehouse);
                            intent.putExtra("empb",srno);
                            intent.putExtra("sign_path",digitalSignPath);
                            intent.putExtra("count", count);
                            intent.putExtra("custcode",cust_code);
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