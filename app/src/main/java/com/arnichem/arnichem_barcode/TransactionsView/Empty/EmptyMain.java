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
import com.arnichem.arnichem_barcode.Barcode.LaserScannerActivity;
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery.DuraDeliveryMain;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.Main;
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
import com.arnichem.arnichem_barcode.view.syncHelper;
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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;

public class EmptyMain extends AppCompatActivity implements Listener, LocationData.AddressCallBack {
    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    ArrayList<String> book_id, book_title;
    RecyclerView recyclerView, fillwithrec;
    emptyadpter emptyadpter;
    FilledWithAdapter filledWithAdapter;
    ArrayList<String> name, tot;
    ImageView empty_imageview;
    boolean status = false;
    TextView no_data, usernamevalue, totalscanval, date, vehiclevalue;
    AddClyHelper addClyHelper;
    DatabaseHandler databaseHandlercustomer;
    fromloccodehandler fromloccodehandler;
    SharedPreferences pref;
    Button button, print;
    ProgressDialog dialog;
    String digital_sign = "", digitalSignPath = "";
    Spinner spinner, customerspinnerdelivery;
    AutoCompleteTextView emptycylindernumber;
    public int poslocfixdel, poscustfixdel;
    static JSONObject object = null;
    List<String> cylinder;
    List<String> is_scan;
    syncHelper synchelper;
    APIInterface apiInterface;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    private int checks = 123;

    FloatingActionButton mAddCameraScanFab, mAddBarcodeScanFab;
    ExtendedFloatingActionButton mAddFab;
    Boolean isAllFabsVisible;

    String from_warehouse, to_warehouse, cust_code, from_code, srno, count, latitude = "0", logitude = "0",
            address = "0";

    ImageView closeImg, signedImg;
    ConstraintLayout constraintSigned;
    Button uploadSign;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        getSupportActionBar().setTitle("Empty");
        mAddFab = findViewById(R.id.add_fab);
        mAddCameraScanFab = findViewById(R.id.camera_scan);
        mAddBarcodeScanFab = findViewById(R.id.barcode_scan);
        isAllFabsVisible = false;

        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false, true, this);
        print = findViewById(R.id.emptyprintbtn);
        totalscanval = findViewById(R.id.Totalscanvalue);
        print.setVisibility(View.GONE);
        cylinder = new ArrayList<String>();
        is_scan = new ArrayList<>();
        addClyHelper = new AddClyHelper(EmptyMain.this);
        synchelper = new syncHelper(EmptyMain.this);

        spinner = findViewById(R.id.spinfromemp);
        emptycylindernumber = findViewById(R.id.emptycylindersea);
        customerspinnerdelivery = findViewById(R.id.custnamespinemp);
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);
        closeImg = findViewById(R.id.closeImg);
        signedImg = findViewById(R.id.signedImg);

        // New RecyclerView init
        fillwithrec = findViewById(R.id.fillwithrec);
        name = new ArrayList<>();
        tot = new ArrayList<>();

        poslocfixdel = Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel = Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        databaseHandlercustomer = new DatabaseHandler(EmptyMain.this);
        fromloccodehandler = new fromloccodehandler(EmptyMain.this);
        loadata();
        fetchData();
        loadSpinnerData();
        vehiclevalue = findViewById(R.id.vno);
        usernamevalue = findViewById(R.id.usernametxtvalue);
        button = findViewById(R.id.EmptyMainPost);
        button.setEnabled(true);
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        vehiclevalue.setText(SharedPref.getInstance(this).getVehicleNo());
        date = findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        recyclerView = findViewById(R.id.recyclerView);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                from_warehouse = dataAdapter.getItem(position);
                poslocfixdel = position;
                SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(poslocfixdel));
                Cursor cursor = fromloccodehandler.readAllData();
                if (cursor.getCount() > 0) {
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
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        if (col.contentEquals(to_warehouse)) {
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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                postUsingRetrofit();
            }
        });

        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        storeDataInArrays();
        emptyadpter = new emptyadpter(EmptyMain.this, EmptyMain.this, book_id, book_title, filled_with_list);
        totalscanval.setText(count);
        recyclerView.setAdapter(emptyadpter);
        recyclerView.setLayoutManager(new LinearLayoutManager(EmptyMain.this));

        // New Logic
        check();
        filledWithAdapter = new FilledWithAdapter(EmptyMain.this, EmptyMain.this, name, tot);
        fillwithrec.setAdapter(filledWithAdapter);
        fillwithrec.setLayoutManager(new LinearLayoutManager(EmptyMain.this));

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmptyMain.this, Empty_Print.class);
                intent.putExtra("durano", String.valueOf(cylinder));
                intent.putExtra("custname", to_warehouse);
                intent.putExtra("empb", srno);
                intent.putExtra("count", count);
                intent.putExtra("custcode", cust_code);
                intent.putExtra("cylinder", String.valueOf(cylinder));
                intent.putExtra("filled_with", String.valueOf(filled_with_list));
                startActivity(intent);
            }
        });

        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmptyMain.this, ActivityDigitalSignature.class);
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
                        Intent intent = new Intent(EmptyMain.this, LaserScannerActivity.class);
                        intent.putExtra("type", "empty");
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
                        Intent intent = new Intent(EmptyMain.this, NewScanner.class);
                        intent.putExtra("type", "empty");
                        startActivity(intent);
                    }
                });

    }

    @Override
    protected void onResume() {
        if (status) {
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

    private void checkdual(String cust_code, View view) {
        dialog = new ProgressDialog(EmptyMain.this);
        dialog.setTitle("Loading");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.check_dual_delivery,
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
                                if (count.equals("0")) {

                                } else {
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
                params.put("type", "EMP");
                params.put("cust_code", cust_code);
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username", SharedPref.mInstance.getDBUsername());
                params.put("db_password", SharedPref.mInstance.getDBPassword());
                params.put("db_name", SharedPref.mInstance.getDBName());
                return params;
            }

        };
        VolleySingleton.getInstance(EmptyMain.this).addToRequestQueue(stringRequest);
    }

    public void showAlertDialogDualDelivery(View view) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle("Alert!");
        builder.setMessage(
                "काय तुह्मी खरचं या कस्टमर ला एम्पटी सिलेंडर घेणार आहात का ? कारण आज या कस्टमर ला एकदा एम्पटी सिलेंडर घेतले आहेत.");
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

    private void loadSpinnerData() {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        customerdataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        customerdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // data adapter to spinner
        customerspinnerdelivery.setAdapter(customerdataAdapter);
        if (poscustfixdel != 0) {
            customerspinnerdelivery.setSelection(poscustfixdel);
        }

    }

    private void loadata() {
        List<ItemCode> itemCodes = new ArrayList<>();
        SearchAdapter searchAdapter = new SearchAdapter(getApplicationContext(), itemCodes);
        emptycylindernumber.setThreshold(1);
        emptycylindernumber.setAdapter(searchAdapter);
        emptycylindernumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCyl = emptycylindernumber.getText().toString();
                String filledWith = "";
                String volume = "";
                Cursor cursor = synchelper.readAllData();
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        if (cursor.getString(1).equals(selectedCyl)) { // Column 1 is item_code (cylinder number)
                            filledWith = cursor.getString(5); // Column 5 is filled_with
                            volume = cursor.getString(4); // Column 4 is volume
                            break;
                        }
                    }
                    cursor.close();
                }
                addClyHelper.addBook(selectedCyl, filledWith, volume, "N");
                finish();
                startActivity(getIntent());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            // recreate();
        }
        if (requestCode == checks) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Toast.makeText(EmptyMain.this, "GPS On", Toast.LENGTH_SHORT).show();
                    break;

                case Activity.RESULT_CANCELED:
                    Toast.makeText(EmptyMain.this, "GPS OFF", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }

    ArrayList<String> filled_with_list;

    void storeDataInArrays() {
        filled_with_list = new ArrayList<>();
        Cursor cursor = addClyHelper.readAllData();
        if (cursor.getCount() == 0) {
            // empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                book_id.add(cursor.getString(0));
                book_title.add(cursor.getString(1));
                cylinder.add(cursor.getString(1));
                is_scan.add(cursor.getString(4));
                String fw = cursor.getString(5);
                if (fw == null)
                    fw = "";
                filled_with_list.add(fw);
                // book_author.add(cursor.getString(2));
                // book_pages.add(cursor.getString(3));
            }
            int cou = cursor.getCount();
            count = String.valueOf(cou);
            // empty_imageview.setVisibility(View.GONE);
            no_data.setVisibility(View.GONE);
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
            // String commaseparatedlist = str.toString();

            ArrayList<String> itemList = new ArrayList<>();
            ArrayList<String> itemQList = new ArrayList<>();
            ArrayList<String> quantityVolumeList = new ArrayList<>();

            Cursor cursor = addClyHelper.readcount();
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    // SUM(PAGES) at 1, COUNT(FILLED) at 2, FILLED at 3, PAGES at 4
                    quantityVolumeList.add(cursor.getString(1));
                    itemQList.add(cursor.getString(2));
                    itemList.add(cursor.getString(3));
                }
            }

            StringBuilder itemStr = new StringBuilder();
            StringBuilder itemQStr = new StringBuilder();
            StringBuilder volStr = new StringBuilder();

            for (String s : itemList)
                itemStr.append(s).append(",");
            for (String s : itemQList)
                itemQStr.append(s).append(",");
            for (String s : quantityVolumeList)
                volStr.append(s).append(",");

            RequestBody item = RequestBody.create(itemStr.toString(), MediaType.parse("text/plain"));
            RequestBody itemq = RequestBody.create(itemQStr.toString(), MediaType.parse("text/plain"));
            RequestBody itemVolume = RequestBody.create(volStr.toString(), MediaType.parse("text/plain"));

            // Create MultipartBody.Part for image file

            // Create RequestBody for other parameters
            String joinedCyl = android.text.TextUtils.join(",", cylinder);
            String joinedIsScan = android.text.TextUtils.join(",", is_scan);

            RequestBody duraCode = RequestBody.create(joinedCyl, MediaType.parse("text/plain"));
            RequestBody isScan = RequestBody.create(joinedIsScan, MediaType.parse("text/plain"));
            RequestBody fromWarehouse = RequestBody.create(from_warehouse, MediaType.parse("text/plain"));
            RequestBody toWarehouse = RequestBody.create(to_warehouse, MediaType.parse("text/plain"));
            RequestBody transportType = RequestBody.create("ARNICHEM", MediaType.parse("text/plain"));
            RequestBody custCode = RequestBody.create(cust_code, MediaType.parse("text/plain"));
            RequestBody fromCode = RequestBody.create(from_code, MediaType.parse("text/plain"));
            RequestBody lati = RequestBody.create(latitude, MediaType.parse("text/plain"));
            RequestBody logi = RequestBody.create(logitude, MediaType.parse("text/plain"));
            RequestBody addr = RequestBody.create(address, MediaType.parse("text/plain"));
            RequestBody transportNo = RequestBody.create(SharedPref.getInstance(EmptyMain.this).getVehicleNo(),
                    MediaType.parse("text/plain"));
            RequestBody driver = RequestBody.create(SharedPref.getInstance(EmptyMain.this).getID(),
                    MediaType.parse("text/plain"));
            RequestBody email = RequestBody.create(SharedPref.getInstance(EmptyMain.this).getEmail(),
                    MediaType.parse("text/plain"));
            RequestBody countRequest = RequestBody.create(count, MediaType.parse("text/plain"));
            RequestBody dbHost = RequestBody.create(SharedPref.mInstance.getDBHost(), MediaType.parse("text/plain"));
            RequestBody dbUsername = RequestBody.create(SharedPref.mInstance.getDBUsername(),
                    MediaType.parse("text/plain"));
            RequestBody dbPassword = RequestBody.create(SharedPref.mInstance.getDBPassword(),
                    MediaType.parse("text/plain"));
            RequestBody dbName = RequestBody.create(SharedPref.mInstance.getDBName(), MediaType.parse("text/plain"));

            MultipartBody.Part signPart;
            if (digitalSignPath != null && !digitalSignPath.isEmpty()) {
                File file = new File(digitalSignPath);
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                signPart = MultipartBody.Part.createFormData("sign", file.getName(), requestFile);
            } else {
                RequestBody attachmentEmpty = RequestBody.create(MediaType.parse("text/plain"), "");
                signPart = MultipartBody.Part.createFormData("sign", "", attachmentEmpty);
            }

            // Create Retrofit service
            Call<MyResponseModel> call = apiInterface.uploadEmptyData(
                    duraCode,
                    isScan,
                    item,
                    itemq,
                    itemVolume,
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
                    signPart);

            call.enqueue(new Callback<MyResponseModel>() {

                @Override
                public void onResponse(Call<MyResponseModel> call, retrofit2.Response<MyResponseModel> response) {
                    if (response.isSuccessful()) {
                        MyResponseModel myResponseModel = response.body();
                        if (myResponseModel != null && myResponseModel.getStatus().equals("success")) {
                            // Handle success
                            MDToast.makeText(EmptyMain.this, "Empty Entry Done!", MDToast.LENGTH_SHORT,
                                    MDToast.TYPE_SUCCESS).show();
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
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

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

    void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All?");
        builder.setMessage("Are you sure you want to delete all Data?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                addClyHelper.deleteAllData();
                // Refresh Activity
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
        if (addClyHelper != null)
            addClyHelper.close();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, Transactions.class);
        startActivity(intent);
    }

    void check() {
        name.clear();
        tot.clear();
        Cursor cursor = addClyHelper.readAllData();
        Map<String, Integer> counts = new HashMap<>();

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String fw = cursor.getString(5); // filled_with column
                if (fw == null || fw.isEmpty() || fw.equals("null"))
                    fw = "Other";

                if (counts.containsKey(fw)) {
                    counts.put(fw, counts.get(fw) + 1);
                } else {
                    counts.put(fw, 1);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            name.add(entry.getKey());
            tot.add(String.valueOf(entry.getValue()));
        }
        if (filledWithAdapter != null) {
            filledWithAdapter.notifyDataSetChanged();
        }
    }

    private BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("digital_sign")) {
                // Extract your data - better to use constants...
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

}
