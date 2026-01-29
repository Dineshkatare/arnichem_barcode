package com.arnichem.arnichem_barcode.other_entries;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.arnichem.arnichem_barcode.R;

public class OtherEntriesSelectionActivity extends AppCompatActivity {

    CardView cardOtherDelivery, cardCustEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_entries_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Entry");
        }

        cardOtherDelivery = findViewById(R.id.cardOtherDelivery);
        cardCustEmpty = findViewById(R.id.cardCustEmpty);

        cardOtherDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherEntriesSelectionActivity.this, OtherEntryActivity.class);
                startActivity(intent);
            }
        });

        cardCustEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherEntriesSelectionActivity.this, CustomerEmptyActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
