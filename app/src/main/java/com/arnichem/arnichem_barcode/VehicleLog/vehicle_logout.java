package com.arnichem.arnichem_barcode.VehicleLog;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;

public class vehicle_logout extends AppCompatActivity {
    ProgressDialog dialog;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_logout);
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        dialog = new ProgressDialog(vehicle_logout.this);
        dialog.setTitle("Vehicle User Logout");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        SharedPref.getInstance(getApplicationContext()).storeVStatus("failed");
        dialog.show();
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                    startActivity(intent);
                    finish();
                    dialog.dismiss();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                super.run();
            }
        }.start();
    }
}