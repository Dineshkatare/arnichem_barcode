package com.arnichem.arnichem_barcode.TransactionsView.FullRecipt;

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
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryMainActivity;
import com.arnichem.arnichem_barcode.OnItemClickListener;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.Duraempty.duraemptymain;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.EmptyMain;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.AddActivity;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CustomAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CylinderNamePrintAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliveryprint;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
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

public class FullReciptMainActivity extends AppCompatActivity
        implements Listener, LocationData.AddressCallBack, OnItemClickListener {
    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    ProgressDialog dialog;
    ArrayList<String> book_id, book_title, fillwith, volume;
    RecyclerView recyclerView, fillwithrec;
    FilledWithAdapter filledWithAdapter;
    ArrayList<String> name, tot;
    FloatingActionButton add_button;
    ImageView empty_imageview;
    boolean status = false;
    DatabaseHandler databaseHandlercustomer;
    com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;
    TextView no_data, vehiclevalue, usernamevalue, date, Totalscanvalue;
    Spinner spinner, customerspinnerdelivery;
    String from_warehouse, to_warehouse, cust_code, from_code, srno, count, latitude = "0", logitude = "0",
            address = "0", digitalSignPath = "";
    SharedPreferences pref;
    Button button, print;
    CustomAdapter customAdapter;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    FullReciptHelper addClyHelper;
    public int poslocfixdel, poscustfixdel;
    static JSONObject object = null;
    List<String> cylinder;
    List<String> is_scan;
    AutoCompleteTextView emptycylindernumber;
    Button uploadSign;
    syncHelper synchelper;

    ConstraintLayout constraintSigned;
    ImageView closeImg, signedImg;
    String digital_sign = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_recipt_main2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Full Receipt");
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false, true, this);
        print = findViewById(R.id.emptyprintbtn);
        print.setVisibility(View.GONE);
        cylinder = new ArrayList<String>();
        is_scan = new ArrayList<>();
        addClyHelper = new FullReciptHelper(FullReciptMainActivity.this);
        spinner = findViewById(R.id.spinfromemp);
        emptycylindernumber = findViewById(R.id.emptycylindersea);
        customerspinnerdelivery = findViewById(R.id.custnamespinemp);
        poslocfixdel = Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel = Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        databaseHandlercustomer = new DatabaseHandler(FullReciptMainActivity.this);
        fromloccodehandler = new fromloccodehandler(FullReciptMainActivity.this);
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);
        closeImg = findViewById(R.id.closeImg);
        signedImg = findViewById(R.id.signedImg);
        synchelper = new syncHelper(FullReciptMainActivity.this);

        loadata();
        fetchData();
        loadSpinnerData();
        vehiclevalue = findViewById(R.id.vno);
        usernamevalue = findViewById(R.id.usernametxtvalue);
        Totalscanvalue = findViewById(R.id.Totalscanvalue);
        button = findViewById(R.id.EmptyMainPost);
        button.setEnabled(true);
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        vehiclevalue.setText(SharedPref.getInstance(this).getVehicleNo());
        date = findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        recyclerView = findViewById(R.id.recyclerView);
        fillwithrec = findViewById(R.id.fillwithrec);
        name = new ArrayList<>();
        tot = new ArrayList<>();

        empty_imageview = findViewById(R.id.empty_imageview);
        add_button = findViewById(R.id.emptyscan);

        no_data = findViewById(R.id.no_data);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                from_warehouse = dataAdapter.getItem(position);
                poslocfixdel = position;
                SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(poslocfixdel));
                Cursor cursor = fromloccodehandler.readAllData();
                if (cursor.getCount() == 0) {
                    // empty_imageview.setVisibility(View.VISIBLE);
                    // no_data.setVisibility(View.VISIBLE);
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
                    // empty_imageview.setVisibility(View.VISIBLE);
                    // no_data.setVisibility(View.VISIBLE);
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
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FullReciptMainActivity.this, AddActivity.class);
                startActivity(intent);
                // startScan();
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
        fillwith = new ArrayList<>();
        volume = new ArrayList<>();
        customAdapter = new CustomAdapter(FullReciptMainActivity.this, this, book_id, book_title, fillwith, this,
                "full_receipt");

        // emptyadpter = new FullReciptAdapter(FullReciptMainActivity.this, this,
        // book_id, book_title);
        storeDataInArrays();
        Totalscanvalue.setText(count);
        customAdapter = new CustomAdapter(FullReciptMainActivity.this, this, book_id, book_title, fillwith, this,
                "full_receipt");
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(FullReciptMainActivity.this));

        check();
        filledWithAdapter = new FilledWithAdapter(FullReciptMainActivity.this, this, name, tot);
        fillwithrec.setAdapter(filledWithAdapter);
        fillwithrec.setLayoutManager(new LinearLayoutManager(FullReciptMainActivity.this));
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FullReciptMainActivity.this, FullRecPrint.class);
                intent.putExtra("durano", String.valueOf(cylinder));
                intent.putExtra("custname", to_warehouse);
                intent.putExtra("empb", srno);
                intent.putExtra("count", count);
                intent.putExtra("custcode", cust_code);
                intent.putExtra("cylinder", String.valueOf(cylinder));
                startActivity(intent);
            }
        });

        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FullReciptMainActivity.this, ActivityDigitalSignature.class);
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
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status = true;
                Intent intent = new Intent(FullReciptMainActivity.this, NewScanner.class);
                intent.putExtra("type", "full_receipt");
                startActivity(intent);

            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
                new IntentFilter("digital_sign"));

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

                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                    // empty_imageview.setVisibility(View.VISIBLE);
                    // no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String volume = cursor.getString(4);
                        String Fillwith = cursor.getString(5);
                        String col1 = cursor.getString(1);
                        if (col1.contentEquals(emptycylindernumber.getText().toString())) {
                            addClyHelper.addBook(emptycylindernumber.getText().toString(), Fillwith, volume, "no");
                            finish();
                            startActivity(getIntent());
                        }
                    }
                }
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
    }

    void storeDataInArrays() {
        Cursor cursor = addClyHelper.readAllData();
        if (cursor.getCount() == 0) {
            // empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                book_id.add(cursor.getString(0));
                book_title.add(cursor.getString(1));
                fillwith.add(cursor.getString(2));
                volume.add(cursor.getString(3)); // Ensure index 3 is correct for volume. In Helper: 0=id, 1=title,
                                                 // 2=author(fill), 3=pages(volume)
                cylinder.add(cursor.getString(1));
                is_scan.add(cursor.getString(4));
                // book_author.add(cursor.getString(2));
                // book_pages.add(cursor.getString(3));
            }
            int cou = cursor.getCount();
            count = String.valueOf(cou);
            // empty_imageview.setVisibility(View.GONE);
            no_data.setVisibility(View.GONE);
        }
    }

    private void postUsingVolley() {
        dialog = new ProgressDialog(FullReciptMainActivity.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poslocfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(FullReciptMainActivity.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT,
                    MDToast.TYPE_ERROR).show();

        } else if (poscustfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(FullReciptMainActivity.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT,
                    MDToast.TYPE_ERROR).show();

        } else {
            // Group items by Gas Type (fillwith) and Volume
            Map<String, Integer> groupedMap = new HashMap<>();

            for (int i = 0; i < cylinder.size(); i++) {
                String gasType = fillwith.get(i);
                if (gasType == null || gasType.isEmpty())
                    gasType = "Unknown";

                String vol = volume.get(i);
                if (vol == null || vol.isEmpty())
                    vol = "0";

                String key = gasType + "###" + vol;

                if (groupedMap.containsKey(key)) {
                    groupedMap.put(key, groupedMap.get(key) + 1);
                } else {
                    groupedMap.put(key, 1);
                }
            }

            ArrayList<String> itemList = new ArrayList<>();
            ArrayList<String> itemQList = new ArrayList<>();
            ArrayList<String> quantityVolumeList = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : groupedMap.entrySet()) {
                String[] parts = entry.getKey().split("###");
                itemList.add(parts[0]);
                quantityVolumeList.add(parts[1]);
                itemQList.add(String.valueOf(entry.getValue()));
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

            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.full_recipt_entry,
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

                                    if (status.equals("success")) {
                                        MDToast.makeText(FullReciptMainActivity.this, "FullRecipt Entry Done!",
                                                MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                                        button.setVisibility(View.GONE);
                                        print.setVisibility(View.VISIBLE);
                                        srno = object.getString("srno");
                                        //
                                        // //
                                        // SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));
                                        dialog.dismiss();
                                        Intent intent = new Intent(FullReciptMainActivity.this, FullRecPrint.class);
                                        intent.putExtra("durano", String.valueOf(cylinder));
                                        intent.putExtra("custname", to_warehouse);
                                        intent.putExtra("empb", srno);
                                        intent.putExtra("count", count);
                                        intent.putExtra("sign_path", digitalSignPath);
                                        intent.putExtra("custcode", cust_code);
                                        intent.putExtra("cylinder", String.valueOf(cylinder));
                                        button.setEnabled(true);
                                        intent.setFlags(
                                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                        startActivity(intent);
                                        //// Toast.makeText(login.this, "msg " + msg, Toast.LENGTH_SHORT).show();

                                    } else {
                                        dialog.dismiss();
                                        button.setEnabled(true);

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
                            dialog.dismiss();
                            button.setEnabled(true);

                            error.printStackTrace();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("dura_code", android.text.TextUtils.join(",", cylinder));
                    params.put("is_scan", android.text.TextUtils.join(",", is_scan));
                    params.put("from_warehouse", from_warehouse);
                    params.put("to_warehouse", to_warehouse);
                    params.put("transport_type", "ARNICHEM");
                    params.put("cust_code", cust_code);
                    params.put("from_code", from_code);
                    params.put("lati", latitude);
                    params.put("logi", logitude);
                    params.put("addr", address);
                    params.put("sign", digital_sign);
                    params.put("transport_no", SharedPref.getInstance(FullReciptMainActivity.this).getVehicleNo());
                    params.put("driver", SharedPref.getInstance(FullReciptMainActivity.this).getID());
                    params.put("email", SharedPref.getInstance(FullReciptMainActivity.this).getEmail());
                    params.put("count", count);

                    // Added grouped parameters
                    params.put("item", itemStr.toString());
                    params.put("itemq", itemQStr.toString());
                    params.put("item_volume", volStr.toString());

                    params.put("db_host", SharedPref.mInstance.getDBHost());
                    params.put("db_username", SharedPref.mInstance.getDBUsername());
                    params.put("db_password", SharedPref.mInstance.getDBPassword());
                    params.put("db_name", SharedPref.mInstance.getDBName());
                    return params;
                }
            };
            VolleySingleton.getInstance(FullReciptMainActivity.this).addToRequestQueue(stringRequest);

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
                String fw = cursor.getString(2); // filled_with column (book_author)
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

    @Override
    public void onItemClick(int position) {

    }
}