package com.arnichem.arnichem_barcode.other_entries;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.EditText;
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
import com.arnichem.arnichem_barcode.GodownView.GOdownMainActivity;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDelPrint;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryHelper;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryMainActivity;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CustomAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.OtherItemsHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
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

public class OtherEntryActivity extends AppCompatActivity implements Listener, LocationData.AddressCallBack {

    static JSONObject object = null;
    public int poslocfixdel, poscustfixdel,positemfixdel;
    GetLocationDetail getLocationDetail;
    ArrayList<String> name, tot, volume;
    ArrayList<String> book_id, book_title, quantity_list;
    com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;
    DatabaseHandler databaseHandlercustomer;
    OtherItemsHandler otherItemsHandler;
    OtherEntryAdapter otherEntryAdapter;

    RecyclerView recyclerView;
    FloatingActionButton add_button;
    ImageView empty_imageview;
    TextView no_data, vehiclevalue, usernamevalue, date;
    String from_warehouse, to_warehouse,spinner_item, cust_code,item_code="", srno, count, from_code, latitude = "0", logitude = "0", address = "0";
    OtherEntryHelper otherEntryHelper;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    ArrayAdapter<String> itemsAdapter;

    Spinner spinner, spinnercust, item_spinner;
    SharedPreferences pref;
    Button button, dataAddBtn,printBtn;

    EditText quantityEdt;
    ProgressDialog dialog;
    List<String> cylinder;
    List<String> quantity;
//    List<String> itemq;
//    List<String> item_volume;
    Boolean checkInvoice = false;
    private EasyWayLocation easyWayLocation;
    Button uploadSign;
    ConstraintLayout constraintSigned;
    ImageView closeImg,signedImg;
    String digital_sign = "",digitalSignPath = "";
    boolean status = false;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_entry);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Other Delivery");

        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false, true, this);
        spinner = findViewById(R.id.spinfromgodel);
        cylinder = new ArrayList<String>();
        spinnercust = findViewById(R.id.custnamespingodowndel);
        printBtn = findViewById(R.id.printbtn);
        dataAddBtn = findViewById(R.id.dataAddBtn);
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);
        closeImg = findViewById(R.id.closeImg);
        signedImg = findViewById(R.id.signedImg);
        item_spinner = findViewById(R.id.item);
        quantityEdt = findViewById(R.id.quantity);

        poslocfixdel = Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel = Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        positemfixdel = Integer.parseInt(SharedPref.getInstance(this).get_item_sel());

        otherEntryHelper = new OtherEntryHelper(OtherEntryActivity.this);
        databaseHandlercustomer = new DatabaseHandler(OtherEntryActivity.this);
        fromloccodehandler = new fromloccodehandler(OtherEntryActivity.this);
        otherItemsHandler = new OtherItemsHandler(OtherEntryActivity.this);
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
        add_button = findViewById(R.id.godowndelscan);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status =true;

                Intent intent =new Intent(OtherEntryActivity.this, NewScanner.class);
                intent.putExtra("type", "_delivery");
                startActivity(intent);
            }
        });
        button.setEnabled(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                postUsingVolley();
            }
        });
        dataAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(item_code.isEmpty()){
                    MDToast.makeText(OtherEntryActivity.this, "Please select item", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }else if(quantityEdt.getText().toString().trim().isEmpty()){
                    MDToast.makeText(OtherEntryActivity.this, "Please enter quantity", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }else {
                    otherEntryHelper.addBook(spinner_item,item_code, quantityEdt.getText().toString());
                    finish();
                    positemfixdel = 0;
                    quantityEdt.setText("");
                    item_spinner.setSelection(0);
                    spinner_item = "";
                    item_code = "";
                    SharedPref.getInstance(OtherEntryActivity.this).store_item_sel("0");
                    startActivity(getIntent());
                }
            }
        });


        otherEntryHelper = new OtherEntryHelper(OtherEntryActivity.this);
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        quantity_list = new ArrayList<>();
        name = new ArrayList<>();
        tot = new ArrayList<>();
        volume = new ArrayList<>();
        quantity = new ArrayList<>();
//        itemq = new ArrayList<>();
//        item_volume = new ArrayList<>();
        fetchItems();
        storeDataInArrays();
        check();
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

        item_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner_item = itemsAdapter.getItem(position);
                positemfixdel = position;
                SharedPref.getInstance(getApplicationContext()).store_item_sel(String.valueOf(positemfixdel));
                Cursor cursor = otherItemsHandler.readAllData();
                if (cursor.getCount() == 0) {

                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(0);
                        if (col.contentEquals(spinner_item)) {
                            item_code = col1;
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


        otherEntryAdapter = new OtherEntryAdapter(OtherEntryActivity.this, this, book_id, book_title,quantity_list);

        // customAdapter = new GodownDeliveryAdapter(GodownDeliveryMainActivity.this, this, book_id, book_title);
        recyclerView.setAdapter(otherEntryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(OtherEntryActivity.this));

        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherEntryActivity.this, ActivityDigitalSignature.class);
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
        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        if (poslocfixdel != 0) {
            spinner.setSelection(poslocfixdel);
        }
    }

    private void fetchItems() {
        OtherItemsHandler db = new OtherItemsHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        itemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        item_spinner.setAdapter(itemsAdapter);
        if (positemfixdel != 0) {
            item_spinner.setSelection(positemfixdel);
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



    void storeDataInArrays() {
        Cursor cursor = otherEntryHelper.readAllData();
        if (cursor.getCount() == 0) {
//            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                book_id.add(cursor.getString(0));
                book_title.add(cursor.getString(1));
                quantity_list.add(cursor.getString(3));
                cylinder.add(cursor.getString(2));
                quantity.add(cursor.getString(3));

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
//        Cursor cursor = otherEntryHelper.readcount();
//        if (cursor.getCount() == 0) {
//        } else {
//            while (cursor.moveToNext()) {
//                name.add(cursor.getString(3));
//                volume.add(cursor.getString(4));
//                tot.add(cursor.getString(2));
//                if (cursor.getString(3).equals("MEDOX7")) {
//                    TMEDOXVOLUME = cursor.getString(4);
//
//                    TMEDOXCOUNT = Integer.parseInt(cursor.getString(2));
//                }
//
//                item.add(cursor.getString(3));
//                item_volume.add(cursor.getString(4));
//
//
//
//                .add(cursor.getString(2));
//            }
//            item.add("TRANSPORT");
//            item_volume.add("1");
//            itemq.add("1");
//            if (TMEDOXCOUNT != 0) {
//                item.add("TMEDOX7");
//                item_volume.add(TMEDOXVOLUME);
//                itemq.add(String.valueOf(TMEDOXCOUNT));
//            }
//
//
//        }
    }


    private void postUsingVolley() {
        dialog = new ProgressDialog(OtherEntryActivity.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poslocfixdel == 0) {
            button.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(OtherEntryActivity.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        } else if (poscustfixdel == 0) {
            button.setEnabled(true);

            dialog.dismiss();
            MDToast.makeText(OtherEntryActivity.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


        } else {
            StringBuilder str = new StringBuilder("");
            for (String eachstring : cylinder) {
                str.append(eachstring).append(",");
            }

            String commaseparatedlist = str.toString();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.other_entry,
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
                                        MDToast.makeText(OtherEntryActivity.this, "Other Delivey Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                        MDToast.makeText(OtherEntryActivity.this, "आता प्रिंट बटण दाबा !", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                                        button.setVisibility(View.GONE);
                                        printBtn.setVisibility(View.VISIBLE);
                                        srno = object.getString("srno");
                                        SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(0));
                                        SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(0));

                                        //  SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));
                                        dialog.dismiss();
                                        Intent intent = new Intent(OtherEntryActivity.this, OtherEntryPrintActivity.class);
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
                    params.put("item_code", String.valueOf(cylinder));
                    params.put("quantity", String.valueOf(quantity));
//                    params.put("itemq", String.valueOf(itemq));
//                    params.put("item_volume", String.valueOf(item_volume));
                    params.put("from_warehouse", from_warehouse);
                    params.put("to_warehouse", to_warehouse);
                    params.put("transport_type", "OWN");
                    params.put("cust_code", cust_code);
                    params.put("from_code", from_code);
                    params.put("lati", latitude);
                    params.put("logi", logitude);
                    params.put("addr", address);
                    params.put("transport_no", SharedPref.getInstance(OtherEntryActivity.this).getVehicleNo());
                    params.put("driver", SharedPref.getInstance(OtherEntryActivity.this).getID());
                    params.put("email", SharedPref.getInstance(OtherEntryActivity.this).getEmail());
                    params.put("count", count);
                    params.put("db_host",SharedPref.mInstance.getDBHost());
                    params.put("db_username",SharedPref.mInstance.getDBUsername());
                    params.put("db_password",SharedPref.mInstance.getDBPassword());
                    params.put("db_name",SharedPref.mInstance.getDBName());
                    return params;
                }
            };
            VolleySingleton.getInstance(OtherEntryActivity.this).addToRequestQueue(stringRequest);


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
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

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
                otherEntryHelper.deleteAllData();
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
        if (otherEntryHelper != null)
            otherEntryHelper.close();
    }

    @Override
    public void onBackPressed() {
        finish();
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