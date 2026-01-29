package com.arnichem.arnichem_barcode.Barcode;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryHelper;
import com.arnichem.arnichem_barcode.GodownView.godownempty.GodownEmptyHelper;
import com.arnichem.arnichem_barcode.OnItemClickListener;
import com.arnichem.arnichem_barcode.Producation.NewAmmonia.ammadaoter;
import com.arnichem.arnichem_barcode.Producation.NewAmmonia.ammoniaHelper;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.AddClyHelper;
import com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardCustomAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardDatabaseHelper;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CustomAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.MyDatabaseHelper;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliDB;
import com.arnichem.arnichem_barcode.TransactionsView.deliveryAmmonia.ammoia_deliAdapter;
import com.arnichem.arnichem_barcode.view.syncHelper;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LaserScannerActivity extends AppCompatActivity implements OnItemClickListener {
    RecyclerView recyclerView;
    syncHelper synchelper;
    boolean status = true;

    // UI Elements
    ArrayList<String> book_id, book_title;
    TextView Totalscanvalue;
    Button doneBtn;
    EditText editText;
    RecyclerView Filled_with_Recycle_View;

    // Helpers & Adapters
    InWardCustomAdapter customAdapter;
    InWardDatabaseHelper myDB;
    GodownDeliveryHelper godownDeliveryHelper;
    GodownEmptyHelper godownEmptyHelper;
    AddClyHelper addClyHelper;
    deliDB delidb;
    CustomAdapter outwardCustomAdapter;
    FilledWithAdapter filledWithAdapter;
    MyDatabaseHelper outwatdMyDB;
    ammoniaHelper ammoniaHelperDB;
    ammadaoter ammoniaAdapter;
    ammoia_deliAdapter ammoniaDeliveryAdapter;
    com.arnichem.arnichem_barcode.TransactionsView.deliveryAmmonia.deliDB ammoniaDeliveryDB;

    // Data Holders
    String count;
    List<String> cylinder;
    List<String> is_scan;
    String type = "";
    String dis = "";
    ArrayList<String> name, tot, volume;
    ArrayList<String> cylIdList, cyclinderNameList, fillwith;
    // Ammonia Arrays
    ArrayList<String> ammonia_id, ammonia_cylname, ammonia_mani, ammonia_vol, ammonia_distfull, ammonia_distnet;
    ArrayList<String> ad_id, ad_cylname, ad_full, ad_empty, ad_net;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser_scanner);
        getIntentData();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle("data");
            if (bundle != null) {
                type = bundle.getString("type");
                dis = bundle.getString("dis");
            }
        }

        setupTitle();

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerView);
        Totalscanvalue = findViewById(R.id.Totalscanvalue);
        Filled_with_Recycle_View = findViewById(R.id.fillwithrec);
        doneBtn = findViewById(R.id.doneBtn);
        editText = findViewById(R.id.newScan);

        // Initialize Helpers
        synchelper = new syncHelper(LaserScannerActivity.this);
        addClyHelper = new AddClyHelper(LaserScannerActivity.this);
        godownDeliveryHelper = new GodownDeliveryHelper(LaserScannerActivity.this);
        godownEmptyHelper = new GodownEmptyHelper(this);
        ammoniaHelperDB = new ammoniaHelper(this);
        ammoniaDeliveryDB = new com.arnichem.arnichem_barcode.TransactionsView.deliveryAmmonia.deliDB(this);
        myDB = new InWardDatabaseHelper(LaserScannerActivity.this);
        delidb = new deliDB(LaserScannerActivity.this);
        outwatdMyDB = new MyDatabaseHelper(LaserScannerActivity.this);

        // Initialize Lists
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        cylinder = new ArrayList<>();
        is_scan = new ArrayList<>();
        cylIdList = new ArrayList<>();
        cyclinderNameList = new ArrayList<>();
        fillwith = new ArrayList<>();
        name = new ArrayList<>();
        tot = new ArrayList<>();
        volume = new ArrayList<>();
        // Initialize Ammonia Arrays
        ammonia_id = new ArrayList<>();
        ammonia_cylname = new ArrayList<>();
        ammonia_mani = new ArrayList<>();
        ammonia_vol = new ArrayList<>();
        ammonia_distfull = new ArrayList<>();
        ammonia_distnet = new ArrayList<>();
        ad_id = new ArrayList<>();
        ad_cylname = new ArrayList<>();
        ad_full = new ArrayList<>();
        ad_empty = new ArrayList<>();
        ad_net = new ArrayList<>();

        // Setup Scanner Input Resilience and Focus Management
        setupScannerInput();

        // Initial Data Load
        refreshUI();

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupTitle() {
        if (type == null)
            type = "";

        if (type.contentEquals("inward")) {
            getSupportActionBar().setTitle("Inward Barcode Scan");
        } else if (type.contentEquals("outward")) {
            getSupportActionBar().setTitle("Outward Barcode Scan");
        } else if (type.contentEquals("delivery")) {
            getSupportActionBar().setTitle("Delivery Barcode Scan");
        } else if (type.contentEquals("empty")) {
            getSupportActionBar().setTitle("Empty Barcode Scan");
        } else if (type.contentEquals("godown_delivery")) {
            getSupportActionBar().setTitle("Godwon Delivery Barcode Scan");
        } else if (type.contentEquals("godown_empty")) {
            getSupportActionBar().setTitle("Godwon Empty Barcode Scan");
        }
    }

    private void setupScannerInput() {
        editText.requestFocus();
        // aggressively clear focus on startup to ensure we get it fresh
        editText.setText("");

        // 1. OnKeyListener to capture ENTER events (Hardware Keyboard)
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Action UP is safer for barcode scanners to avoid repeating events
                if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    processInput();
                    return true;
                }
                return false;
            }
        });

        // 2. TextWatcher to capture newline characters (Batch Input)
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("\n")) {
                    processInput();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 3. Focus Management (Aggressive Reclaim)
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // Post a runnable to reclaim focus after a short delay
                    // This handles cases where focus is briefly lost during UI updates or system
                    // dialogs
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (editText != null) {
                                editText.requestFocus();
                            }
                        }
                    }, 50);
                }
            }
        });
    }

    private void processInput() {
        String text = editText.getText().toString().trim();
        if (!text.isEmpty()) {
            scanned(text);
        }
        editText.setText("");
        editText.requestFocus();
    }

    private void clearDataLists() {
        book_id.clear();
        book_title.clear();
        cylinder.clear();
        is_scan.clear();
        cylIdList.clear();
        cyclinderNameList.clear();
        fillwith.clear();
        name.clear();
        tot.clear();
        volume.clear();
    }

    private void refreshUI() {
        // 1. Clear Data
        clearDataLists();

        // 2. Reload Data based on type
        if (type.equalsIgnoreCase("inward")) {
            storeDataInArrays(); // Populates book_id, book_title, cylinder, is_scan

            // Setup or Notify Adapter
            if (customAdapter == null) {
                customAdapter = new InWardCustomAdapter(LaserScannerActivity.this, this, book_id, book_title, this,
                        type);
                recyclerView.setAdapter(customAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(LaserScannerActivity.this));
            } else {
                customAdapter.notifyDataSetChanged();
            }

        } else if (type.equalsIgnoreCase("delivery") || type.equalsIgnoreCase("godown_delivery")
                || type.equalsIgnoreCase("outward") || type.equalsIgnoreCase("empty")
                || type.equalsIgnoreCase("godown_empty")) {
            storedOutwardValues(); // Populates cylIdList, cyclinderNameList, fillwith
            check(); // Populates name, tot, volume

            Collections.reverse(cylIdList);
            Collections.reverse(cyclinderNameList);
            Collections.reverse(fillwith);

            // Setup or Notify Main Adapter
            if (outwardCustomAdapter == null) {
                outwardCustomAdapter = new CustomAdapter(LaserScannerActivity.this, this, cylIdList, cyclinderNameList,
                        fillwith, this, type);
                recyclerView.setAdapter(outwardCustomAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(LaserScannerActivity.this));
            } else {
                outwardCustomAdapter.notifyDataSetChanged();
            }

            // Setup or Notify FilledWith Adapter
            if (filledWithAdapter == null) {
                filledWithAdapter = new FilledWithAdapter(LaserScannerActivity.this, this, name, tot);
                Filled_with_Recycle_View.setAdapter(filledWithAdapter);
                Filled_with_Recycle_View.setLayoutManager(new LinearLayoutManager(LaserScannerActivity.this));
            } else {
                filledWithAdapter.notifyDataSetChanged();
            }
        } else if (type.equalsIgnoreCase("ammonia_delivery")) {
            storeAmmoniaDeliveryValues();
            if (ammoniaDeliveryAdapter == null) {
                ammoniaDeliveryAdapter = new ammoia_deliAdapter(LaserScannerActivity.this, LaserScannerActivity.this,
                        ad_id, ad_cylname, ad_full, ad_empty, ad_net);
                recyclerView.setAdapter(ammoniaDeliveryAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(LaserScannerActivity.this));
            } else {
                ammoniaDeliveryAdapter.notifyDataSetChanged();
            }
            Totalscanvalue.setText(count);
        }

        // 3. Update Text
        Totalscanvalue.setText(count);

        // 4. Scroll to top if needed
        if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
            recyclerView.scrollToPosition(0);
        }
    }

    void check() {
        Cursor cursor = null;
        if (type.equalsIgnoreCase("delivery")) {
            cursor = delidb.readAllDataInFIFOOrder();
        } else if (type.equalsIgnoreCase("godown_delivery")) {
            cursor = godownDeliveryHelper.readAllDataWithoutOrder();
        } else if (type.equalsIgnoreCase("outward")) {
            cursor = outwatdMyDB.readAllDataWithoutOrder();
        } else if (type.equalsIgnoreCase("empty")) {
            cursor = addClyHelper.readcount();
        } else if (type.equalsIgnoreCase("godown_empty")) {
            cursor = godownEmptyHelper.readcount();
        }

        if (cursor != null) {
            while (cursor.moveToNext()) {
                name.add(cursor.getString(3));
                volume.add(cursor.getString(4));
                tot.add(cursor.getString(2));
            }
            cursor.close();
        }
    }

    void storeDataInArrays() {
        Cursor cursor = null;
        if (type.equalsIgnoreCase("inward")) {
            cursor = myDB.readAllDataWithoutOrder();
        } else if (type.equalsIgnoreCase("godown_delivery")) {
            // Fallback/Safety - though typically handled in blocked above
            cursor = godownDeliveryHelper.readAllDataWithoutOrder();
        }

        if (cursor != null) {
            while (cursor.moveToNext()) {
                book_id.add(cursor.getString(0));
                book_title.add(cursor.getString(1));
                cylinder.add(cursor.getString(1));
                is_scan.add(cursor.getString(4));
            }
            int cou = cursor.getCount();
            count = String.valueOf(cou);
            cursor.close();
        }
    }

    void storedOutwardValues() {
        Cursor cursor = null;
        if (type.equalsIgnoreCase("delivery")) {
            cursor = delidb.readAllDataInFIFOOrder();
        } else if (type.equalsIgnoreCase("godown_delivery")) {
            cursor = godownDeliveryHelper.readAllDataWithoutOrder();
        } else if (type.equalsIgnoreCase("outward")) {
            cursor = outwatdMyDB.readAllData();
        } else if (type.equalsIgnoreCase("empty")) {
            cursor = addClyHelper.readAllDataWithoutOrder();
        } else if (type.equalsIgnoreCase("godown_empty")) {
            cursor = godownEmptyHelper.readAllData();
        }

        if (cursor != null) {
            while (cursor.moveToNext()) {
                cylIdList.add(cursor.getString(0));
                cyclinderNameList.add(cursor.getString(1));

                String filledVal = "";
                if (type.equalsIgnoreCase("empty") || type.equalsIgnoreCase("godown_empty")) {
                    filledVal = cursor.getString(5);
                } else {
                    filledVal = cursor.getString(2);
                }
                fillwith.add(filledVal);

                cylinder.add(cursor.getString(1));
                is_scan.add(cursor.getString(4));
            }
            int cou = cursor.getCount();
            count = String.valueOf(cou);
            cursor.close();
        }
    }

    void storeAmmoniaValues() {
        Cursor cursor = ammoniaHelperDB.readAllData();
        // Clear previous data
        ammonia_id.clear();
        ammonia_cylname.clear();
        ammonia_mani.clear();
        ammonia_vol.clear();
        ammonia_distfull.clear();
        ammonia_distnet.clear();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ammonia_id.add(cursor.getString(0));
                ammonia_cylname.add(cursor.getString(1));
                ammonia_distfull.add(cursor.getString(3));
                ammonia_vol.add(cursor.getString(4));
                ammonia_mani.add(cursor.getString(5));
                ammonia_distnet.add(cursor.getString(6));
            }
            count = String.valueOf(cursor.getCount());
        } else {
            count = "0";
        }
        if (cursor != null)
            cursor.close();
    }

    void storeAmmoniaDeliveryValues() {
        Cursor cursor = ammoniaDeliveryDB.readAllData();
        // Clear previous data
        ad_id.clear();
        ad_cylname.clear();
        ad_full.clear();
        ad_empty.clear();
        ad_net.clear();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                // Table: my_library
                // 0: _id
                // 1: cylname
                // 2: fullcl (Full)
                // 3: empty (Empty)
                // 4: net (Net)
                // 5: fill
                // 6: vol
                // 7: is_scan

                ad_id.add(cursor.getString(0));
                ad_cylname.add(cursor.getString(1));
                ad_full.add(cursor.getString(2));
                ad_empty.add(cursor.getString(3));
                ad_net.add(cursor.getString(4));
            }
            count = String.valueOf(cursor.getCount());
        } else {
            count = "0";
        }
        if (cursor != null)
            cursor.close();
    }

    private void scanned(String displayValue) {
        // Basic Validation
        if (displayValue == null || displayValue.isEmpty())
            return;

        Cursor cursor = synchelper.readAllData();
        if (cursor == null || cursor.getCount() == 0)
            return;

        boolean found = false;

        // Iterate through master data to find match
        while (cursor.moveToNext()) {
            String col = cursor.getString(1); // Item Code / Cylinder No
            String col1 = cursor.getString(2); // Barcode
            String volume = cursor.getString(4);
            String filledWith = cursor.getString(5);

            // Safety checks for null strings
            if (col == null)
                col = "";
            if (col1 == null)
                col1 = "";

            // Check based on type specific logic if needed, but generally we match input to
            // master data
            // Original code matched displayValue against col1 (Barcode) often, let's
            // preserve that logic or improve it
            // Issue found previously: Some manual entry used col instead of col1.
            // For SCANNER, we expect Barcode (col1) or Cylinder No (col)?
            // Usually scanner reads barcode (col1).

            // Logic Consolidation:
            // Check if scanned value matches Barcode (col1) OR Cylinder No (col)
            // But strict requirement was to match existing logic.
            // Existing logic mostly matched `col1.contentEquals(displayValue)`.

            if (col1.contentEquals(displayValue)) {
                found = true;
                // Match found! Add to appropriate DB
                if (type.equalsIgnoreCase("inward")) {
                    myDB.addBook(col, "B");
                } else if (type.equalsIgnoreCase("ammonia_delivery")) {
                    // API Call for Ammonia Delivery
                    String finalCol = col;
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.ammonia_del_update,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONArray array = new JSONArray(response);
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject object = array.getJSONObject(i);
                                            String status = object.getString("status");
                                            if (status.equalsIgnoreCase("success")) {
                                                String fullwt = object.getString("full_wt");
                                                String emtywt = object.getString("empty_wt");
                                                String netwt = object.getString("net_wt");

                                                String volume = "60"; // Default
                                                String fill_with = "AMMONIA"; // Default

                                                // Lookup local details
                                                Cursor cursor = synchelper.readAllData();
                                                if (cursor != null) {
                                                    while (cursor.moveToNext()) {
                                                        String colCode = cursor.getString(1);
                                                        String barcode = cursor.getString(2);
                                                        if (colCode.equals(displayValue)
                                                                || barcode.equals(displayValue)) {
                                                            volume = cursor.getString(4);
                                                            fill_with = cursor.getString(5);
                                                            break;
                                                        }
                                                    }
                                                    cursor.close();
                                                }

                                                String cylinderName = (finalCol != null && !finalCol.isEmpty())
                                                        ? finalCol
                                                        : displayValue;
                                                String volumeToUse = (volume != null && !volume.isEmpty()) ? volume
                                                        : "60";

                                                // Add to Delivery DB directly (Ammonia DB)
                                                // addBook(cyname, full, empty, net, fill_with, vol, is_scan)
                                                ammoniaDeliveryDB.addBook(cylinderName, fullwt, emtywt, netwt,
                                                        fill_with, volumeToUse, "yes");

                                                // Broadcast result to refresh Main UI
                                                Intent intent = new Intent("ammonia_delivery");
                                                intent.putExtra("ammonia_no", cylinderName);
                                                intent.putExtra("volume", volumeToUse);
                                                intent.putExtra("fill_with", fill_with);
                                                intent.putExtra("full_wt", fullwt);
                                                intent.putExtra("empty_wt", emtywt);
                                                intent.putExtra("net_wt", netwt);
                                                intent.putExtra("is_scan", "yes");
                                                LocalBroadcastManager.getInstance(LaserScannerActivity.this)
                                                        .sendBroadcast(intent);

                                                refreshUI();
                                                editText.setText("");
                                                editText.requestFocus();

                                            } else {
                                                String msg = object.getString("msg");
                                                Toast.makeText(LaserScannerActivity.this, msg, Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(LaserScannerActivity.this, "Network Error", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("ammoniacyl", displayValue);
                            params.put("db_host", SharedPref.mInstance.getDBHost());
                            params.put("db_username", SharedPref.mInstance.getDBUsername());
                            params.put("db_password", SharedPref.mInstance.getDBPassword());
                            params.put("db_name", SharedPref.mInstance.getDBName());
                            return params;
                        }
                    };
                    VolleySingleton.getInstance(LaserScannerActivity.this).addToRequestQueue(stringRequest);

                } else if (type.equalsIgnoreCase("empty")) {
                    addClyHelper.addBook(col, filledWith, volume, "B");
                } else if (type.equalsIgnoreCase("godown_empty")) {
                    godownEmptyHelper.addBook(col, filledWith, volume, "B");
                } else if (type.equalsIgnoreCase("delivery")) {
                    delidb.addBook(col, filledWith, volume, "B");
                } else if (type.equalsIgnoreCase("godown_delivery")) {
                    godownDeliveryHelper.addBook(col, filledWith, volume, "B");
                } else if (type.equalsIgnoreCase("outward")) {
                    outwatdMyDB.addBook(col, filledWith, volume, "B");
                }

                break; // Stop after finding match
            }
        }
        cursor.close();

        if (found) {
            refreshUI(); // Update UI without recreating activity
        } else {
            // Optional: Feedback for not found?
            // Toast.makeText(this, "Barcode not found", Toast.LENGTH_SHORT).show();
        }
    }

    public static void closeKeypad(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        editText.requestFocus();
        closeKeypad(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent.hasExtra("type")) {
            type = intent.getExtras().getString("type", "");
            dis = intent.getExtras().getString("dis", "");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putString("dis", dis);
        outState.putBundle("data", bundle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myDB != null)
            myDB.close();
        if (outwatdMyDB != null)
            outwatdMyDB.close();
        if (addClyHelper != null)
            addClyHelper.close();
        if (delidb != null)
            delidb.close();
        if (godownDeliveryHelper != null)
            godownDeliveryHelper.close();
        if (godownEmptyHelper != null)
            godownEmptyHelper.close();
        if (synchelper != null)
            synchelper.close();
        if (ammoniaHelperDB != null)
            ammoniaHelperDB.close();
    }

    @Override
    public void onItemClick(int position) {
        // This was refreshing the activity previously.
        // Now we should probably just refresh UI if it was intended to delete or
        // update?
        // OnItemClick in these adapters usually (checking history) might delete item?
        // In original code: finish(); startActivity(getIntent());
        // We should just refreshUI()
        refreshUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}