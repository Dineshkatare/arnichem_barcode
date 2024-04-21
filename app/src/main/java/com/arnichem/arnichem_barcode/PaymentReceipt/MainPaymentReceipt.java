package com.arnichem.arnichem_barcode.PaymentReceipt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.CustomerSearchHandler;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPaymentReceipt extends AppCompatActivity {
    TextView usernamevalue,date;
    EditText ammountValue;
    Spinner customerspinner,amountspinner,spinnerparticular;
    ArrayAdapter<CharSequence> adapter;
    ArrayAdapter<CharSequence> particularadapter;
    AutoCompleteTextView autoCompleteTextView;
    SharedPreferences pref;
    FusedLocationProviderClient fusedLocationProviderClient;
    ProgressDialog dialog;
    String s,srno,amountType,particularStr;
    String custname,cust_code;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    public  int particularpos,amountTypepos,poscust;
    Button submit;
    boolean show=false;
    DatabaseHandler databaseHandlercustomer;
    com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;
    static JSONObject object =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_payment_receipt);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Payment Receipt");
        final RequestQueue requestQueue= Volley.newRequestQueue(this);
        pref = getSharedPreferences(constant.TAG,MODE_PRIVATE);
        spinnerparticular=findViewById(R.id.particular);
        databaseHandlercustomer=new DatabaseHandler(MainPaymentReceipt.this);
        fromloccodehandler=new fromloccodehandler(MainPaymentReceipt.this);
        adapter= ArrayAdapter.createFromResource(this,R.array.ammounttype, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        particularadapter= ArrayAdapter.createFromResource(this,R.array.particulars, android.R.layout.simple_spinner_item);
        particularadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainPaymentReceipt.this);
        autoCompleteTextView=findViewById(R.id.duradelivercylinder);
        ammountValue=findViewById(R.id.amountval);
        customerspinner=findViewById(R.id.customerspinner);
        amountspinner=findViewById(R.id.amountspinner);
        submit=findViewById(R.id.PaymentSubmitBtn);
        submit.setEnabled(true);

        submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                submit.setEnabled(false);

                postUsingVolley();
            }
        });
        amountspinner.setAdapter(adapter);
        spinnerparticular.setAdapter(particularadapter);
        loadSpinnerData();
        usernamevalue=findViewById(R.id.usernametxtvalue);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName());
        date=findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);

        amountspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                ///  Log.v("item", (String) parent.getItemAtPosition(position));
                amountType=(String) parent.getItemAtPosition(position);
               amountTypepos=position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        spinnerparticular.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                particularStr=(String) parent.getItemAtPosition(position);
                particularpos=position;


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });




//        submit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                postUsingVolley();
//            }
//        });
//        s=getIntent().getStringExtra("result");



        customerspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                custname = customerdataAdapter.getItem(position);
                poscust=position;
                SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(poscust));
                Cursor cursor = databaseHandlercustomer.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col=cursor.getString(1);
                        String col1 =cursor.getString(2);
                        String invoice =cursor.getString(3);



                        if(col.contentEquals(custname))
                        {
                            if(invoice.equalsIgnoreCase(
                                    "Y"))
                            {
                                showAlertDialogButtonClicked(view);
                            }
                            cust_code=col1;


                        }
                    }
                }


                poscust=position;


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    public void showAlertDialogButtonClicked(View view) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
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
        customerdataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        customerdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        customerspinner.setAdapter(customerdataAdapter);
    }


    private void postUsingVolley() {
        if (ActivityCompat.checkSelfPermission(MainPaymentReceipt.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


            dialog = new ProgressDialog(MainPaymentReceipt.this);
            dialog.setTitle("Data Inserting");
            dialog.setMessage("Please wait....");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            dialog.show();
//            if (posloc == 0) {
//                dialog.dismiss();
//                MDToast.makeText(MainPaymentReceipt.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
//
//            } else if (poscust == 0) {
//                dialog.dismiss();
//                MDToast.makeText(MainPaymentReceipt.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
//
//
//            }  else {

            if (poscust == 0) {
                dialog.dismiss();
                submit.setEnabled(true);
                MDToast.makeText(MainPaymentReceipt.this, "कृपया ग्राहक निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            }
            else if(amountTypepos == 0) {
                dialog.dismiss();
                submit.setEnabled(true);
                MDToast.makeText(MainPaymentReceipt.this, "कृपया amountType निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            }
            else if(particularpos == 0) {
                submit.setEnabled(true);
                dialog.dismiss();
                MDToast.makeText(MainPaymentReceipt.this, "कृपया Particulars निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            }else if (ammountValue.getText().toString().isEmpty()) {
                dialog.dismiss();
                submit.setEnabled(true);
                MDToast.makeText(MainPaymentReceipt.this, "कृपया Amount टाका  !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            }else {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.payment_recipt_entry,
                        new Response.Listener<String>() {
                            @SuppressLint("WrongConstant")
                            @Override
                            public void onResponse(String response) {
                                try {
                                    submit.setEnabled(true);
                                    JSONArray array = new JSONArray(response);
                                    for (int i = 0; i < array.length(); i++) {
                                        object = array.getJSONObject(i);
                                        String status = object.getString("status");
                                        String msg = object.getString("msg");


                                        if (status.equals("success")) {

                                            srno = object.getString("srno");
                                            ;
                                            MDToast.makeText(MainPaymentReceipt.this, " Payment Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                            Intent intent = new Intent(MainPaymentReceipt.this, PaymentPrint.class);
                                            intent.putExtra("custname", custname);
                                            intent.putExtra("custcode", cust_code);
                                            intent.putExtra("amountstr", ammountValue.getText().toString());
                                            intent.putExtra("paymentstr", amountType);
                                            intent.putExtra("particularStr", particularStr);
                                            intent.putExtra("srno", srno);
                                            startActivity(intent);
                                            submit.setVisibility(View.GONE);
                                            dialog.dismiss();

                                        } else {
                                            dialog.dismiss();

                                        }

                                        Log.e("JSON", "> " + status + msg);
                                    }


                                } catch (JSONException e) {
                                    dialog.dismiss();
                                    submit.setEnabled(true);
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @SuppressLint("WrongConstant")
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                dialog.dismiss();
                                submit.setEnabled(true);
                                MDToast.makeText(MainPaymentReceipt.this, "कृपया इंटरनेट तपासा " + error, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                                error.printStackTrace();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("description", particularStr);
                        params.put("mode", amountType);
                        params.put("cust_code", cust_code);
                        params.put("amount", ammountValue.getText().toString());
                        params.put("email", SharedPref.getInstance(MainPaymentReceipt.this).getEmail());
                        params.put("db_host",SharedPref.mInstance.getDBHost());
                        params.put("db_username",SharedPref.mInstance.getDBUsername());
                        params.put("db_password",SharedPref.mInstance.getDBPassword());
                        params.put("db_name",SharedPref.mInstance.getDBName());
                        return params;
                    }
                };
                VolleySingleton.getInstance(MainPaymentReceipt.this).addToRequestQueue(stringRequest);
//            }
            }
        } else
        {
            submit.setEnabled(true);
            ActivityCompat.requestPermissions(MainPaymentReceipt.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);

        }
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){

        }
    }





    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Destroy", "onDestroy: ");

    }
}

