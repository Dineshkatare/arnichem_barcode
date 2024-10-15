package com.arnichem.arnichem_barcode.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.arnichem.arnichem_barcode.Company.SelectCompanyActivity;
import com.arnichem.arnichem_barcode.Settings.newPassword;
import com.arnichem.arnichem_barcode.view.Dashboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyApplication extends Application implements LifecycleObserver {

    private static Application sApplication;

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreatePerformTask() {
        // here for demonstration purpose the Log messages are printed in logcat
        // one may perform their own custom tasks
        Log.i("dinesh", "I\'m inside Observer of MainActivity ON_CREATE");
        SimpleDateFormat df = new SimpleDateFormat("dd", Locale.getDefault());
        String datestr=df.format(new Date());
        String loginDate=SharedPref.getInstance(sApplication).getLoginDate();
        if(!datestr.equalsIgnoreCase(loginDate))
        {
            SharedPref.getInstance(sApplication).logout();
            Intent intent = new Intent(sApplication,SelectCompanyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
            sApplication.startActivity(intent);


        }

    }

    // To observe the onResume state of MainActivity
    // and perform the assigned tasks
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResumePerformTask() {
        // here for demonstration purpose the Log messages are printed in logcat
        // one may perform their own custom tasks
        Log.i("dinesh", "I\'m inside Observer of MainActivity ON_RESUME");
    }


    // To observe the onResume state of MainActivity
    // and perform the assigned tasks
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void ON_STOP() {
        // here for demonstration purpose the Log messages are printed in logcat
        // one may perform their own custom tasks
        Log.i("dinesh", "I\'m inside Observer of MainActivity STOP");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void ON_START() {
        // here for demonstration purpose the Log messages are printed in logcat
        // one may perform their own custom tasks
        SimpleDateFormat df = new SimpleDateFormat("dd", Locale.getDefault());
        String datestr=df.format(new Date());
        String loginDate=SharedPref.getInstance(sApplication).getLoginDate();
        if(!datestr.equalsIgnoreCase(loginDate))
        {
            SharedPref.getInstance(sApplication).logout();
            Intent intent = new Intent(sApplication,SelectCompanyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
            sApplication.startActivity(intent);


        }
        Log.i("dinesh", "I\'m inside Observer of MainActivity START");

    }



}
