package com.arnichem.arnichem_barcode.order;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.util.SharedPref;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import android.app.ProgressDialog;

public class PickViewActivity extends AppCompatActivity {
    private TextView dateTextView, codeTextView, nameTextView, messageTextView, remarksTextView, itemsTextView, linkTextView;
    private LinearLayout copyIcon, shareIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("PICK ENTRY");

        // Retrieve data passed from the previous activity
        int srnoInt = getIntent().getIntExtra("srno", 0); 
        String srno = srnoInt != 0 ? String.valueOf(srnoInt) : null;
        String date = getIntent().getStringExtra("date_added");
        String code = getIntent().getStringExtra("code");
        String name = getIntent().getStringExtra("name");
        String message = getIntent().getStringExtra("message");
        String remarks = getIntent().getStringExtra("remarks");
        String items = getIntent().getStringExtra("items");
        String link = getIntent().getStringExtra("link");

        // If srno is missing but pick_id is present, fetch data dynamically (from notification)
        String notificationPickId = getIntent().getStringExtra("pick_id");

        android.util.Log.d("PickViewActivity", "onCreate: srno=" + srno + ", pick_id=" + notificationPickId);

        // Find views
        dateTextView = findViewById(R.id.cddateid);
        codeTextView = findViewById(R.id.codeid);
        nameTextView = findViewById(R.id.cdcustnameid);
        messageTextView = findViewById(R.id.message_txt);
        remarksTextView = findViewById(R.id.remarks);
        linkTextView = findViewById(R.id.link_val);

        copyIcon = findViewById(R.id.text_copy);
        shareIcon = findViewById(R.id.text_whatsapp);

        if (srno != null) {
            // Opened normally from a list — all data already in intent
            android.util.Log.d("PickViewActivity", "Populating UI from Intent extras (srno present)");
            populateUI(srno, date, code, name, message, remarks, items, link);
        } else if (notificationPickId != null) {
            // Opened via notification tap — check if FCM data payload has embedded pick fields
            String embeddedName    = getIntent().getStringExtra("name");
            String embeddedMsg     = getIntent().getStringExtra("message");
            String embeddedDate    = getIntent().getStringExtra("date_added");
            String embeddedCode    = getIntent().getStringExtra("code");
            String embeddedRemarks = getIntent().getStringExtra("remarks");
            String embeddedLink    = getIntent().getStringExtra("link");

            if (embeddedName != null && !embeddedName.isEmpty()) {
                // FCM payload has full pick details — show immediately, no network call
                android.util.Log.d("PickViewActivity", "Populating UI from embedded FCM data for pick: " + notificationPickId);
                populateUI(
                    notificationPickId,
                    embeddedDate    != null ? embeddedDate    : "",
                    embeddedCode    != null ? embeddedCode    : "",
                    embeddedName,
                    embeddedMsg     != null ? embeddedMsg     : "",
                    embeddedRemarks != null ? embeddedRemarks : "",
                    "",
                    embeddedLink    != null ? embeddedLink    : ""
                );
            } else {
                // Fallback: fetch from server (older notifications without embedded data)
                android.util.Log.d("PickViewActivity", "No embedded data — triggering dynamic fetch for pick_id: " + notificationPickId);
                fetchPickDetails(notificationPickId);
            }
        } else {
            android.util.Log.d("PickViewActivity", "No pick data in intent");
            Toast.makeText(this, "Pick data not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPickDetails(String pickId) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading pick details...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                "http://arnichem.co.in/intranet/barcode/APP/app_apis/fetch_pick_by_id.php",
                response -> {
                    progressDialog.dismiss();
                    android.util.Log.d("PickViewActivity", "API Response: " + response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if ("success".equals(obj.optString("status"))) {
                            String srno = obj.optString("srno", "");
                            String date = obj.optString("date_added", "");
                            String code = obj.optString("code", "");
                            String name = obj.optString("name", "");
                            String message = obj.optString("message", "");
                            String remarks = obj.optString("remarks", "");
                            String link = obj.optString("link", "");
                            populateUI(srno, date, code, name, message, remarks, "", link);
                        } else {
                            Toast.makeText(this, "API Error: " + obj.optString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        android.util.Log.e("PickViewActivity", "Parse error", e);
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
                params.put("db_host", SharedPref.getInstance(PickViewActivity.this).getDBHost());
                params.put("db_username", SharedPref.getInstance(PickViewActivity.this).getDBUsername());
                params.put("db_password", SharedPref.getInstance(PickViewActivity.this).getDBPassword());
                params.put("db_name", SharedPref.getInstance(PickViewActivity.this).getDBName());
                params.put("pick_id", pickId);
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void populateUI(String srno, String date, String code, String name, String message, String remarks, String items, String link) {
        // Set the text in the TextViews
        dateTextView.setText(date);
        codeTextView.setText(code);
        nameTextView.setText(name);
        messageTextView.setText(message);
        remarksTextView.setText(remarks);
        linkTextView.setText(link);
        Linkify.addLinks(linkTextView, Linkify.WEB_URLS);
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // Set listeners with current data
        copyIcon.setOnClickListener(v -> copyData(srno, date, code, name, message, remarks, items, link));
        shareIcon.setOnClickListener(v -> shareData(srno, date, code, name, message, remarks, items, link));
    }
    private void copyData(String srno,String date, String code, String name, String message, String remarks, String items, String link) {
        String dataToCopy = "*New Holding Pick Details*\n" +
                "No: " + srno + "\n" +
                "Pick Date: " + date + "\n" +
                "*Name: " + name + "*\n" +
                "Message: " + message + "\n" +
                "Remarks: " + remarks + "\n" +
                "Link: " + link;

        // Get the ClipboardManager system service
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Pick Data", dataToCopy);
        clipboard.setPrimaryClip(clip);

        // Show a confirmation toast
        Toast.makeText(PickViewActivity.this, "Data copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    // Function to share data via WhatsApp
    private void shareData(String srno,String date, String code, String name, String message, String remarks, String items, String link) {
        String shareMessage = "*New Holding Pick Details*\n" +
                "No: " + srno + "\n" +
                "Pick Date: " + date + "\n" +
                "*Name: " + name + "*\n" +
                "Message: " + message + "\n" +
                "Remarks: " + remarks + "\n" +
                "Link: " + link;


        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        // Check if WhatsApp is installed and share data
        shareIntent.setPackage("com.whatsapp");
        try {
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(PickViewActivity.this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(PickViewActivity.this, Dashboard.class));
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PickViewActivity.this, Dashboard.class));
        finish();
    }

}