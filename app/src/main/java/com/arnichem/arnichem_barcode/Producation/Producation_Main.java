package com.arnichem.arnichem_barcode.Producation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;


import com.arnichem.arnichem_barcode.Producation.Co2.FirstCo2;
import com.arnichem.arnichem_barcode.Producation.DryIce.DryIceFIrstScreen;
import com.arnichem.arnichem_barcode.Producation.DryIce.DryIceMain;
import com.arnichem.arnichem_barcode.Producation.HydroTest.HydroMain;
import com.arnichem.arnichem_barcode.Producation.NewAmmonia.ammoniafirstpage;
import com.arnichem.arnichem_barcode.Producation.NewAmmonia.ammoniaprint;
import com.arnichem.arnichem_barcode.Producation.Nitrogen.FirstNitrogen;
import com.arnichem.arnichem_barcode.Producation.Oxygen.FisrtPart;
import com.arnichem.arnichem_barcode.Producation.ZeroAir.FirstZeroAir;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.view.Dashboard;

public class Producation_Main extends AppCompatActivity {
    CardView DuraCylinder,OXygenCylinder,CO2Cylinder,NitrogenCylinder,ZeroAir,Ammonia,cd_dry_ice,hydro_test;
    SharedPreferences pref;
    ScrollView scrollView;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producation_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Production");
        scrollView=findViewById(R.id.productionscroll);
        pref = getSharedPreferences(constant.TAG,MODE_PRIVATE);
        DuraCylinder=findViewById(R.id.DuraCylinder);
        OXygenCylinder=findViewById(R.id.OxygenCylinder);
        CO2Cylinder=findViewById(R.id.CO2Cylinder);
        Ammonia = findViewById(R.id.Ammonia);
        NitrogenCylinder =findViewById(R.id.NitrogenCylinder);
        ZeroAir=findViewById(R.id.Zero_Air_Cylinder);
        cd_dry_ice = findViewById(R.id.cd_dry_ice);
        hydro_test= findViewById(R.id.cd_hydrotest);

        DuraCylinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Producation_Main.this,Dura.class);
                startActivity(i);

            }
        });

        OXygenCylinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Producation_Main.this, FisrtPart.class);
                startActivity(i);
            }
        });
        CO2Cylinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i=new Intent(Producation_Main.this, FirstCo2.class);
                startActivity(i);

            }
        });
        NitrogenCylinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Producation_Main.this, FirstNitrogen.class);
                startActivity(i);
            }
        });
        ZeroAir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Producation_Main.this, FirstZeroAir.class);
                startActivity(i);
            }
        });
        Ammonia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Producation_Main.this, ammoniafirstpage.class);
                startActivity(i);
            }
        });
        cd_dry_ice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Producation_Main.this, DryIceFIrstScreen.class);
                startActivity(i);
            }
        });
        hydro_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Producation_Main.this, HydroMain.class);
                startActivity(i);
            }
        });






    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
        finish();
    }

}