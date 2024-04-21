package com.arnichem.arnichem_barcode.TransactionsView.InWard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import com.arnichem.arnichem_barcode.GPStracker.GPSTracker;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.view.ItemCode;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InWardMain extends AppCompatActivity implements Listener, LocationData.AddressCallBack{
    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    RecyclerView recyclerView;
    FloatingActionButton add_button;
    ImageView empty_imageview;
    List<String> is_scan;
    TextView no_data,vehiclevalue,usernamevalue,date,Totalscanvalue;
    InWardDatabaseHelper myDB;
    com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;
    ArrayList<String> book_id, book_title;
    InWardCustomAdapter customAdapter;
    SharedPreferences pref;
    Button button,inwardpint;
    ProgressDialog dialog;
    public String selected;
    Spinner spinner;
    boolean status = false;
    AutoCompleteTextView inwardmaincylindernumber;
    public  int poslocfixdel;
    String from_warehouse,from_code,count,srno,latitude="0",logitude="0",address="0";
    ArrayAdapter<String> dataAdapter;
    static JSONObject object =null;
    List<String> cylinder;

    private static final int REQUEST_PERMISSION = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
    };
    GPSTracker gps;
    double lati ;
    double longi;
    double lat1 =17.6746875;
    double lng1 =75.9177949;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_ward_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Inward");
        recyclerView = findViewById(R.id.recyclerView);
        this.checkPermissions();
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false,true,this);
        cylinder=new ArrayList<String>();
        is_scan=new ArrayList<>();
        add_button = findViewById(R.id.inwardscan);
        spinner=findViewById(R.id.spinfrominward);
        Totalscanvalue=findViewById(R.id.Totalscanvalue);
        inwardpint=findViewById(R.id.inwardprintbtn);
        inwardpint.setVisibility(View.GONE);
        empty_imageview = findViewById(R.id.empty_imageview);
        inwardmaincylindernumber=findViewById(R.id.inwardmaincylindernumber);
        poslocfixdel=Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        fetchData();
        loadata();
        vehiclevalue=findViewById(R.id.vno);
        usernamevalue=findViewById(R.id.usernametxtvalue);
        date=findViewById(R.id.date);
        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        pref = getSharedPreferences(constant.TAG,MODE_PRIVATE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName());
        vehiclevalue.setText(SharedPref.getInstance(this).getVehicleNo());
        button=findViewById(R.id.InWardMainPost);
        button.setEnabled(true);
        myDB = new InWardDatabaseHelper(InWardMain.this);
        fromloccodehandler=new fromloccodehandler(InWardMain.this);

        gps = new GPSTracker(getApplicationContext());

//        if(gps.canGetLocation())
//        {
//
//
//            lati = gps.getLatitude();
//            longi = gps.getLongitude();
//            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + lati + "\nLong: " + longi, Toast.LENGTH_LONG).show();
//            if (distance(lat1, lng1, lati, longi) < 0.1) {
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Toast.makeText(InWardMain.this, "True", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                // if distance < 0.1 miles we take locations as equal
//                //do what you want to do...
//            }
//        }
//        else {
//            // Can't get location.
//        }
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

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status = true;
                Intent intent = new Intent(InWardMain.this, NewScanner.class);
                intent.putExtra("type", "inward");
                startActivity(intent);
   //             startScan();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                postUsingVolley();

            }
        });
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        storeDataInArrays();
        Totalscanvalue.setText(count);
        customAdapter = new InWardCustomAdapter(com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardMain.this, this, book_id, book_title);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardMain.this));
        inwardpint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(InWardMain.this, InwardPrint.class);
                intent.putExtra("durano", String.valueOf(cylinder));
                intent.putExtra("custname",from_warehouse);
                intent.putExtra("empb",srno);
                intent.putExtra("count", count);
                intent.putExtra("cylinder", String.valueOf(cylinder));
                startActivity(intent);
            }
        });
    }
    private void checkPermissions() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // ask user for permissions
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    REQUEST_PERMISSION
            );
        }
    }
    private void fetchData() {
        fromloccodehandler db = new fromloccodehandler(getApplicationContext());
        List<String> labels = db.getAllLabels();
      dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        if(poslocfixdel!=0)
        {
            spinner.setSelection(poslocfixdel);
        }
    }
    private void postUsingVolley() {
        dialog = new ProgressDialog(InWardMain.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poslocfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(InWardMain.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        } else {
            StringBuilder str = new StringBuilder("");
            for (String eachstring : cylinder) {
                str.append(eachstring).append(",");
            }

//            // StringBuffer to String conversion
            String commaseparatedlist = str.toString();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.inward_entry,
                    new Response.Listener<String>() {
                        @SuppressLint("WrongConstant")
                        @Override
                        public void onResponse(String response) {
                            dialog.dismiss();
                            try {
                                JSONArray array = new JSONArray(response);
                                for (int i = 0; i < array.length(); i++) {
                                    object = array.getJSONObject(i);
                                    String status = object.getString("status");
                                    String msg = object.getString("msg");
                                    srno = object.getString("srno");


                                    if (status.equals("success")) {

                                        MDToast.makeText(InWardMain.this, "INWARD Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
//                                        //  SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));
                                        dialog.dismiss();

                                        button.setVisibility(View.GONE);
                                        inwardpint.setVisibility(View.VISIBLE);
                                        Intent intent=new Intent(InWardMain.this, InwardPrint.class);
                                        intent.putExtra("durano", String.valueOf(cylinder));
                                        intent.putExtra("custname",from_warehouse);
                                        intent.putExtra("empb",srno);
                                        intent.putExtra("count", count);
                                        intent.putExtra("cylinder", String.valueOf(cylinder));
                                        button.setEnabled(true);
                                        startActivity(intent);
////                                        Toast.makeText(login.this, "msg " + msg, Toast.LENGTH_SHORT).show();

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
                    params.put("from_warehouse", from_warehouse);
                    params.put("from_code",from_code);
                    params.put("transport_type", "ARNICHEM");
                    params.put("lati", latitude);
                    params.put("logi", logitude);
                    params.put("addr",address);
                    params.put("transport_no", SharedPref.getInstance(InWardMain.this).getVehicleNo());
                    params.put("driver", SharedPref.getInstance(InWardMain.this).getID());
                    params.put("email", SharedPref.getInstance(InWardMain.this).getEmail());
                    params.put("count", count);
                    params.put("db_host",SharedPref.mInstance.getDBHost());
                    params.put("db_username",SharedPref.mInstance.getDBUsername());
                    params.put("db_password",SharedPref.mInstance.getDBPassword());
                    params.put("db_name",SharedPref.mInstance.getDBName());
                    return params;
                }
            };
            VolleySingleton.getInstance(InWardMain.this).addToRequestQueue(stringRequest);


        }
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            recreate();
        }
    }
    private  void  loadata()
    {
        List<ItemCode>  itemCodes=new ArrayList<>();
        SearchAdapter searchAdapter=new SearchAdapter(getApplicationContext(),itemCodes);
        inwardmaincylindernumber.setThreshold(1);
        inwardmaincylindernumber.setAdapter(searchAdapter);
        inwardmaincylindernumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myDB.addBook(inwardmaincylindernumber.getText().toString(),"no");
                finish();
                startActivity(getIntent());
            }
        });
    }
    void storeDataInArrays() {
        Cursor cursor = myDB.readAllData();
        if (cursor.getCount() == 0) {

        } else {
            while (cursor.moveToNext()) {
                book_id.add(cursor.getString(0));
                book_title.add(cursor.getString(1));
                cylinder.add(cursor.getString(1));
                is_scan.add(cursor.getString(4));

            }
            int cou = cursor.getCount();
            count= String.valueOf(cou);

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



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myDB != null)
            myDB.close();


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Transactions.class);
        startActivity(intent);
    }


}
