package com.arnichem.arnichem_barcode.Producation.HydroTest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint.DeliveryPrintDB;
import com.arnichem.arnichem_barcode.Producation.DryIce.DryIceFIrstScreen;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.InventoryGases;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HydroMain extends AppCompatActivity {


    private EditText serialNumberEditText;
    private EditText cylinderIdEditText;
    private EditText manufacturerEditText;
    private Spinner gasTypeSpinner;

    private EditText waterCapcityEditText;


    private EditText ownerEditText;
    private EditText tareWeightEditText;
    private EditText actualWeightEditText;
    private Spinner internalSpinner;
    private EditText c1EditText;
    private EditText c2EditText;
    private EditText c3EditText;
    private EditText weightOfCylinderEditText;
    private EditText waterTempEditText;
    private Button saveButton;

     private ScrollView scannerviewmain;

     private ProgressDialog dialog;
    public Spinner spinmm;
    public Spinner spinyyyy;

    String Selectedmm,Selectedyy,selectedItem="";
    private ArrayAdapter<String> spinnerAdapter;
    private String[] spinnerData = {"SELECT INTERNAL","OK", "NOT OK"};
    ArrayAdapter<String> customerdataAdapter;

    private String gasTypeString = "";
    private String gasItemCode = "";

    private InventoryGases inventoryGases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hydro_test_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("HydroTest");
        serialNumberEditText = findViewById(R.id.edserialnumber);
        cylinderIdEditText = findViewById(R.id.edcylinderid);
        manufacturerEditText = findViewById(R.id.manufacturerval);
        waterCapcityEditText = findViewById(R.id.waterCapcityEdt);
        gasTypeSpinner = findViewById(R.id.edgastype);
        ownerEditText = findViewById(R.id.edowner);
        tareWeightEditText = findViewById(R.id.edtarewt);
        actualWeightEditText = findViewById(R.id.edactualwt);
        internalSpinner = findViewById(R.id.spininternal);
        c1EditText = findViewById(R.id.edc1);
        c2EditText = findViewById(R.id.edc2);
        c3EditText = findViewById(R.id.edc3);
        waterTempEditText = findViewById(R.id.water_temp_val);
        weightOfCylinderEditText = findViewById(R.id.weight_of_cylinder_val);
        saveButton = findViewById(R.id.savebutton);
        scannerviewmain= findViewById(R.id.scannerviewmain);
        spinmm = findViewById(R.id.spinmm);
        spinyyyy = findViewById(R.id.spinyyyy);
        inventoryGases=new InventoryGases(this);


        String[] items1= new String[]{"00","01", "02", "03", "04", "05", "06","07", "08", "09", "10", "11", "12"};

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items1);

        spinmm.setAdapter(adapter1);

        spinmm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Selectedmm = adapter1.getItem(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String[] items2= new String[]{"00000","2010", "2011", "2012", "2013", "2014", "2015","2016", "2017", "2018", "2019","2020", "2021", "2022", "2023", "2024", "2025","2026", "2027", "2028", "2029", "2030","2031"};

        ArrayAdapter<String> adapter2= new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items2);

        spinyyyy.setAdapter(adapter2);

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerData);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        internalSpinner.setAdapter(spinnerAdapter);


        spinyyyy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Selectedyy = adapter2.getItem(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        internalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 selectedItem = parent.getItemAtPosition(position).toString();
                // Do something with the selected item
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when nothing is selected
            }
        });

        gasTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gasTypeString = customerdataAdapter.getItem(position);
                Cursor cursor = inventoryGases.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        if(gasTypeString.equalsIgnoreCase("gas type निवडा")){

                        }else {
                            String col = cursor.getString(0);
                            String col1 = cursor.getString(1);
                            if (col.contentEquals(gasTypeString)) {
                                gasItemCode = col1;
                            }
                        }
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputFields()) {
                    // Get input values
                    postUsingVolley();
                    // Prepare the date value

                    // Example: Call the insertRecord method
                  //  ApiHandler.insertRecord(formattedDate, serialNumber, cylinderId, manufacturer, /* other parameters */);

                    // Clear input fields after successful submission
                }
            }
        });
        loadSpinnerData();

        // Add your logic and event handlers here

    }

    private boolean validateInputFields() {

        String serialNumber = serialNumberEditText.getText().toString().trim();
        String cylinderId = cylinderIdEditText.getText().toString().trim();
        String manufacturer = manufacturerEditText.getText().toString().trim();
        String waterCapcity = waterCapcityEditText.getText().toString().trim();
        String owner = ownerEditText.getText().toString().trim();
        String tareWeight = tareWeightEditText.getText().toString().trim();
        String actualWeight = actualWeightEditText.getText().toString().trim();
        String c1 = c1EditText.getText().toString().trim();
        String c2 = c2EditText.getText().toString().trim();
        String c3 = c3EditText.getText().toString().trim();
        String weight_of_cylinder = weightOfCylinderEditText.getText().toString().trim();
        String water_temp = waterTempEditText.getText().toString().trim();



        if (serialNumber.isEmpty()) {
            showToast("Please enter the serial number");
            return false;
        }

        if (cylinderId.isEmpty()) {
            showToast("Please enter the cylinder ID");
            return false;
        }

        if (manufacturer.isEmpty()) {
            showToast("Please enter the manufacturer");
            return false;
        }

        if (gasItemCode.isEmpty()) {
            showToast("Please select the gas type");
            return false;
        }

        if (waterCapcity.isEmpty()) {
            showToast("Please enter the water capacity");
            return false;
        }

        if (owner.isEmpty()) {
            showToast("Please enter the owner");
            return false;
        }

        if (tareWeight.isEmpty()) {
            showToast("Please enter the tare weight");
            return false;
        }

        if (actualWeight.isEmpty()) {
            showToast("Please enter the actual weight");
            return false;
        }

        if (c1.isEmpty()) {
            showToast("Please enter the C1 value");
            return false;
        }

        if (c2.isEmpty()) {
            showToast("Please enter the C2 value");
            return false;
        }

        if (c3.isEmpty()) {
            showToast("Please enter the C3 value");
            return false;
        }

        if (weight_of_cylinder.isEmpty()) {
            showToast("Please enter the weight of cylinder with water value");
            return false;
        }


        if (water_temp.isEmpty()) {
            showToast("Please enter the water temperature value");
            return false;
        }


        if(selectedItem.isEmpty()){
            showToast("Please select internal");
            return false;

        }else {
            if (selectedItem.equalsIgnoreCase("SELECT INTERNAL")){
                showToast("Please select internal");
                return false;

            }

        }

        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void postUsingVolley() {
        dialog = new ProgressDialog(HydroMain.this);
        dialog.setTitle("Login");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

        // Creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(HydroMain.this);
        StringRequest request = new StringRequest(Request.Method.POST, APIClient.hydro_test, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.dismiss();
                try {
                    JSONObject jsonResponse = new JSONObject(response);

                    // Access the values from the JSON object
                    String status = jsonResponse.getString("status");
                    String msg = jsonResponse.getString("message");

                    // Use the status and message as needed
                    if (status.equalsIgnoreCase("success")) {
                        showAlert(true, msg);
                    } else {
                        showAlert(false, msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Method to handle errors
                Toast.makeText(HydroMain.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("enterDate",Selectedyy+"-"+Selectedmm+"-"+"01");
                params.put("serialNumber", serialNumberEditText.getText().toString());
                params.put("cylinderId", cylinderIdEditText.getText().toString());
                params.put("manufacturer", manufacturerEditText.getText().toString());
                params.put("gasType", gasItemCode);
                params.put("owner", ownerEditText.getText().toString());
                params.put("water_capcity", waterCapcityEditText.getText().toString());
                params.put("tareWeight", tareWeightEditText.getText().toString());
                params.put("actualWeight", actualWeightEditText.getText().toString());
                params.put("internalCheck", selectedItem);
                params.put("c1", c1EditText.getText().toString());
                params.put("c2", c2EditText.getText().toString());
                params.put("c3", c3EditText.getText().toString());
                params.put("water_weight", weightOfCylinderEditText.getText().toString());
                params.put("water_temp", waterTempEditText.getText().toString());
                params.put("email", SharedPref.mInstance.getEmail());
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };

        // Adding the request to the queue
        queue.add(request);
    }

    private void showAlert(boolean val,String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HydroMain.this);
        if(val){
            builder.setTitle("Success")
                    .setMessage(msg)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Handle OK button click
                            finish();
                        }
                    })
                    .setCancelable(false) // Set dialog to not cancelable

                    // Optional: Add more buttons or customize the dialog further

                    .show();

        }else {
            builder.setTitle("Failed")
                    .setMessage("This is an alert dialog")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Handle OK button click
                        }
                    })
                    // Optional: Add more buttons or customize the dialog further

                    .show();

        }
    }


    private void loadSpinnerData() {
        InventoryGases db = new InventoryGases(getApplicationContext());
        List<String> labels = db.getAllLabels();
        labels.add(0,"gas type निवडा");

        // Creating adapter for spinner
        customerdataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        customerdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //  data adapter to spinner
        gasTypeSpinner.setAdapter(customerdataAdapter);
    }

}
