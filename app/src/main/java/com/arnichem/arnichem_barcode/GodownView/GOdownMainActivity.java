package com.arnichem.arnichem_barcode.GodownView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.arnichem.arnichem_barcode.GodownView.Closing_stock.Closing_stock;
import com.arnichem.arnichem_barcode.GodownView.Closing_stock.NewClosingStock.ClosingStockMain;
import com.arnichem.arnichem_barcode.GodownView.GodownFullRecipt.FullReciptMain;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryMainActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.Main;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;

public class GOdownMainActivity extends AppCompatActivity {
    CardView godownempty,godowndelivery,godownFullRecipt,Closing_stock,outward,inward;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_godown_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Godown");
        godownempty=findViewById(R.id.godownempty);
        godowndelivery=findViewById(R.id.godowdelivery);
        godownFullRecipt=findViewById(R.id.godownfullrecipt);
        Closing_stock=findViewById(R.id.closing_stock);
        outward=findViewById(R.id.outward);
        inward=findViewById(R.id.inward);
        outward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(GOdownMainActivity.this, Main.class);
                startActivity(i);
            }
        });
        inward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(GOdownMainActivity.this, com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardMain.class);
                startActivity(i);
            }
        });

        godownempty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(GOdownMainActivity.this, com.arnichem.arnichem_barcode.GodownView.godownempty.GodownEmptyMainActivity.class);
                startActivity(i);

            }
        });
        godowndelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(GOdownMainActivity.this, GodownDeliveryMainActivity.class);
                startActivity(i);

            }
        });
        godownFullRecipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(GOdownMainActivity.this, FullReciptMain.class);
                startActivity(i);

            }
        });
        Closing_stock.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 Intent i=new Intent(GOdownMainActivity.this, ClosingStockMain.class);
                                                 startActivity(i);
                                             }
                                         }
        );
        SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(0));
        SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(0));
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }
}