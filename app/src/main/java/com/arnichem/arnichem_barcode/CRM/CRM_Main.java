package com.arnichem.arnichem_barcode.CRM;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.GetData.BpContact;
import com.arnichem.arnichem_barcode.GetData.Data;
import com.arnichem.arnichem_barcode.GetData.GetDataResponse;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.order.OrderMainActivity;
import com.arnichem.arnichem_barcode.order.OrderViewActivity;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.bp_contact_handler;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
import com.valdesekamdem.library.mdtoast.MDToast;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;

public class CRM_Main extends AppCompatActivity implements Listener, LocationData.AddressCallBack {
    TextView usernamevalue, date, new_contact_tv;
    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> customerdataAdapter;
    Spinner nameOfContactVal, spinnercust, meeting_type;
    Button button;
    ProgressDialog dialog;
    String to_warehouse, cust_code = "", designation = "";
    String bp_conatc_name, bp_contact_code, meeting_type_name;
    DatabaseHandler databaseHandlercustomer;
    bp_contact_handler bp_contac;
    public int poscustfixdel, pos_bp_contact, pos_meeting_type, new_contact_pos;
    static JSONObject object = null;
    LinearLayout add_spinner_ll;
    EditText discussionVal;
    ArrayAdapter<CharSequence> adapter;
    ImageView add_person;
    APIInterface apiInterface;
    EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    String lati = "", logi = "", addr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crm_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("CRM");
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        nameOfContactVal = findViewById(R.id.nameOfContactVal);
        spinnercust = findViewById(R.id.custnamespingodowndel);
        add_person = findViewById(R.id.person_add);
        meeting_type = findViewById(R.id.meetingTypeVal);
        discussionVal = findViewById(R.id.discussionVal);
        new_contact_tv = findViewById(R.id.new_contact_tv);
        add_spinner_ll = findViewById(R.id.add_spinner_ll);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        databaseHandlercustomer = new DatabaseHandler(CRM_Main.this);
        bp_contac = new bp_contact_handler(CRM_Main.this);

        easyWayLocation = new EasyWayLocation(this, false, false, this);
        getLocationDetail = new GetLocationDetail(this, this);

        loadSpinnerData();
        loadSpinnerData1();
        usernamevalue = findViewById(R.id.usernametxtvalue);
        button = findViewById(R.id.GodownDelMainPost);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        date = findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postUsingVolley();
            }
        });
        adapter = ArrayAdapter.createFromResource(this, R.array.metting_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        meeting_type.setAdapter(adapter);
        meeting_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                /// Log.v("item", (String) parent.getItemAtPosition(position));
                meeting_type_name = (String) parent.getItemAtPosition(position);
                pos_meeting_type = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        spinnercust.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                to_warehouse = customerdataAdapter.getItem(position);
                poscustfixdel = position;
                SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(poscustfixdel));
                Cursor cursor = databaseHandlercustomer.readAllData();
                if (cursor.getCount() == 0) {
                    // empty_imageview.setVisibility(View.VISIBLE);
                    // no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        if (col.contentEquals(to_warehouse)) {
                            cust_code = col1;
                        }
                    }
                }
                bp_conatc_name = "";
                bp_contact_code = "";
                loadSpinnerData1();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        add_person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (poscustfixdel == 0) {
                    MDToast.makeText(CRM_Main.this, "Please Select Customer Name !", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();
                } else {
                    customerDetails();
                }
            }
        });
        nameOfContactVal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bp_conatc_name = dataAdapter.getItem(position);
                pos_bp_contact = position;
                SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(poscustfixdel));
                Cursor cursor = bp_contac.readAllData();
                if (cursor.getCount() == 0) {
                    // empty_imageview.setVisibility(View.VISIBLE);
                    // no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        if (cust_code.contentEquals(col1) && col.contentEquals(bp_conatc_name)) {
                            bp_contact_code = col1;
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Executors.newSingleThreadExecutor().execute(new Runnable() {
        // public void run() {
        // bp_contac.deleteAllData();
        // }
        // });

        getData();
    }

    private void customerDetails() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.crn_contact_details, null);

        TextView cust_code_tv = dialogView.findViewById(R.id.tvCustCode);

        // Find the EditText and Spinner in the custom layout
        EditText cust_name_et = dialogView.findViewById(R.id.cust_name_tv);
        Spinner spinner = dialogView.findViewById(R.id.spinnerDesignation);
        EditText mobile_et = dialogView.findViewById(R.id.mobile_et);
        EditText phone_no_et = dialogView.findViewById(R.id.phone_no_et);
        EditText email_et = dialogView.findViewById(R.id.email_et);
        EditText remarks_et = dialogView.findViewById(R.id.remarks_et);
        cust_name_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.length() > 0) {
                    String capitalizedText = text.substring(0, 1).toUpperCase() + text.substring(1);
                    if (!capitalizedText.equals(text)) {
                        cust_name_et.setText(capitalizedText);
                        cust_name_et.setSelection(cust_name_et.getText().length()); // Set the cursor to the end of the
                                                                                    // text
                    }

                }
            }
        });

        remarks_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.length() > 0) {
                    String capitalizedText = text.substring(0, 1).toUpperCase() + text.substring(1);
                    if (!capitalizedText.equals(text)) {
                        remarks_et.setText(capitalizedText);
                        remarks_et.setSelection(remarks_et.getText().length()); // Set the cursor to the end of the text
                    }
                }
            }
        });

        // Set up the Spinner with data (e.g., an array of options)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.designations,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        cust_code_tv.setText(cust_code);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                designation = parent.getItemAtPosition(position).toString();
                new_contact_pos = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Create the custom dialog without positive/negative buttons
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set custom button click actions
        Button btnSubmit = dialogView.findViewById(R.id.save_customer);
        Button btnCancel = dialogView.findViewById(R.id.cancel_button);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new_contact_pos == 0) {
                    MDToast.makeText(CRM_Main.this, "Please Select Designation !", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();

                } else if (cust_name_et.getText().toString().isEmpty()) {
                    MDToast.makeText(CRM_Main.this, "Please Enter Person Name!", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();

                } else if (mobile_et.getText().toString().isEmpty()) {
                    MDToast.makeText(CRM_Main.this, "Please Enter Mobile No!", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR)
                            .show();

                } else {
                    dialog.dismiss(); // Close the dialog
                    post_customer_details(cust_code_tv.getText().toString().trim(),
                            cust_name_et.getText().toString().trim(), email_et.getText().toString().trim(),
                            mobile_et.getText().toString().trim(), phone_no_et.getText().toString().trim(),
                            remarks_et.getText().toString().trim(), designation);
                }

                // Handle the "Submit" button click
                // Process the input data as needed

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the "Cancel" button click
                dialog.dismiss(); // Close the dialog
            }
        });

        dialog.show();

    }

    private void loadSpinnerData() {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Creating adapter for spinner
        customerdataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        customerdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnercust.setAdapter(customerdataAdapter);
        if (poscustfixdel != 0) {
            spinnercust.setSelection(poscustfixdel);
        }
    }

    private void loadSpinnerData1() {
        bp_contact_handler db = new bp_contact_handler(getApplicationContext());
        List<String> labels = db.getAllLabels(cust_code);

        // Clear previous spinner data
        if (dataAdapter != null) {
            dataAdapter.clear();
        }

        // Creating a new adapter with updated labels
        dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attaching data adapter to spinner
        nameOfContactVal.setAdapter(dataAdapter);

        // Set selected position if applicable
        if (pos_bp_contact != 0) {
            nameOfContactVal.setSelection(pos_bp_contact);
        }
    }

    private void postUsingVolley() {

        dialog = new ProgressDialog(CRM_Main.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (poscustfixdel == 0) {
            dialog.dismiss();
            MDToast.makeText(CRM_Main.this, "Please Select Customer Name !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR)
                    .show();

        } else if (pos_bp_contact == 0) {
            dialog.dismiss();
            MDToast.makeText(CRM_Main.this, "Please Select Name Of Contact !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR)
                    .show();
        } else if (pos_meeting_type == 0) {
            dialog.dismiss();
            MDToast.makeText(CRM_Main.this, "Please Select Meeting Type !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR)
                    .show();
        } else if (discussionVal.getText().toString().isEmpty()) {
            dialog.dismiss();
            MDToast.makeText(CRM_Main.this, "Please Enter Discussion !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR)
                    .show();
        } else {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.crm_entry,
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

                                        MDToast.makeText(CRM_Main.this, "CRM Entry Done!", MDToast.LENGTH_LONG,
                                                MDToast.TYPE_SUCCESS).show();
                                        dialog.dismiss();
                                        Intent intent = new Intent(CRM_Main.this, CRMViewActivity.class);
                                        intent.putExtra("name", to_warehouse);
                                        intent.putExtra("contact_name", bp_conatc_name);
                                        intent.putExtra("meeting_type", meeting_type_name);
                                        intent.putExtra("discussion", discussionVal.getText().toString());
                                        intent.putExtra("link",
                                                "https://arnichem.co.in/intranet/bpview.php?code=" + cust_code);
                                        startActivity(intent);

                                    } else {
                                        dialog.dismiss();

                                    }

                                    Log.e("JSON", "> " + status + msg);
                                }

                            } catch (JSONException e) {
                                dialog.dismiss();
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @SuppressLint("WrongConstant")
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            error.printStackTrace();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("bp_code", bp_contact_code);
                    params.put("visit_type", meeting_type_name);
                    params.put("name", bp_conatc_name);
                    params.put("detail", discussionVal.getText().toString());
                    params.put("email", SharedPref.getInstance(CRM_Main.this).getEmail());
                    params.put("db_host", SharedPref.mInstance.getDBHost());
                    params.put("db_username", SharedPref.mInstance.getDBUsername());
                    params.put("db_password", SharedPref.mInstance.getDBPassword());
                    params.put("db_name", SharedPref.mInstance.getDBName());

                    params.put("GPS_lat", lati);
                    params.put("GPS_long", logi);
                    params.put("address", addr);

                    return params;
                }
            };
            VolleySingleton.getInstance(CRM_Main.this).addToRequestQueue(stringRequest);

        }
    }

    private void post_customer_details(String cust_code, String cust_name, String email, String mobile, String phone,
            String remaks, String designation) {

        dialog = new ProgressDialog(CRM_Main.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.bpcontact_entry,
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

                                    MDToast.makeText(CRM_Main.this, msg, MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS)
                                            .show();

                                    bp_conatc_name = cust_name;
                                    bp_contact_code = cust_code;
                                    new_contact_tv.setVisibility(View.VISIBLE);
                                    pos_bp_contact = 1;
                                    new_contact_tv.setText(cust_name);
                                    add_spinner_ll.setVisibility(View.GONE);
                                    spinnercust.setEnabled(false);

                                } else {

                                }

                                Log.e("JSON", "> " + status + msg);
                            }
                            dialog.dismiss();

                        } catch (JSONException e) {
                            dialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("cust_code", cust_code);
                params.put("name_of_contact", cust_name);
                params.put("mobile_no", mobile);
                params.put("phone_no", phone);
                params.put("contact_email", email);
                params.put("remarks", remaks);
                params.put("designation", designation);
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username", SharedPref.mInstance.getDBUsername());
                params.put("db_password", SharedPref.mInstance.getDBPassword());
                params.put("db_name", SharedPref.mInstance.getDBName());
                params.put("email", SharedPref.mInstance.getEmail());
                return params;
            }
        };
        VolleySingleton.getInstance(CRM_Main.this).addToRequestQueue(stringRequest);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CRM_Main.this, Dashboard.class));
    }

    public static boolean isValidEmail(String email) {
        // Regular expression pattern for a valid email address
        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";

        // Create a Pattern object
        Pattern pattern = Pattern.compile(emailPattern);

        // Create a Matcher object
        Matcher matcher = pattern.matcher(email);

        // Check if the email matches the pattern
        return matcher.matches();
    }

    private void getData() {
        dialog = new ProgressDialog(CRM_Main.this);
        dialog.setTitle("Data Syncing");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        Call<GetDataResponse> call = apiInterface.sync_bp_contact(SharedPref.mInstance.getDBHost(),
                SharedPref.mInstance.getDBUsername(), SharedPref.mInstance.getDBPassword(),
                SharedPref.mInstance.getDBName());
        call.enqueue(new Callback<GetDataResponse>() {
            @Override
            public void onResponse(Call<GetDataResponse> call, retrofit2.Response<GetDataResponse> response) {
                dialog.dismiss();
                if (response.body().getData() != null)
                    loadData(response.body().getData());
            }

            @Override
            public void onFailure(Call<GetDataResponse> call, Throwable t) {
                call.cancel();
                dialog.dismiss();
            }
        });
    }

    private void loadData(Data data) {

        if (data.getBpContact().size() != 0 && data.getBpContact() != null)
            loadBPContact(data.getBpContact());

    }

    private void loadBPContact(List<BpContact> bpContact) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for (int i = 1; i < bpContact.size(); i++) {
                    bp_contac.addcust(bpContact.get(i).getName(), bpContact.get(i).getCode());
                }
            }
        });
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
        // Optional: Toast.makeText(this, "Location On", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void currentLocation(Location location) {
        if (location != null) {
            lati = String.valueOf(location.getLatitude());
            logi = String.valueOf(location.getLongitude());
            getLocationDetail.getAddress(location.getLatitude(), location.getLongitude(), "xyz");
        }
    }

    @Override
    public void locationCancelled() {
        // Optional
    }

    @Override
    public void locationData(LocationData locationData) {
        if (locationData != null) {
            addr = locationData.getFull_address();
        }
    }

}