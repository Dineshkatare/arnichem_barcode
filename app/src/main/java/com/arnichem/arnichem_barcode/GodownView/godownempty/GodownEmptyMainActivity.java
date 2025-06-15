package com.arnichem.arnichem_barcode.GodownView.godownempty;

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
import com.arnichem.arnichem_barcode.Barcode.LaserScannerActivity;
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.GodownView.GOdownMainActivity;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryMainActivity;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery.DuraDeliveryMain;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.EmptyMain;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.AddActivity;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.LocationHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

public class GodownEmptyMainActivity  extends AppCompatActivity implements Listener, LocationData.AddressCallBack{
    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    Button uploadSign;
    ConstraintLayout constraintSigned;
    ImageView closeImg,signedImg;
    ArrayList<String> book_id, book_title;
    RecyclerView recyclerView;
    ImageView empty_imageview;
    boolean status = false;
    TextView no_data,usernamevalue,Totalscanvalue,date;
    GodownEmptyHelper myDB;
    GodownEmptyAdapter customAdapter;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    DatabaseHandler databaseHandlercustomer;
    fromloccodehandler fromloccodehandler;
    SharedPreferences pref;
    Button button,print;
    ProgressDialog dialog;
    String digital_sign = "",digitalSignPath="" ;
    Spinner spinnerloc,spinnercust;
    AutoCompleteTextView godownemptycylindernumber;
    public  int poslocfixdel,poscustfixdel;
    static JSONObject object =null;
    List<String> cylinder;
    List<String> is_scan;

    FloatingActionButton mAddCameraScanFab, mAddBarcodeScanFab;

    // Use the ExtendedFloatingActionButton to handle the
    // parent FAB
    ExtendedFloatingActionButton mAddFab;
    Boolean isAllFabsVisible;


    String from_warehouse,to_warehouse,cust_code,from_code,srno,count,latitude="0",logitude="0",address="0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_godown_empty_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Godown Empty");
        mAddFab = findViewById(R.id.add_fab);
        // FAB button
        mAddCameraScanFab = findViewById(R.id.camera_scan);
        mAddBarcodeScanFab =
                findViewById(R.id.barcode_scan);
        isAllFabsVisible = false;
        is_scan = new ArrayList<>();

        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false,true,this);
        spinnerloc=findViewById(R.id.spinlocgodown);
        spinnercust=findViewById(R.id.custnamespingodown);
        Totalscanvalue=findViewById(R.id.Totalscanvalue);
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);
        closeImg = findViewById(R.id.closeImg);
        signedImg = findViewById(R.id.signedImg);

        godownemptycylindernumber=findViewById(R.id.godownemptycylindernumber);
        print=findViewById(R.id.Godownprintbtn);
        print.setVisibility(View.GONE);
        cylinder=new ArrayList<String>();
        poslocfixdel= Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel=Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        databaseHandlercustomer=new DatabaseHandler(GodownEmptyMainActivity.this);
        fromloccodehandler=new fromloccodehandler(GodownEmptyMainActivity.this);
        fetchData();
        loadSpinnerData();
        loadata();
        usernamevalue=findViewById(R.id.usernametxtvalue);
        button=findViewById(R.id.GodownEmptyMainPost);
        button.setEnabled(true);
        pref = getSharedPreferences(constant.TAG,MODE_PRIVATE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName());
        date=findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        recyclerView = findViewById(R.id.recyclerView);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);

        myDB = new GodownEmptyHelper(GodownEmptyMainActivity.this);
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        storeDataInArrays();
        Totalscanvalue.setText(count);
        spinnerloc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
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

        spinnercust.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                to_warehouse = customerdataAdapter.getItem(position);
                poscustfixdel=position;
                SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(poscustfixdel));
                Cursor cursor = databaseHandlercustomer.readAllData();
                if (cursor.getCount() == 0) {
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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                postUsingVolley();
            }
        });

        customAdapter = new GodownEmptyAdapter(GodownEmptyMainActivity.this,this, book_id, book_title);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(GodownEmptyMainActivity.this));
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GodownEmptyMainActivity.this, GOdownEmptyprint.class);
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
                Intent intent = new Intent(GodownEmptyMainActivity.this, ActivityDigitalSignature.class);
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


        mAddCameraScanFab.setVisibility(View.GONE);
        mAddBarcodeScanFab.setVisibility(View.GONE);

        mAddFab.shrink();

        // We will make all the FABs and action name texts
        // visible only when Parent FAB button is clicked So
        // we have to handle the Parent FAB button first, by
        // using setOnClickListener you can see below
        mAddFab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isAllFabsVisible) {

                            // when isAllFabsVisible becomes
                            // true make all the action name
                            // texts and FABs VISIBLE.
                            mAddBarcodeScanFab.show();
                            mAddCameraScanFab.show();

                            mAddFab.extend();

                            // make the boolean variable true as
                            // we have set the sub FABs
                            // visibility to GONE
                            isAllFabsVisible = true;
                        } else {

                            // when isAllFabsVisible becomes
                            // true make all the action name
                            // texts and FABs GONE.
                            mAddBarcodeScanFab.hide();
                            mAddCameraScanFab.hide();

                            // Set the FAB to shrink after user
                            // closes all the sub FABs
                            mAddFab.shrink();

                            // make the boolean variable false
                            // as we have set the sub FABs
                            // visibility to GONE
                            isAllFabsVisible = false;
                        }
                    }
                });

        // below is the sample action to handle add person
        // FAB. Here it shows simple Toast msg. The Toast
        // will be shown only when they are visible and only
        // when user clicks on them
        mAddBarcodeScanFab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        status = true;
                        Intent intent = new Intent(GodownEmptyMainActivity.this, LaserScannerActivity.class);
                        intent.putExtra("type", "godown_empty");
                        startActivity(intent);
                    }
                });

        // below is the sample action to handle add alarm
        // FAB. Here it shows simple Toast msg The Toast
        // will be shown only when they are visible and only
        // when user clicks on them
        mAddCameraScanFab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        status = true;
                        Intent intent = new Intent(GodownEmptyMainActivity.this, NewScanner.class);
                        intent.putExtra("type", "godown_empty");
                        startActivity(intent);
                    }
                });





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
        spinnerloc.setAdapter(dataAdapter);
        if(poslocfixdel!=0)
        {
            spinnerloc.setSelection(poslocfixdel);
        }
    }
    private void loadSpinnerData() {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        customerdataAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        customerdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnercust.setAdapter(customerdataAdapter);
        if(poscustfixdel!=0)
        {
            spinnercust.setSelection(poscustfixdel);
        }
    }
    private  void  loadata()
    {
        List<ItemCode>  itemCodes=new ArrayList<>();
        SearchAdapter searchAdapter=new SearchAdapter(getApplicationContext(),itemCodes);
        godownemptycylindernumber.setThreshold(1);
        godownemptycylindernumber.setAdapter(searchAdapter);
        godownemptycylindernumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myDB.addBook(godownemptycylindernumber.getText().toString(),"N");
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
    }

    void storeDataInArrays(){
        Cursor cursor = myDB.readAllData();
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
                myDB.deleteAllData();
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
    private void postUsingVolley() {

        dialog = new ProgressDialog(GodownEmptyMainActivity.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poslocfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(GodownEmptyMainActivity.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        } else if (poscustfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(GodownEmptyMainActivity.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


        }  else {
            StringBuilder str = new StringBuilder("");
            for (String eachstring : cylinder) {
                str.append(eachstring).append(",");
            }

            // StringBuffer to String conversion
            String commaseparatedlist = str.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.godown_empty_entry,
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
                                        MDToast.makeText(GodownEmptyMainActivity.this, "Empty Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                        MDToast.makeText(GodownEmptyMainActivity.this,  "आता प्रिंट बटण दाबा !", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                                        button.setVisibility(View.GONE);
                                        print.setVisibility(View.VISIBLE);
                                        srno = object.getString("srno");

                                        //  SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));
                                        dialog.dismiss();
                                        Intent intent=new Intent(GodownEmptyMainActivity.this, GOdownEmptyprint.class);
                                        intent.putExtra("durano", String.valueOf(cylinder));
                                        intent.putExtra("custname",to_warehouse);
                                        intent.putExtra("empb",srno);
                                        intent.putExtra("count", count);
                                        intent.putExtra("sign_path",digitalSignPath);
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
                                button.setEnabled(true);

                                dialog.dismiss();
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
                    params.put("transport_type", "OWN");
                    params.put("cust_code", cust_code);
                    params.put("from_code",from_code);
                    params.put("lati", latitude);
                    params.put("sign", digital_sign);
                    params.put("logi", logitude);
                    params.put("addr",address);
                    params.put("transport_no", SharedPref.getInstance(GodownEmptyMainActivity.this).getVehicleNo());
                    params.put("driver", SharedPref.getInstance(GodownEmptyMainActivity.this).getID());
                    params.put("email", SharedPref.getInstance(GodownEmptyMainActivity.this).getEmail());
                    params.put("count", count);
                    params.put("db_host",SharedPref.mInstance.getDBHost());
                    params.put("db_username",SharedPref.mInstance.getDBUsername());
                    params.put("db_password",SharedPref.mInstance.getDBPassword());
                    params.put("db_name",SharedPref.mInstance.getDBName());

                    return params;
                }
            };
            VolleySingleton.getInstance(GodownEmptyMainActivity.this).addToRequestQueue(stringRequest);


        }
        }



    private void startCameraPreviewActivity(){
        //   startActivity(new Intent(this, CameraPreviewActivity.class));
    }

    /** Request permission and check */


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myDB != null)
            myDB.close();


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, GOdownMainActivity.class);
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




}