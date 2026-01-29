package com.arnichem.arnichem_barcode.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.arnichem.arnichem_barcode.Barcode.ScannerView;
import com.arnichem.arnichem_barcode.CRM.CRM_Main;
import com.arnichem.arnichem_barcode.CallLogManager;
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
import com.arnichem.arnichem_barcode.VoucherActivity;
import com.arnichem.arnichem_barcode.attendance.Attendance_log;
import com.arnichem.arnichem_barcode.attendance.MyResponseModel;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.data.ReportAccess;
import com.arnichem.arnichem_barcode.data.response.ReportResponse;
import com.arnichem.arnichem_barcode.data.response.TaskCountResponse;

import com.arnichem.arnichem_barcode.driver.DriverInstructions;
import com.arnichem.arnichem_barcode.driver.HrActivity;
import com.arnichem.arnichem_barcode.leave.LeaveApplicationActivity;
import com.arnichem.arnichem_barcode.order.OrderMainActivity;
import com.arnichem.arnichem_barcode.order.PickActivity;
import com.arnichem.arnichem_barcode.other_entries.OtherEntryActivity;
import com.arnichem.arnichem_barcode.report.ReportActivity;
import com.arnichem.arnichem_barcode.report.ResourceActivity;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
import com.example.myapplication.reset.ApiClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dashboard extends AppCompatActivity implements Listener, LocationData.AddressCallBack {
    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    CardView vehicle, Barcode, transactions, Producation, GooglePay, Godown, file_upload, setting, Payment_Receipt, CRM,
            DieselEntry, customerHoldCl, otherCl, report, order, resource, contactSearchCl, tasksCl, printhistory;
    SharedPreferences pref;
    ScrollView scrollView;
    boolean doubleBackToExitPressedOnce = false;
    ProgressDialog dialog;
    private static final int PERMISSION_REQUEST_READ_CALL_LOG = 100;

    String status;
    ImageView vehiclelogimageview;
    TextView textView, tvTaskCount;
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
        easyWayLocation = new EasyWayLocation(this, false, true, this);
        scrollView = findViewById(R.id.dashlayout);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        TextView tvCompany = findViewById(R.id.tvCompanyName);
        TextView tvUsername = findViewById(R.id.tvUserName);
        TextView tvVehicle = findViewById(R.id.tvVehicleNumber);
        TextView tvVersion = findViewById(R.id.tvAppVersion);
        ImageView icSync = findViewById(R.id.btnSync);
        ImageView icMenu = findViewById(R.id.btnMenu);

        // Set values dynamically
        tvCompany.setText(SharedPref.getInstance(this).getCompanyFullName());
        tvUsername.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        if (!SharedPref.getInstance(this).getVehicleNo().isEmpty()) {
            tvVehicle.setText(SharedPref.getInstance(this).getVehicleNo() + " |");
        } else {
            tvVehicle.setText("NO VEHICLE |");
        }
        tvVersion.setText(" Version :" + "10.0");

        // click listeners
        icSync.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, Test.class)));
        icMenu.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, MainSettings.class)));

        LinearLayout toolbarLayout = findViewById(R.id.toolbar);

        // Example hex color (you can change this dynamically)
        String hexColor = SharedPref.getInstance(this).getBgColor();
        int bgColor;
        try {
            if (hexColor != null && !hexColor.isEmpty()) {
                bgColor = Color.parseColor(hexColor);
            } else {
                bgColor = Color.parseColor("#2E3192"); // Default to a safe color (Dark Blue)
            }
        } catch (IllegalArgumentException e) {
            bgColor = Color.parseColor("#2E3192");
        }

        // Create rounded background shape
        float cornerRadius = getResources().getDisplayMetrics().density * 20; // 20dp
        float[] radii = new float[] {
                0f, 0f, // top-left
                0f, 0f, // top-right
                cornerRadius, cornerRadius, // bottom-right
                cornerRadius, cornerRadius // bottom-left
        };

        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setColor(bgColor);
        bgShape.setCornerRadii(radii);

        // Apply background to layout
        toolbarLayout.setBackground(bgShape);

        vehiclelogimageview = findViewById(R.id.vehiclelog);
        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        status = SharedPref.getInstance(Dashboard.this).vLoggedInUser();
        textView = findViewById(R.id.vehicleloginlogout);
        vehicle = findViewById(R.id.vehicledetails);
        Barcode = findViewById(R.id.BarcodeRegistration);
        file_upload = findViewById(R.id.file_upload);
        transactions = findViewById(R.id.Transactions);
        customerHoldCl = findViewById(R.id.customerHoldCl);
        otherCl = findViewById(R.id.other_cl);
        report = findViewById(R.id.report);
        // logout=findViewById(R.id.Logout);
        Producation = findViewById(R.id.Producations);
        GooglePay = findViewById(R.id.googlepay);
        // Payment_Receipt=findViewById(R.id.PrintReceipt);
        setting = findViewById(R.id.setting);
        Godown = findViewById(R.id.Godown);

        DieselEntry = findViewById(R.id.Diesel);
        CRM = findViewById(R.id.CRM);
        order = findViewById(R.id.order);
        resource = findViewById(R.id.resource);
        contactSearchCl = findViewById(R.id.contactSearchCl);
        tasksCl = findViewById(R.id.tasksCl);
        tvTaskCount = findViewById(R.id.tvTaskCount);
        printhistory = findViewById(R.id.printhistory);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (SharedPref.getInstance(Dashboard.this).get_report_status().equalsIgnoreCase("1")) {
            report.setVisibility(View.VISIBLE);
        }

        Log.d("chech", "0" + SharedPref.getInstance(Dashboard.this).get_show_msg_status());
        if (SharedPref.getInstance(Dashboard.this).get_show_msg_status().equalsIgnoreCase("0")) {
            SharedPref.getInstance(Dashboard.this).store_show_msg_status("1");
            if (SharedPref.getInstance(Dashboard.this).getLoginMsg() != null
                    && !SharedPref.getInstance(Dashboard.this).getLoginMsg().isEmpty()) {
                String message = SharedPref.getInstance(Dashboard.this).getLoginMsg(); // Replace literal "\n" with a
                                                                                       // newline
                String message1 = message.replace("\\n", "\n"); // Replace literal "\n" with a newline

                showCustomMsg(message1);
                Log.d("chech", "1" + message);
            }
        }
        // Toast.makeText(Dashboard.this,
        // ""+SharedPref.getInstance(Dashboard.this).getCompanyFullName(),
        // Toast.LENGTH_SHORT).show();

        if (status.equals("success")) {
            vehiclelogimageview.setImageDrawable(ContextCompat.getDrawable(Dashboard.this, R.drawable.logout));
            textView.setText("Vehicle Logout");
            Snackbar.make(scrollView,
                    SharedPref.getInstance(Dashboard.this).FirstName() + " "
                            + SharedPref.getInstance(Dashboard.this).LastName() + " तुमचा  गाडी  नंबर "
                            + SharedPref.getInstance(this).getVehicleNo() + " सोबत रजिस्टर झाला आहे ",
                    Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN).setTextColor(Color.BLACK).show();
            vehiclelogimageview.setPadding(20, 20, 20, 20);
            vehicle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Dashboard.this, vehicle_logout.class);
                    startActivity(i);
                }
            });
        } else {
            vehiclelogimageview.setImageDrawable(ContextCompat.getDrawable(Dashboard.this, R.drawable.truck));
            textView.setText("Vehicle Login");
            vehicle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Dashboard.this, check.class);
                    startActivity(i);
                }
            });
        }

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this, HrActivity.class);
                startActivity(i);

            }
        });
        Godown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this, GOdownMainActivity.class);
                startActivity(i);

            }
        });
        otherCl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this,
                        com.arnichem.arnichem_barcode.other_entries.OtherEntriesSelectionActivity.class);
                startActivity(i);
            }
        });

        Barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Dashboard.this, ScannerView.class);
                startActivity(i);
            }
        });
        CRM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Dashboard.this, CRM_Main.class);
                startActivity(i);
            }
        });
        customerHoldCl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this, MainHoldingScreen.class);
                startActivity(i);
            }
        });
        transactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (status.equals("success")) {
                    Intent i = new Intent(Dashboard.this, Transactions.class);
                    startActivity(i);
                } else {
                    Snackbar.make(scrollView, "कृपया वाहन माहिती टाका !", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();

                }

            }
        });
        Producation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Dashboard.this, Producation_Main.class);
                startActivity(i);
                finish();
            }
        });
        GooglePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this, VoucherActivity.class);
                startActivity(i);
            }
        });
        // logout.setOnClickListener(new View.OnClickListener() {
        // @Override
        // public void onClick(View v) {
        //
        // if(status.equals("success")) {
        // Snackbar.make(scrollView, "कृपया वाहन माहिती लॉगऑऊट टाका !",
        // Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
        // }
        // else
        // {
        // SharedPref.getInstance(getApplicationContext()).logout();
        // startActivity(new Intent(Dashboard.this, SelectCompanyActivity.class));
        // finish();
        //
        // }
        //
        //
        // }
        // });

        // Payment_Receipt.setOnClickListener(new View.OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // Intent i=new Intent(Dashboard.this,
        // com.arnichem.arnichem_barcode.PaymentReceipt.PaymentsActivity.class);
        // startActivity(i);
        // }
        // });

        resource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this, ResourceActivity.class);
                startActivity(i);
            }
        });
        file_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this, FIleUploadMainActivity.class);
                startActivity(i);

            }
        });
        DieselEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if (status.equals("success"))
                // {
                Intent i = new Intent(Dashboard.this, MainDieselEntry.class);
                startActivity(i);
                // }
                // else {
                // Snackbar.make(scrollView, "कृपया वाहन माहिती टाका !",
                // Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                //
                // }
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this, ReportActivity.class);
                startActivity(i);
            }
        });

        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this, DriverInstructions.class);
                startActivity(i);

            }
        });

        contactSearchCl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this, ContactSearchActivity.class);
                startActivity(i);
            }
        });

        tasksCl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this,
                        com.arnichem.arnichem_barcode.CustomerHolding.TasksWebViewActivity.class);
                startActivity(i);
            }
        });

        printhistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Dashboard.this, MainPrintActivity.class);
                startActivity(i);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, 1);

            }
        }
        getReport();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            fetchAndUploadCallLogs(this);

        } else {
            retrieveAndUploadLogs(this);

            // Permission already granted
        }

        // Set the second alarm at 3:30 PM

        // Schedule the call log sync worker to run every 5 hours
        // Intent serviceIntent = new Intent(this, CallLogSyncService.class);
        // startService(serviceIntent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        easyWayLocation.startLocation();
        fetchTaskCount();
        if (SharedPref.getInstance(this).getPersistentDeviceName().isEmpty()) {
            checkAndShowDeviceSelectionDialog();
        }
    }

    private void checkAndShowDeviceSelectionDialog() {
        String dbHost = SharedPref.mInstance.getDBHost();
        String dbUsername = SharedPref.mInstance.getDBUsername();
        String dbPassword = SharedPref.mInstance.getDBPassword();
        String dbName = SharedPref.mInstance.getDBName();

        Call<okhttp3.ResponseBody> call = apiInterface.getDeviceList(dbHost, dbUsername, dbPassword, dbName);
        call.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        org.json.JSONArray jsonArray = new org.json.JSONArray(jsonString);
                        final String[] deviceNames = new String[jsonArray.length()];
                        final String[] deviceNos = new String[jsonArray.length()];

                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject obj = jsonArray.getJSONObject(i);
                            deviceNames[i] = obj.getString("device_name");
                            deviceNos[i] = obj.getString("device_no");
                        }

                        showDeviceDialog(deviceNames, deviceNos);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(Dashboard.this, "Error parsing device list", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Toast.makeText(Dashboard.this, "Failed to fetch devices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeviceDialog(final String[] deviceNames, final String[] deviceNos) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Select Device");
        builder.setCancelable(false);
        builder.setItems(deviceNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedDevice = deviceNames[which];
                String selectedDeviceNo = deviceNos[which];
                saveDeviceSelection(selectedDevice, selectedDeviceNo, dialog);
            }
        });
        builder.show();
    }

    private void saveDeviceSelection(String deviceName, String deviceNo, DialogInterface dialogInterface) {
        // Save locally only, as per requirement
        SharedPref.getInstance(Dashboard.this).setPersistentDevice(deviceName, deviceNo);
        SharedPref.getInstance(Dashboard.this).setPhoneNumber(deviceNo);
        dialogInterface.dismiss();
        Toast.makeText(Dashboard.this, "Device Selected: " + deviceName, Toast.LENGTH_SHORT).show();

        // Sync call logs immediately after selection
        fetchAndUploadCallLogs(this);
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

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(Dashboard.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(Dashboard.this, new String[] { permission }, requestCode);
        } else {

            // Toast.makeText(Scanner
            //
            // View.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Toast.makeText(ScannerView.this, "Camera Permission Granted",
                // Toast.LENGTH_SHORT) .show();
            } else {
                Toast.makeText(Dashboard.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PERMISSION_REQUEST_READ_CALL_LOG) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with fetching and uploading call logs
                retrieveAndUploadLogs(this);
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission denied to read call logs.", Toast.LENGTH_SHORT).show();
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
        if (item.getItemId() == R.id.menu) {
            startActivity(new Intent(Dashboard.this, MainSettings.class));

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
                doubleBackToExitPressedOnce = false;
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
                    SharedPref.getInstance(Dashboard.this)
                            .store_report_status(String.valueOf(reportResponse.getReportsAccess()));
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

    }

    private void fetchAndUploadCallLogs(Context context) {
        // Check if permission to read call logs is granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if it's not granted
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_CALL_LOG },
                    PERMISSION_REQUEST_READ_CALL_LOG);
        } else {
            // Permission already granted, fetch and upload call logs
            retrieveAndUploadLogs(context);
        }
    }

    // Method to fetch and upload the call logs
    private void retrieveAndUploadLogs(Context context) {
        // Create a background thread pool
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Run the task in the background
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (SharedPref.getInstance(context).getPersistentDeviceName().isEmpty()) {
                    Log.d("call log", "Device not selected, skipping sync");
                    return;
                }

                CallLogManager callLogManager = new CallLogManager(Dashboard.this);
                List<CallLogManager.CallLogEntry> callLogs = callLogManager.getCallLogs();

                if (SharedPref.getInstance(context).get_call_log_access().equalsIgnoreCase("Y")) {
                    if (!callLogs.isEmpty()) {
                        // This method will run on a background thread
                        callLogManager.sendCallLogsToServer(callLogs);
                    }
                    Log.d("call log", "sync");
                } else {
                    Log.d("call log", "not sync");

                }
            }
        });

        // Shutdown the executor once done
        executorService.shutdown();
    }

    public void showCustomMsg(String msg) {
        // Create the MaterialAlertDialogBuilder for Material Design styling
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog);

        // Create the dialog and get the reference of the button
        builder.setMessage(msg);
        builder.setCancelable(false); // Disable dismissing the dialog with back button or touch
        builder.setNegativeButton("Okay", (dialog, which) -> dialog.dismiss()); // The default "Okay" button

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Get the "Okay" button from the dialog and make it initially show the
        // countdown value
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setVisibility(View.VISIBLE); // Ensure the button is visible
        negativeButton.setText("5 sec");

        // Set the button text color to white
        negativeButton.setTextColor(getResources().getColor(android.R.color.white)); // White text color

        // Apply the background drawable with rounded corners to the dialog window
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);

        TextView messageTextView = dialog.findViewById(android.R.id.message); // This accesses the dialog's message
                                                                              // TextView
        if (messageTextView != null) {
            messageTextView.setTextColor(getResources().getColor(android.R.color.white)); // Set message text color to
                                                                                          // white
        }

        // Countdown timer logic (5 seconds)
        final int[] countdown = { 5 }; // Array to hold the countdown value so it's accessible inside the Runnable
        final Handler handler = new Handler();

        // Run a countdown on the UI thread
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (countdown[0] > 0) {
                    // Update the button text to show the countdown
                    negativeButton.setText(countdown[0] + " sec");
                    countdown[0]--;
                    handler.postDelayed(this, 1000); // Run every second
                } else {
                    // After countdown ends, change the button text to "Okay"
                    negativeButton.setText("Okay");
                }
            }
        }, 1000); // Start the countdown with a delay of 1 second
    }

    // Handle the result of the permission request

    private void fetchTaskCount() {
        String user = SharedPref.getInstance(Dashboard.this).getEmail();
        String dbHost = SharedPref.mInstance.getDBHost();
        String dbUsername = SharedPref.mInstance.getDBUsername();
        String dbPassword = SharedPref.mInstance.getDBPassword();
        String dbName = SharedPref.mInstance.getDBName();

        Call<TaskCountResponse> call = apiInterface.getTaskCount(dbHost, dbUsername, dbPassword, dbName, user);
        call.enqueue(new Callback<TaskCountResponse>() {
            @Override
            public void onResponse(Call<TaskCountResponse> call, Response<TaskCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TaskCountResponse data = response.body();
                    if (data.getCount() != null && data.getCount() > 0) {
                        tvTaskCount.setText(String.valueOf(data.getCount()));
                        tvTaskCount.setVisibility(View.VISIBLE);
                    } else {
                        tvTaskCount.setVisibility(View.GONE);
                    }
                } else {
                    tvTaskCount.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<TaskCountResponse> call, Throwable t) {
                tvTaskCount.setVisibility(View.GONE);
                Log.e("Dashboard", "Task count failed", t);
            }
        });
    }

}
