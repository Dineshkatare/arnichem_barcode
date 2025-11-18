package com.arnichem.arnichem_barcode.GodownView.Closing_stock.NewClosingStock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.GodownView.Closing_stock.Closing_Adapter;
import com.arnichem.arnichem_barcode.GodownView.Closing_stock.Closing_stock;
import com.arnichem.arnichem_barcode.GodownView.Closing_stock.closing_stock_print;
import com.arnichem.arnichem_barcode.GodownView.GodownFullRecipt.FullReciptMain;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery.DuraDeliveryMain;
import com.arnichem.arnichem_barcode.TransactionsView.Liquid_Delivery.LiquidDel_main;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.InventoryGases;
import com.arnichem.arnichem_barcode.view.LocationHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClosingStockMain extends AppCompatActivity {
  InventoryGases inventoryGases;
  RecyclerView editableRecycle;
  ClosingStockAdapter closingStockAdapter;
  List<String> arrayList;
  public static ArrayList<ClosingModel> closingModelList = new ArrayList<>();
  com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;
  Button dieselSubmitBtn, printButton;
  TextView usernamevalue, date;
  SharedPreferences pref;
  ArrayAdapter<String> dataAdapter;
  Spinner spinnerWareHouse;
  public int poslocfixdel;
  ProgressDialog dialog;
  List<String> gasType;
  List<String> fullWt;
  List<String> empWt;
  static JSONObject object = null;
  String from_warehouse, from_code, srno;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_closing_stock_main);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle("Stock");
    inventoryGases = new InventoryGases(ClosingStockMain.this);
    editableRecycle = findViewById(R.id.editableRecycle);
    dieselSubmitBtn = findViewById(R.id.dieselSubmitBtn);
    dieselSubmitBtn.setEnabled(true);
    usernamevalue = findViewById(R.id.usernametxtvalue);
    spinnerWareHouse = findViewById(R.id.spinnerWareHouse);


    fromloccodehandler = new fromloccodehandler(ClosingStockMain.this);
    date = findViewById(R.id.date);
    String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
    date.setText(currentDateTimeString);
    pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
    usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());

    arrayList = new ArrayList<>();
    gasType = new ArrayList<>();
    fullWt = new ArrayList<>();
    empWt = new ArrayList<>();

    Cursor cursor = inventoryGases.readAllData();
    if (cursor.getCount() == 0) {
    } else {
      closingModelList.clear();
      arrayList.clear();
      while (cursor.moveToNext()) {
        String col = cursor.getString(0);
        ClosingModel closingModel = new ClosingModel();
        closingModel.setGasType(col);
        closingModel.setFull_Wt("");
        closingModel.setEmp_wt("");
        closingModelList.add(closingModel);
        arrayList.add(col);
        Log.d("gastype", "onCreate: " + col);
      }
    }
    fetchData();
    closingStockAdapter = new ClosingStockAdapter(ClosingStockMain.this);
    editableRecycle.setAdapter(closingStockAdapter);
    editableRecycle.setLayoutManager(new LinearLayoutManager(ClosingStockMain.this));

    spinnerWareHouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        from_warehouse = dataAdapter.getItem(position);
        poslocfixdel = position;
        SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(poslocfixdel));
        Cursor cursor = fromloccodehandler.readAllData();
        if (cursor.getCount() == 0) {
        } else {
          while (cursor.moveToNext()) {
            String col = cursor.getString(1);
            String col1 = cursor.getString(2);
            if (col.contentEquals(from_warehouse)) {
              from_code = col1;

            }
          }
        }




      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });
    dieselSubmitBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        gasType.clear();
        fullWt.clear();
        empWt.clear();
        for (ClosingModel closingModel : closingModelList) {
          gasType.add(closingModel.getGasType());
          if (closingModel.getFull_Wt() == null && closingModel.getFull_Wt().isEmpty()) {
            fullWt.add("0");
          } else {
            fullWt.add(closingModel.getFull_Wt());
          }
          if (closingModel.getEmp_wt() == null && closingModel.getEmp_wt().isEmpty()) {
            empWt.add("0");
          } else {
            empWt.add(closingModel.getEmp_wt());
          }
        }
        postUsingVolley();
      }
    });


  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  private void fetchData() {
    fromloccodehandler db = new fromloccodehandler(getApplicationContext());
    List<String> labels = db.getAllLabels();
    dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerWareHouse.setAdapter(dataAdapter);
    if (poslocfixdel != 0) {
      spinnerWareHouse.setSelection(poslocfixdel);
    }
  }

  private void postUsingVolley() {
    dialog = new ProgressDialog(ClosingStockMain.this);
    dialog.setTitle("Data Inserting");
    dialog.setMessage("Please wait....");
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.show();
    if (poslocfixdel == 0) {
      dialog.dismiss();
      dieselSubmitBtn.setEnabled(true);
      MDToast.makeText(ClosingStockMain.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
    } else {
      StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.closing_stock_entry,
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
                      srno = object.getString("srno");
                      if (status.equals("success")) {
                        MDToast.makeText(ClosingStockMain.this, "Closing Stcok Entry Done!"+srno, MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                        dialog.dismiss();
                        dieselSubmitBtn.setVisibility(View.GONE);
                        Intent intent = new Intent(ClosingStockMain.this, ClosingPrint.class);
                        intent.putExtra("empb", srno);
                        intent.putExtra("warehouse", from_warehouse);
                        dieselSubmitBtn.setEnabled(true);

                        startActivity(intent);
                         finish();
                      } else {
                        dieselSubmitBtn.setEnabled(true);
                        dialog.dismiss();
                      }

                    }
                  } catch (JSONException e) {
                    dialog.dismiss();
                    dieselSubmitBtn.setEnabled(true);

                    e.printStackTrace();
                  }
                }
              },
              new Response.ErrorListener() {
                @SuppressLint("WrongConstant")
                @Override
                public void onErrorResponse(VolleyError error) {
                  dialog.dismiss();
                  dieselSubmitBtn.setEnabled(true);

                  error.printStackTrace();
                }
              }) {
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
          Map<String, String> params = new HashMap<>();
          params.put("gasType", String.valueOf(gasType));
          params.put("fullWt", String.valueOf(fullWt));
          params.put("empWt", String.valueOf(empWt));
          params.put("warehouse", from_code);
          params.put("email", SharedPref.getInstance(ClosingStockMain.this).getEmail());
          params.put("db_host",SharedPref.mInstance.getDBHost());
          params.put("db_username",SharedPref.mInstance.getDBUsername());
          params.put("db_password",SharedPref.mInstance.getDBPassword());
          params.put("db_name",SharedPref.mInstance.getDBName());
          return params;
        }
      };
      VolleySingleton.getInstance(ClosingStockMain.this).addToRequestQueue(stringRequest);
    }
  }

}