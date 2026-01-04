package com.arnichem.arnichem_barcode.TransactionsView.deliverynew;

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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
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
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.TransactionsView.Duraempty.duraemptymain;
import com.arnichem.arnichem_barcode.TransactionsView.Duraempty.duraemptyprint;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CustomAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.Main;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.attendance.MyResponseModel;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;


public class Maindelivery extends AppCompatActivity implements Listener, LocationData.AddressCallBack , OnItemClickListener {
    static JSONObject object = null;
    public int poslocfixdel, poscustfixdel;
    ProgressDialog dialog;
    ArrayList<String> name, tot, volume;
    fromloccodehandler fromloccodehandler;

    ImageView empty_imageview, signedImg;
    DatabaseHandler databaseHandlercustomer;
    TextView no_data, vehiclevalue, usernamevalue, date, totalscanval;
    Spinner spinner, customerspinnerdelivery;
    String from_warehouse, to_warehouse, cust_code, srno, count, from_code, latitude = "0", logitude = "0", address = "0";
    SharedPreferences pref;
    Button button, delprint;
   // deliAdapter deliadapter;
    ArrayAdapter<String> dataAdapter;
    ArrayList<String> book_id, book_title;
    ArrayList<String> cylIdList, cyclinderNameList, fillwith;
    CustomAdapter customAdapter;


    RecyclerView recyclerView, Filled_with_Recycle_View;
    FilledWithAdapter filledWithAdapter;
    ArrayAdapter<String> customerdataAdapter;
    deliDB delidb;
    List<String> cylinder;
    List<String> is_scan;
    List<String> item;
    List<String> itemq;
    List<String> item_volume;
    syncHelper synchelper;
    AutoCompleteTextView deliverycylindersea;
    Boolean checkInvoice = false;
    boolean status = false;
    GetLocationDetail getLocationDetail;
    APIInterface apiInterface;
    Button uploadSign;
    ConstraintLayout constraintSigned;
    ImageView closeImg;
    String digital_sign = "", digitalSignPath = "";
    private EasyWayLocation easyWayLocation;

    FloatingActionButton mAddCameraScanFab, mAddBarcodeScanFab;

    // Use the ExtendedFloatingActionButton to handle the
    // parent FAB
    ExtendedFloatingActionButton mAddFab;

    Boolean isAllFabsVisible;

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
        setContentView(R.layout.activity_maindelivery);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPref.getInstance(Maindelivery.this).setDoubleEntry("false");
        getSupportActionBar().setTitle("Delivery");
        mAddFab = findViewById(R.id.add_fab);
        // FAB button
        mAddCameraScanFab = findViewById(R.id.camera_scan);
        mAddBarcodeScanFab =
                findViewById(R.id.barcode_scan);
        isAllFabsVisible = false;

        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false, true, this);
        delidb = new deliDB(Maindelivery.this);
        cylinder = new ArrayList<String>();
        cylIdList = new ArrayList<>();
        cyclinderNameList = new ArrayList<>();
        fillwith = new ArrayList<>();
        is_scan = new ArrayList<>();
        apiInterface = APIClient.getClient().create(APIInterface.class);

      //  deliadapter = new deliAdapter(Maindelivery.this, this, book_id, book_title);
        customAdapter = new CustomAdapter(Maindelivery.this, this, cylIdList, cyclinderNameList, fillwith,this,"delivery");

        filledWithAdapter = new FilledWithAdapter(Maindelivery.this, this, name, tot);
        spinner = findViewById(R.id.deliveryloc);
        deliverycylindersea = findViewById(R.id.deliverycylindersea);
        totalscanval = findViewById(R.id.totalscanval);
        customerspinnerdelivery = findViewById(R.id.cutomerdelivery);
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);

        poslocfixdel = Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel = Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        databaseHandlercustomer = new DatabaseHandler(Maindelivery.this);
        fromloccodehandler = new fromloccodehandler(Maindelivery.this);
        synchelper = new syncHelper(Maindelivery.this);
        signedImg = findViewById(R.id.signedImg);
        loadata();
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
        empty_imageview = findViewById(R.id.empty_imageview);
        closeImg = findViewById(R.id.closeImg);
        no_data = findViewById(R.id.no_data);
        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Maindelivery.this, ActivityDigitalSignature.class);
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
                            if (cursor.getString(4) != null && !cursor.getString(4).isEmpty()) {
                                String message = cursor.getString(4).replace("\\n", "\n"); // Replace literal "\n" with a newline
                                showCustomMsg(message);
                                Log.d("chech",""+message);
                            }

                        }
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        delprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Maindelivery.this, deliveryprint.class);
                intent.putExtra("durano", String.valueOf(cylinder));
                intent.putExtra("custname", to_warehouse);
                intent.putExtra("empb", srno);
                intent.putExtra("count", count);
                intent.putExtra("custcode", cust_code);
                intent.putExtra("cylinder", String.valueOf(cylinder));
                startActivity(intent);
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
        customAdapter = new CustomAdapter(Maindelivery.this, this, cylIdList, cyclinderNameList, fillwith,this,"delivery");
        name = new ArrayList<>();
        tot = new ArrayList<>();
        volume = new ArrayList<>();
        item = new ArrayList<>();
        itemq = new ArrayList<>();
        item_volume = new ArrayList<>();
        storeDataInArrays();
        check();
        totalscanval.setText(count);
        filledWithAdapter = new FilledWithAdapter(Maindelivery.this, this, name, tot);
        Filled_with_Recycle_View.setAdapter(filledWithAdapter);
        Filled_with_Recycle_View.setLayoutManager(new LinearLayoutManager(Maindelivery.this));
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(Maindelivery.this));
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
                        Intent intent = new Intent(Maindelivery.this, LaserScannerActivity.class);
                        intent.putExtra("type", "delivery");
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
                        Intent intent = new Intent(Maindelivery.this, NewScanner.class);
                        intent.putExtra("type", "delivery");
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
                delidb.deleteAllData();
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

    void storeDataInArrays() {
        Cursor cursor = delidb.readAllData();
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


    private void checkdual(String cust_code, View view) {
        dialog = new ProgressDialog(Maindelivery.this);
        dialog.setTitle("Loading");
        dialog.setMessage("Please wait....");
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
        VolleySingleton.getInstance(Maindelivery.this).addToRequestQueue(stringRequest);
    }

    private void postUsingRetrofit() {
        dialog = new ProgressDialog(Maindelivery.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        if (poslocfixdel == 0) {
            button.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(Maindelivery.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else if (poscustfixdel == 0) {
            button.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(Maindelivery.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else {
            StringBuilder str = new StringBuilder();
            for (String eachstring : cylinder) {
                str.append(eachstring).append(",");
            }
            MultipartBody.Part signPart = null;
            // Create a File instance from your image file path
            if(!digitalSignPath.isEmpty()){
                File file = new File(digitalSignPath); // Replace with the actual path to your image file
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                 signPart = MultipartBody.Part.createFormData("sign", file.getName(), requestFile);
            }

            RequestBody dura_code = RequestBody.create(
                    String.valueOf(cylinder),
                    MediaType.parse("text/plain")
            );
            RequestBody item_request = RequestBody.create(
                    String.valueOf(item),
                    MediaType.parse("text/plain")
            );
            RequestBody itemq_request = RequestBody.create(
                    String.valueOf(itemq),
                    MediaType.parse("text/plain")
            );
            RequestBody item_volume_request = RequestBody.create(
                    String.valueOf(item_volume),
                    MediaType.parse("text/plain")
            );
            RequestBody from_warehouse_request = RequestBody.create(
                    String.valueOf(from_warehouse),
                    MediaType.parse("text/plain")
            );
            RequestBody to_warehouse_request = RequestBody.create(
                    String.valueOf(to_warehouse),
                    MediaType.parse("text/plain")
            );
            RequestBody transport_type = RequestBody.create(
                    "ARNICHEM",
                    MediaType.parse("text/plain")
            );
            RequestBody cust_code_request = RequestBody.create(
                    cust_code,
                    MediaType.parse("text/plain")
            );

            RequestBody from_code_request = RequestBody.create(
                    from_code,
                    MediaType.parse("text/plain")
            );
            RequestBody lati = RequestBody.create(
                    latitude,
                    MediaType.parse("text/plain")
            );
            RequestBody logi = RequestBody.create(
                    logitude,
                    MediaType.parse("text/plain")
            );
            RequestBody addr = RequestBody.create(
                    address,
                    MediaType.parse("text/plain")
            );
            RequestBody is_scan_request = RequestBody.create(
                    String.valueOf(is_scan),
                    MediaType.parse("text/plain")
            );
            RequestBody transport_no = RequestBody.create(
                    SharedPref.getInstance(Maindelivery.this).getVehicleNo(),
                    MediaType.parse("text/plain")
            );

            RequestBody driver = RequestBody.create(
                    SharedPref.getInstance(Maindelivery.this).getID(),
                    MediaType.parse("text/plain")
            );

            RequestBody email = RequestBody.create(
                    SharedPref.getInstance(Maindelivery.this).getEmail(),
                    MediaType.parse("text/plain")
            );
            RequestBody count_request = RequestBody.create(
                    count,
                    MediaType.parse("text/plain")
            );
            RequestBody db_host = RequestBody.create(
                    SharedPref.mInstance.getDBHost(),
                    MediaType.parse("text/plain")
            );
            RequestBody db_username = RequestBody.create(
                    SharedPref.mInstance.getDBUsername(),
                    MediaType.parse("text/plain")
            );
            RequestBody db_password = RequestBody.create(
                    SharedPref.mInstance.getDBPassword(),
                    MediaType.parse("text/plain")
            );
            RequestBody db_name = RequestBody.create(
                    SharedPref.mInstance.getDBName(),
                    MediaType.parse("text/plain")
            );

            // Create Retrofit service
            Call<MyResponseModel> call = apiInterface.uploadDeliveryData(
                    dura_code,
                    item_request,
                    itemq_request,
                    item_volume_request,
                    from_warehouse_request,
                    to_warehouse_request,
                    transport_type,
                    cust_code_request,
                    from_code_request,
                    lati,
                    logi,
                    addr,
                    is_scan_request,
                    transport_no,
                    driver,
                    email,
                    count_request,
                    db_host,
                    db_username,
                    db_password,
                    db_name,
                    signPart
            );

            call.enqueue(new Callback<MyResponseModel>() {
                @Override
                public void onResponse(retrofit2.Call<MyResponseModel> call, retrofit2.Response<MyResponseModel> response) {
                    if (response.isSuccessful()) {
                        MyResponseModel myResponseModel = response.body();
                        if (myResponseModel != null && myResponseModel.getStatus().equals("success")) {
                            // Handle success
                            MDToast.makeText(Maindelivery.this, "Delivery Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                            MDToast.makeText(Maindelivery.this, "आता प्रिंट बटण दाबा!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                            button.setVisibility(View.GONE);
                            delprint.setVisibility(View.VISIBLE);
                            srno = myResponseModel.getSrno();

                            dialog.dismiss();
                            Intent intent = new Intent(Maindelivery.this, deliveryprint.class);
                            intent.putExtra("durano", String.valueOf(cylinder));
                            intent.putExtra("custname", to_warehouse);
                            intent.putExtra("custcode", cust_code);
                            intent.putExtra("empb", srno);
                            intent.putExtra("count", count);
                            intent.putExtra("sign_path", digitalSignPath);
                            button.setEnabled(true);
                            intent.putExtra("cylinder", String.valueOf(cylinder));
                            startActivity(intent);
                        } else {
                            // Handle error
                            button.setEnabled(true);
                            dialog.dismiss();
                        }
                    } else {
                        MDToast.makeText(
                                Maindelivery.this,
                                "Error in API call: "+response.errorBody(),
                                MDToast.LENGTH_SHORT,
                                MDToast.TYPE_ERROR
                        ).show();
                        Log.e("error", String.valueOf(response.errorBody()));
                        // Handle other responses (e.g., HTTP error)
                        button.setEnabled(true);
                        dialog.dismiss();
                    }
                }
                
                @Override
                public void onFailure(Call<MyResponseModel> call, Throwable t) {
                    MDToast.makeText(
                            Maindelivery.this,
                    "Error in API call: "+t.getMessage(),
                            MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR
                ).show();
                    Log.e("error",t.getMessage());
                    button.setEnabled(true);
                    dialog.dismiss();
                    // Handle failure here
                }
            });
        }
    }

    private void loadata() {
        List<ItemCode> itemCodes = new ArrayList<>();
        SearchAdapter searchAdapter = new SearchAdapter(getApplicationContext(), itemCodes);
        deliverycylindersea.setThreshold(1);
        deliverycylindersea.setAdapter(searchAdapter);
        deliverycylindersea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                processBarcode(deliverycylindersea.getText().toString());
            }
        });

        // Fix: Add OnKeyListener to bypass dropdown delay
        deliverycylindersea.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    
                    String code = deliverycylindersea.getText().toString().trim();
                    if(!code.isEmpty()){
                         processBarcode(code);
                         deliverycylindersea.dismissDropDown(); // Hide dropdown if visible
                         deliverycylindersea.setText(""); // Clear for next scan
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void processBarcode(String barcode) {
        Cursor cursor = synchelper.readAllData();
        if (cursor.getCount() == 0) {
            // No data
        } else {
            while (cursor.moveToNext()) {
                String volume = cursor.getString(4);
                String Fillwith = cursor.getString(5);
                String col1 = cursor.getString(1);
                        
                if (col1.contentEquals(barcode)) {
                    delidb.addBook(barcode, Fillwith, volume, "N");
                    finish();
                    startActivity(getIntent());
                    return; // Found and added
                }
            }
        }
    }

    void check() {
        int TMEDOXCOUNT = 0;
        String TMEDOXVOLUME = "";
        Cursor cursor = delidb.readcount();
        if (cursor.getCount() == 0) {
        } else {
            while (cursor.moveToNext()) {
                name.add(cursor.getString(3));
                volume.add(cursor.getString(4));
                tot.add(cursor.getString(2));
                if (cursor.getString(3).equals("MEDOX7")) {
                    TMEDOXVOLUME = cursor.getString(4);

                    TMEDOXCOUNT = Integer.parseInt(cursor.getString(2));
                }

                item.add(cursor.getString(3));
                item_volume.add(cursor.getString(4));
                itemq.add(cursor.getString(2));
            }
            item.add("TRANSPORT");
            item_volume.add("1");
            itemq.add("1");
            if (TMEDOXCOUNT != 0) {
                item.add("TMEDOX7");
                item_volume.add(TMEDOXVOLUME);
                itemq.add(String.valueOf(TMEDOXCOUNT));
            }


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
        super.onBackPressed();
        Intent intent = new Intent(this, Transactions.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(int position) {

    }
}
