package com.arnichem.arnichem_barcode.FileUpload;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.arnichem.arnichem_barcode.R;

public class FIleUploadMainActivity extends AppCompatActivity {

    LinearLayout delivery, empty, outward, inward, bmr,dcrf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("File Upload");
        delivery = findViewById(R.id.delivery);
        empty = findViewById(R.id.empty);
        outward = findViewById(R.id.outward);
        inward = findViewById(R.id.inward);
        bmr = findViewById(R.id.bmr);
        dcrf =findViewById(R.id.dcrf);
        delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFun("DC");
            }
        });
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFun("EMPB");
            }
        });
        outward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFun("OUTWARD");
            }
        });
        inward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFun("INWARD");
            }
        });
        bmr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFun("BMR");
            }
        });
        dcrf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFun("DCRF");
            }
        });


    }

    private void startActivityFun(String type) {
        Intent intent = new Intent(FIleUploadMainActivity.this, FileUploadSubActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}