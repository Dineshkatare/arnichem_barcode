package com.arnichem.arnichem_barcode.attendance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AttendanceViewActivity extends AppCompatActivity {

    private TextView empNameTxt, typeTxt, timeTxt, addressTxt, remarksTxt;
    private ImageView selfieImg;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_view);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Attendance Details");
        }

        empNameTxt = findViewById(R.id.attn_emp_name);
        typeTxt = findViewById(R.id.attn_type);
        timeTxt = findViewById(R.id.attn_time);
        addressTxt = findViewById(R.id.attn_address);
        remarksTxt = findViewById(R.id.attn_remarks);
        selfieImg = findViewById(R.id.attn_selfie);
        btnBack = findViewById(R.id.btn_back_attn);

        String logId = getIntent().getStringExtra("log_id");
        Log.d("AttendanceView", "onCreate: log_id=" + logId);

        if (logId != null) {
            // Check if FCM data payload has embedded attendance fields
            String embeddedEmpName = getIntent().getStringExtra("emp_name");
            String embeddedInOut   = getIntent().getStringExtra("in_out");
            String embeddedTime    = getIntent().getStringExtra("time");
            String embeddedDate    = getIntent().getStringExtra("date");
            String embeddedAddress = getIntent().getStringExtra("address");
            String embeddedRemarks = getIntent().getStringExtra("remarks");
            String embeddedImage   = getIntent().getStringExtra("image_url");

            if (embeddedEmpName != null && !embeddedEmpName.isEmpty()) {
                // FCM payload has full attendance details — show immediately, no network call
                Log.d("AttendanceView", "Populating UI from embedded FCM data for log: " + logId);
                empNameTxt.setText(embeddedEmpName);
                typeTxt.setText(embeddedInOut != null ? embeddedInOut : "");
                timeTxt.setText((embeddedTime != null ? embeddedTime : "") + " | " + (embeddedDate != null ? embeddedDate : ""));
                addressTxt.setText(embeddedAddress != null ? embeddedAddress : "No location provided.");
                remarksTxt.setText(embeddedRemarks != null ? embeddedRemarks : "No remarks.");

                if (embeddedImage != null && !embeddedImage.isEmpty()) {
                    Picasso.get()
                            .load(embeddedImage)
                            .placeholder(R.drawable.other)
                            .error(R.drawable.other)
                            .into(selfieImg);
                }
            } else {
                // Fallback: fetch from server (older notifications without embedded data)
                Log.d("AttendanceView", "No embedded data — triggering dynamic fetch for log_id: " + logId);
                fetchAttendanceDetails(logId);
            }
        } else {
            Toast.makeText(this, "No Attendance ID found", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v -> {
            goToDashboard();
        });
    }

    private void fetchAttendanceDetails(String logId) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading details...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                "http://arnichem.co.in/intranet/barcode/APP/app_apis/fetch_attendance_by_id.php",
                response -> {
                    progressDialog.dismiss();
                    Log.d("AttendanceView", "API Response: " + response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if ("success".equals(obj.optString("status"))) {
                            empNameTxt.setText(obj.optString("emp_name", "N/A"));
                            typeTxt.setText(obj.optString("in_out", "N/A"));
                            timeTxt.setText(obj.optString("time", "") + " | " + obj.optString("date", ""));
                            addressTxt.setText(obj.optString("address", "No location provided."));
                            remarksTxt.setText(obj.optString("remarks", "No remarks."));

                            String imageUrl = obj.optString("image_url", "");
                            if (!imageUrl.isEmpty()) {
                                Picasso.get()
                                        .load(imageUrl)
                                        .placeholder(R.drawable.other)
                                        .error(R.drawable.other)
                                        .into(selfieImg);
                            }
                        } else {
                            Toast.makeText(this, "Error: " + obj.optString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("AttendanceView", "Parse error", e);
                        Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("db_host", SharedPref.getInstance(AttendanceViewActivity.this).getDBHost());
                params.put("db_username", SharedPref.getInstance(AttendanceViewActivity.this).getDBUsername());
                params.put("db_password", SharedPref.getInstance(AttendanceViewActivity.this).getDBPassword());
                params.put("db_name", SharedPref.getInstance(AttendanceViewActivity.this).getDBName());
                params.put("log_id", logId);
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void goToDashboard() {
        Intent intent = new Intent(this, Dashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        goToDashboard();
        return true;
    }

    @Override
    public void onBackPressed() {
        goToDashboard();
    }
}
