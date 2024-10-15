package com.arnichem.arnichem_barcode.view;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.arnichem.arnichem_barcode.Barcode.ScannerView;
import com.arnichem.arnichem_barcode.CRM.CRM_Main;
import com.arnichem.arnichem_barcode.CallLogSyncReceiver;
import com.arnichem.arnichem_barcode.CallLogSyncService;

import com.arnichem.arnichem_barcode.Company.SelectCompanyActivity;
import com.arnichem.arnichem_barcode.CustomerHolding.MainHoldingScreen;
import com.arnichem.arnichem_barcode.DieselEntry.MainDieselEntry;
import com.arnichem.arnichem_barcode.FileUpload.FIleUploadMainActivity;
import com.arnichem.arnichem_barcode.GetData.Test;
import com.arnichem.arnichem_barcode.GodownView.GOdownMainActivity;
import com.arnichem.arnichem_barcode.Googlepay.GooglepayScreen;
import com.arnichem.arnichem_barcode.PaymentReceipt.MainPaymentReceipt;
import com.arnichem.arnichem_barcode.PrintReceipt.MainPrintActivity;
import com.arnichem.arnichem_barcode.Producation.Producation_Main;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.Settings.MainSettings;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliveryprint;
import com.arnichem.arnichem_barcode.VehicleLog.check;
import com.arnichem.arnichem_barcode.VehicleLog.vehicle_logout;
import com.arnichem.arnichem_barcode.attendance.Attendance_log;
import com.arnichem.arnichem_barcode.attendance.MyResponseModel;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.data.ReportAccess;
import com.arnichem.arnichem_barcode.data.response.ReportResponse;
import com.arnichem.arnichem_barcode.leave.LeaveApplicationActivity;
import com.arnichem.arnichem_barcode.other_entries.OtherEntryActivity;
import com.arnichem.arnichem_barcode.report.ReportActivity;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
import com.example.myapplication.reset.ApiClient;
import com.google.android.material.snackbar.Snackbar;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dashboard extends AppCompatActivity implements Listener, LocationData.AddressCallBack{
    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    CardView vehicle,Barcode,transactions,Producation,GooglePay,Godown,logout,file_upload,setting,PrintReceipt,Payment_Receipt,CRM,DieselEntry,customerHoldCl,logCL,otherCl,report,leave;
    SharedPreferences pref;
    ScrollView scrollView;
    boolean doubleBackToExitPressedOnce = false;
    ProgressDialog dialog;

    String status;
    ImageView vehiclelogimageview;
    TextView textView;
    LocationManager locationManager;
    private static final int CAMERA_PERMISSION_CODE = 100;
    APIInterface apiInterface;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false,true,this);
        scrollView=findViewById(R.id.dashlayout);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        vehiclelogimageview=findViewById(R.id.vehiclelog);
        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        pref = getSharedPreferences(constant.TAG,MODE_PRIVATE);
        status = SharedPref.getInstance(Dashboard.this).vLoggedInUser();
        textView=findViewById(R.id.vehicleloginlogout);
        vehicle=findViewById(R.id.vehicledetails);
        Barcode=findViewById(R.id.BarcodeRegistration);
        file_upload = findViewById(R.id.file_upload);
        transactions=findViewById(R.id.Transactions);
        customerHoldCl = findViewById(R.id.customerHoldCl);
        otherCl = findViewById(R.id.other_cl);
        report = findViewById(R.id.report);
        logout=findViewById(R.id.Logout);
        Producation =findViewById(R.id.Producations);
        GooglePay=findViewById(R.id.googlepay);
        Payment_Receipt=findViewById(R.id.PrintReceipt);
        setting =findViewById(R.id.setting);
        Godown=findViewById(R.id.Godown);
        PrintReceipt=findViewById(R.id.print);
        DieselEntry = findViewById(R.id.Diesel);
        logCL = findViewById(R.id.log_cl);
        CRM=findViewById(R.id.CRM);
        leave = findViewById(R.id.leave);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(SharedPref.getInstance(Dashboard.this).get_report_status().equalsIgnoreCase("1")){
            report.setVisibility(View.VISIBLE);
        }
//        Toast.makeText(Dashboard.this, ""+SharedPref.getInstance(Dashboard.this).getCompanyFullName(), Toast.LENGTH_SHORT).show();

        if(status.equals("success"))
        {
            vehiclelogimageview.setImageDrawable(ContextCompat.getDrawable(Dashboard.this, R.drawable.logout));
            textView.setText("Vehicle Logout");
            Snackbar.make(scrollView, SharedPref.getInstance(Dashboard.this).FirstName()+" "+SharedPref.getInstance(Dashboard.this).LastName()+" तुमचा  गाडी  नंबर "+SharedPref.getInstance(this).getVehicleNo()+" सोबत रजिस्टर झाला आहे ", Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN).setTextColor(Color.BLACK).show();
            vehiclelogimageview.setPadding(20,20,20,20);
            vehicle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(Dashboard.this, vehicle_logout.class);
                    startActivity(i);
                }
            });
        }
        else
        {
            vehiclelogimageview.setImageDrawable(ContextCompat.getDrawable(Dashboard.this, R.drawable.truck));
            textView.setText("Vehicle Login");
            vehicle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(Dashboard.this, check.class);
                    startActivity(i);
                }
            });
        }
        PrintReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Dashboard.this, MainPrintActivity.class);
                startActivity(i);

            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Dashboard.this, MainSettings.class);
                startActivity(i);
            }
        });
        Godown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Dashboard.this, GOdownMainActivity.class);
                startActivity(i);

            }
        });
        otherCl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Dashboard.this, OtherEntryActivity.class);
                startActivity(i);
            }
        });


        Barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Dashboard.this, ScannerView.class);
                startActivity(i);
            }
        });
        CRM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Dashboard.this, CRM_Main.class);
                startActivity(i);
            }
        });
        customerHoldCl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Dashboard.this, MainHoldingScreen.class);
                startActivity(i);
            }
        });
        transactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (status.equals("success"))
                {
                    Intent i=new Intent(Dashboard.this, Transactions.class);
                    startActivity(i);
                }
                else {
                    Snackbar.make(scrollView, "कृपया वाहन माहिती टाका !", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();

                }

            }
        });
        Producation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Dashboard.this, Producation_Main.class);
                startActivity(i);
                finish();
            }
        });
        GooglePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Dashboard.this, GooglepayScreen.class);
                startActivity(i);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(status.equals("success")) {
                    Snackbar.make(scrollView, "कृपया वाहन माहिती लॉगऑऊट टाका !", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                }
                else
                {
                    SharedPref.getInstance(getApplicationContext()).logout();
                    startActivity(new Intent(Dashboard.this, SelectCompanyActivity.class));
                    finish();

                }


            }
        });

        Payment_Receipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Dashboard.this, MainPaymentReceipt.class);
                startActivity(i);
            }
        });

        file_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Dashboard.this, FIleUploadMainActivity.class);
                startActivity(i);

            }
        });
        DieselEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (status.equals("success"))
//                {
                    Intent i=new Intent(Dashboard.this, MainDieselEntry.class);
                    startActivity(i);
//                }
//                else {
//                    Snackbar.make(scrollView, "कृपया वाहन माहिती टाका !", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
//
//                }
            }
        });
        logCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (isGpsEnabled) {
                    Intent i=new Intent(Dashboard.this, Attendance_log.class);
                    startActivity(i);

                    // GPS is enabled, perform your action here
                } else {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);

                    // GPS is not enabled, show a message or request activation
                    Toast.makeText(getApplicationContext(), "Please enable GPS", Toast.LENGTH_SHORT).show();
                }


            }
        });
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Dashboard.this, ReportActivity.class);
                startActivity(i);
            }
        });
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Dashboard.this, LeaveApplicationActivity.class);
                startActivity(i);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);

            }
        }
        getReport();
// Set the second alarm at 3:30 PM

        // Schedule the call log sync worker to run every 5 hours
//        Intent serviceIntent = new Intent(this, CallLogSyncService.class);
//        startService(serviceIntent);

    }
    @Override
    protected void onResume() {
        super.onResume();
        easyWayLocation.startLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        easyWayLocation.endUpdates();
    }

    @Override
    public void locationOn() {
        Toast.makeText(this, "Location On", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void currentLocation(Location location) {
        getLocationDetail.getAddress(location.getLatitude(), location.getLongitude(), "xyz");
    }

    @Override
    public void locationCancelled() {
        Toast.makeText(this, "Location Cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void locationData(LocationData locationData) {

    }
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(Dashboard.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(Dashboard.this, new String[] { permission }, requestCode);
        }
        else {

//            Toast.makeText(Scanner
//
//            View.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

//                Toast.makeText(ScannerView.this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(Dashboard.this, "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.newmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.sync) {
            startActivity(new Intent(Dashboard.this, Test.class));

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private void getReport() {
        dialog = new ProgressDialog(Dashboard.this);
        dialog.setTitle("Fetching Data");
        dialog.setMessage("Please wait...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        // Fetch the required data from Shared Preferences
        String user = SharedPref.getInstance(Dashboard.this).getEmail();
        String company = SharedPref.getInstance(Dashboard.this).getCompanyShortName();
        String dbHost = SharedPref.mInstance.getDBHost();
        String dbUsername = SharedPref.mInstance.getDBUsername();
        String dbPassword = SharedPref.mInstance.getDBPassword();
        String dbName = SharedPref.mInstance.getDBName();

        // Make the API call
        Call<ReportResponse> call = apiInterface.postReport(user, company, dbHost, dbUsername, dbPassword, dbName);
        call.enqueue(new Callback<ReportResponse>() {
            @Override
            public void onResponse(Call<ReportResponse> call, Response<ReportResponse> response) {
                dialog.dismiss();
                ReportResponse reportResponse = response.body();
                if (reportResponse != null) {
                    // Assuming getData() returns the data object that contains the report access

                    // Save the report status and update the UI
                    SharedPref.getInstance(Dashboard.this).store_report_status(String.valueOf(reportResponse.getReportsAccess()));
                    if (reportResponse.getReportsAccess() == 1) {
                        report.setVisibility(View.VISIBLE);
                    } else {
                        report.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(Dashboard.this, "Failed to get a valid response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReportResponse> call, Throwable t) {
                dialog.dismiss();
                // Handle the error
                Toast.makeText(Dashboard.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        scheduleCallLogSync();
    }


    private void scheduleCallLogSync() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, CallLogSyncReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the alarm to trigger every minute (60000 ms)
        long interval = 9000L;
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + interval,
                interval,
                pendingIntent);
    }


}