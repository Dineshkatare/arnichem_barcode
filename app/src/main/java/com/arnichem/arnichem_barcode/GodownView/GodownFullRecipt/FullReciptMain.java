package com.arnichem.arnichem_barcode.GodownView.GodownFullRecipt;

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
import androidx.annotation.Nullable;
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
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.GodownView.GOdownMainActivity;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryMainActivity;
import com.arnichem.arnichem_barcode.GodownView.godownempty.GodownEmptyMainActivity;
import com.arnichem.arnichem_barcode.OnItemClickListener;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery.DuraDeliveryMain;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.AddActivity;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CustomAdapter;
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

public class  FullReciptMain extends AppCompatActivity implements Listener, LocationData.AddressCallBack, OnItemClickListener {
    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    syncHelper synchelper;

    ArrayList<String> book_id, book_title,fillwith;
    RecyclerView recyclerView;
    FloatingActionButton add_button;
    ImageView empty_imageview;
    TextView no_data,usernamevalue,Totalscanvalue,date;
    SharedPreferences pref;
    Button button,print;
    ProgressDialog dialog;
    boolean status = false;
    Spinner spinnerloc,spinnercust;
    GodownFullReciptHelper myDB;
    CustomAdapter customAdapter;
    AutoCompleteTextView godownfullrecptcylinder;
    DatabaseHandler databaseHandlercustomer;
    fromloccodehandler fromloccodehandler;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    public  int poslocfixdel,poscustfixdel;
    static JSONObject object =null;
    List<String> cylinder;
    String digital_sign = "",digitalSignPath="" ;
    String from_warehouse,to_warehouse,cust_code,from_code,srno,count,latitude="0",logitude="0",address="0";
    Button uploadSign;
    ConstraintLayout constraintSigned;
    ImageView closeImg,signedImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_recipt_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Godown FullReceipt");
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false,true,this);
        spinnerloc=findViewById(R.id.spinlocgodownfullrecipt);
        spinnercust=findViewById(R.id.custnamespingodownfullrecipt);
        Totalscanvalue=findViewById(R.id.Totalscanvalue);
        godownfullrecptcylinder=findViewById(R.id.godownfullrecptcylinder);
        print=findViewById(R.id.godownfullreciptprintbtn);
        uploadSign = findViewById(R.id.uploadSign);
        constraintSigned = findViewById(R.id.constraintSigned);
        closeImg = findViewById(R.id.closeImg);
        signedImg = findViewById(R.id.signedImg);
        add_button = findViewById(R.id.godwonfullrecscan);

        print.setVisibility(View.GONE);
        cylinder=new ArrayList<String>();
        poslocfixdel= Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        poscustfixdel=Integer.parseInt(SharedPref.getInstance(this).getcustomersel());
        databaseHandlercustomer=new DatabaseHandler(FullReciptMain.this);
        fromloccodehandler=new fromloccodehandler(FullReciptMain.this);
        fetchData();
        loadSpinnerData();
        loadata();
        usernamevalue=findViewById(R.id.usernametxtvaluefullrecipt);
        pref = getSharedPreferences(constant.TAG,MODE_PRIVATE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName());
        date=findViewById(R.id.date);
        synchelper = new syncHelper(FullReciptMain.this);

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        recyclerView = findViewById(R.id.recyclerViewfullrecipt);
        add_button = findViewById(R.id.godwonfullrecscan);
        no_data = findViewById(R.id.no_datafullrecipt);
        button=findViewById(R.id.godownfullreciptMainPost);
        button.setEnabled(true);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraPreviewActivity();
                Intent intent = new Intent(FullReciptMain.this, AddActivity.class);
                startActivity(intent);
                //          startScan();
            }
        });
        myDB = new GodownFullReciptHelper(FullReciptMain.this);
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        fillwith = new ArrayList<>();
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
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(FullReciptMain.this, GodownFullRecpPrint.class);
                intent.putExtra("durano", String.valueOf(cylinder));
                intent.putExtra("custname",to_warehouse);
                intent.putExtra("empb",srno);
                intent.putExtra("count", count);
                intent.putExtra("custcode",cust_code);
                intent.putExtra("cylinder", String.valueOf(cylinder));
                startActivity(intent);
            }
        });
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status =true;
                Intent intent =new Intent(FullReciptMain.this, NewScanner.class);
                intent.putExtra("type", "godown_fullreceipt");
                startActivity(intent);
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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                postUsingVolley();
            }
        });
        customAdapter = new CustomAdapter(FullReciptMain.this, this, book_id, book_title, fillwith,this,"full_receipt");

      //  customAdapter = new GodownFullReciptAdapter(FullReciptMain.this,this, book_id, book_title);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(FullReciptMain.this));

        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FullReciptMain.this, ActivityDigitalSignature.class);
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
        customerdataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        customerdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnercust.setAdapter(customerdataAdapter);
        if(poscustfixdel!=0)
        {
            spinnercust.setSelection(poscustfixdel);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
//            recreate();
        }
    }
    private  void  loadata()
    {
        List<ItemCode>  itemCodes=new ArrayList<>();
        SearchAdapter searchAdapter=new SearchAdapter(getApplicationContext(),itemCodes);
        godownfullrecptcylinder.setThreshold(1);
        godownfullrecptcylinder.setAdapter(searchAdapter);
        godownfullrecptcylinder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                        if (col1.contentEquals(godownfullrecptcylinder.getText().toString())) {
                           // delidb.addBook(deliverycylindersea.getText().toString(), Fillwith, volume,"no");
                            myDB.addBook(godownfullrecptcylinder.getText().toString(),Fillwith,volume,"no");

                            finish();
                            startActivity(getIntent());
                        }
                    }
                }

//                finish();
//                startActivity(getIntent());
            }
        });
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
                fillwith.add(cursor.getString(2));
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

    private void postUsingVolley() {

        dialog = new ProgressDialog(FullReciptMain.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poslocfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(FullReciptMain.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        } else if (poscustfixdel == 0) {
            dialog.dismiss();
            button.setEnabled(true);
            MDToast.makeText(FullReciptMain.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


        }  else {
            StringBuilder str = new StringBuilder("");
            for (String eachstring : cylinder) {
                str.append(eachstring).append(",");
            }

            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.godown_fullrecipt_entry,
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
                                        MDToast.makeText(FullReciptMain.this, "FullRecipt Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                        button.setVisibility(View.GONE);
                                        print.setVisibility(View.VISIBLE);
                                        srno = object.getString("srno");

                                        //  SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));
                                        dialog.dismiss();

                                                Intent intent=new Intent(FullReciptMain.this, GodownFullRecpPrint.class);
                                                intent.putExtra("durano", String.valueOf(cylinder));
                                                intent.putExtra("custname",to_warehouse);
                                                intent.putExtra("empb",srno);
                                                intent.putExtra("sign_path",digitalSignPath);
                                                intent.putExtra("count", count);
                                                intent.putExtra("custcode",cust_code);
                                                intent.putExtra("cylinder", String.valueOf(cylinder));
                                        button.setEnabled(true);

                                        startActivity(intent);

//                                        Toast.makeText(login.this, "msg " + msg, Toast.LENGTH_SHORT).show();

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
                    params.put("from_warehouse", from_warehouse);
                    params.put("to_warehouse", to_warehouse);
                    params.put("transport_type", "OWN");
                    params.put("cust_code", cust_code);
                    params.put("from_code",from_code);
                    params.put("lati", latitude);
                    params.put("logi", logitude);
                    params.put("addr",address);
                    params.put("sign", digital_sign);
                    params.put("transport_no", SharedPref.getInstance(FullReciptMain.this).getVehicleNo());
                    params.put("driver", SharedPref.getInstance(FullReciptMain.this).getID());
                    params.put("email", SharedPref.getInstance(FullReciptMain.this).getEmail());
                    params.put("count", count);
                    params.put("db_host",SharedPref.mInstance.getDBHost());
                    params.put("db_username",SharedPref.mInstance.getDBUsername());
                    params.put("db_password",SharedPref.mInstance.getDBPassword());
                    params.put("db_name",SharedPref.mInstance.getDBName());
                    return params;
                }
            };
            VolleySingleton.getInstance(FullReciptMain.this).addToRequestQueue(stringRequest);


        }
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