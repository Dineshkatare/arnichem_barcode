package com.arnichem.arnichem_barcode.TransactionsView.deliveryAmmonia;

import static android.view.View.GONE;

import androidx.annotation.Nullable;
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
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

public class AmmoniaMaindelivery extends AppCompatActivity {
    ArrayList<String> id, cylindername,adempty,adfull,adnet;
    ProgressDialog dialog;
    ArrayList<String> book_id, book_title;
    ImageView closeImg;
    Button uploadSign;
    String s="";
    boolean status = false;

    com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;
    RecyclerView recyclerView;
    FloatingActionButton add_button;
    FusedLocationProviderClient fusedLocationProviderClient;
    ImageView empty_imageview,signedImg;
    DatabaseHandler databaseHandlercustomer;
    TextView no_data, vehiclevalue, usernamevalue, date, totalscanval,FullTv,emptyTv,netTv,cylinderTv;
    Spinner spinner, customerspinnerdelivery;
    String from_warehouse, to_warehouse, cust_code, srno, from_code,latitude="0",logitude="0",address="0";

    int count;
    SharedPreferences pref;
    Button button, delprint;
    ammoia_deliAdapter deliadapter;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    deliDB delidb;
    public int poslocfixdel, poscustfixdel;
    static JSONObject object = null;
    List<String> cylinder;
    List<String> lastnet;
    List<String> is_scan;
    syncHelper synchelper;
    AutoCompleteTextView deliverycylindersea;


    ConstraintLayout constraintSigned;
    String digital_sign = "", digitalSignPath = "";

    List<String> item;
    List<String> itemq;
    List<String> item_volume;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ammonia_maindelivery);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(" Ammonia Delivery");
        delidb = new deliDB(AmmoniaMaindelivery.this);
        id = new ArrayList<String>();
        cylinder = new ArrayList<String>();
        lastnet = new ArrayList<String>();
        cylindername= new ArrayList<String>();
        is_scan = new ArrayList<String>();
        adempty=new ArrayList<String>();
        adfull=new ArrayList<String>();
        adnet=new ArrayList<String>();
        item = new ArrayList<>();
        itemq = new ArrayList<>();
        item_volume = new ArrayList<>();
        deliadapter = new ammoia_deliAdapter(AmmoniaMaindelivery.this, this,id,cylindername,adfull,adempty,adnet);
        spinner = findViewById(R.id.deliveryloc);
        deliverycylindersea = findViewById(R.id.deliverycylindersea);
        totalscanval = findViewById(R.id.totalscanval);
        constraintSigned = findViewById(R.id.constraintSigned);
        closeImg = findViewById(R.id.closeImg);
        add_button = findViewById(R.id.deliveryscan);
        customerspinnerdelivery = findViewById(R.id.cutomerdelivery);
        poslocfixdel = Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel = Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        databaseHandlercustomer = new DatabaseHandler(AmmoniaMaindelivery.this);
        fromloccodehandler = new fromloccodehandler(AmmoniaMaindelivery.this);
        FullTv=findViewById(R.id.fuulwt);
        emptyTv=findViewById(R.id.emwt);
        netTv=findViewById(R.id.netwt);
        cylinderTv=findViewById(R.id.cylno);
        FullTv.setVisibility(GONE);
        emptyTv.setVisibility(GONE);
        netTv.setVisibility(GONE);
        cylinderTv.setVisibility(GONE);
        uploadSign = findViewById(R.id.uploadSign);
        synchelper = new syncHelper(AmmoniaMaindelivery.this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(AmmoniaMaindelivery.this);
        loadata();
        fetchData();
        loadSpinnerData();
        vehiclevalue = findViewById(R.id.vno);
        usernamevalue = findViewById(R.id.usernametxtvalue);
        button = findViewById(R.id.delMainPost);
        signedImg = findViewById(R.id.signedImg);
        delprint = findViewById(R.id.delprintbtn);
        delprint.setVisibility(GONE);
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        vehiclevalue.setText(SharedPref.getInstance(this).getVehicleNo());
        date = findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        recyclerView = findViewById(R.id.recyclerView);

//        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);
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
                        if (col.contentEquals(to_warehouse)) {
                            cust_code = col1;

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
                Intent intent = new Intent(AmmoniaMaindelivery.this, ammonia_deliveryprint.class);
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
                postUsingVolley();
            }
        });

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status =true;
                Intent intent =new Intent(AmmoniaMaindelivery.this, NewScanner.class);
                intent.putExtra("type", "delivery");
                startActivity(intent);
            }
        });
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        deliadapter = new ammoia_deliAdapter(AmmoniaMaindelivery.this, this,id,cylindername,adfull,adempty,adnet);
        storeDataInArrays();
        check();
        totalscanval.setText(String.valueOf(count));
        if(count!=0)
        {
            FullTv.setVisibility(View.VISIBLE);
            emptyTv.setVisibility(View.VISIBLE);
            netTv.setVisibility(View.VISIBLE);
            cylinderTv.setVisibility(View.VISIBLE);

        }
        recyclerView.setAdapter(deliadapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(AmmoniaMaindelivery.this));
        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AmmoniaMaindelivery.this, ActivityDigitalSignature.class);
                intent.putExtra("type", "ammonia_delivery");
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
        LocalBroadcastManager.getInstance(this).registerReceiver(ammonia_no_receiver,
                new IntentFilter("ammonia_delivery"));



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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
//            recreate();
        }
    }

    void storeDataInArrays() {
        Cursor cursor = delidb.readAllData();
        if (cursor.getCount() == 0) {
//            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                id.add(cursor.getString(0));
                cylindername.add(cursor.getString(1));
                cylinder.add(cursor.getString(1));
                adfull.add(cursor.getString(2));
                adempty.add(cursor.getString(3));
                adnet.add(cursor.getString(4));
                lastnet.add(cursor.getString(4));
                is_scan.add(cursor.getString(7));
            }
            //         empty_imageview.setVisibility(View.GONE);
           count = cursor.getCount();


            no_data.setVisibility(View.GONE);
        }

    }
    private void postUsingVolley() {

        dialog = new ProgressDialog(AmmoniaMaindelivery.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poslocfixdel == 0) {
            dialog.dismiss();
            MDToast.makeText(AmmoniaMaindelivery.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            button.setEnabled(true);

        } else if (poscustfixdel == 0) {
            dialog.dismiss();
            MDToast.makeText(AmmoniaMaindelivery.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

            button.setEnabled(true);

        } else {
            StringBuilder str = new StringBuilder("");
            for (String eachstring : cylinder) {
                str.append(eachstring).append(",");
            }

            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.ammonia_delivery,
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
                                        MDToast.makeText(AmmoniaMaindelivery.this, "Delivey Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                        MDToast.makeText(AmmoniaMaindelivery.this, "आता प्रिंट बटण दाबा !", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                                        button.setVisibility(View.GONE);
                                        delprint.setVisibility(View.VISIBLE);
                                        srno = object.getString("srno");

//                                        //  SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));

                                        Intent intent = new Intent(AmmoniaMaindelivery.this, ammonia_deliveryprint.class);
                                        intent.putExtra("durano", String.valueOf(cylinder));
                                        intent.putExtra("custname", to_warehouse);
                                        intent.putExtra("custcode", cust_code);
                                        intent.putExtra("empb", srno);
                                        intent.putExtra("sign_path", digitalSignPath);

                                        startActivity(intent);
                                        dialog.dismiss();
                                        button.setEnabled(true);


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
                            error.printStackTrace();
                            dialog.dismiss();
                            button.setEnabled(true);

                            Log.e("JSON", "> " +error.toString());

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("dura_code", String.valueOf(cylinder));
                    params.put("item", String.valueOf(item));
                    params.put("itemq", String.valueOf(itemq));
                    params.put("item_volume", String.valueOf(item_volume));
                    params.put("net_wt", String.valueOf(lastnet));
                    params.put("from_warehouse", from_warehouse);
                    params.put("to_warehouse", to_warehouse);
                    params.put("transport_type", "ARNICHEM");
                    params.put("cust_code", cust_code);
                    params.put("from_code", from_code);
                    params.put("lati", latitude);
                    params.put("sign", digital_sign);
                    params.put("logi", logitude);
                    params.put("addr",address);
                    params.put("is_scan",String.valueOf(is_scan));
                    params.put("transport_no", SharedPref.getInstance(AmmoniaMaindelivery.this).getVehicleNo());
                    params.put("driver", SharedPref.getInstance(AmmoniaMaindelivery.this).getID());
                    params.put("email", SharedPref.getInstance(AmmoniaMaindelivery.this).getEmail());
                    params.put("count", String.valueOf(count));
                    params.put("db_host", SharedPref.mInstance.getDBHost());
                    params.put("db_username", SharedPref.mInstance.getDBUsername());
                    params.put("db_password", SharedPref.mInstance.getDBPassword());
                    params.put("db_name", SharedPref.mInstance.getDBName());
                    return params;
                }
            };
            VolleySingleton.getInstance(AmmoniaMaindelivery.this).addToRequestQueue(stringRequest);


        }

    }



    private  void  loadata()
    {
        List<ItemCode>  itemCodes=new ArrayList<>();
        SearchAdapter searchAdapter=new SearchAdapter(getApplicationContext(),itemCodes);
        deliverycylindersea.setThreshold(1);
        deliverycylindersea.setAdapter(searchAdapter);
        deliverycylindersea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                        if (col1.contentEquals(deliverycylindersea.getText().toString())) {
                            login(deliverycylindersea.getText().toString(),"no",Fillwith,volume);

                           // delidb.addBook(deliverycylindersea.getText().toString(), Fillwith, volume,"no");
//                            finish();
//                            startActivity(getIntent());
                        }
                    }
                }

            }
        });
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(delidb != null)
            delidb.close();


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Transactions.class);
        startActivity(intent);
    }

    private void login (String cyl,String is_scan,String fill_with,String volume) {
        dialog = new ProgressDialog(AmmoniaMaindelivery.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,APIClient.ammonia_del_update,
                new Response.Listener<String>() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                object = array.getJSONObject(i);
                                String status = object.getString("status");
                                if(status.equalsIgnoreCase("success")){
                                    String fullwt = object.getString("full_wt");
                                    String emtywt = object.getString("empty_wt");
                                    String netwt = object.getString("net_wt");

                                    delidb.addBook(cyl,fullwt,emtywt,netwt,fill_with,volume,is_scan);
                                    finish();
                                    startActivity(getIntent());
                                    dialog.dismiss();

                                }else {
                                    String msg = object.getString("msg");
                                    Toast.makeText(AmmoniaMaindelivery.this, ""+msg, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();


                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AmmoniaMaindelivery.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
                        MDToast.makeText(AmmoniaMaindelivery.this, "कृपया इंटरनेट तपासा ", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("ammoniacyl",cyl);
                params.put("db_host",SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name", SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(AmmoniaMaindelivery.this).addToRequestQueue(stringRequest);
    }

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

    private final BroadcastReceiver ammonia_no_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("ammonia_delivery")) {
                //Extract your data - better to use constants...
                s=intent.getStringExtra("ammonia_no");
                String volume= intent.getStringExtra("volume");
                String fill_with= intent.getStringExtra("fill_with");

                if(s!=null)
                {
                    login(s,"yes",fill_with,volume);
                }
            }

        }
    };

    void check() {
        int TMEDOXCOUNT = 0;
        String TMEDOXVOLUME = "";
        Cursor cursor = delidb.readcount();
        if (cursor.getCount() == 0) {
        } else {
            while (cursor.moveToNext()) {
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

}
