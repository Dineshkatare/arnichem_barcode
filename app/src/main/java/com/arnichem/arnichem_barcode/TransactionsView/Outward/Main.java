package com.arnichem.arnichem_barcode.TransactionsView.Outward;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.Barcode.LaserScannerActivity;
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.OnItemClickListener;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardMain;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.LocationHandler;
import com.arnichem.arnichem_barcode.view.VehicleHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends AppCompatActivity implements Listener, LocationData.AddressCallBack, OnItemClickListener {
    static JSONObject object = null;
    public int poslocfixdel;
    GetLocationDetail getLocationDetail;
    ArrayList<String> cylIdList, cyclinderNameList, fillwith;
    RecyclerView recyclerView, Filled_with_Recycle_View;
   // FloatingActionButton add_button;
    ImageView empty_imageview;
    TextView no_data, vehiclevalue, usernamevalue, Totalscanvalue, date;
    MyDatabaseHelper myDB;
    boolean status = false;
    ArrayList<String> name, tot, volume;
    CustomAdapter customAdapter;
    FilledWithAdapter filledWithAdapter;
    Spinner spinneroutwatloc;
    SharedPreferences pref;
    Button button, outprintbutton;
    ProgressDialog dialog;
    AutoCompleteTextView outwardcylindernumber;
    String to_warehouse, to_code, count, srno, latitude = "0", logitude = "0", address = "0";
    ArrayAdapter<String> dataAdapter;
    com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;
    List<String> cylinder;
    List<String> is_scan;
    syncHelper synchelper;
    private EasyWayLocation easyWayLocation;
    Boolean isAllFabsVisible;

    FloatingActionButton mAddCameraScanFab, mAddBarcodeScanFab;

    // Use the ExtendedFloatingActionButton to handle the
    // parent FAB
    ExtendedFloatingActionButton mAddFab;

    public  int pos;
    Spinner spinner;
    ArrayAdapter<String> dataAdapterVehicle;

    public String selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Outward");
        mAddFab = findViewById(R.id.add_fab);
        // FAB button
        mAddCameraScanFab = findViewById(R.id.camera_scan);
        mAddBarcodeScanFab =
                findViewById(R.id.barcode_scan);
        isAllFabsVisible = false;
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false, true, this);
        cylinder = new ArrayList<String>();
        is_scan = new ArrayList<>();
        spinneroutwatloc = findViewById(R.id.spinoutwardloc);
        poslocfixdel = Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        fetchData();
        outwardcylindernumber = findViewById(R.id.outwardcylindernumber);
        synchelper = new syncHelper(Main.this);
        loadata();
        vehiclevalue = findViewById(R.id.vno);
        usernamevalue = findViewById(R.id.usernametxtvalue);
        button = findViewById(R.id.OutwardMainPost);
        spinner=findViewById(R.id.dynamic_spinner);
        fetchDataVehicle();

        button.setEnabled(true);
        Totalscanvalue = findViewById(R.id.Totalscanvalue);
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        vehiclevalue.setText(SharedPref.getInstance(this).getVehicleNo());
        date = findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        recyclerView = findViewById(R.id.recyclerView);
        Filled_with_Recycle_View = findViewById(R.id.fillwithrec);
        //add_button = findViewById(R.id.add_fab);
        outprintbutton = findViewById(R.id.outwardprintbtn);
        outprintbutton.setVisibility(View.GONE);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);
        outprintbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, OutwardPrint.class);
                intent.putExtra("durano", String.valueOf(cylinder));
                intent.putExtra("custname", to_warehouse);
                intent.putExtra("empb", srno);
                intent.putExtra("count", count);
                intent.putExtra("cylinder", String.valueOf(cylinder));
                startActivity(intent);
            }
        });
//

        myDB = new MyDatabaseHelper(Main.this);
        fromloccodehandler = new fromloccodehandler(Main.this);
        cylIdList = new ArrayList<>();
        cyclinderNameList = new ArrayList<>();
        fillwith = new ArrayList<>();
        name = new ArrayList<>();
        tot = new ArrayList<>();
        volume = new ArrayList<>();
        storeDataInArrays();
        Totalscanvalue.setText(count);
        check();
        customAdapter = new CustomAdapter(Main.this, this, cylIdList, cyclinderNameList, fillwith,this,"outward");
        filledWithAdapter = new FilledWithAdapter(Main.this, this, name, tot);
        Filled_with_Recycle_View.setAdapter(filledWithAdapter);
        Filled_with_Recycle_View.setLayoutManager(new LinearLayoutManager(Main.this));
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(Main.this));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                postUsingVolley();

            }
        });
        spinneroutwatloc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                to_warehouse = dataAdapter.getItem(position);
                poslocfixdel = position;
                SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(poslocfixdel));
                Cursor cursor = fromloccodehandler.readAllData();
                if (cursor.getCount() == 0) {

                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        if (col.contentEquals(to_warehouse)) {
                            to_code = col1;

                        }
                    }
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
                selected = dataAdapterVehicle.getItem(position);
                pos=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

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
                        Intent intent = new Intent(Main.this, LaserScannerActivity.class);
                        intent.putExtra("type", "outward");
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
                        Intent intent = new Intent(Main.this, NewScanner.class);
                        intent.putExtra("type", "outward");
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
    private void fetchDataVehicle() {
        VehicleHandler db = new VehicleHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();
        dataAdapterVehicle= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);
        // Creating adapter for spinner
        // Drop down layout style - list view with radio button
        dataAdapterVehicle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapterVehicle);
    }



    private void fetchData() {
        fromloccodehandler db = new fromloccodehandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinneroutwatloc.setAdapter(dataAdapter);
        if (poslocfixdel != 0) {
            spinneroutwatloc.setSelection(poslocfixdel);
        }
    }

    private void postUsingVolley() {
        dialog = new ProgressDialog(Main.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poslocfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(Main.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        } if(pos==0) {
            dialog.dismiss();
            MDToast.makeText(Main.this, "कृपया वाहन क्रमांक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else {
            StringBuilder str = new StringBuilder();
            for (String eachstring : cylinder) {
                str.append(eachstring).append(",");
            }

            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.outward_entry,
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
                                        MDToast.makeText(Main.this, "OUTWARD Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                        button.setVisibility(View.GONE);
                                        outprintbutton.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                        Intent intent = new Intent(Main.this, OutwardPrint.class);
                                        intent.putExtra("durano", String.valueOf(cylinder));
                                        intent.putExtra("custname", to_warehouse);
                                        intent.putExtra("empb", srno);
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
                                button.setEnabled(true);
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @SuppressLint("WrongConstant")
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            button.setEnabled(true);
                            error.printStackTrace();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("dura_code", String.valueOf(cylinder));
                    params.put("to_warehouse", to_warehouse);
                    params.put("transport_type", "ARNICHEM");
                    params.put("is_scan",String.valueOf(is_scan));
                    params.put("to_code", to_code);
                    params.put("lati", latitude);
                    params.put("logi", logitude);
                    params.put("addr", address);
                    params.put("transport_no", selected);
                    params.put("driver", SharedPref.getInstance(Main.this).getID());
                    params.put("email", SharedPref.getInstance(Main.this).getEmail());
                    params.put("count", count);
                    params.put("db_host",SharedPref.mInstance.getDBHost());
                    params.put("db_username",SharedPref.mInstance.getDBUsername());
                    params.put("db_password",SharedPref.mInstance.getDBPassword());
                    params.put("db_name",SharedPref.mInstance.getDBName());
                    return params;
                }
            };
            VolleySingleton.getInstance(Main.this).addToRequestQueue(stringRequest);
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }

    }

    private void loadata() {
        List<ItemCode> itemCodes = new ArrayList<>();
        SearchAdapter searchAdapter = new SearchAdapter(getApplicationContext(), itemCodes);
        outwardcylindernumber.setThreshold(1);
        outwardcylindernumber.setAdapter(searchAdapter);
        outwardcylindernumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                } else {
                    while (cursor.moveToNext()) {
                        String volume = cursor.getString(4);
                        String Fillwith = cursor.getString(5);
                        String col1 = cursor.getString(1);
                        if (col1.contentEquals(outwardcylindernumber.getText().toString())) {
                            name.add(Fillwith);
                            myDB.addBook(outwardcylindernumber.getText().toString(), Fillwith, volume,"N");
                            finish();
                            startActivity(getIntent());
                        }
                    }
                }
            }
        });
    }

    void storeDataInArrays() {
        Cursor cursor = myDB.readAllData();
        if (cursor.getCount() == 0) {
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                cylIdList.add(cursor.getString(0));
                cyclinderNameList.add(cursor.getString(1));
                fillwith.add(cursor.getString(2));
                cylinder.add(cursor.getString(1));
                is_scan.add(cursor.getString(4));
            }
            int cou = cursor.getCount();
            count = String.valueOf(cou);
            no_data.setVisibility(View.GONE);
        }
    }


    private void startCameraPreviewActivity() {
        //   startActivity(new Intent(this, CameraPreviewActivity.class));
    }

    /**
     * Request permission and check
     */
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

    void confirmDialog() {
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
        super.onBackPressed();
        Intent intent = new Intent(this, Transactions.class);
        startActivity(intent);
    }

    void check() {

        Cursor cursor = myDB.readcount();
        if (cursor.getCount() == 0) {
        } else {
            while (cursor.moveToNext()) {
                name.add(cursor.getString(3));
                volume.add(cursor.getString(4));
                tot.add(cursor.getString(2));

            }
        }
    }


    @Override
    public void onItemClick(int position) {

    }
}
