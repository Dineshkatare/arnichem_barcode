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
import com.arnichem.arnichem_barcode.report.ReportActivity;
import com.arnichem.arnichem_barcode.view.Dashboard;

public class DriverInstructions extends AppCompatActivity {
    CardView pickCard,orderCard,reports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_instructions);
        pickCard  = findViewById(R.id.pick);
        orderCard = findViewById(R.id.orderCard);
        reports = findViewById(R.id.report);
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
        reports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverInstructions.this, ReportActivity.class);
                intent.putExtra("title", "Reports");
                intent.putExtra("url", "https://www.arnichem.co.in/intranet/reports_orders_app.php");
                startActivity(intent);

            }
        });

    }
}