package com.arnichem.arnichem_barcode.CustomerHolding;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Settings.newPassword;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.github.ybq.android.spinkit.SpinKitView;

public class WebViewActivity extends AppCompatActivity {

    WebView mywebview;
    String cust_code = "";
    SpinKitView spinKitView;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Customer Holding");
        spinKitView = findViewById(R.id.spin_kit);
        mywebview = (WebView) findViewById(R.id.webView);
        Intent intent = getIntent();
        cust_code = intent.getExtras().getString("code","");
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


        String fullUrl = "http://arnichem.co.in/intranet/1239812038120831.php?code=" + cust_code + "&username=" + SharedPref.getInstance(WebViewActivity.this).getEmail();

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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}