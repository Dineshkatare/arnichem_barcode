package com.arnichem.arnichem_barcode.order;

import android.app.ProgressDialog;
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

public class OrderViewActivity extends AppCompatActivity {

    private TextView dateTextView, codeTextView, nameTextView, messageTextView, remarksTextView, itemsTextView, linkTextView;
    private LinearLayout copyIcon, shareIcon;
    private android.widget.ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Order View");

        // Retrieve data passed from the previous activity
        String srno = getIntent().getStringExtra("srno");
        String date = getIntent().getStringExtra("date_added");
        String code = getIntent().getStringExtra("code");
        String name = getIntent().getStringExtra("name");
        String message = getIntent().getStringExtra("message");
        String remarks = getIntent().getStringExtra("remarks");
        String items = getIntent().getStringExtra("items");
        String link = getIntent().getStringExtra("link");

        // If srno is missing but order_id is present, fetch data dynamically (from notification)
        String notificationOrderId = getIntent().getStringExtra("order_id");

        android.util.Log.d("OrderViewActivity", "onCreate: srno=" + srno + ", order_id=" + notificationOrderId);

        // Find views
        dateTextView = findViewById(R.id.cddateid);
        codeTextView = findViewById(R.id.codeid);
        nameTextView = findViewById(R.id.cdcustnameid);
        messageTextView = findViewById(R.id.message_txt);
        remarksTextView = findViewById(R.id.remarks);
        linkTextView = findViewById(R.id.link_val);

        copyIcon = findViewById(R.id.text_copy);
        shareIcon = findViewById(R.id.text_whatsapp);
        progressBar = findViewById(R.id.progressBar);

        if (srno != null) {
            // Opened normally from a list — all data already in intent
            android.util.Log.d("OrderViewActivity", "Populating UI from Intent extras (srno present)");
            populateUI(srno, date, code, name, message, remarks, items, link);
        } else if (notificationOrderId != null) {
            // Opened via notification tap — check if FCM data payload has embedded order fields
            String embeddedName    = getIntent().getStringExtra("name");
            String embeddedMsg     = getIntent().getStringExtra("message");
            String embeddedDate    = getIntent().getStringExtra("date_added");
            String embeddedCode    = getIntent().getStringExtra("code");
            String embeddedRemarks = getIntent().getStringExtra("remarks");
            String embeddedLink    = getIntent().getStringExtra("link");

            if (embeddedName != null && !embeddedName.isEmpty()) {
                // FCM payload has full order details — show immediately, no network call
                android.util.Log.d("OrderViewActivity", "Populating UI from embedded FCM data for order: " + notificationOrderId);
                populateUI(
                    notificationOrderId,
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
                android.util.Log.d("OrderViewActivity", "No embedded data — triggering dynamic fetch for order_id: " + notificationOrderId);
                fetchOrderDetails(notificationOrderId);
            }
        } else {
            android.util.Log.d("OrderViewActivity", "No order data in intent");
            Toast.makeText(this, "Order data not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchOrderDetails(String orderId) {
        if (progressBar != null) progressBar.setVisibility(android.view.View.VISIBLE);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                "http://arnichem.co.in/intranet/barcode/APP/app_apis/fetch_order_by_id.php",
                response -> {
                    if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
                    android.util.Log.d("OrderViewActivity", "API Response: " + response);
                    try {
                        String trimmedResponse = response.trim();
                        if (trimmedResponse.startsWith("{")) {
                            JSONObject obj = new JSONObject(trimmedResponse);
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
                        } else {
                            // Server returned plain text instead of JSON (e.g., "Could not connect")
                            android.util.Log.e("OrderViewActivity", "Non-JSON response: " + trimmedResponse);
                            Toast.makeText(this, "Server Error: " + trimmedResponse, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        android.util.Log.e("OrderViewActivity", "Parse error", e);
                        Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("db_host", SharedPref.getInstance(OrderViewActivity.this).getDBHost());
                params.put("db_username", SharedPref.getInstance(OrderViewActivity.this).getDBUsername());
                params.put("db_password", SharedPref.getInstance(OrderViewActivity.this).getDBPassword());
                params.put("db_name", SharedPref.getInstance(OrderViewActivity.this).getDBName());
                params.put("order_id", orderId);
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

    // Function to copy data to clipboard
    private void copyData(String srno,String date, String code, String name, String message, String remarks, String items, String link) {
        String dataToCopy = "*New Order Details*\n" +
                "No: " + srno + "\n" +
                "Delivery Date: " + date + "\n" +
                "*Name: " + name + "*\n" +
                "Message: " + message + "\n" +
                "Remarks: " + remarks + "\n" +
                "Link: " + link;

        // Get the ClipboardManager system service
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Order Data", dataToCopy);
        clipboard.setPrimaryClip(clip);

        // Show a confirmation toast
        Toast.makeText(OrderViewActivity.this, "Data copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    // Function to share data via WhatsApp
    private void shareData(String srno,String date, String code, String name, String message, String remarks, String items, String link) {
        String shareMessage = "*New Order Details*\n" +
                "No: " + srno + "\n" +
                "Delivery Date: " + date + "\n" +
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
            Toast.makeText(OrderViewActivity.this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(OrderViewActivity.this, Dashboard.class));
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(OrderViewActivity.this, Dashboard.class));
        finish();
    }

}
