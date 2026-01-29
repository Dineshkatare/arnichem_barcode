package com.arnichem.arnichem_barcode.Settings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryMainActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.loading;
import com.arnichem.arnichem_barcode.view.login;
import com.chaos.view.PinView;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChanePassword extends AppCompatActivity implements Listener, LocationData.AddressCallBack
{
        Button login;
        RelativeLayout layout;
        ProgressDialog dialog;
        PinView pinView;
        Editable pinvalue;
        String userid, latitude = "0", logitude = "0", address = "0";
        static JSONObject object = null;
        String imeistr = "0";
        String ipstr = "0";
        private EasyWayLocation easyWayLocation;
        GetLocationDetail getLocationDetail;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_chane_password);
        login = findViewById(R.id.btnext);
        pinView = findViewById(R.id.pinview);
        pinView.setHideLineWhenFilled(true);
        pinView.setPasswordHidden(true);
        ipstr = ipget();

        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false, true, this);


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
        layout = findViewById(R.id.relativelay);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });



    }


        @Override
        protected void onResume () {
        super.onResume();
        easyWayLocation.startLocation();
    }

        @Override
        protected void onPause () {
        super.onPause();
        easyWayLocation.endUpdates();

    }

        @Override
        public void locationOn () {
        Toast.makeText(this, "Location On", Toast.LENGTH_SHORT).show();
    }

        @Override
        public void currentLocation (Location location){

        latitude = String.valueOf(location.getLatitude());

        logitude = String.valueOf(location.getLongitude());

        getLocationDetail.getAddress(location.getLatitude(), location.getLongitude(), "xyz");
    }

        @Override
        public void locationCancelled () {
        Toast.makeText(this, "Location Cancelled", Toast.LENGTH_SHORT).show();
    }

        @Override
        public void locationData (LocationData locationData){
        address = locationData.getFull_address();
    }


        private void login () {
        dialog = new ProgressDialog(ChanePassword.this);
        dialog.setTitle("Login");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.access_login,
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
                                    Intent intent = new Intent(ChanePassword.this, newPassword.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    MDToast.makeText(ChanePassword.this, "Login suceesfull!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                                } else {
                                    dialog.dismiss();
                                    Snackbar.make(layout, "invalid username or password", Toast.LENGTH_SHORT).show();

                                }

                                Log.e("JSON", "> " + status + msg);
                            }


                        } catch (JSONException e) {
                            dialog.dismiss();
                            MDToast.makeText(ChanePassword.this, "कृपया  User Id आणि Password  तपासा ", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        MDToast.makeText(ChanePassword.this, "कृपया  इंटरनेट  तपासा " + error, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username",SharedPref.getInstance(ChanePassword.this).getEmail());
                params.put("app_pin", pinvalue.toString());
                params.put("ipaddress", ipstr);
                params.put("imei", imeistr);
                params.put("appversion", "10.0");
                params.put("lati", latitude);
                params.put("logi", logitude);
                params.put("addr", address);
                params.put("db_host",SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(ChanePassword.this).addToRequestQueue(stringRequest);


    }

        private void closekey () {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

        private String ipget () {
        String readline = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL myur = new URL("https://checkip.amazonaws.com/");
            URLConnection connection = myur.openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            readline = in.readLine();

        } catch (Exception e) {
            e.printStackTrace();

        }
        return readline;


    }
    }
