package com.arnichem.arnichem_barcode.Barcode;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.OnItemClickListener;
import com.arnichem.arnichem_barcode.Producation.Co2.Co2Helper;
import com.arnichem.arnichem_barcode.Producation.Nitrogen.NitrogenHelper;
import com.arnichem.arnichem_barcode.Producation.Oxygen.OxygenAdapter;
import com.arnichem.arnichem_barcode.Producation.Oxygen.OxygenHelper;
import com.arnichem.arnichem_barcode.Producation.ZeroAir.ZeroAirHelper;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.view.syncHelper;

import java.util.ArrayList;
import java.util.List;

public class ProductionLaserScannerActivity extends AppCompatActivity implements OnItemClickListener {
    ArrayList<String> id, cylindername,dis,vol,disname,distot,iddist,distotvol;

    RecyclerView recyclerView;
    syncHelper synchelper;
    boolean status = true;
    private String inputHolder = "";

    ArrayList<String> book_id, book_title;
    TextView Totalscanvalue;

    Button doneBtn;

    String count;

    List<String> cylinder;

    List<String> is_scan;
    EditText editText;
    String type = "";
    String disStr = "";

    ZeroAirHelper zeroAirHelper;

    OxygenHelper oxygenHelper;

    NitrogenHelper nitrogenHelper;

    Co2Helper co2Helper;


    OxygenAdapter oxygenAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production_laser_scanner);
        getIntentData();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState != null) {

            // Get the bundle from the savedInstanceState
            Bundle bundle = savedInstanceState.getBundle("data");

            // Get the type and dis variables from the bundle
            type = bundle.getString("type");
            disStr = bundle.getString("dis");

        }
        if (type.contentEquals("oxygen")) {
            getSupportActionBar().setTitle("Oxygen Scan");
        } else if (type.contentEquals("no2")) {
            getSupportActionBar().setTitle("Nitrogen Barcode Scan");
        } else if (type.contentEquals("co2")) {
            getSupportActionBar().setTitle("CO2 Scan");
        } else if (type.contentEquals("air")) {
            getSupportActionBar().setTitle("Zero Air Scan");
        }
        id = new ArrayList<>();
        cylindername =new ArrayList<>();
        dis=new ArrayList<>();
        vol=new ArrayList<>();


        recyclerView = findViewById(R.id.recyclerView);
        synchelper = new syncHelper(ProductionLaserScannerActivity.this);
        oxygenHelper = new OxygenHelper(ProductionLaserScannerActivity.this);
        nitrogenHelper = new NitrogenHelper(ProductionLaserScannerActivity.this);
        co2Helper = new Co2Helper(ProductionLaserScannerActivity.this);
        zeroAirHelper=new ZeroAirHelper(this);

        Totalscanvalue = findViewById(R.id.Totalscanvalue);
        doneBtn = findViewById(R.id.doneBtn);
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        cylinder = new ArrayList<String>();
        is_scan = new ArrayList<>();


        editText = findViewById(R.id.newScan);
        editText.requestFocus();
        closeKeypad(this);
        storeDataInArrays();

        oxygenAdapter = new OxygenAdapter(ProductionLaserScannerActivity.this, this, id, cylindername, dis, vol,this,type);
        recyclerView.setAdapter(oxygenAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProductionLaserScannerActivity.this));


        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    void storeDataInArrays() {
        Cursor cursor = null;
        if (type.contentEquals("oxygen")) {
            cursor = oxygenHelper.readAllDataWithoutOrder();
        } else if (type.contentEquals("no2")) {
            cursor = nitrogenHelper.readAllDataWithoutOrder();
        } else if (type.contentEquals("co2")) {
            cursor = co2Helper.readAllDataWithoutOrder();
        } else if (type.contentEquals("air")) {
            cursor = zeroAirHelper.readAllDataWithoutOrder();
        }


        if (cursor.getCount() == 0) {
//            empty_imageview.setVisibility(View.VISIBLE);

        } else {
            while (cursor.moveToNext()) {
                id.add(cursor.getString(0));
                cylindername.add(cursor.getString(1));
                cylinder.add(cursor.getString(1));
                is_scan.add(cursor.getString(4));
                dis.add(cursor.getString(2));
                vol.add(cursor.getString(3));

            }
            int cou = cursor.getCount();
            count = String.valueOf(cou);

        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event == null) {
            return false;
        }

        int action = event.getAction();

        try {
            if (action == KeyEvent.ACTION_DOWN) {
                Log.d("KEYDOWN", event.getKeyCode() + "");
            } else if (action == KeyEvent.ACTION_UP) {
                char pressedKey = (char) event.getUnicodeChar();
                Log.d("pressedKey != 0", pressedKey + "");
                if (pressedKey != 0) {
                    if (pressedKey == ',' || pressedKey == 10) {
                        Log.d("pressedKey != ','", "inputHolder " + this.inputHolder);
                        // Perform action based on inputHolder value
                        // registerID(this.inputHolder); // Replace with your actual method call
                        this.inputHolder = "";
                    } else {
                        this.inputHolder += pressedKey;
                        Log.d("pressedKey", pressedKey + "");
                    }
                }
                Log.d("KEYUP", event.getKeyCode() + "");
            }

            Log.d("load", editText.getText().toString());
            android.os.Handler handler = new Handler();

            // Create a runnable that will be executed after 1000 milliseconds (1 second)
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // Do something here

                    String text = editText.getText().toString();
                    if (!text.isEmpty()) {
                        // Replace with your actual method call
                        scanned(text, true);
                        Log.d("load1", editText.getText().toString() + text);
                    }
                    editText.setText("");
                    editText.requestFocus();
                }
            };

            // Schedule the runnable to be executed after 1000 milliseconds
            handler.postDelayed(runnable, 500);
            // Delay the call to the `call()` method by 1000 milliseconds

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DispatchKeyEvent", "An exception occurred: " + e.getMessage());
        }

        Log.d("KEY", event.getKeyCode() + "");
        return false;
    }

    private void scanned(String displayValue, boolean val) {

        if (status) {
            if (type.equalsIgnoreCase("oxygen")) {

                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                } else {
                    while (cursor.moveToNext()) {

                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        String tempvol = cursor.getString(4);
                        Log.e("col1", "An exception occurred: " + col1 + " " + displayValue);

                        if (col1.contentEquals(displayValue)) {
                            status = false;
                            oxygenHelper.addBook(col, disStr, tempvol,"B");
                            editText.setText("");
                            editText.requestFocus();

                            finish();
                            startActivity(getIntent());
                            break;
                        }
                    }
                    editText.setText("");
                    editText.requestFocus();


                }


            } else if (type.equalsIgnoreCase("no2")) {

                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        Log.e("col1", "An exception occurred: " + col1 + " " + displayValue);
                        String tempvol = cursor.getString(4);

                        if (col1.contentEquals(displayValue)) {
                            status = false;
                            nitrogenHelper.addBook(col, disStr, tempvol,"B");
                            editText.setText("");
                            editText.requestFocus();

                            finish();
                            startActivity(getIntent());
                            break;
                        }
                    }
                    editText.setText("");
                    editText.requestFocus();
                }
            } else if (type.equalsIgnoreCase("co2")) {
                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String volume = cursor.getString(4);
                        String Fillwith = cursor.getString(5);
                        String col1 = cursor.getString(2);
                        String col = cursor.getString(1);
                        String tempvol = cursor.getString(4);

                        Log.e("col1", "An exception occurred: " + col1 + " " + displayValue);

                        if (col1.contentEquals(displayValue)) {
                            co2Helper.addBook(col, disStr, tempvol,"B");
                            finish();
                            startActivity(getIntent());
                            break;
                        }
                    }
                    editText.setText("");
                    editText.requestFocus();
                }
            } else {
                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                } else {
                    while (cursor.moveToNext()) {
                        String volume = cursor.getString(4);
                        String Fillwith = cursor.getString(5);
                        String col1 = cursor.getString(2);
                        String col = cursor.getString(1);
                        String tempvol = cursor.getString(4);

                        Log.e("col1", "An exception occurred: " + col1 + " " + displayValue);

                        if (col1.contentEquals(displayValue)) {
                            status = false;
                            editText.setText("");
                            editText.requestFocus();

                            Log.e("col1", "An exception occurred: " + col1 + " " + displayValue);

                            zeroAirHelper.addBook(col, disStr, tempvol,"B");
                            finish();
                            startActivity(getIntent());
                        }
                    }
                    editText.setText("");
                    editText.requestFocus();


                }

            }


        }

    }

    public static void closeKeypad(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), 0);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();

    }

    private void getIntentData() {
        Intent intent = getIntent();

        if (intent.hasExtra("type")) {
            type = intent.getExtras().getString("type", "");
            disStr = intent.getExtras().getString("dis", "");

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Create a new Bundle object
        Bundle bundle = new Bundle();

        // Put the type and dis variables into the bundle
        bundle.putString("type", type);
        bundle.putString("dis", disStr);

        // Put the bundle into the savedInstanceState
        outState.putBundle("data", bundle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (oxygenHelper != null)
            oxygenHelper.close();
        if (zeroAirHelper != null)
            zeroAirHelper.close();
        if (co2Helper != null)
            co2Helper.close();
        if (nitrogenHelper != null)
            nitrogenHelper.close();


    }


    @Override
    public void onItemClick(int position) {
        finish();
        startActivity(getIntent());

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