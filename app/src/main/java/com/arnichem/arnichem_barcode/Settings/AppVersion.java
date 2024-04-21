package com.arnichem.arnichem_barcode.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.arnichem.arnichem_barcode.R;

public class AppVersion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_version);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("App Version");

    }
}