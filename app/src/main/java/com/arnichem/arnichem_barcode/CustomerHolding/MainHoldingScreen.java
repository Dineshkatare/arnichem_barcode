package com.arnichem.arnichem_barcode.CustomerHolding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.List;

public class MainHoldingScreen extends AppCompatActivity {
    ArrayAdapter<String> customerdataAdapter;
    Spinner customerspinnerdelivery;
    public int poslocfixdel, poscustfixdel;
    DatabaseHandler databaseHandlercustomer;
    String to_warehouse,cust_code;
    Button submitBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_holding_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Customer Holding");
        customerspinnerdelivery = findViewById(R.id.customerspin);
        databaseHandlercustomer = new DatabaseHandler(MainHoldingScreen.this);
        submitBtn = findViewById(R.id.submitBtn);
        loadSpinnerData();
        customerspinnerdelivery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                        String invoice = cursor.getString(3);
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
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(poscustfixdel==0){
                    MDToast.makeText(MainHoldingScreen.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }else {
                    nextScreeen();
                }
            }
        });
    }

    private void nextScreeen() {
        Intent intent = new Intent(MainHoldingScreen.this,WebViewActivity.class);
        intent.putExtra("code",cust_code);
        startActivity(intent);
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}