package com.arnichem.arnichem_barcode.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.order.OrderMainActivity;
import com.arnichem.arnichem_barcode.order.PickActivity;
import com.arnichem.arnichem_barcode.other_entries.OtherEntryActivity;
import com.arnichem.arnichem_barcode.view.Dashboard;

public class DriverInstructions extends AppCompatActivity {
    CardView pickCard,orderCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_instructions);
        pickCard  = findViewById(R.id.pick);
        orderCard = findViewById(R.id.orderCard);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Driver Instructions");


        pickCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(DriverInstructions.this, PickActivity.class);
                startActivity(i);

            }
        });

        orderCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(DriverInstructions.this, OrderMainActivity.class);
                startActivity(i);
            }
        });

    }
}