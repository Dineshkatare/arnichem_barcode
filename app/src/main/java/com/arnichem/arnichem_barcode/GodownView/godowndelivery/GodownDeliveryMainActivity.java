package com.arnichem.arnichem_barcode.GodownView.godowndelivery;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import com.arnichem.arnichem_barcode.GodownView.Closing_stock.Closing_stock;
import com.arnichem.arnichem_barcode.GodownView.GOdownMainActivity;
import com.arnichem.arnichem_barcode.OnItemClickListener;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery.DuraDeliveryMain;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.AddActivity;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CustomAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;
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


public class GodownDeliveryMainActivity extends AppCompatActivity implements Listener, LocationData.AddressCallBack  , OnItemClickListener {
    static JSONObject object = null;
    public int poslocfixdel, poscustfixdel;
    GetLocationDetail getLocationDetail;
    ArrayList<String> name, tot, volume;
    ArrayList<String> book_id, book_title,fillwith;
    fromloccodehandler fromloccodehandler;
    DatabaseHandler databaseHandlercustomer;
    RecyclerView recyclerView, Filled_with_Recycle_View;
    FilledWithAdapter filledWithAdapter;
    ImageView empty_imageview;
    TextView no_data, vehiclevalue, usernamevalue, Totalscanvalue, date;
    String from_warehouse, to_warehouse, cust_code, srno, count, from_code, latitude = "0", logitude = "0", address = "0";
    GodownDeliveryHelper myDB;
    CustomAdapter customAdapter;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    Spinner spinner, spinnercust;
    SharedPreferences pref;
    List<String> is_scan;

    Button button, cyadd, godowndelprint;
    ProgressDialog dialog;
    AutoCompleteTextView godowndelcylindernumber;
    syncHelper synchelper;
    List<String> cylinder;
    List<String> item;
    List<String> itemq;
    List<String> item_volume;
    Boolean checkInvoice = false;
    private EasyWayLocation easyWayLocation;
    Button uploadSign;
    ConstraintLayout constraintSigned;
    ImageView closeImg,signedImg;
    String digital_sign = "",digitalSignPath = "";
    boolean status = false;


    FloatingActionButton mAddCameraScanFab, mAddBarcodeScanFab;

    // Use the ExtendedFloatingActionButton to handle the
    // parent FAB
    ExtendedFloatingActionButton mAddFab;

    Boolean isAllFabsVisible;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_godown_delivery_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Godown Delivery");
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false, true, this);
        spinner = findViewById(R.id.spinfromgodel);
        godowndelcylindernumber = findViewById(R.id.godowndelcylindernumber);
        cylinder = new ArrayList<String>();
        is_scan = new ArrayList<>();

        spinnercust = findViewById(R.id.custnamespingodowndel);
        cyadd = findViewById(R.id.addcylindergodowndel);
        godowndelprint = findViewById(R.id.Godowndelprintbtn);
        Totalscanvalue = findViewById(R.id.Totalscanvalue);
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);
        closeImg = findViewById(R.id.closeImg);
        signedImg = findViewById(R.id.signedImg);

        godowndelprint.setVisibility(View.GONE);
        poslocfixdel = Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel = Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        synchelper = new syncHelper(GodownDeliveryMainActivity.this);
        databaseHandlercustomer = new DatabaseHandler(GodownDeliveryMainActivity.this);
        fromloccodehandler = new fromloccodehandler(GodownDeliveryMainActivity.this);
        fetchData();
        loadSpinnerData();
        vehiclevalue = findViewById(R.id.vno);
        usernamevalue = findViewById(R.id.usernametxtvalue);
        button = findViewById(R.id.GodownDelMainPost);
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        date = findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        recyclerView = findViewById(R.id.recyclerView);
        Filled_with_Recycle_View = findViewById(R.id.fillwithrec);
        mAddFab = findViewById(R.id.add_fab);
        // FAB button
        mAddCameraScanFab = findViewById(R.id.camera_scan);
        mAddBarcodeScanFab =
                findViewById(R.id.barcode_scan);
        isAllFabsVisible = false;

        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);

        button.setEnabled(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                postUsingVolley();
            }
        });


        myDB = new GodownDeliveryHelper(GodownDeliveryMainActivity.this);
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        name = new ArrayList<>();
        tot = new ArrayList<>();
        volume = new ArrayList<>();
        item = new ArrayList<>();
        itemq = new ArrayList<>();
        item_volume = new ArrayList<>();
        fillwith = new ArrayList<>();

        loadata();
        storeDataInArrays();
        check();
        Totalscanvalue.setText(count);
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

        spinnercust.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                                showAlertDialogButtonClicked(view);
                            }
                            cust_code = col1;

                            if (cursor.getString(4) != null && !cursor.getString(4).isEmpty()) {
                                String message = cursor.getString(4).replace("\\n", "\n"); // Replace literal "\n" with a newline
                                showCustomMsg( message);
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
//        cyadd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                myDB.addBook(godowndelcylindernumber.getText().toString());
//                finish();
//                startActivity(getIntent());
//
//            }
//        });

        godowndelprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GodownDeliveryMainActivity.this, GodownDelPrint.class);
                intent.putExtra("durano", String.valueOf(cylinder));
                intent.putExtra("custname", to_warehouse);
                intent.putExtra("empb", srno);
                intent.putExtra("custcode", cust_code);
                intent.putExtra("count", count);
                intent.putExtra("cylinder", String.valueOf(cylinder));
                startActivity(intent);
            }
        });
        filledWithAdapter = new FilledWithAdapter(GodownDeliveryMainActivity.this, this, name, tot);
        Filled_with_Recycle_View.setAdapter(filledWithAdapter);
        Filled_with_Recycle_View.setLayoutManager(new LinearLayoutManager(GodownDeliveryMainActivity.this));
        customAdapter = new CustomAdapter(GodownDeliveryMainActivity.this, this, book_id, book_title, fillwith,this,"godown_delivery");

       // customAdapter = new GodownDeliveryAdapter(GodownDeliveryMainActivity.this, this, book_id, book_title);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(GodownDeliveryMainActivity.this));

        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GodownDeliveryMainActivity.this, ActivityDigitalSignature.class);
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
                        Intent intent = new Intent(GodownDeliveryMainActivity.this, LaserScannerActivity.class);
                        intent.putExtra("type", "godown_delivery");
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
                        Intent intent = new Intent(GodownDeliveryMainActivity.this, NewScanner.class);
                        intent.putExtra("type", "godown_delivery");
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
        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        if (poslocfixdel != 0) {
            spinner.setSelection(poslocfixdel);
        }
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
    }

    private void loadSpinnerData() {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        customerdataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        customerdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnercust.setAdapter(customerdataAdapter);
        if (poscustfixdel != 0) {
            spinnercust.setSelection(poscustfixdel);
        }
    }

    private void loadata() {
        List<ItemCode> itemCodes = new ArrayList<>();
        SearchAdapter searchAdapter = new SearchAdapter(getApplicationContext(), itemCodes);
        godowndelcylindernumber.setThreshold(1);
        godowndelcylindernumber.setAdapter(searchAdapter);
        godowndelcylindernumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String volume = cursor.getString(4);
                        String Fillwith = cursor.getString(5);
                        String col1 = cursor.getString(1);
                        if (col1.contentEquals(godowndelcylindernumber.getText().toString())) {
                            myDB.addBook(godowndelcylindernumber.getText().toString(), Fillwith, volume,"N");
                            finish();
                            startActivity(getIntent());
                        }
                    }
                }
            }
        });
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


    void storeDataInArrays() {
        Cursor cursor = myDB.readAllData();
        if (cursor.getCount() == 0) {
//            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                book_id.add(cursor.getString(0));
                book_title.add(cursor.getString(1));
                fillwith.add(cursor.getString(2));
                cylinder.add(cursor.getString(1));
                is_scan.add(cursor.getString(3));


            }
            int cou = cursor.getCount();
            count = String.valueOf(cou);
            //         empty_imageview.setVisibility(View.GONE);
            no_data.setVisibility(View.GONE);
        }
    }

    void check() {
        int TMEDOXCOUNT = 0;
        String TMEDOXVOLUME = "";
        Cursor cursor = myDB.readcount();
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


    private void postUsingVolley() {
        dialog = new ProgressDialog(GodownDeliveryMainActivity.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poslocfixdel == 0) {
            button.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(GodownDeliveryMainActivity.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        } else if (poscustfixdel == 0) {
            button.setEnabled(true);

            dialog.dismiss();
            MDToast.makeText(GodownDeliveryMainActivity.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


        } else {
            StringBuilder str = new StringBuilder("");
            for (String eachstring : cylinder) {
                str.append(eachstring).append(",");
            }

            String commaseparatedlist = str.toString();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.godown_delivery_entry,
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
                                        MDToast.makeText(GodownDeliveryMainActivity.this, "Delivey Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                        MDToast.makeText(GodownDeliveryMainActivity.this, "आता प्रिंट बटण दाबा !", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                                        button.setVisibility(View.GONE);
                                        godowndelprint.setVisibility(View.VISIBLE);
                                        srno = object.getString("srno");

                                        //  SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));
                                        dialog.dismiss();
                                        Intent intent = new Intent(GodownDeliveryMainActivity.this, GodownDelPrint.class);
                                        intent.putExtra("durano", String.valueOf(cylinder));
                                        intent.putExtra("custname", to_warehouse);
                                        intent.putExtra("custcode", cust_code);
                                        intent.putExtra("empb", srno);
                                        intent.putExtra("sign_path",digitalSignPath);
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
                            dialog.dismiss();
                            button.setEnabled(true);

                            error.printStackTrace();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("dura_code", String.valueOf(cylinder));
                    params.put("item", String.valueOf(item));
                    params.put("itemq", String.valueOf(itemq));
                    params.put("item_volume", String.valueOf(item_volume));
                    params.put("from_warehouse", from_warehouse);
                    params.put("to_warehouse", to_warehouse);
                    params.put("transport_type", "OWN");
                    params.put("is_scan",String.valueOf(is_scan));
                    params.put("cust_code", cust_code);
                    params.put("from_code", from_code);
                    params.put("lati", latitude);
                    params.put("logi", logitude);
                    params.put("addr", address);
                    params.put("transport_no", SharedPref.getInstance(GodownDeliveryMainActivity.this).getVehicleNo());
                    params.put("driver", SharedPref.getInstance(GodownDeliveryMainActivity.this).getID());
                    params.put("email", SharedPref.getInstance(GodownDeliveryMainActivity.this).getEmail());
                    params.put("count", count);
                    params.put("db_host",SharedPref.mInstance.getDBHost());
                    params.put("db_username",SharedPref.mInstance.getDBUsername());
                    params.put("db_password",SharedPref.mInstance.getDBPassword());
                    params.put("db_name",SharedPref.mInstance.getDBName());
                    return params;
                }
            };
            VolleySingleton.getInstance(GodownDeliveryMainActivity.this).addToRequestQueue(stringRequest);


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


    private void startCameraPreviewActivity() {
        //   startActivity(new Intent(this, CameraPreviewActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myDB != null)
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


    @Override
    public void onItemClick(int position) {

    }
}