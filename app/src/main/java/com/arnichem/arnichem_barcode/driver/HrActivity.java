package com.arnichem.arnichem_barcode.driver;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.attendance.Attendance_log;
import com.arnichem.arnichem_barcode.leave.LeaveApplicationActivity;
import com.arnichem.arnichem_barcode.order.PickActivity;
import com.arnichem.arnichem_barcode.other_entries.OtherEntryActivity;
import com.arnichem.arnichem_barcode.view.Dashboard;

public class HrActivity extends AppCompatActivity {
    CardView attendance,leave;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr);

        attendance  = findViewById(R.id.attendanceCard);
        leave = findViewById(R.id.leaveCard);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("HR");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(HrActivity.this, PickActivity.class);
                startActivity(i);

            }
        });
        attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (isGpsEnabled) {
                    Intent i=new Intent(HrActivity.this, Attendance_log.class);
                    startActivity(i);
                } else {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "Please enable GPS", Toast.LENGTH_SHORT).show();
                }


            }
        });
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(HrActivity.this, LeaveApplicationActivity.class);
                startActivity(i);
            }
        });




    }
}