package com.arnichem.arnichem_barcode.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.google.android.material.snackbar.Snackbar;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class loading extends AppCompatActivity {
    syncHelper sync;
    DatabaseHandler databaseHandlercustomer;
    DistributorHelper distributorHelper;
    bp_contact_handler bp_contact_handler;
    LocationHandler locationHandler;
    VehicleHandler vehicleHandler;
    CylinderSearch cylinderSearch;
    CustomerSearchHandler customerSearchHandler;
    Delivery_type_liquid_Handler delivery_type_liquidHandler;
    fromloccodehandler fromloccodehandler;
    DurasyncHelper durasyncHelper;
    InventoryGases inventoryGases;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);


        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                init();
            }
        }, 1000);

    }



    private void init() {

        sync = new syncHelper(loading.this);
        durasyncHelper=new DurasyncHelper(loading.this);
        cylinderSearch=new CylinderSearch(loading.this);
        databaseHandlercustomer=new DatabaseHandler(loading.this);
        locationHandler= new LocationHandler(loading.this);
        distributorHelper=new DistributorHelper(loading.this);
        bp_contact_handler= new bp_contact_handler(loading.this);
        vehicleHandler=new VehicleHandler(loading.this);
        fromloccodehandler=new fromloccodehandler(loading.this);
        customerSearchHandler=new CustomerSearchHandler(loading.this);
        delivery_type_liquidHandler=new Delivery_type_liquid_Handler(loading.this);
        inventoryGases =new InventoryGases(loading.this);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {

                sync.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {

                durasyncHelper.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                databaseHandlercustomer.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                locationHandler.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                distributorHelper.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                vehicleHandler.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                fromloccodehandler.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                cylinderSearch.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                customerSearchHandler.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                delivery_type_liquidHandler.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                bp_contact_handler.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                inventoryGases.deleteAllData();
            }
        });


        fetchinsert();
        fetchdurainsert();
        fetchDistributor();
        fetchbp_contact();
        fetchcustomerlist();
        fetchcust();
        fetchloc();
        fetchvehicle();
        fetchcylindersearch();
        delivery_type_liuid();
        fetchfromlocationcode();
        fetchAllInventoryGases();
    }

    private void fetchAllInventoryGases() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://arnichem.co.in/intranet/barcode/APP/fetchInventoryGases.php";
        final JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject ob=response.getJSONObject(0);
                    String msg=ob.getString("status");
                    if(msg.equalsIgnoreCase("success"))
                    {
                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            public void run() {
                                for(int i=1;i<response.length();i++)
                                {
                                    JSONObject ob1= null;
                                    try {
                                        ob1 = response.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        inventoryGases.addGas(ob1.getString("gasName"),ob1.getString("item_code"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });
                        dialog.dismiss();
                    }
                    else {

                    }
                }catch (Exception e)
                {
                    Toast.makeText(loading.this, "catch"+e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(loading.this, "ERROR RESPONSE"+error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(request);
    }

    private void fetchinsert() {

        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        String url="http://arnichem.co.in/intranet/barcode/APP/tesFetchApi.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url,
                new Response.Listener<String>() {
                    @SuppressLint("WrongConstant")

                    @Override
                    public void onResponse(String response) {
                        try {   JSONObject object;
                            JSONArray array = new JSONArray(response);
                            for(int i=0; i < array.length(); i++) {
                                object = array.getJSONObject(i);

                                String msg = object.getString("status");
                                if (msg.equalsIgnoreCase("success")) {

                                    JSONObject finalObject = object;
                                    Executors.newSingleThreadExecutor().execute(new Runnable() {
                                        public void run() {
                                            for (int i = 1; i < finalObject.length(); i++) {
                                                JSONObject ob1 = null;
                                                try {
                                                    ob1 = finalObject.getJSONObject(String.valueOf(i));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

//                                                try {
//                                                    sync.addBook(ob1.getString("item_code"),
//                                                            ob1.getString("barcode"), ob1.getString("weight"), ob1.getString("volume"), ob1.getString("filled_with")
//
//                                                    );
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }

                                            }
                                        }
                                    });
                                    dialog.dismiss();
                                } else {

                                }
                            }
                        }catch (Exception e)
                        {
                            Toast.makeText(loading.this, "catch"+e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        MDToast.makeText(loading.this, "कृपया  इंटरनेट  तपासा "+error, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("db_host",SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(loading.this).addToRequestQueue(stringRequest);

    }

    private void fetchdurainsert() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://arnichem.co.in/intranet/barcode/APP/Duracylindersearch.php";
        final JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject ob=response.getJSONObject(0);
                    String msg=ob.getString("status");
                    if(msg.equalsIgnoreCase("success"))
                    {

                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            public void run() {
                                for(int i=1;i<response.length();i++)
                                {
                                    JSONObject ob1= null;
                                    try {
                                        ob1 = response.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        durasyncHelper.addBook(ob1.getString("item_code"),ob1.getString("barcode"), ob1.getString("weight"),ob1.getString("volume"),ob1.getString("filled_with"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });
                        dialog.dismiss();

                    }
                    else {
                        //     Toast.makeText(Dashboard.this,"else sucess"+ msg, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e)
                {
                    Toast.makeText(loading.this, "catch"+e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(loading.this, "ERROR RESPONSE"+error.toString(), Toast.LENGTH_LONG).show();

            }
        });
        queue.add(request);
    }
    private void fetchloc() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://arnichem.co.in/intranet/barcode/APP/cylinder_location_code_list.php";
        final JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject ob=response.getJSONObject(0);
                    String msg=ob.getString("status");
                    if(msg.equalsIgnoreCase("success"))
                    {

                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            public void run() {
                                for(int i=1;i<response.length();i++)
                                {
                                    JSONObject ob1= null;
                                    try {
                                        ob1 = response.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        locationHandler.insertLabel(ob1.getString("name"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });

                        dialog.dismiss();
                    }
                    else {
                        Toast.makeText(loading.this, msg, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e)
                {
                    Toast.makeText(loading.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
    }
    private void fetchDistributor() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://arnichem.co.in/intranet/barcode/APP/Distributor.php";
        final JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject ob=response.getJSONObject(0);
                    String msg=ob.getString("status");
                    if(msg.equalsIgnoreCase("success"))
                    {
                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            public void run() {
                                for(int i=1;i<response.length();i++)
                                {
                                    JSONObject ob1= null;
                                    try {
                                        ob1 = response.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        distributorHelper.addcust(ob1.getString("name"),ob1.getString("code"));


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });
                        dialog.dismiss();
                    }
                    else {
                        Toast.makeText(loading.this, msg, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e)
                {
                    Toast.makeText(loading.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
    }
    private void fetchbp_contact() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://arnichem.co.in/intranet/barcode/APP/bp_contact_fetch.php";
        final JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject ob=response.getJSONObject(0);
                    String msg=ob.getString("status");
                    if(msg.equalsIgnoreCase("success"))
                    {

                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            public void run() {
                                for(int i=1;i<response.length();i++)
                                {
                                    JSONObject ob1= null;
                                    try {
                                        ob1 = response.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        bp_contact_handler.addcust(ob1.getString("name"),ob1.getString("code"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });

                        dialog.dismiss();
                    }
                    else {
                        Toast.makeText(loading.this, msg, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e)
                {
                    Toast.makeText(loading.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
    }
    private void fetchcust() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://arnichem.co.in/intranet/barcode/APP/businesspartners_fetch_data.php";
        final JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject ob=response.getJSONObject(0);
                    String msg=ob.getString("status");
                    if(msg.equalsIgnoreCase("success"))
                    {


                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            public void run() {
                                for(int i=1;i<response.length();i++)
                                {
                                    JSONObject ob1= null;
                                    try {
                                        ob1 = response.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        databaseHandlercustomer.addcust(ob1.getString("name"),ob1.getString("code"),ob1.getString("invoice"));


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });


                        dialog.dismiss();


                    }
                    else {
                        Toast.makeText(loading.this, msg, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e)
                {
                    Toast.makeText(loading.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
    }

    private void fetchvehicle() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://arnichem.co.in/intranet/barcode/APP/Fetch.php";
        final JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject ob=response.getJSONObject(0);
                    String msg=ob.getString("status");
                    if(msg.equalsIgnoreCase("success"))
                    {

                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            public void run() {
                                for(int i=1;i<response.length();i++)
                                {
                                    JSONObject ob1= null;
                                    try {
                                        ob1 = response.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        vehicleHandler.insertLabel(ob1.getString("name"));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });

                        dialog.dismiss();

                    }
                    else {
                        Toast.makeText(loading.this, msg, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e)
                {
                    Toast.makeText(loading.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
    }


    private void delivery_type_liuid() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://arnichem.co.in/intranet/barcode/APP/liquid_delivery_type.php";
        final JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject ob=response.getJSONObject(0);
                    String msg=ob.getString("status");
                    if(msg.equalsIgnoreCase("success"))
                    {

                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            public void run() {
                                for(int i=1;i<response.length();i++)
                                {
                                    JSONObject ob1= null;
                                    try {
                                        ob1 = response.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        delivery_type_liquidHandler.insertLabel(ob1.getString("name"),ob1.getString("code"),ob1.getString("unit"),ob1.getString("conv_factor"),ob1.getString("HSN"),ob1.getString("GST"));


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });
                        dialog.dismiss();
                    }
                    else {
                        Toast.makeText(loading.this, msg, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e)
                {
                    Toast.makeText(loading.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
    }

    private void fetchcylindersearch() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://arnichem.co.in/intranet/barcode/APP/cylinder_search.php";
        final JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject ob=response.getJSONObject(0);
                    String msg=ob.getString("status");
                    if(msg.equalsIgnoreCase("success"))
                    {


                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            public void run() {
                                for(int i=1;i<response.length();i++)
                                {
                                    JSONObject ob1= null;
                                    try {
                                        ob1 = response.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        cylinderSearch.insertLabel(ob1.getString("name"));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });


                        dialog.dismiss();

                    }
                    else {
                        Toast.makeText(loading.this, msg, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e)
                {
                    Toast.makeText(loading.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
    }

    private void fetchcustomerlist() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://arnichem.co.in/intranet/barcode/APP/customerlist.php";
        final JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject ob=response.getJSONObject(0);
                    String msg=ob.getString("status");
                    if(msg.equalsIgnoreCase("success"))
                    {

                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            public void run() {
                                for(int i=1;i<response.length();i++)
                                {
                                    JSONObject ob1= null;
                                    try {
                                        ob1 = response.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        customerSearchHandler.insertLabel(ob1.getString("name"));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });


                        dialog.dismiss();
                    }
                    else {
                        Toast.makeText(loading.this, msg, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e)
                {
                    Toast.makeText(loading.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
    }

    private void fetchfromlocationcode() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(loading.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://arnichem.co.in/intranet/barcode/APP/fromlocationcode.php";
        final JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject ob=response.getJSONObject(0);
                    String msg=ob.getString("status");
                    if(msg.equalsIgnoreCase("success"))
                    {

                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            public void run() {
                                for(int i=1;i<response.length();i++)
                                {
                                    JSONObject ob1= null;
                                    try {
                                        ob1 = response.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        fromloccodehandler.addcust(ob1.getString("name"),ob1.getString("code"));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });



                        dialog.dismiss();


                        loin();



                    }
                    else {
                        Toast.makeText(loading.this, msg, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e)
                {
                    Toast.makeText(loading.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
    }

    private void loin() {

                Intent intent=new Intent(loading.this,Dashboard.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }


}

