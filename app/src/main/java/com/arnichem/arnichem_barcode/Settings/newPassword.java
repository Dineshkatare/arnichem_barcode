package com.arnichem.arnichem_barcode.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.Company.SelectCompanyActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.login;
import com.chaos.view.PinView;
import com.example.easywaylocation.LocationData;
import com.google.android.material.snackbar.Snackbar;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class newPassword extends AppCompatActivity {
    ProgressDialog dialog;
    PinView pinView;
    Editable pinvalue;
    static JSONObject object =null;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Change Password");
        pinView = findViewById(R.id.pinview);
        button=findViewById(R.id.btnext);
        pinView.setHideLineWhenFilled(true);
        pinView.setPasswordHidden(true);
        pinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pinvalue = s;

            }
        });



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });



    }





    private void login () {
        dialog = new ProgressDialog(newPassword.this);
        dialog.setTitle("Login");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.change_password,
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
                                    closekey();
                                    dialog.dismiss();
                                    SharedPref.getInstance(getApplicationContext()).logout();
                                    startActivity(new Intent(newPassword.this, SelectCompanyActivity.class));
                                    finish();
                                } else {
                                    dialog.dismiss();
                                }

                                Log.e("JSON", "> " + status + msg);
                            }


                        } catch (JSONException e) {
                            dialog.dismiss();
                            MDToast.makeText(newPassword.this, "कृपया  User Id आणि Password  तपासा ", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        MDToast.makeText(newPassword.this, "कृपया  इंटरनेट  तपासा " + error, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username",SharedPref.getInstance(newPassword.this).getEmail());
                params.put("app_pins", pinvalue.toString());
                params.put("db_host",SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(newPassword.this).addToRequestQueue(stringRequest);


    }

    private void closekey () {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}