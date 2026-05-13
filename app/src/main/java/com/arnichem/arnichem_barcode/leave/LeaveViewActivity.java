package com.arnichem.arnichem_barcode.leave;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LeaveViewActivity extends AppCompatActivity {

    private TextView empNameTxt, typeTxt, fromDateTxt, toDateTxt, reasonTxt, statusTxt;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Leave Details");

        empNameTxt = findViewById(R.id.leave_emp_name);
        typeTxt = findViewById(R.id.leave_type);
        fromDateTxt = findViewById(R.id.leave_from_date);
        toDateTxt = findViewById(R.id.leave_to_date);
        reasonTxt = findViewById(R.id.leave_reason);
        statusTxt = findViewById(R.id.leave_status);
        btnBack = findViewById(R.id.btn_back);

        String leaveId = getIntent().getStringExtra("leave_id");
        android.util.Log.d("LeaveViewActivity", "onCreate: leave_id=" + leaveId);

        if (leaveId != null) {
            fetchLeaveDetails(leaveId);
        } else {
            Toast.makeText(this, "No Leave ID found", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(LeaveViewActivity.this, Dashboard.class));
            finish();
        });
    }

    private void fetchLeaveDetails(String leaveId) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading leave details...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                "http://arnichem.co.in/intranet/barcode/APP/app_apis/fetch_leave_by_id.php",
                response -> {
                    progressDialog.dismiss();
                    android.util.Log.d("LeaveViewActivity", "API Response: " + response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if ("success".equals(obj.optString("status"))) {
                            empNameTxt.setText(obj.optString("emp_name", "N/A"));
                            typeTxt.setText(obj.optString("leave_type", "N/A"));
                            fromDateTxt.setText(obj.optString("from_date", "N/A"));
                            toDateTxt.setText(obj.optString("to_date", "N/A"));
                            reasonTxt.setText(obj.optString("reason", "N/A"));
                            statusTxt.setText(obj.optString("leave_status", "N/A"));
                        } else {
                            Toast.makeText(this, "API Error: " + obj.optString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        android.util.Log.e("LeaveViewActivity", "Parse error", e);
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
                params.put("db_host", SharedPref.getInstance(LeaveViewActivity.this).getDBHost());
                params.put("db_username", SharedPref.getInstance(LeaveViewActivity.this).getDBUsername());
                params.put("db_password", SharedPref.getInstance(LeaveViewActivity.this).getDBPassword());
                params.put("db_name", SharedPref.getInstance(LeaveViewActivity.this).getDBName());
                params.put("leave_id", leaveId);
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(LeaveViewActivity.this, Dashboard.class));
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(LeaveViewActivity.this, Dashboard.class));
        finish();
    }
}
