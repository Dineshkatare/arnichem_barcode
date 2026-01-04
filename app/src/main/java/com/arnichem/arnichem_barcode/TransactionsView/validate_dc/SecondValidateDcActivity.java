package com.arnichem.arnichem_barcode.TransactionsView.validate_dc;

import static com.arnichem.arnichem_barcode.Reset.APIClient.delivery_validation;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.Barcode.LaserScannerActivity;
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.OnItemClickListener;
import com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint.ViewDeliveryPrint;
import com.arnichem.arnichem_barcode.PrintReceipt.MainPrintActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.Listener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Placeholder for CylinderData (replace with actual implementation)

@RequiresApi(api = Build.VERSION_CODES.O)
public class SecondValidateDcActivity extends AppCompatActivity implements OnItemClickListener , Listener {
    String latitude = "0", logitude = "0", address = "0";

            private RecyclerView recyclerView;
            private TextView dcNoTextView;
            private TextView invoiceNoTextView;
            private TextView customerNameTextView;
    private EasyWayLocation easyWayLocation;

    private EditText newScan;

    private Button submitButton;
            private Button uploadSign;
            private ConstraintLayout constraintSigned;
            private ImageView signedImg;
    private String inputHolder = "";

    private ImageView closeImg;
            private ProgressDialog dialog;
            private List<CylinderData> cylinderList = new ArrayList<>();
            private String digitalSignPath = "";
            private String digitalSignBase64 = "";
            private static final String TAG = "SecondValidateDcActivity";
            private boolean isAllFabsVisible = false;
            private FusedLocationProviderClient fusedLocationClient;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_second_validate_dc);

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Validate DC");

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
                        new IntentFilter("digital_sign"));
                easyWayLocation = new EasyWayLocation(this, false,true,this);


                // Initialize location client
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                dcNoTextView = findViewById(R.id.dcNoTextView);
                invoiceNoTextView = findViewById(R.id.invoiceNoTextView);
                customerNameTextView = findViewById(R.id.customerNameTextView);
                recyclerView = findViewById(R.id.recyclerView);
                submitButton = findViewById(R.id.submitButton);
                uploadSign = findViewById(R.id.uploadSign);
                constraintSigned = findViewById(R.id.constraintSigned);
                signedImg = findViewById(R.id.signedImg);
                closeImg = findViewById(R.id.closeImg);
                newScan = findViewById(R.id.newScan);
                
                // Fix: Use OnKeyListener instead of dispatchKeyEvent
                newScan.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                                && event.getAction() == KeyEvent.ACTION_UP) {
                            
                            String text = newScan.getText().toString().trim();
                            if (!text.isEmpty()) {
                                Log.d(TAG, "Validate Scan Detected: " + text);
                                validateCylinder(text);
                            }
                            newScan.setText("");
                            newScan.requestFocus();
                            return true;
                        }
                        return false;
                    }
                });

                // Focus Protection
                newScan.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                             newScan.postDelayed(() -> newScan.requestFocus(), 50);
                        }
                    }
                });



                Intent intent = getIntent();
                String dcno = intent.getStringExtra("dcno");
                String invoiceNo = intent.getStringExtra("invoiceNo");
                String customerName = intent.getStringExtra("customerName");
                String cylinderNumbersJson = intent.getStringExtra("cylinderNumbers");

                Type listType = new TypeToken<List<CylinderData>>() {}.getType();
                cylinderList = new Gson().fromJson(cylinderNumbersJson, listType);
                if (cylinderList == null) cylinderList = new ArrayList<>();

                dcNoTextView.setText("Dc No: " + dcno);
                invoiceNoTextView.setText("Invoice No: " + invoiceNo);
                customerNameTextView.setText("Customer Name: " + customerName);

                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(new CylinderAdapter(this, cylinderList, this::validateCylinder));


                submitButton.setOnClickListener(v -> {
                    dialog = new ProgressDialog(this);
                    dialog.setTitle("Submitting");
                    dialog.setMessage("Please wait...");
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setCancelable(false);
                    dialog.show();
                    postUsingVolley();
                });

                uploadSign.setOnClickListener(v -> {
                    startActivity(new Intent(this, ActivityDigitalSignature.class).putExtra("type", "validate"));
                });

                closeImg.setOnClickListener(v -> {
                    SharedPref.getInstance(this).setSign("");
                    constraintSigned.setVisibility(View.GONE);
                    digitalSignPath = "";
                    digitalSignBase64 = "";
                });
                LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
                        new IntentFilter("digital_sign"));


            }

            private void validateCylinder(String scannedBarcode) {
                if (scannedBarcode.isEmpty()) return;

                int idx = -1;
                for (int i = 0; i < cylinderList.size(); i++) {
                    if (scannedBarcode.equals(cylinderList.get(i).getBarcode_no())) {
                        idx = i;
                        break;
                    }
                }

                if (idx != -1) {
                    CylinderData c = cylinderList.get(idx);
                    if (!c.isValidated()) {
                        c.setValidated(true);
                        recyclerView.getAdapter().notifyItemChanged(idx);
                    }
                    Toast.makeText(this, "Cylinder " + scannedBarcode + " validated", Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("त्रुटी")
                            .setMessage("स्कॅन केलेला बारकोड '" + scannedBarcode + "' यादीत आढळला नाही.")
                            .setPositiveButton("ठीक", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setCancelable(false)
                            .show();
                }
            }

            private void postUsingVolley() {
                String dcno = getIntent().getStringExtra("dcno");
                String email = SharedPref.getInstance(this).getEmail();
                String vehicleNo = SharedPref.getInstance(this).getVehicleNo();
                String transType = "DEL";

                // Calculate validated quantity and cylinder numbers
                List<String> validatedCylinders = new ArrayList<>();
                for (CylinderData cylinder : cylinderList) {
                    if (cylinder.isValidated()) {
                        validatedCylinders.add(cylinder.getCyl_code());
                    }
                }
                String cylNos = String.join(",", validatedCylinders);
                String validatedQty = String.valueOf(validatedCylinders.size());

                // Validate inputs
                if (dcno == null || dcno.isEmpty() || validatedQty.equals("0") || cylNos.isEmpty() ||
                        email == null || email.isEmpty() || digitalSignBase64.isEmpty()) {
                    dialog.dismiss();
                    Toast.makeText(this, "Missing required data. Please ensure all fields are filled and signature is provided.", Toast.LENGTH_LONG).show();
                    return;
                }

                String dbHost = SharedPref.getInstance(this).getDBHost();
                String dbUsername = SharedPref.getInstance(this).getDBUsername();
                String dbPassword = SharedPref.getInstance(this).getDBPassword();
                String dbName = SharedPref.getInstance(this).getDBName();

                if (dbHost == null || dbUsername == null || dbPassword == null || dbName == null) {
                    dialog.dismiss();
                    Toast.makeText(this, "Database configuration missing.", Toast.LENGTH_LONG).show();
                    return;
                }

                String url = delivery_validation; // Replace with actual URL

                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST, url,
                        response -> {
                            dialog.dismiss();
                            try {
                                ValidateDcResponse validateDcResponse = new Gson().fromJson(response, ValidateDcResponse.class);
                                if (validateDcResponse.getStatus() != null && !validateDcResponse.getStatus().isEmpty()) {
                                    if ("success".equals(validateDcResponse.getStatus())) {
                                        Toast.makeText(this, validateDcResponse.getMsg(), Toast.LENGTH_LONG).show();
                                        Intent i = new Intent(SecondValidateDcActivity.this, ViewDeliveryPrint.class);
                                        i.putExtra("no",validateDcResponse.getDcno());
                                        i.putExtra("type","DEL");
                                        startActivity(i);
                                        finish();
                                    } else {
                                        Toast.makeText(this, validateDcResponse.getMsg(), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(this, "Invalid response from server", Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "JSON parsing error: " + e.getMessage());
                                Toast.makeText(this, "Error parsing response", Toast.LENGTH_LONG).show();
                            }
                        },
                        error -> {
                            dialog.dismiss();
                            Log.e(TAG, "Volley error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                            Toast.makeText(this, "Network error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("db_host", dbHost);
                        params.put("db_username", dbUsername);
                        params.put("db_password", dbPassword);
                        params.put("db_name", dbName);
                        params.put("dcno", dcno);
                        params.put("validated_qty", validatedQty);
                        params.put("cyl_nos", cylNos);
                        params.put("email", email);
                        params.put("trans_type", transType);
                        params.put("sign", digitalSignBase64);
                        params.put("lati", latitude);
                        params.put("logi", logitude);
                        params.put("vehicle_no", vehicleNo);
                        return params;
                    }
                };

                VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
            }

            private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if ("digital_sign".equals(intent.getAction())) {
                        String signed = intent.getStringExtra("Signed");
                        digitalSignPath = intent.getStringExtra("path") != null ? intent.getStringExtra("path") : "";
                        if ("true".equals(signed)) {
                            constraintSigned.setVisibility(View.VISIBLE);
                            File imgFile = new File(digitalSignPath);
                            if (imgFile.exists()) {
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                signedImg.setImageBitmap(myBitmap);
                                try {
                                    digitalSignBase64 = Base64.getEncoder().encodeToString(Files.readAllBytes(imgFile.toPath()));
                                } catch (IOException e) {
                                    Log.e(TAG, "Error encoding signature: " + e.getMessage());
                                    digitalSignBase64 = "";
                                }
                            }
                        }
                    }
                }
            };



            @Override
            public void onItemClick(int position) {
                // Not used, but required by OnItemClickListener
            }


    // dispatchKeyEvent removed. Logic moved to OnKeyListener.



    @Override
    public void locationOn() {
        Toast.makeText(this, "Location On", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void currentLocation(Location location) {
        latitude = String.valueOf(location.getLatitude());

        logitude = String.valueOf(location.getLongitude());

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
    public void locationCancelled() {
        Toast.makeText(this, "Location Cancelled", Toast.LENGTH_SHORT).show();
    }

}


