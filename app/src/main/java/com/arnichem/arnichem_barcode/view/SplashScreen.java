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
import com.arnichem.arnichem_barcode.order.OrderViewActivity;
import com.arnichem.arnichem_barcode.view.Dashboard;

public class SplashScreen extends AppCompatActivity {
    SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
        MyApplication myApplication=new MyApplication();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(myApplication);

        checkNotificationIntent();

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
                            Intent nextIntent;
                            String eventType = getIntent().getStringExtra("event_type");
                            
                            if ("new_order".equals(eventType)) {
                                nextIntent = new Intent(getApplicationContext(), OrderViewActivity.class);
                                nextIntent.putExtra("order_id", getIntent().getStringExtra("order_id"));
                            } else {
                                nextIntent = new Intent(getApplicationContext(), Dashboard.class);
                            }
                            startActivity(nextIntent);
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

    private void checkNotificationIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("notification_id")) {
            String notificationId = intent.getStringExtra("notification_id");
            if (notificationId != null) {
                String dbHost = SharedPref.mInstance.getDBHost();
                String dbUsername = SharedPref.mInstance.getDBUsername();
                String dbPassword = SharedPref.mInstance.getDBPassword();
                String dbName = SharedPref.mInstance.getDBName();

                com.arnichem.arnichem_barcode.Reset.APIInterface apiInterface =
                        com.arnichem.arnichem_barcode.Reset.APIClient.getClient().create(com.arnichem.arnichem_barcode.Reset.APIInterface.class);

                retrofit2.Call<okhttp3.ResponseBody> call = apiInterface.updateNotificationStatus(
                        notificationId, "opened", dbHost, dbUsername, dbPassword, dbName);

                call.enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                        // Status updated successfully
                    }

                    @Override
                    public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                        // Error updating status
                    }
                });
            }
        }
    }
}