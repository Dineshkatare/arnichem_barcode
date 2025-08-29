package com.arnichem.arnichem_barcode.TransactionsView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.DryIce.DryIceDelivery;
import com.arnichem.arnichem_barcode.TransactionsView.Duraempty.duraemptymain;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.AddClyHelper;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.EmptyMain;
import com.arnichem.arnichem_barcode.TransactionsView.Liquid_Delivery.LiquidDel_main;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.Main;
import com.arnichem.arnichem_barcode.TransactionsView.validate_dc.FirstValidateDcActivity;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;

public class Transactions extends AppCompatActivity implements Listener, LocationData.AddressCallBack{
    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    CardView outward,inward,delivery,empty,fullrecipt,DuraDelivery,Duraempty,LiquidDelivery,ammoniaDelivery,dryIceDelivery,validateDc;
    AddClyHelper addClymyDB;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Transactions");
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false,true,this);
        outward=findViewById(R.id.outward);
        addClymyDB=new AddClyHelper(Transactions.this);
      //  addClymyDB.deleteAllData();
        inward=findViewById(R.id.inward);
        delivery=findViewById(R.id.delivery);
        empty=findViewById(R.id.empty);
        fullrecipt=findViewById(R.id.FullRecipt);
        Duraempty=findViewById(R.id.Duraempty);
        DuraDelivery=findViewById(R.id.DuraDelivery);
        LiquidDelivery=findViewById(R.id.LiquidDel);
        ammoniaDelivery=findViewById(R.id.ammoniaDelivery);
        dryIceDelivery=findViewById(R.id.dryIceDelivery);
        validateDc = findViewById(R.id.validateDc);

        outward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Transactions.this, Main.class);
                startActivity(i);
            }
        });
        inward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Transactions.this, com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardMain.class);
                startActivity(i);
            }
        });
        delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Transactions.this, com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery.class);
                startActivity(i);
            }
        });
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Transactions.this, com.arnichem.arnichem_barcode.TransactionsView.Empty.EmptyMain.class);
                startActivity(i);
            }
        });
        Duraempty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Transactions.this, duraemptymain.class);
                startActivity(i);
            }
        });
        fullrecipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Transactions.this, com.arnichem.arnichem_barcode.TransactionsView.FullRecipt.FullReciptMainActivity.class);
                startActivity(i);

            }
        });
        LiquidDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Transactions.this, LiquidDel_main.class);
                startActivity(i);
            }
        });
        DuraDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Transactions.this, com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery.DuraDeliveryMain.class);
                startActivity(i);

            }
        });
        ammoniaDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Transactions.this, com.arnichem.arnichem_barcode.TransactionsView.deliveryAmmonia.AmmoniaMaindelivery.class);
                startActivity(i);

            }
        });
        dryIceDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Transactions.this, DryIceDelivery.class);
                startActivity(i);

            }
        });
        validateDc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Transactions.this, FirstValidateDcActivity.class);
                startActivity(i);

            }
        });

        SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(0));
        SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(0));

    }
    @Override
    protected void onResume() {
        super.onResume();
        easyWayLocation.startLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        easyWayLocation.endUpdates();
    }

    @Override
    public void locationOn() {
        Toast.makeText(this, "Location On", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void currentLocation(Location location) {
        getLocationDetail.getAddress(location.getLatitude(), location.getLongitude(), "xyz");
    }

    @Override
    public void locationCancelled() {
        Toast.makeText(this, "Location Cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void locationData(LocationData locationData) {

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }
}