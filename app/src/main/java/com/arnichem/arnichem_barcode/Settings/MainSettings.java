package com.arnichem.arnichem_barcode.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.arnichem.arnichem_barcode.GetData.Test;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.Main;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.valdesekamdem.library.mdtoast.MDToast;

public class MainSettings extends AppCompatActivity {
    CardView changePassword,appversionbtn,TestptBtn,SyncBarcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");
        changePassword=findViewById(R.id.changePassword);
        appversionbtn=findViewById(R.id.appversioncardview);
        TestptBtn=findViewById(R.id.testprintercardview);
        SyncBarcode = findViewById(R.id.inventorySync);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainSettings.this,ChanePassword.class);
                startActivity(i);
            }
        });
        appversionbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainSettings.this,AppVersion.class);
                startActivity(i);
            }
        });
        TestptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainSettings.this,TestPrinter.class);
                startActivity(i);
            }
        });
        SyncBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSettings.this, SyncInventoryActivity.class));

            }
        });
    }
}