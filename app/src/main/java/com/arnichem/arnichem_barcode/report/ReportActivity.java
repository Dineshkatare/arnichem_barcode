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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Reports");
        spinKitView = findViewById(R.id.spin_kit);
        mywebview = (WebView) findViewById(R.id.webView);
        spinKitView.setVisibility(View.VISIBLE);

        mywebview.getSettings().setJavaScriptEnabled(true); // enable javascript

        mywebview.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                spinKitView.setVisibility(View.GONE);

            }
            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                spinKitView.setVisibility(View.GONE);

                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });


        String fullUrl = "https://www.arnichem.co.in/intranet/reports_app.php?username=" + SharedPref.getInstance(ReportActivity.this).getEmail() + "&company_name=" + SharedPref.getInstance(ReportActivity.this).getCompanyShortName();

        Log.d("url",""+fullUrl);
        mywebview.loadUrl(fullUrl);
        mywebview.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                spinKitView.setVisibility(View.GONE);

            }
        });



    }
    @Override
    public void onBackPressed() {
        if (mywebview.canGoBack()) {
            mywebview.goBack();  // Navigate back to the previous web page
        } else {
            super.onBackPressed();  // Exit the activity
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}