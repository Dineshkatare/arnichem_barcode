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
import com.arnichem.arnichem_barcode.report.ReportActivity;
import com.arnichem.arnichem_barcode.view.Dashboard;

public class HrActivity extends AppCompatActivity {
    CardView attendance, leave, hr_report;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr);

        attendance = findViewById(R.id.attendanceCard);
        leave = findViewById(R.id.leaveCard);
        hr_report = findViewById(R.id.hr_reports);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("HR");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HrActivity.this, PickActivity.class);
                startActivity(i);

            }
        });
        hr_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = com.arnichem.arnichem_barcode.util.SharedPref.getInstance(HrActivity.this).getEmail();
                String companyName = com.arnichem.arnichem_barcode.util.SharedPref.getInstance(HrActivity.this)
                        .getCompanyShortName();
                String url = "https://www.arnichem.co.in/intranet/reports_hr_app.php?email=" + email + "&company_name="
                        + companyName;

                Intent intent = new Intent(HrActivity.this, ReportActivity.class);
                intent.putExtra("title", "HR Reports");
                intent.putExtra("url", url);
                startActivity(intent);

            }
        });
        attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (isGpsEnabled) {
                    Intent i = new Intent(HrActivity.this, Attendance_log.class);
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
                Intent i = new Intent(HrActivity.this, LeaveApplicationActivity.class);
                startActivity(i);
            }
        });

    }
}