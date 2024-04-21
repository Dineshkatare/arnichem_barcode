package com.arnichem.arnichem_barcode.view;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.arnichem.arnichem_barcode.Company.SelectCompanyActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.MyApplication;
import com.arnichem.arnichem_barcode.util.SharedPref;

import java.util.Calendar;

public class SplashScreen extends AppCompatActivity {
    SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
        MyApplication myApplication=new MyApplication();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(myApplication);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        pref = getSharedPreferences(constant.TAG,MODE_PRIVATE);
        String status = SharedPref.getInstance(this).LoggedInUser();
        Calendar currentTime = Calendar.getInstance();

        // Set the time to 10 AM
        Calendar tenAM = Calendar.getInstance();
        tenAM.set(Calendar.HOUR_OF_DAY, 10);
        tenAM.set(Calendar.MINUTE, 0);
        tenAM.set(Calendar.SECOND, 0);
        tenAM.set(Calendar.MILLISECOND, 0);

        if (currentTime.before(tenAM)) {
            AlarmReceiver.setAlarm(SplashScreen.this,10, 0);
        } else if (currentTime.after(tenAM)) {
            AlarmReceiver.setAlarm(SplashScreen.this,18, 0);

        } else {
            System.out.println("The current time is exactly 10 AM." +
                    "");
        }
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
//                    if(SharedPref.getInstance(SplashScreen.this).isSelectedCompany())
//                    {
                        if (status.equals("success"))
                        {
                            Intent intent = new Intent(getApplicationContext(),Dashboard.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(),SelectCompanyActivity.class);
                            startActivity(intent);
                        }
//                    } else
//                    {
//                        Intent intent = new Intent(getApplicationContext(),SelectCompanyActivity.class);
//                        startActivity(intent);
//                    }
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                super.run();
            }
        }.start();
    }
}