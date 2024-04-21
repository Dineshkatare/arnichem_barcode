package com.arnichem.arnichem_barcode.Producation;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.Printer.MainPrint;
import com.arnichem.arnichem_barcode.Producation.Co2.CO2Filling;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.Duraempty.duraemptymain;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.DistributorHelper;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.durasearchadapter;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dura extends AppCompatActivity {
    TextView Username,Startvalue,endvalue;
    EditText after_tank_pressure,after_tank_liquid_liter;
    int smHour;
    int smMinute;
    int emHour;
    int emMinute;
    String smHourstring;
    String smMinutestring;
    String emHourstring;
    String emMinutestring;
    Button cal,print;
    Spinner spinnerOwner;
    EditText Grossvalue,Tarevalue,Pressurevalue;
    TextView Netvalue,LOXvalue,CylQuantityvalue,filled_with;
    String netv,loxvv,cylinderqu;
    String s,wei,type;
    String second;
    syncHelper synchelper;
    ProgressDialog progressDialog;
    Button submit;
    String finalAI_qty;
    String finaldist_qty,batch_id;
    ArrayAdapter<CharSequence> adapter;
    int counter=0;
    int distributorpos;
    String distributorname,distributorcode;
    AutoCompleteTextView autoCompleteTextView,before_tank_pressure,before_tank_liquid_liter;
    FloatingActionButton floatingActionButton;
    static JSONObject object =null;
    String startime,endtime;
    ArrayAdapter<String> distributordataAdapter;
    DistributorHelper distributorHelper;
    String temp;
    TextView textViewl;
    RadioGroup radioGroup;
    RadioButton lin,lox,radioButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dura);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Dura Producation");
        LocalBroadcastManager.getInstance(this).registerReceiver(dura_no_receiver,
                new IntentFilter("dura_production"));

        autoCompleteTextView=findViewById(R.id.duraedaicode);
        distributorHelper=new DistributorHelper(this);
        loadata();

        print=findViewById(R.id.printnext);
        synchelper=new syncHelper(Dura.this);
        floatingActionButton=findViewById(R.id.scandura);
        Username=findViewById(R.id.usernametxtvalue);
        CylQuantityvalue=findViewById(R.id.CylQuantityvalue);
        Startvalue=findViewById(R.id.Startvalue);
        endvalue=findViewById(R.id.endvaluevalue);
        Grossvalue=findViewById(R.id.Grossvalue);
        Tarevalue=findViewById(R.id.Tarevalue);
        Netvalue=findViewById(R.id.Netvalue);
        radioGroup=findViewById(R.id.radGroup);
        filled_with = findViewById(R.id.filled_with);
        textViewl=findViewById(R.id.LOXtxt);
        LOXvalue=findViewById(R.id.LOXvalue);
        lox = findViewById(R.id.radBixolon);
        lin = findViewById(R.id.radRongta);
        cal=findViewById(R.id.calsi);
        Pressurevalue=findViewById(R.id.Pressurevalue);
        after_tank_pressure =findViewById(R.id.EndTankPressurevalue);
        after_tank_liquid_liter=findViewById(R.id.EndTankVolumevalue);
        before_tank_pressure=findViewById(R.id.StartTankPressurevalue);
        before_tank_liquid_liter=findViewById(R.id.StartTankVolumevalue);
        print.setVisibility(View.GONE);
        submit=findViewById(R.id.EmptyMainPost);
        submit.setEnabled(true);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit.setEnabled(false);

                Durapost();


            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Dura.this, MainPrint.class);
                i.putExtra("batchDt",batch_id);
                i.putExtra("durano",autoCompleteTextView.getText().toString());
                i.putExtra("deliveryDt","24/0721");
                i.putExtra("grossWt",Grossvalue.getText().toString());
                i.putExtra("tareWt",Tarevalue.getText().toString());
                i.putExtra("netWt",netv);
                i.putExtra("Gas",loxvv);
                i.putExtra("pressure",Pressurevalue.getText().toString());
                startActivity(i);
            }
        });
        lin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewl.setText("LIN Volume");
                filled_with.setText("Liquid Nitrogen");
            }
        });
        lox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                filled_with.setText("Liquid Oxygen");
                textViewl.setText("LOX Volume");

            }
        });
        radioGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click event here
                RadioButton clickedRadioButton = (RadioButton) v;

                // Get the text or ID of the clicked radio button
                int selectedId = clickedRadioButton.getId();
                if(selectedId==R.id.radBixolon){


                }
                if(selectedId==R.id.radRongta){

                }


                // Perform actions based on the clicked radio button
                // For example, you can update UI, show a message, etc.
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(Dura.this, NewScanner.class);
                intent.putExtra("type", "dura_production");
                startActivity(intent);

            }
        });



//        s=getIntent().getStringExtra("result");
 before_tank_liquid_liter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int foo;
                try {
                    foo = Integer.parseInt(before_tank_liquid_liter.getText().toString());
                }
                catch (NumberFormatException e)
                {
                    foo = 0;
                }
                after_tank_liquid_liter.setText(foo-200);
            }
        });
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str=autoCompleteTextView.getText().toString();
                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View
                    //      .VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String cyl=cursor.getString(1);
                        String wei =cursor.getString(3);
                        if(cyl.contentEquals(str))
                        {
                            Tarevalue.setText(wei);
                        }
                    }
                }

            }
        });
        cal.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if(Grossvalue.getText().toString().equalsIgnoreCase(""))
                {
                    MDToast.makeText(Dura.this, "कृपया Gross Weight टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else if(Tarevalue.getText().toString().equalsIgnoreCase(""))
                {
                    MDToast.makeText(Dura.this, "कृपया Tare Weight टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else {
                    int radioId=radioGroup.getCheckedRadioButtonId();
                    radioButton=findViewById(radioId);
                    if(radioButton.getText().toString().equalsIgnoreCase("LOX"))
                    {
                        type="MEDOXDURA";
                        float tv= Float.parseFloat(Tarevalue.getText().toString());
                        float gv= Float.parseFloat(Grossvalue.getText().toString());
                        float nev=gv-tv;
                        float loxv= (float) (nev*.77);
                        float cyliquan=loxv/7;
                        cylinderqu=String.valueOf(cyliquan);
                        netv= String.valueOf(nev);
                        loxvv= String.valueOf(loxv);
                        LOXvalue.setText(loxvv);
                        Netvalue.setText(netv);
                        filled_with.setText("Liquid Oxygen");
                        CylQuantityvalue.setText(cylinderqu);
                    }
                    else
                    {
                        type="LIN";
                        textViewl.setText("LIN Volume");
                        filled_with.setText("Liquid Nitrogen");
                        float tv= Float.parseFloat(Tarevalue.getText().toString());
                        float gv= Float.parseFloat(Grossvalue.getText().toString());
                        float nev=gv-tv;
                        float loxv= (float) (nev*.88);
                        float cyliquan=loxv/7;
                        cylinderqu=String.valueOf(cyliquan);
                        netv= String.valueOf(nev);
                        loxvv= String.valueOf(loxv);
                        LOXvalue.setText(loxvv);
                        Netvalue.setText(netv);
                        CylQuantityvalue.setText(cylinderqu);
                    }
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
        spinnerOwner=findViewById(R.id.spinselectowner);
        loadSpinnerData();
//        adapter= ArrayAdapter.createFromResource(this,R.array.owner, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerOwner.setAdapter(adapter);
        spinnerOwner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                distributorname = distributordataAdapter.getItem(position);
                distributorpos=position;

                SharedPref.getInstance(getApplicationContext()).store_dist(String.valueOf(distributorpos));
                Cursor cursor = distributorHelper.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col=cursor.getString(1);
                        String col1 =cursor.getString(2);
                        if(col.contentEquals(distributorname))
                        {
                            if(distributorname.equalsIgnoreCase(SharedPref.getInstance(Dura.this).getOwnCode())){
                                temp  = SharedPref.getInstance(Dura.this).getOwnCode();
                            }else {
                                temp=col1;

                            }

                        }
                    }
                }


                String t = String.valueOf(temp);
                if(t.equalsIgnoreCase(SharedPref.getInstance(Dura.this).getOwnCode()))
                {
                    finalAI_qty="1";
                    finaldist_qty="0";
                }
                else
                {
                    finaldist_qty="1";
                    finalAI_qty="0";
                }



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Username.setText(SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName());

    }




    private void tiemPickerstart(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        smHour = c.get(Calendar.HOUR_OF_DAY);
        smMinute = c.get(Calendar.MINUTE);
        smHourstring= String.valueOf(smHour);
        smMinutestring=String.valueOf(smMinute);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,int minute) {
                        smHour = hourOfDay;
                        smMinute = minute;
                        int length = (int)(Math.log10(minute)+1);
                        if(length==1)
                        {
                            Startvalue.setText(hourOfDay + ":" +"0"+minute);
                            startime=hourOfDay + ":"+"0"+minute;

                        }
                        else {
                            Startvalue.setText(hourOfDay + ":" + minute);
                            startime=hourOfDay + ":" + minute;

                        }
                    }
                }, smHour, smMinute, false);
        timePickerDialog.show();
    }


    private void loadSpinnerData() {
        DistributorHelper db = new DistributorHelper(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        distributordataAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        distributordataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner

        spinnerOwner.setAdapter(distributordataAdapter);
        if(distributorpos!=0)
        {
            spinnerOwner.setSelection(distributorpos);
        }


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
    //    private void Toast()
//    {
//        Toast.makeText(this,"starttime"+smHour+":"+smMinute+"endtime"+emHour+":"+emMinute+"owner_code"+Selected+"dura_code"+second+    "Full_wt"+Grossvalue.getText().toString()+
//                        "empty_wt"+Tarevalue.getText().toString()+
//                        "net_wt"+netv+
//                        "cubic"+loxvv+
//                        "Full_wt"+Grossvalue.getText().toString()+
//                        "empty_wt"+Tarevalue.getText().toString()+
//                        "net_wt"+netv+
//                        "cubic"+loxvv+ "pressure"+Pressurevalue.getText().toString()+
//                        "cyl_quan"+cylinderqu+          "username"+ SharedPref.getInstance(Dura.this).FirstName()+" "+SharedPref.getInstance(Dura.this).LastName()+
//                        "remarks"+ "Transaction through Arnichem App", Toast.LENGTH_LONG).show(); }
    private void Durapost() {
        progressDialog = new ProgressDialog(Dura.this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please wait....");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(Dura.this);
        StringRequest request = new StringRequest(Request.Method.POST, APIClient.fill_dura_entry, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                //      Snackbar.make(scrollView,"हा सिलेंडर नंबर "+"या बारकोड सोबत रजिस्टर झाला आहे ", Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN).setTextColor(Color.BLACK).show();
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        object = array.getJSONObject(i);
                        String status = object.getString("status");
                        String msg = object.getString("msg");

                        if (status.equals("success")) {
                            print.setVisibility(View.VISIBLE);
                            submit.setVisibility(View.GONE);

                            MDToast.makeText(Dura.this, "तुमची डूरा एन्ट्री झाली आहे!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                            batch_id = object.getString("batch_id");

                            //  SharedPref.getInstance(getApplicationContext()).(object.getString("lname"));
                            progressDialog.dismiss();
                            Intent intent=new Intent(Dura.this, MainPrint.class);
                            intent.putExtra("batchDt",batch_id);
                            intent.putExtra("durano",autoCompleteTextView.getText().toString());
                            intent.putExtra("deliveryDt","24/0721");
                            intent.putExtra("grossWt",Grossvalue.getText().toString());
                            intent.putExtra("tareWt",Tarevalue.getText().toString());
                            intent.putExtra("netWt",netv);
                            intent.putExtra("Gas",loxvv);
                            intent.putExtra("gas_type",filled_with.getText());
                            intent.putExtra("pressure",Pressurevalue.getText().toString());
                            submit.setEnabled(true);

                            startActivity(intent);

                        } else {
                            submit.setEnabled(true);
                            progressDialog.dismiss();

                        }

                        Log.e("JSON", "> " + status + msg);
                    }

                } catch (JSONException e) {
                    submit.setEnabled(true);
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                submit.setEnabled(true);
                // method to handle errors.
                Toast.makeText(Dura.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("starttime",startime);
                params.put("endtime", endtime);
                params.put("owner_code",temp);
                params.put("dura_code",autoCompleteTextView.getText().toString());
                params.put("Full_wt",Grossvalue.getText().toString());
                params.put("empty_wt",Tarevalue.getText().toString());
                params.put("net_wt",netv);
                params.put("cubic",loxvv);
                params.put("pressure",Pressurevalue.getText().toString());
                params.put("cyl_quan",cylinderqu);
                params.put("type",type);
                params.put("AI_qty",finalAI_qty);
                params.put("dist_qty",finaldist_qty);
                params.put("after_tank_pressure",after_tank_pressure.getText().toString());
                params.put("after_tank_liquid_liter",after_tank_liquid_liter.getText().toString());
                params.put("before_tank_pressure",before_tank_pressure.getText().toString());
                params.put("before_tank_liquid_liter",before_tank_liquid_liter.getText().toString());
                params.put("supervisor", SharedPref.getInstance(Dura.this).Id());
                params.put("email", SharedPref.getInstance(Dura.this).getEmail());
                params.put("username", SharedPref.getInstance(Dura.this).FirstName()+" "+SharedPref.getInstance(Dura.this).LastName());
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

    private  void  loadata()
    {
        List<ItemCode>  itemCodes=new ArrayList<>();
        durasearchadapter searchAdapter=new durasearchadapter(getApplicationContext(),itemCodes);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(searchAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(synchelper != null)
            synchelper.close();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Producation_Main.class);
        startActivity(intent);
    }

    private final BroadcastReceiver dura_no_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("dura_production")) {
                //Extract your data - better to use constants...
                s = intent.getStringExtra("dura_no");
                wei=intent.getStringExtra("wieght");
                Tarevalue.setText(wei);
                autoCompleteTextView.setText(s);

            }

        }
    };


}