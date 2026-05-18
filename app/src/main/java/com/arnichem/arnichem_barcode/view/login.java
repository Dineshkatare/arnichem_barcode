package com.arnichem.arnichem_barcode.view;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.GetData.Test;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
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
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.messaging.FirebaseMessaging;

public class login extends AppCompatActivity implements Listener, LocationData.AddressCallBack {
  private static final String TAG = "login";
  Button login;
  EditText username;
  private static final int PERMISSION_REQUEST_READ_CALL_LOG = 100;

  RelativeLayout layout;
  ProgressDialog dialog;
  SharedPreferences pref;
  PinView pinView;
  Editable pinvalue;
  String userid, latitude = "0", logitude = "0", address = "0";
  static JSONObject object = null;
  String imeistr = "0";
  String ipstr = "0";
  private EasyWayLocation easyWayLocation;
  GetLocationDetail getLocationDetail;
  ImageView ivLogLogo;

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportActionBar().hide();
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    setContentView(R.layout.activity_login);
    login = findViewById(R.id.btnlogin);
    username = findViewById(R.id.editUsername);
    pinView = findViewById(R.id.pinview);
    ivLogLogo = findViewById(R.id.ivLogLogo);
    pinView.setHideLineWhenFilled(true);
    pinView.setPasswordHidden(true);
    ipstr = ipget();
    getLocationDetail = new GetLocationDetail(this, this);
    easyWayLocation = new EasyWayLocation(this, false, true, this);
    pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
    String strImage = SharedPref.mInstance.getLogo();
    String phoneNumber = SharedPref.mInstance.getPhoneNumber();
    Log.i("dinesh", "phone number" + phoneNumber);
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
        ||
        ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

      // Request both permissions if they are not granted
      ActivityCompat.requestPermissions(this,
          new String[] {
              Manifest.permission.READ_CALL_LOG,
              Manifest.permission.READ_PHONE_STATE
          },
          PERMISSION_REQUEST_READ_CALL_LOG);
    }
    File imgFile = new File(strImage);

    if (imgFile.exists()) {

      Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

      ivLogLogo.setImageBitmap(myBitmap);

    }

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

    userid = username.getText().toString();

    // ivLogLogo.setImageBitmap();
    pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);

    login.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        loginfun();
      }
    });

    if (ActivityCompat.checkSelfPermission(login.this,
        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(login.this,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(login.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);

    } else {

      // Write you code here if permission already given.
    }

  }

  @Override
  protected void onResume() {
    super.onResume();
    easyWayLocation.startLocation();
  }

  @Override
  protected void onPause() {
    super.onPause();
    easyWayLocation.endUpdates();

  }

  @Override
  public void locationOn() {
    Toast.makeText(this, "Location On", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void currentLocation(Location location) {

    latitude = String.valueOf(location.getLatitude());

    logitude = String.valueOf(location.getLongitude());

    getLocationDetail.getAddress(location.getLatitude(), location.getLongitude(), "xyz");
  }

  @Override
  public void locationCancelled() {
    Toast.makeText(this, "Location Cancelled", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void locationData(LocationData locationData) {
    address = locationData.getFull_address();
  }

  private void loginfun() {
    dialog = new ProgressDialog(login.this);
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
                  SimpleDateFormat df = new SimpleDateFormat("dd", Locale.getDefault());
                  String datestr = df.format(new Date());
                  SharedPref.getInstance(getApplicationContext()).storeLoginDate(datestr);
                  SharedPref.getInstance(getApplicationContext()).storeStatus(object.getString("status"));
                  SharedPref.getInstance(getApplicationContext()).storeFName(object.getString("fname"));
                  SharedPref.getInstance(getApplicationContext()).storeLName(object.getString("lname"));
                  SharedPref.getInstance(getApplicationContext()).storeEmail(object.getString("email"));
                  SharedPref.getInstance(getApplicationContext()).storeUserName(username.getText().toString());
                  SharedPref.getInstance(getApplicationContext()).storesuperId(object.getString("id"));
                  SharedPref.getInstance(getApplicationContext())
                      .store_call_log_access(object.getString("call_log_access"));
                  closekey();
                  dialog.dismiss();
                  MDToast.makeText(login.this, "Login suceesfull!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                  // Fetch roles & FCM token BEFORE navigating away so the activity stays alive
                  fetchUserRoles(username.getText().toString());

                } else {
                  dialog.dismiss();
                  MDToast.makeText(login.this, "कृपया  User Id आणि Password  तपासा ", MDToast.LENGTH_SHORT,
                      MDToast.TYPE_ERROR).show();

                }
                Log.e("JSON", "> " + status + msg);
              }

            } catch (JSONException e) {
              dialog.dismiss();
              MDToast.makeText(login.this, "Eroor" + e.toString(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
              e.printStackTrace();
            }
          }
        },
        new Response.ErrorListener() {
          @SuppressLint("WrongConstant")
          @Override
          public void onErrorResponse(VolleyError error) {
            dialog.dismiss();
            MDToast.makeText(login.this, "कृपया  इंटरनेट  तपासा " + error, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR)
                .show();

            error.printStackTrace();
          }
        }) {
      @Override
      protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put("username", username.getText().toString());
        params.put("app_pin", pinvalue.toString());
        params.put("ipaddress", ipstr);
        params.put("imei", imeistr);
        params.put("appversion", "11");
        params.put("lati", latitude);
        params.put("logi", logitude);
        params.put("addr", address);
        params.put("db_host", SharedPref.mInstance.getDBHost());
        params.put("db_username", SharedPref.mInstance.getDBUsername());
        params.put("db_password", SharedPref.mInstance.getDBPassword());
        params.put("db_name", SharedPref.mInstance.getDBName());
        params.put("selected_device", SharedPref.getInstance(login.this).getPersistentDeviceName());
        return params;
      }
    };
    VolleySingleton.getInstance(login.this).addToRequestQueue(stringRequest);

    Log.d(TAG, "db name: " + SharedPref.mInstance.getDBName());

  }

  private void fetchUserRoles(final String usernameStr) {
    Log.d(TAG, "fetchUserRoles called for: " + usernameStr);
    StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.fetch_user_roles,
        response -> {
          Log.d(TAG, "fetchUserRoles response: " + response);
          try {
            JSONObject obj = new JSONObject(response);
            if (obj.getString("status").equals("success")) {
              String roles = obj.getString("roles");
              SharedPref.getInstance(getApplicationContext()).storeRoleKey(roles);
              Log.d(TAG, "Role stored: " + roles);
            }
          } catch (Exception e) {
            Log.e(TAG, "fetchUserRoles parse error: " + e.getMessage());
            e.printStackTrace();
          }
          syncFcmToken();
        },
        error -> {
          Log.e(TAG, "Error fetching roles: " + error.getMessage());
          // Still sync FCM token even if roles fail, then navigate
          syncFcmToken();
        }) {
      @Override
      protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("username", usernameStr);
        params.put("db_host", SharedPref.mInstance.getDBHost());
        params.put("db_username", SharedPref.mInstance.getDBUsername());
        params.put("db_password", SharedPref.mInstance.getDBPassword());
        params.put("db_name", SharedPref.mInstance.getDBName());
        return params;
      }
    };
    VolleySingleton.getInstance(login.this).addToRequestQueue(stringRequest);
  }

  private void syncFcmToken() {
    FirebaseMessaging.getInstance().getToken()
        .addOnCompleteListener(task -> {
          if (!task.isSuccessful()) {
            Log.w("FCM", "Fetching FCM registration token failed", task.getException());
            return;
          }

          // Get new FCM registration token
          String token = task.getResult();
          Log.d("FCM", "Token: " + token);

          SharedPref.getInstance(login.this).storeFcmToken(token);
          sendTokenToServer(token);
        });
  }

  private void sendTokenToServer(String token) {
    String appUsername = username.getText().toString().trim();   // login EditText value
    String roleKey = SharedPref.getInstance(login.this).getRoleKey();

    if (appUsername.isEmpty()) {
      Log.e(TAG, "sendTokenToServer: username is empty, skipping");
      navigateToHome();
      return;
    }

    Log.d(TAG, "sendTokenToServer: username=" + appUsername + " | role=" + roleKey + " | token=" + token);

    StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.register_fcm_token,
        response -> {
          // PHP returns: {"status":"success","message":"Token registered successfully"}
          //           or {"status":"error","message":"..."}
          try {
            JSONObject obj = new JSONObject(response);
            String status = obj.getString("status");
            String message = obj.getString("message");
            if (status.equals("success")) {
              Log.d(TAG, "FCM token registered successfully: " + message);
            } else {
              Log.e(TAG, "FCM token registration error from server: " + message);
            }
          } catch (JSONException e) {
            Log.e(TAG, "FCM token response parse error: " + e.getMessage() + " | raw=" + response);
          }
          navigateToHome();
        },
        error -> {
          Log.e(TAG, "FCM Token registration network error: " + error.getMessage());
          navigateToHome();
        }) {
      @Override
      protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("username", appUsername);          // was "user_id"
        params.put("fcm_token", token);
        params.put("device_type", "android");
        params.put("role_key", roleKey != null ? roleKey : "");
        params.put("db_host", SharedPref.mInstance.getDBHost());
        params.put("db_username", SharedPref.mInstance.getDBUsername());
        params.put("db_password", SharedPref.mInstance.getDBPassword());
        params.put("db_name", SharedPref.mInstance.getDBName());
        return params;
      }
    };
    VolleySingleton.getInstance(login.this).addToRequestQueue(stringRequest);
  }


  private void navigateToHome() {
    Intent intent = new Intent(login.this, Test.class);
    intent.setFlags(
        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
  }

  private void closekey() {
    View view = this.getCurrentFocus();
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  private String ipget() {
    String readline = "";
    try {
      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
      StrictMode.setThreadPolicy(policy);
      URL myur = new URL("https://checkip.amazonaws.com/");
      URLConnection connection = myur.openConnection();
      connection.setConnectTimeout(1000);
      connection.setReadTimeout(1000);
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      readline = in.readLine();

    } catch (Exception e)

    {
      e.printStackTrace();

    }
    return readline;

  }
}