package com.arnichem.arnichem_barcode.report;

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.github.ybq.android.spinkit.SpinKitView;

public class ReportActivity extends AppCompatActivity {
    WebView mywebview;
    SpinKitView spinKitView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get data from Intent
        String dynamicTitle = getIntent().getStringExtra("title");
        String dynamicLink = getIntent().getStringExtra("url");

        // Set title (fallback to "Reports" if null)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(dynamicTitle != null ? dynamicTitle : "Reports");
        }

        spinKitView = findViewById(R.id.spin_kit);
        mywebview = findViewById(R.id.webView);
        spinKitView.setVisibility(View.VISIBLE);

        mywebview.getSettings().setJavaScriptEnabled(true);

        mywebview.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                spinKitView.setVisibility(View.GONE);
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                spinKitView.setVisibility(View.GONE);
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                spinKitView.setVisibility(View.GONE);
            }
        });

        // Build URL dynamically
        String fullUrl;
        if (dynamicLink != null && !dynamicLink.isEmpty()) {
            fullUrl = dynamicLink;
        } else {
            fullUrl = "https://www.arnichem.co.in/intranet/reports_app.php?username="
                    + SharedPref.getInstance(ReportActivity.this).getEmail()
                    + "&company_name="
                    + SharedPref.getInstance(ReportActivity.this).getCompanyShortName();
        }

        Log.d("ReportActivity", "Loading URL: " + fullUrl);
        mywebview.loadUrl(fullUrl);
    }

    @Override
    public void onBackPressed() {
        if (mywebview.canGoBack()) {
            mywebview.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
