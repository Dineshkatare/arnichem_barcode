package com.arnichem.arnichem_barcode.Producation.ZeroAir;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.Barcode.LaserScannerActivity;
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.Barcode.ProductionLaserScannerActivity;
import com.arnichem.arnichem_barcode.Producation.Co2.CO2Filling;
import com.arnichem.arnichem_barcode.Producation.Nitrogen.NitrogenFilling;
import com.arnichem.arnichem_barcode.Producation.Oxygen.FisrtPart;
import com.arnichem.arnichem_barcode.Producation.Oxygen.OxygenFilling;
import com.arnichem.arnichem_barcode.Producation.Oxygen.distnameadapter;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.EmptyMain;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.DistributorHelper;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;

public class ZeroAirFilling extends AppCompatActivity {
    ArrayList<String> id, cylindername,dis,vol,disname,distot,iddist,distotvol;
    TextView Totalscanvalue;
    RecyclerView recyclerView,recyclerView1;
    Zerodistadapter oxygenAdapter;
    distnameadapter distna;
    AutoCompleteTextView cylindernumber,cylindernumber1,cylindervolume;
    Button print,submit,adddata;
    Spinner spinnerDistributor,spinnermanifold;
    ArrayAdapter<CharSequence> adapter;
    boolean status = false;
    String distributorname="",distributorcode,manifoldval,count,batch_id;
    public int distributorpos,manifoldpos;
    ArrayAdapter<String> distributordataAdapter;
    DistributorHelper distributorHelper;
    ZeroAirHelper oxygenHelper;
    syncHelper sync;
    ProgressDialog progressDialog;
    static JSONObject object =null;
    List<String> cylinder;
    List<String> is_scan;
    List<String> cubic;
    List<String> Selected;
    String temp="",tempvol;
    int finalAI_qty,finaldist_qty,totvolume;
    String sm,em,after_tank_pressure,after_tank_liquid_liter,before_tank_pressure,before_tank_liquid_liter,fillingp;

    FloatingActionButton mAddCameraScanFab, mAddBarcodeScanFab;

    // Use the ExtendedFloatingActionButton to handle the
    // parent FAB
    ExtendedFloatingActionButton mAddFab;

    Boolean isAllFabsVisible;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zero_air_filling);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("ZeroAir Cylinder Fill");
        print=findViewById(R.id.OxygenPrint);
        print.setVisibility(GONE);
        spinnerDistributor=findViewById(R.id.spinnerDistributor);
        spinnermanifold=findViewById(R.id.spinermanifold);
        Totalscanvalue=findViewById(R.id.Totalscanvalue);
        adddata=findViewById(R.id.adddata);
        cylindernumber=findViewById(R.id.cylindernumber);
        cylindernumber1=findViewById(R.id.cylindernumber1);
        mAddFab = findViewById(R.id.add_fab);
        // FAB button
        mAddCameraScanFab = findViewById(R.id.camera_scan);
        mAddBarcodeScanFab =
                findViewById(R.id.barcode_scan);
        isAllFabsVisible = false;
        cylinder=new ArrayList<String>();
        is_scan=new ArrayList<>();
        cubic=new ArrayList<String>();
        cylindervolume=findViewById(R.id.cylindervolume);
        adapter= ArrayAdapter.createFromResource(this,R.array.manifold, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        try {
            manifoldpos = Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        }catch(NumberFormatException ex){

        }
        try{
            distributorpos=Integer.parseInt(SharedPref.getInstance(this).get_dist());
        } catch(NumberFormatException ex){

        }
        spinnermanifold.setAdapter(adapter);
        if(manifoldpos!=0)
        {
            spinnermanifold.setSelection(manifoldpos);
        }
        sm=SharedPref.getInstance(this).getSm();
        em=SharedPref.getInstance(this).getEm();
        after_tank_pressure= SharedPref.getInstance(this).getAfter_tank_pressure();
        after_tank_liquid_liter=SharedPref.getInstance(this).getAfter_tank_liquid_liter();
        before_tank_pressure=SharedPref.getInstance(this).getBefore_tank_pressure();
        before_tank_liquid_liter= SharedPref.getInstance(this).getBefore_tank_liquid_liter();
        fillingp=SharedPref.getInstance(this).getFillGapPressure();
        recyclerView=findViewById(R.id.oxygenfillrecyle);
        recyclerView1=findViewById(R.id.distnamerecycle);
        spinnermanifold.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                ///  Log.v("item", (String) parent.getItemAtPosition(position));
                manifoldval=(String) parent.getItemAtPosition(position);
                manifoldpos=position;
                SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(manifoldpos));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        loadSpinnerData();
        distributorHelper=new DistributorHelper(this);
        oxygenHelper=new ZeroAirHelper(this);
        sync=new syncHelper(this);

        submit=findViewById(R.id.OxygenSubmit);
        submit.setEnabled(true);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(distributorname.isEmpty()){
                    MDToast.makeText(ZeroAirFilling.this, "कृपया distributor निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }else {

                    submit.setEnabled(false);
                    Oxygenpost();
                }
            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(ZeroAirFilling.this, ZeroAirPrint.class);
                i.putExtra("batchDt",batch_id);
                i.putExtra("starttimevolume",before_tank_liquid_liter);
                i.putExtra("endtimevolume",after_tank_liquid_liter);
                i.putExtra("manifoldval",manifoldval);
                startActivity(i);
            }
        });
        adddata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (manifoldpos == 0) {

                    MDToast.makeText(ZeroAirFilling.this, "कृपया manifold निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }else if(distributorpos==0){
                    MDToast.makeText(ZeroAirFilling.this, "कृपया distributor निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                } else if(cylindernumber1.getText().toString().isEmpty()){
                    MDToast.makeText(ZeroAirFilling.this, "कृपया सिलेंडर नंबर टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }else if(cylindervolume.getText().toString().isEmpty()){
                    MDToast.makeText(ZeroAirFilling.this, "कृपया सिलेंडर व्हॉल्युम टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                } else {
                    oxygenHelper.addBook(cylindernumber1.getText().toString(), distributorname, cylindervolume.getText().toString(),"N");
                    finish();
                    startActivity(getIntent());
                }



            }
        });
        id = new ArrayList<>();
        cylindername =new ArrayList<>();
        dis=new ArrayList<>();
        vol=new ArrayList<>();
        iddist=new ArrayList<>();
        disname=new ArrayList<>();
        distot=new ArrayList<>();
        distotvol=new ArrayList<>();
        Selected=new ArrayList<>();
        storeDataInArrays();
        check();
        loadata();
        Totalscanvalue.setText(count);


        spinnerDistributor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                distributorname = distributordataAdapter.getItem(position);
                distributorpos=position;
                if(distributorname.equalsIgnoreCase(SharedPref.getInstance(ZeroAirFilling.this).getOwnCode()))
                {

                    cylindernumber1.setVisibility(GONE);
                    cylindervolume.setVisibility(GONE);
                    adddata.setVisibility(GONE);
                    cylindernumber.setVisibility(View.VISIBLE);
                }
                else
                {
                    cylindernumber.setVisibility(GONE);
                    cylindervolume.setVisibility(View.VISIBLE);
                    cylindernumber1.setVisibility(View.VISIBLE);
                    adddata.setVisibility(View.VISIBLE);

                }
                SharedPref.getInstance(getApplicationContext()).store_dist(String.valueOf(distributorpos));
                Cursor cursor = distributorHelper.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col=cursor.getString(1);
                        String col1 =cursor.getString(2);
                        if(col.contentEquals(distributorname))
                        {
                            if(distributorname.equalsIgnoreCase(SharedPref.getInstance(ZeroAirFilling.this).getOwnCode())){
                                temp  = SharedPref.getInstance(ZeroAirFilling.this).getOwnCode();
                            }else {
                                temp=col1;

                            }

                        }
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        oxygenAdapter = new Zerodistadapter(ZeroAirFilling.this,this, id, cylindername,dis,vol);
        recyclerView.setAdapter(oxygenAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ZeroAirFilling.this));
        distna=new distnameadapter(ZeroAirFilling.this,this,iddist,disname,distot,distotvol);
        recyclerView1.setAdapter(distna);
        recyclerView1.setLayoutManager(new LinearLayoutManager(ZeroAirFilling.this));
//        zero_air_scan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (manifoldpos == 0) {
//
//                    MDToast.makeText(ZeroAirFilling.this, "कृपया manifold निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
//
//                }else if(distributorpos==0){
//                    MDToast.makeText(ZeroAirFilling.this, "कृपया distributor निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
//
//                } else {
//
//                    status = true;
//                    Intent intent = new Intent(ZeroAirFilling.this, NewScanner.class);
//                    intent.putExtra("type", "zero_air");
//                    intent.putExtra("dis", distributorname);
//                    startActivity(intent);
//                }
//            }
//        });
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
                        if (manifoldpos == 0) {

                            MDToast.makeText(ZeroAirFilling.this, "कृपया manifold निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                        }else if(distributorpos==0){
                            MDToast.makeText(ZeroAirFilling.this, "कृपया distributor निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                        } else {
                            Intent intent = new Intent(ZeroAirFilling.this, ProductionLaserScannerActivity.class);
                            intent.putExtra("type", "air");
                            intent.putExtra("dis", distributorname);
                          //  intent.putExtra("vol", cylindervolume.getText().toString());
                            startActivity(intent);
                        }
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
                        Intent intent = new Intent(ZeroAirFilling.this, NewScanner.class);
                        intent.putExtra("type", "empty");
                        startActivity(intent);
                    }
                });




    }



    private void loadSpinnerData() {
        DistributorHelper db = new DistributorHelper(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        distributordataAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        distributordataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner

        spinnerDistributor.setAdapter(distributordataAdapter);
        if(distributorpos!=0)
        {
            spinnerDistributor.setSelection(distributorpos);
        }


    }



    void storeDataInArrays(){
        Cursor cursor = oxygenHelper.readAllData();
        if(cursor.getCount() == 0){
//            empty_imageview.setVisibility(View.VISIBLE);

        }else{
            while (cursor.moveToNext()){
                id.add(cursor.getString(0));
                cylindername.add(cursor.getString(1));
                cylinder.add(cursor.getString(1));
                is_scan.add(cursor.getString(4));
                dis.add(cursor.getString(2));
                vol.add(cursor.getString(3));
                cubic.add(cursor.getString(3));
                Cursor distcursor = distributorHelper.readAllData();
                if (cursor.getString(2).equals(SharedPref.getInstance(ZeroAirFilling.this).getOwnCode())) {
                    temp=SharedPref.getInstance(ZeroAirFilling.this).getOwnCode();
                } else {
                    while (distcursor.moveToNext()) {
                        String col=distcursor.getString(1);
                        String col1 =distcursor.getString(2);
                        if(col.contentEquals(cursor.getString(2)))
                        {
                            temp=col1;
                        }
                    }
                }
                Selected.add(temp);
            }
            int cou = cursor.getCount();
            count= String.valueOf(cou);

        }
    }
    void check(){
        Cursor cursor = oxygenHelper.readcount();
        if(cursor.getCount() == 0){
        }else{
            while (cursor.moveToNext()){
                disname.add(cursor.getString(3));
                distot.add(cursor.getString(2));
                iddist.add(cursor.getString(0));
                distotvol.add(cursor.getString(1));
                try {
                    totvolume=totvolume+Integer.parseInt(cursor.getString(1));
                }catch(NumberFormatException ex){

                }


                if(cursor.getString(3).equals(SharedPref.getInstance(ZeroAirFilling.this).getOwnCode()))
                {
                    finalAI_qty= Integer.parseInt(cursor.getString(2));
                }
                else
                {
                    finaldist_qty= finaldist_qty+Integer.parseInt(cursor.getString(2));
                }

            }
        }
    }
    private  void  loadata()
    {
        List<ItemCode>  itemCodes=new ArrayList<>();
        SearchAdapter searchAdapter=new SearchAdapter(getApplicationContext(),itemCodes);
        cylindernumber.setThreshold(1);
        cylindernumber.setAdapter(searchAdapter);
        cylindernumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (manifoldpos == 0) {
                    cylindernumber.setText("");
                    MDToast.makeText(ZeroAirFilling.this, "कृपया manifold निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                }else if(distributorpos==0){
                    MDToast.makeText(ZeroAirFilling.this, "कृपया distributor निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }   else {
                    Cursor cursor = sync.readAllData();
                    if (cursor.getCount() == 0) {
                        //      empty_imageview.setVisibility(View.VISIBLE);
                        //      no_data.setVisibility(View.VISIBLE);
                    } else {
                        while (cursor.moveToNext()) {
                            String col = cursor.getString(1);
                            String col1 = cursor.getString(2);
                            String fill = cursor.getString(5);
                            String vol = cursor.getString(4);
                            if (col.contentEquals(cylindernumber.getText().toString())) {
                                tempvol = cursor.getString(4);


                            }
                        }
                    }
                    oxygenHelper.addBook(cylindernumber.getText().toString(), distributorname, tempvol,"N");
                    finish();
                    startActivity(getIntent());
                }
            }
        });
    }
    private void Oxygenpost() {
        progressDialog = new ProgressDialog(ZeroAirFilling.this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please wait....");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(ZeroAirFilling.this);
        StringRequest request = new StringRequest(Request.Method.POST, APIClient.zero_air_entry, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                //      Snackbar.make(scrollView,"हा सिलेंडर नंबर "+"या बारकोड सोबत रजिस्टर झाला आहे ", Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN).setTextColor(Color.BLACK).show();
                try {
                    submit.setEnabled(true);
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        object = array.getJSONObject(i);
                        String status = object.getString("status");
                        String msg = object.getString("msg");

                        if (status.equals("success")) {
                            print.setVisibility(View.VISIBLE);
                            submit.setVisibility(GONE);
                            MDToast.makeText(ZeroAirFilling.this, "ZeroAir Fill Entry Done !", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                            progressDialog.dismiss();
                            batch_id = object.getString("batch_id");
                            Intent intent = new Intent(ZeroAirFilling.this, ZeroAirPrint.class);
                            intent.putExtra("batchDt", batch_id);
                            intent.putExtra("starttimevolume", before_tank_liquid_liter);
                            intent.putExtra("endtimevolume", after_tank_liquid_liter);
                            intent.putExtra("manifoldval", manifoldval);
                            startActivity(intent);
                            //  SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));


                        } else {
                            submit.setEnabled(true);
                            progressDialog.dismiss();

                        }

                        Log.e("JSON", "> " + status + msg);
                    }

                } catch (JSONException e) {
                    submit.setEnabled(true);
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // method to handle errors.
                progressDialog.dismiss();
                submit.setEnabled(true);
                Toast.makeText(ZeroAirFilling.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("dura_code",String.valueOf(cylinder));
                params.put("is_scan",String.valueOf(is_scan));
                params.put("owner_code",String.valueOf(Selected));
                params.put("starttime",sm);
                params.put("endtime",em);
                params.put("cubic",String.valueOf(cubic));
                params.put("totcubic",String.valueOf(totvolume));
                params.put("pressure",fillingp);
                params.put("manifold_no",manifoldval);
                params.put("cyl_quan",count);
                params.put("AI_qty", String.valueOf(finalAI_qty));
                params.put("dist_qty", String.valueOf(finaldist_qty));
                params.put("after_tank_pressure",after_tank_pressure);
                params.put("after_tank_liquid_liter",after_tank_liquid_liter);
                params.put("before_tank_pressure",before_tank_pressure);
                params.put("before_tank_liquid_liter",before_tank_liquid_liter);
                params.put("supervisor",SharedPref.getInstance(ZeroAirFilling.this).Id());
                params.put("email",SharedPref.getInstance(ZeroAirFilling.this).getEmail());
                params.put("remarks","Transaction Through App");
                params.put("batch_prefix",SharedPref.mInstance.getBatchPrefix());
                params.put("db_host",SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        // below line is to make
        // a json object request.
        queue.add(request);
    }


    @Override
    protected void onResume() {
        if(status){
            status = false;
            startActivity(getIntent());
        }
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, FisrtPart.class);
        startActivity(intent);
    }
}