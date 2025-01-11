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
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.AddClyHelper;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.EmptyMain;
import com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardCustomAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardDatabaseHelper;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CustomAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.MyDatabaseHelper;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.FilledWithAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliDB;
import com.arnichem.arnichem_barcode.view.syncHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LaserScannerActivity extends AppCompatActivity implements OnItemClickListener {
    RecyclerView recyclerView;
    syncHelper synchelper;
    boolean status = true;
    private String inputHolder = "";

    ArrayList<String> book_id, book_title;
    TextView Totalscanvalue;

    InWardCustomAdapter customAdapter;
    InWardDatabaseHelper myDB;

    AddClyHelper addClyHelper;


    deliDB delidb;


    Button doneBtn;

    String count;

    List<String> cylinder;

    List<String> is_scan;
    EditText editText;
    String type = "";
    String dis = "";


    ArrayList<String> name, tot, volume;
    CustomAdapter outwardCustomAdapter;
    FilledWithAdapter filledWithAdapter;
    MyDatabaseHelper outwatdMyDB;


    ArrayList<String> cylIdList, cyclinderNameList, fillwith;
    RecyclerView Filled_with_Recycle_View;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser_scanner);
        getIntentData();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState != null) {

            // Get the bundle from the savedInstanceState
            Bundle bundle = savedInstanceState.getBundle("data");

            // Get the type and dis variables from the bundle
            type = bundle.getString("type");
            dis = bundle.getString("dis");
        }
        if (type.contentEquals("inward")) {
            getSupportActionBar().setTitle("Inward Barcode Scan");
        } else if (type.contentEquals("outward")) {
            getSupportActionBar().setTitle("Outward Barcode Scan");
        } else if (type.contentEquals("delivery")) {
            getSupportActionBar().setTitle("Delivery Barcode Scan");
        } else if (type.contentEquals("empty")) {
            getSupportActionBar().setTitle("Empty Barcode Scan");
        }




        recyclerView = findViewById(R.id.recyclerView);
        synchelper = new syncHelper(LaserScannerActivity.this);
        addClyHelper=new AddClyHelper(LaserScannerActivity.this);

        Totalscanvalue = findViewById(R.id.Totalscanvalue);
        Filled_with_Recycle_View = findViewById(R.id.fillwithrec);
        doneBtn = findViewById(R.id.doneBtn);
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        cylinder = new ArrayList<String>();
        is_scan = new ArrayList<>();

        myDB = new InWardDatabaseHelper(LaserScannerActivity.this);
        delidb = new deliDB(LaserScannerActivity.this);
        outwatdMyDB = new MyDatabaseHelper(LaserScannerActivity.this);

        editText = findViewById(R.id.newScan);
        editText.requestFocus();
        closeKeypad(this);
        if (type.equalsIgnoreCase("inward")) {

            storeDataInArrays();
            Totalscanvalue.setText(count);

            customAdapter = new InWardCustomAdapter(LaserScannerActivity.this, this, book_id, book_title,this,"inward");
            recyclerView.setAdapter(customAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(LaserScannerActivity.this));
            recyclerView.scrollToPosition(0);

        } else if (type.equalsIgnoreCase("delivery")) {

            cylIdList = new ArrayList<>();
            cyclinderNameList = new ArrayList<>();
            fillwith = new ArrayList<>();
            name = new ArrayList<>();
            tot = new ArrayList<>();
            volume = new ArrayList<>();
            storedOutwardValues();
            Totalscanvalue.setText(count);
            check();
            Collections.reverse(cylIdList);
            Collections.reverse(cyclinderNameList);
            Collections.reverse(fillwith);

            outwardCustomAdapter = new CustomAdapter(LaserScannerActivity.this, this, cylIdList, cyclinderNameList, fillwith,this,type);
            filledWithAdapter = new FilledWithAdapter(LaserScannerActivity.this, this, name, tot);
            Filled_with_Recycle_View.setLayoutManager(new LinearLayoutManager(LaserScannerActivity.this));
            Filled_with_Recycle_View.setAdapter(filledWithAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(LaserScannerActivity.this));
            recyclerView.setAdapter(outwardCustomAdapter);
            recyclerView.scrollToPosition(0);



        } else if (type.equalsIgnoreCase("empty")) {

            storeDataInArrays();
            Totalscanvalue.setText(count);

            customAdapter = new InWardCustomAdapter(LaserScannerActivity.this, this, book_id, book_title,this,"empty");
            recyclerView.setAdapter(customAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(LaserScannerActivity.this));
            recyclerView.scrollToPosition(0);



        } else if (type.equalsIgnoreCase("outward")) {

            cylIdList = new ArrayList<>();
            cyclinderNameList = new ArrayList<>();
            fillwith = new ArrayList<>();
            name = new ArrayList<>();
            tot = new ArrayList<>();
            volume = new ArrayList<>();
            storedOutwardValues();
            Totalscanvalue.setText(count);
            check();
            outwardCustomAdapter = new CustomAdapter(LaserScannerActivity.this, this, cylIdList, cyclinderNameList, fillwith,this,type);
            filledWithAdapter = new FilledWithAdapter(LaserScannerActivity.this, this, name, tot);
            Filled_with_Recycle_View.setLayoutManager(new LinearLayoutManager(LaserScannerActivity.this));
            Filled_with_Recycle_View.setAdapter(filledWithAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(LaserScannerActivity.this));
            recyclerView.setAdapter(outwardCustomAdapter);
            recyclerView.scrollToPosition(0);



        }


        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void check() {

        Cursor cursor = null;

        if (type.equalsIgnoreCase("delivery")) {
            cursor = delidb.readAllDataInFIFOOrder();
        } else if (type.equalsIgnoreCase("outward")) {
            cursor = outwatdMyDB.readAllDataWithoutOrder();
        }

        if (cursor.getCount() == 0) {
        } else {
            while (cursor.moveToNext()) {
                name.add(cursor.getString(3));
                volume.add(cursor.getString(4));
                tot.add(cursor.getString(2));

            }
        }
    }


    void storeDataInArrays() {
        Cursor cursor = null;

        if (type.equalsIgnoreCase("inward")) {
            cursor = myDB.readAllDataWithoutOrder();
        } else if (type.equalsIgnoreCase("empty")) {
            cursor = addClyHelper.readAllDataWithoutOrder();
        }

        if (cursor != null) {
            if (cursor.getCount() == 0) {
                // Handle case where no data is present
            } else {
                while (cursor.moveToNext()) {
                    book_id.add(cursor.getString(0));
                    book_title.add(cursor.getString(1));
                    cylinder.add(cursor.getString(1));
                    is_scan.add(cursor.getString(4));
                }
                int cou = cursor.getCount();
                count = String.valueOf(cou);
            }
            cursor.close();
        } else {
            // Handle case where cursor is null, if necessary
        }
    }

    void storedOutwardValues() {
        Cursor cursor = null;

        if (type.equalsIgnoreCase("delivery")) {
            cursor = delidb.readAllDataInFIFOOrder();
        } else if (type.equalsIgnoreCase("outward")) {
            cursor = outwatdMyDB.readAllData();
        }

        if (cursor != null) {
            if (cursor.getCount() == 0) {
                // no_data.setVisibility(View.VISIBLE);
            } else {
                while (cursor.moveToNext()) {
                    cylIdList.add(cursor.getString(0));
                    cyclinderNameList.add(cursor.getString(1));
                    fillwith.add(cursor.getString(2));
                    cylinder.add(cursor.getString(1));
                    is_scan.add(cursor.getString(4));
                }
                int cou = cursor.getCount();
                count = String.valueOf(cou);
                // no_data.setVisibility(View.GONE);
            }
            cursor.close();
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

            Log.d("load", editText.getText().toString() + "");
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
            if (type.equalsIgnoreCase("inward")) {

                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);

                        if (col1.contentEquals(displayValue)) {
                            status = false;
                            myDB.addBook(col, "B");
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


            }else if (type.equalsIgnoreCase("empty")) {

                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        Log.e("col1", "An exception occurred: " + col1 + " " + displayValue);

                        if (col1.contentEquals(displayValue)) {
                            status = false;
                            addClyHelper.addBook(col,"B");
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
            } else if (type.equalsIgnoreCase("delivery")) {
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
                        Log.e("col1", "An exception occurred: " + col1 + " " + col);

                        if (col1.contentEquals(displayValue)) {
                            delidb.addBook(col, Fillwith, volume,"B");
                            finish();
                            startActivity(getIntent());
                            break;
                        }
                    }
                    editText.setText("");
                    editText.requestFocus();
                }
            }else {
                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                } else {
                    while (cursor.moveToNext()) {
                        String volume = cursor.getString(4);
                        String Fillwith = cursor.getString(5);
                        String col1 = cursor.getString(2);
                        String col = cursor.getString(1);

                        Log.e("col1", "An exception occurred: " + col1 + " " + displayValue);

                        if (col1.contentEquals(displayValue)) {
                            status = false;
                            editText.setText("");
                            editText.requestFocus();

                            Log.e("col1", "An exception occurred: " + col1 + " " + displayValue);

                            outwatdMyDB.addBook(col, Fillwith, volume, "B");
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
            dis = intent.getExtras().getString("dis", "");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Create a new Bundle object
        Bundle bundle = new Bundle();

        // Put the type and dis variables into the bundle
        bundle.putString("type", type);
        bundle.putString("dis", dis);

        // Put the bundle into the savedInstanceState
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