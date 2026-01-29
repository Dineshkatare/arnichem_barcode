package com.arnichem.arnichem_barcode.Producation.NewAmmonia;

import static android.view.View.GONE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.Producation.Oxygen.OxygenFilling;
import com.arnichem.arnichem_barcode.Producation.Oxygen.distnameadapter;
import com.arnichem.arnichem_barcode.Producation.Producation_Main;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.Producation.ZeroAir.ZeroAirFilling;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.DistributorHelper;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ammoniaMain extends AppCompatActivity {
    ArrayList<String> id, cylindername, dis, vol, disname, distot, iddist, distotvol, distfull, distnet, mani;
    TextView Totalscanvalue, maniTv, FullTv, actual_wt, emptyTv, netTv, cylinderTv, sfuulwt, semwt, snetwt;
    RecyclerView recyclerView, recyclerView1;
    ammadaoter oxygenAdapter;
    int count = 0;
    distnameadapter distna;
    boolean status = false;
    AutoCompleteTextView cylindernumber, cylindernumber1, cylinderfull, cylindervolume;
    Button print, submit, adddata;
    Spinner spinnerDistributor, spinnermanifold;
    ArrayAdapter<CharSequence> adapter;
    String distributorname, distributorcode, manifoldval, batch_id;
    public int distributorpos, manifoldpos;
    ArrayAdapter<String> distributordataAdapter;
    DistributorHelper distributorHelper;
    ammoniaHelper oxygenHelper;
    syncHelper sync;
    ProgressDialog progressDialog;
    static JSONObject object = null;
    List<String> cylinder;
    List<String> cubic;
    List<String> fullwts;

    List<String> actual_wts;

    List<String> manifolds;
    List<String> netwts;
    List<String> Selected;
    String temp = "", tempvol;
    int finalAI_qty, finaldist_qty, totvolume;
    String sm, em, after_tank_pressure, after_tank_liquid_liter, before_tank_pressure, before_tank_liquid_liter;
    FloatingActionButton deliveryscan;
    androidx.appcompat.widget.SwitchCompat barcodeSwitch;
    StringBuilder barcode = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ammonia_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ammonia Cylinder Filling");
        sm = SharedPref.getInstance(this).getSm();
        em = SharedPref.getInstance(this).getEm();
        after_tank_pressure = SharedPref.getInstance(this).getAfter_tank_pressure();
        after_tank_liquid_liter = SharedPref.getInstance(this).getAfter_tank_liquid_liter();
        before_tank_pressure = SharedPref.getInstance(this).getBefore_tank_pressure();
        before_tank_liquid_liter = SharedPref.getInstance(this).getBefore_tank_liquid_liter();

        print = findViewById(R.id.OxygenPrint);
        print.setVisibility(GONE);
        spinnerDistributor = findViewById(R.id.spinnerDistributor);
        spinnermanifold = findViewById(R.id.spinermanifold);
        Totalscanvalue = findViewById(R.id.Totalscanvalue);
        adddata = findViewById(R.id.adddata);
        cylindernumber = findViewById(R.id.cylindernumber);
        cylindernumber1 = findViewById(R.id.cylindernumber1);
        cylinderfull = findViewById(R.id.cylinderfullweight);
        actual_wt = findViewById(R.id.actual_wt);
        maniTv = findViewById(R.id.mani);
        FullTv = findViewById(R.id.fuulwt);
        emptyTv = findViewById(R.id.emwt);
        netTv = findViewById(R.id.netwt);
        deliveryscan = findViewById(R.id.deliveryscan);
        barcodeSwitch = findViewById(R.id.barcodeSwitch);
        barcodeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    if (cylindernumber.getVisibility() == View.VISIBLE) {
                        cylindernumber.requestFocus();
                    } else {
                        cylindernumber1.requestFocus();
                    }
                }
            }
        });
        cylinderTv = findViewById(R.id.cylno);
        sfuulwt = findViewById(R.id.sfuulwt);
        semwt = findViewById(R.id.semwt);
        snetwt = findViewById(R.id.snetwt);
        maniTv.setVisibility(GONE);
        FullTv.setVisibility(GONE);
        emptyTv.setVisibility(GONE);
        netTv.setVisibility(GONE);
        cylinderTv.setVisibility(GONE);
        sfuulwt.setVisibility(View.GONE);
        semwt.setVisibility(View.GONE);
        snetwt.setVisibility(View.GONE);

        cylinder = new ArrayList<String>();
        cubic = new ArrayList<String>();
        fullwts = new ArrayList<String>();
        actual_wts = new ArrayList<String>();
        manifolds = new ArrayList<String>();
        netwts = new ArrayList<String>();
        cylindervolume = findViewById(R.id.cylinderempty);
        adapter = ArrayAdapter.createFromResource(this, R.array.ammoniamanifold, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        try {
            distributorpos = Integer.parseInt(SharedPref.getInstance(this).get_dist());
        } catch (NumberFormatException ex) {

        }
        spinnermanifold.setAdapter(adapter);
        if (manifoldpos != 0) {
            spinnermanifold.setSelection(manifoldpos);
        }

        recyclerView = findViewById(R.id.oxygenfillrecyle);
        recyclerView1 = findViewById(R.id.distnamerecycle);
        spinnermanifold.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                /// Log.v("item", (String) parent.getItemAtPosition(position));
                manifoldval = (String) parent.getItemAtPosition(position);
                manifoldpos = position;
                SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(manifoldpos));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        loadSpinnerData();
        loadata();
        distributorHelper = new DistributorHelper(this);
        sync = new syncHelper(this);

        submit = findViewById(R.id.OxygenSubmit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Oxygenpost();
            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ammoniaMain.this, ammoniaprint.class);
                i.putExtra("batchDt", batch_id);
                i.putExtra("starttimevolume", before_tank_liquid_liter);
                i.putExtra("endtimevolume", after_tank_liquid_liter);
                startActivity(i);
            }
        });
        deliveryscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (manifoldpos == 0) {

                    MDToast.makeText(ammoniaMain.this, "कृपया manifold निवडा !", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();

                } else if (distributorpos == 0) {
                    MDToast.makeText(ammoniaMain.this, "कृपया distributor निवडा !", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();

                } else {

                    status = true;
                    Intent intent = new Intent(ammoniaMain.this, NewScanner.class);
                    intent.putExtra("type", "ammonia_delivery");
                    intent.putExtra("dis", distributorname);
                    startActivity(intent);
                }
            }
        });

        adddata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cylinderfull.getText().toString().equals("")) {
                    MDToast.makeText(ammoniaMain.this, "कृपया सिलेंडर फुल वेट टाका !", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();

                } else if (cylindervolume.getText().toString().equals("")) {
                    MDToast.makeText(ammoniaMain.this, "कृपया सिलेंडर एमटी वेट टाका", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();

                } else if (manifoldpos == 0) {

                    MDToast.makeText(ammoniaMain.this, "कृपया manifold निवडा !", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();

                } else {
                    if (distributorname.equals(SharedPref.getInstance(ammoniaMain.this).getOwnCode())) {

                        Double fullcheck = Double.parseDouble(cylinderfull.getText().toString());
                        Double emptycheck = Double.parseDouble(cylindervolume.getText().toString());
                        Double res = fullcheck - emptycheck;
                        String finalres = String.valueOf(res);
                        oxygenHelper.addBook(cylindernumber.getText().toString(), distributorname, manifoldval,
                                finalres, cylinderfull.getText().toString(), cylindervolume.getText().toString(),
                                actual_wt.getText().toString());
                        finish();
                        startActivity(getIntent());
                    } else {
                        Double fullcheck = Double.parseDouble(cylinderfull.getText().toString());
                        Double emptycheck = Double.parseDouble(cylindervolume.getText().toString());
                        Double res = fullcheck - emptycheck;
                        String finalres = String.valueOf(res);
                        oxygenHelper.addBook(cylindernumber1.getText().toString(), distributorname, manifoldval,
                                finalres, cylinderfull.getText().toString(), cylindervolume.getText().toString(),
                                actual_wt.getText().toString());
                        finish();
                        startActivity(getIntent());

                    }

                }

            }
        });
        id = new ArrayList<>();
        cylindername = new ArrayList<>();
        dis = new ArrayList<>();
        vol = new ArrayList<>();
        mani = new ArrayList<>();
        iddist = new ArrayList<>();
        disname = new ArrayList<>();
        distot = new ArrayList<>();
        distotvol = new ArrayList<>();
        Selected = new ArrayList<>();
        distfull = new ArrayList<>();
        distnet = new ArrayList<>();

        oxygenHelper = new ammoniaHelper(this);
        storeDataInArrays();

        check();

        Totalscanvalue.setText(String.valueOf(count));
        if (count != 0) {
            maniTv.setVisibility(View.VISIBLE);
            FullTv.setVisibility(View.VISIBLE);
            emptyTv.setVisibility(View.VISIBLE);
            netTv.setVisibility(View.VISIBLE);
            cylinderTv.setVisibility(View.VISIBLE);
            sfuulwt.setVisibility(View.VISIBLE);
            semwt.setVisibility(View.VISIBLE);
            snetwt.setVisibility(View.VISIBLE);
        }

        spinnerDistributor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                distributorname = distributordataAdapter.getItem(position);
                distributorpos = position;
                if (distributorname.equalsIgnoreCase(SharedPref.getInstance(ammoniaMain.this).getOwnCode())) {
                    cylindernumber1.setVisibility(GONE);
                    cylindernumber.setVisibility(View.VISIBLE);
                    if (distributorname.equalsIgnoreCase("WAPL")) {
                        actual_wt.setVisibility(View.VISIBLE);
                    } else {
                        actual_wt.setVisibility(GONE);

                    }
                } else {
                    cylindernumber.setVisibility(GONE);
                    cylindervolume.setVisibility(View.VISIBLE);
                    cylindernumber1.setVisibility(View.VISIBLE);
                }
                SharedPref.getInstance(getApplicationContext()).store_dist(String.valueOf(distributorpos));
                Cursor cursor = distributorHelper.readAllData();
                if (cursor.getCount() == 0) {
                    // empty_imageview.setVisibility(View.VISIBLE);
                    // no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        if (col.contentEquals(distributorname)) {
                            if (distributorname
                                    .equalsIgnoreCase(SharedPref.getInstance(ammoniaMain.this).getOwnCode())) {
                                temp = SharedPref.getInstance(ammoniaMain.this).getOwnCode();
                            } else {
                                temp = col1;

                            }
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        oxygenAdapter = new ammadaoter(ammoniaMain.this, this, id, cylindername, mani, vol, distfull, distnet);
        recyclerView.setAdapter(oxygenAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ammoniaMain.this));

    }

    private void loadSpinnerData() {
        DistributorHelper db = new DistributorHelper(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        distributordataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        distributordataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner

        spinnerDistributor.setAdapter(distributordataAdapter);
        if (distributorpos != 0) {
            spinnerDistributor.setSelection(distributorpos);
        }

    }

    void storeDataInArrays() {
        Cursor cursor = oxygenHelper.readAllData();
        if (cursor.getCount() > 0) {
            // empty_imageview.setVisibility(View.VISIBLE);
            while (cursor.moveToNext()) {
                id.add(cursor.getString(0));
                cylindername.add(cursor.getString(1));
                cylinder.add(cursor.getString(1));
                dis.add(cursor.getString(2));
                vol.add(cursor.getString(4));
                cubic.add(cursor.getString(4));
                distfull.add(cursor.getString(3));
                fullwts.add(cursor.getString(3));
                actual_wts.add(cursor.getString(7));
                mani.add(cursor.getString(5));
                manifolds.add(cursor.getString(5));
                distnet.add(cursor.getString(6));
                netwts.add(cursor.getString(6));

                Cursor distcursor = distributorHelper.readAllData();
                if (cursor.getString(2).equalsIgnoreCase(SharedPref.getInstance(ammoniaMain.this).getOwnCode())) {
                    temp = SharedPref.getInstance(ammoniaMain.this).getOwnCode();
                } else {

                    while (distcursor.moveToNext()) {
                        String col = distcursor.getString(1);
                        String col1 = distcursor.getString(2);
                        if (col.contentEquals(cursor.getString(2))) {
                            temp = col1;
                        }
                    }
                }
                Selected.add(temp);
            }
            count = cursor.getCount();

        } else {

        }
    }

    void check() {
        Cursor cursor = oxygenHelper.readcount();
        if (cursor.getCount() == 0) {
        } else {
            while (cursor.moveToNext()) {
                disname.add(cursor.getString(3));
                distot.add(cursor.getString(2));
                iddist.add(cursor.getString(0));
                distotvol.add(cursor.getString(1));
                try {
                    totvolume = totvolume + Integer.parseInt(cursor.getString(1));
                } catch (NumberFormatException ex) {

                }

                if (cursor.getString(3).equals(SharedPref.getInstance(ammoniaMain.this).getOwnCode())) {
                    finalAI_qty = Integer.parseInt(cursor.getString(2));
                } else {
                    finaldist_qty = finaldist_qty + Integer.parseInt(cursor.getString(2));
                }

            }
        }
    }

    private void loadata() {
        List<ItemCode> itemCodes = new ArrayList<>();
        SearchAdapter searchAdapter = new SearchAdapter(getApplicationContext(), itemCodes);
        cylindernumber.setThreshold(1);
        cylindernumber.setAdapter(searchAdapter);
        cylindernumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (manifoldpos == 0) {
                    cylindernumber.setText("");
                    MDToast.makeText(ammoniaMain.this, "कृपया manifold निवडा !", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();

                } else if (distributorpos == 0) {
                    MDToast.makeText(ammoniaMain.this, "कृपया distributor निवडा !", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();

                } else {
                    Cursor cursor = sync.readAllData();
                    String foundVol = "";
                    String foundActualWt = "";
                    if (cursor.getCount() == 0) {
                        // empty_imageview.setVisibility(View.VISIBLE);
                        // no_data.setVisibility(View.VISIBLE);
                    } else {
                        while (cursor.moveToNext()) {
                            String col = cursor.getString(1); // Item Code / Cylinder No
                            if (col.contentEquals(cylindernumber.getText().toString())) {
                                foundVol = cursor.getString(3); // Weight -> Empty Weight
                                foundActualWt = cursor.getString(4); // Volume -> Actual Weight
                                break;
                            }
                        }
                        if (!foundVol.isEmpty()) {
                            cylindervolume.setText(foundVol);
                        }
                        if (!foundActualWt.isEmpty()) {
                            actual_wt.setText(foundActualWt);
                        }
                    }

                    // oxygenHelper.addBook(cylindernumber.getText().toString(), distributorname,
                    // tempvol);
                    // finish();
                    // startActivity(getIntent());

                }

            }
        });
    }

    private void Oxygenpost() {

        progressDialog = new ProgressDialog(ammoniaMain.this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please wait....");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(ammoniaMain.this);
        StringRequest request = new StringRequest(Request.Method.POST, APIClient.ammonia_entry,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        MDToast.makeText(ammoniaMain.this, "Ammonia Filling Entry Done !", MDToast.LENGTH_SHORT,
                                MDToast.TYPE_SUCCESS).show();

                        // Snackbar.make(scrollView,"हा सिलेंडर नंबर "+"या बारकोड सोबत रजिस्टर झाला आहे
                        // ",
                        // Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN).setTextColor(Color.BLACK).show();
                        try {
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                object = array.getJSONObject(i);
                                String status = object.getString("status");
                                String msg = object.getString("msg");

                                if (status.equals("success")) {
                                    print.setVisibility(View.VISIBLE);
                                    submit.setVisibility(GONE);

                                    progressDialog.dismiss();
                                    batch_id = object.getString("batch_id");
                                    Intent intent = new Intent(ammoniaMain.this, ammoniaprint.class);
                                    intent.putExtra("batchDt", batch_id);
                                    intent.putExtra("starttimevolume", before_tank_liquid_liter);
                                    intent.putExtra("endtimevolume", after_tank_liquid_liter);
                                    startActivity(intent);
                                    // SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));

                                } else {
                                    progressDialog.dismiss();

                                }

                                Log.e("JSON", "> " + status + msg);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(ammoniaMain.this, "ffh" + e, Toast.LENGTH_LONG).show();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(ammoniaMain.this, "ffh" + error, Toast.LENGTH_LONG).show();

                        // method to handle errors.
                        Toast.makeText(ammoniaMain.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("dura_code", String.valueOf(cylinder));
                params.put("owner_code", String.valueOf(Selected));
                params.put("starttime", sm);
                params.put("endtime", em);
                params.put("full_wt", String.valueOf(fullwts));
                params.put("actual_wt", String.valueOf(actual_wts));
                params.put("emt_wt", String.valueOf(cubic));
                params.put("net_wt", String.valueOf(netwts));
                params.put("totcubic", String.valueOf(totvolume));
                params.put("manifold_no", String.valueOf(manifolds));
                params.put("cyl_quan", String.valueOf(count));
                params.put("AI_qty", String.valueOf(finalAI_qty));
                params.put("dist_qty", String.valueOf(finaldist_qty));
                params.put("after_tank_pressure", after_tank_pressure);
                params.put("after_tank_liquid_liter", after_tank_liquid_liter);
                params.put("before_tank_pressure", before_tank_pressure);
                params.put("before_tank_liquid_liter", before_tank_liquid_liter);
                params.put("supervisor", SharedPref.getInstance(ammoniaMain.this).Id());
                params.put("email", SharedPref.getInstance(ammoniaMain.this).getEmail());
                params.put("remarks", "Transaction Through App");
                params.put("batch_prefix", SharedPref.mInstance.getBatchPrefix());
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username", SharedPref.mInstance.getDBUsername());
                params.put("db_password", SharedPref.mInstance.getDBPassword());
                params.put("db_name", SharedPref.mInstance.getDBName());
                return params;
            }
        };
        // below line is to make
        // a json object request.
        queue.add(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, Producation_Main.class);
        startActivity(intent);
    }

    // Receiver for parsing data from NewScanner
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String ammonia_no = intent.getStringExtra("ammonia_no");
            String volume = intent.getStringExtra("volume");
            String fill_with = intent.getStringExtra("fill_with");

            if (cylindernumber.getVisibility() == View.VISIBLE) {
                cylindernumber.setText(ammonia_no);
                cylindernumber.dismissDropDown();
            } else {
                cylindernumber1.setText(ammonia_no);
                cylindernumber1.dismissDropDown();
            }

            if (volume != null && !volume.isEmpty()) {
                cylindervolume.setText(volume);
            }

            // Focus full weight for manual entry
            cylinderfull.requestFocus();
            MDToast.makeText(ammoniaMain.this, "Scanned: " + ammonia_no, MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS)
                    .show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("ammonia_delivery"));

        if (status) {
            status = false;
            // startActivity(getIntent()); // Removed to preventing reload loop
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (barcodeSwitch != null && barcodeSwitch.isChecked()) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                char pressedKey = (char) event.getUnicodeChar();
                if (Character.isLetterOrDigit(pressedKey)) {
                    barcode.append(pressedKey);
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    final String scannedCode = barcode.toString().trim();
                    barcode.setLength(0);
                    if (!scannedCode.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processScannedCode(scannedCode);
                            }
                        });
                    }
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void processScannedCode(String scannedCode) {
        if (cylindernumber.getVisibility() == View.VISIBLE) {
            cylindernumber.setText(scannedCode);
            cylindernumber.dismissDropDown();
        } else {
            cylindernumber1.setText(scannedCode);
            cylindernumber1.dismissDropDown();
        }

        Cursor cursor = sync.readAllData();
        String foundVol = "";
        String foundActualWt = "";
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                String col = cursor.getString(1); // Cylinder No
                if (col.contentEquals(scannedCode)) {
                    foundVol = cursor.getString(3); // Weight -> Empty Weight
                    foundActualWt = cursor.getString(4); // Volume -> Actual Weight
                    break;
                }
            }
        }

        if (!foundVol.isEmpty()) {
            cylindervolume.setText(foundVol);
        }
        if (!foundActualWt.isEmpty()) {
            actual_wt.setText(foundActualWt);
        }

        // Focus full weight for manual entry or validation
        cylinderfull.requestFocus();
        MDToast.makeText(this, "Scanned: " + scannedCode, MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
    }
}
