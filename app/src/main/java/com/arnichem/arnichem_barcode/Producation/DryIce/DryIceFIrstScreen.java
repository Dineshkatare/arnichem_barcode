package com.arnichem.arnichem_barcode.Producation.DryIce;

import static android.view.View.GONE;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.Producation.Co2.CO2Filling;
import com.arnichem.arnichem_barcode.Producation.Co2.Co2Print;
import com.arnichem.arnichem_barcode.Producation.Co2.FirstCo2;
import com.arnichem.arnichem_barcode.Producation.Producation_Main;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.DryIce.DryIceDelivery;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DryIceFIrstScreen extends AppCompatActivity {
    TextView date,Startvalue,endvalue;
    int smHour;
    int smMinute;
    int emHour;
    int emMinute;
    String smHourstring;
    String smMinutestring;
    String emHourstring;
    String emMinutestring;
    AutoCompleteTextView before_tank_pressure,before_tank_liquid_liter;
    EditText after_tank_pressure,after_tank_liquid_liter,fillingpessure;
    Button nextbutton;
    String starttime,endtime;
    ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dry_ice_first_screen);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Dry Ice Production");
        date=findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        Startvalue=findViewById(R.id.Startvalue);
        endvalue=findViewById(R.id.endvaluevalue);
        fillingpessure=findViewById(R.id.fillingval);
        after_tank_pressure =findViewById(R.id.EndTankPressurevalue);
        after_tank_liquid_liter=findViewById(R.id.EndTankVolumevalue);
        before_tank_pressure=findViewById(R.id.StartTankPressurevalue);
        before_tank_liquid_liter=findViewById(R.id.StartTankVolumevalue);
        nextbutton=findViewById(R.id.nextpage);
        nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(starttime))
                {
                    MDToast.makeText(DryIceFIrstScreen.this, "कृपया Start Time टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else if(TextUtils.isEmpty(endtime))
                {
                    MDToast.makeText(DryIceFIrstScreen.this, "कृपया End Time टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else if(before_tank_pressure.getText().toString().isEmpty())
                {
                    MDToast.makeText(DryIceFIrstScreen.this, "कृपया Start Tank Pressure टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else if(before_tank_liquid_liter.getText().toString().isEmpty())
                {
                    MDToast.makeText(DryIceFIrstScreen.this, "कृपया Start Tank Volume टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


                }
                else if(after_tank_pressure.getText().toString().isEmpty())
                {
                    MDToast.makeText(DryIceFIrstScreen.this, "कृपया End Tank Pressure टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else if(after_tank_liquid_liter.getText().toString().isEmpty())
                {
                    MDToast.makeText(DryIceFIrstScreen.this, "कृपया End Tank Volume टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


                }


                else if(fillingpessure.getText().toString().isEmpty())
                {
                    MDToast.makeText(DryIceFIrstScreen.this, "कृपया Production Weight टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else {
                    DryICPost();


                }
            }
        });
        Startvalue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tiemPickerstart();
            }
        });
        endvalue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tiemPickerend();
            }
        });
        SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(0));
        SharedPref.getInstance(getApplicationContext()).store_dist(String.valueOf(0));
    }

    private void tiemPickerstart(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        smHour = c.get(Calendar.HOUR_OF_DAY);
        smMinute = c.get(Calendar.MINUTE);
        smHourstring= String.valueOf(smHour);
        smMinutestring=String.valueOf(smMinute);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog1 = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        smHour = hourOfDay;
                        smMinute = minute;
                        int length = (int)(Math.log10(minute)+1);
                        if(length==1)
                        {
                            Startvalue.setText(hourOfDay + ":" +"0"+minute);
                            starttime=hourOfDay + ":"+"0"+minute;

                        }
                        else {
                            Startvalue.setText(hourOfDay + ":" + minute);
                            starttime=hourOfDay + ":" + minute;

                        }

                    }
                }, smHour, smMinute, false);
        timePickerDialog1.show();

    }
    private void tiemPickerend(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        emHour = c.get(Calendar.HOUR_OF_DAY);
        emMinute = c.get(Calendar.MINUTE);
        emHourstring= String.valueOf(emHour);
        emMinutestring=String.valueOf(emMinute);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,int minute) {
                        emHour = hourOfDay;
                        emMinute = minute;
                        int length = (int)(Math.log10(minute)+1);
                        if(length==1)
                        {
                            endvalue.setText(hourOfDay + ":" +"0"+minute);
                            endtime=hourOfDay + ":"+"0"+minute;

                        }
                        else {
                            endvalue.setText(hourOfDay + ":" + minute);
                            endtime=hourOfDay + ":" + minute;

                        }
                    }
                }, emHour, emMinute, false);
        timePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Producation_Main.class);
        startActivity(intent);
    }

    private void DryICPost() {
        progressDialog = new ProgressDialog(DryIceFIrstScreen.this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please wait....");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(DryIceFIrstScreen.this);
        StringRequest request = new StringRequest(Request.Method.POST, APIClient.dry_ice_production_entry, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                progressDialog.dismiss();
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    // Assuming the response contains only one object in the array
                    JSONObject respObj = jsonArray.getJSONObject(0);

                    // Access the values from the JSON object
                    String status = respObj.getString("status");
                    String msg = respObj.getString("msg");

                    // Use the status and message as needed
                    if (status.equalsIgnoreCase("success")) {
                        showAlert(true, msg);
                    } else {
                        showAlert(false, msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // method to handle errors.
                progressDialog.dismiss();
                Toast.makeText(DryIceFIrstScreen.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("starttime",starttime);
                params.put("endtime", endtime);
                params.put("cubic",fillingpessure.getText().toString());
                params.put("AI_qty", "1");
                params.put("dist_qty", "0");
                params.put("after_tank_pressure",after_tank_pressure.getText().toString());
                params.put("after_tank_liquid_liter",after_tank_liquid_liter.getText().toString());
                params.put("before_tank_pressure",before_tank_pressure.getText().toString());
                params.put("before_tank_liquid_liter",before_tank_liquid_liter.getText().toString());
                params.put("supervisor", SharedPref.getInstance(DryIceFIrstScreen.this).Id());
                params.put("email", SharedPref.getInstance(DryIceFIrstScreen.this).getEmail());
                params.put("remarks", "Transaction Through App");
                params.put("batch_prefix",SharedPref.mInstance.getBatchPrefix());
                params.put("db_host",SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        // below line is to make
        // a json object request.
        queue.add(request);
    }

    private void showAlert(boolean val,String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DryIceFIrstScreen.this);
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


}