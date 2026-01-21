package com.arnichem.arnichem_barcode.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.arnichem.arnichem_barcode.Company.SelectCompanyActivity;
import com.arnichem.arnichem_barcode.GetData.Test;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.Main;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.google.android.material.snackbar.Snackbar;
import com.valdesekamdem.library.mdtoast.MDToast;

public class MainSettings extends AppCompatActivity {
    CardView changePassword,appversionbtn,TestptBtn,SyncBarcode,Logout,switchUser;
    String status;
    SharedPreferences pref;
    LinearLayout linearLayout;

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
        TestptBtn=findViewById(R.id.testprintercardview);
        SyncBarcode = findViewById(R.id.inventorySync);
        Logout = findViewById(R.id.logout);
        switchUser = findViewById(R.id.switchUser);
        pref = getSharedPreferences(constant.TAG,MODE_PRIVATE);
        status = SharedPref.getInstance(MainSettings.this).vLoggedInUser();
        linearLayout = findViewById(R.id.main);
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
        switchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSettings.this, SwitchDriverActivity.class));
            }
        });
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(status.equals("success")) {
                    Snackbar.make(linearLayout, "कृपया वाहन माहिती लॉगऑऊट टाका !", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                }
                else
                {
                    SharedPref.getInstance(getApplicationContext()).logout();
                    startActivity(new Intent(MainSettings.this, SelectCompanyActivity.class));
                    finish();

                }
            }
        });
    }
}