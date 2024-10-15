package com.arnichem.arnichem_barcode.Barcode;

import static com.arnichem.arnichem_barcode.Barcode.LaserScannerActivity.closeKeypad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.VehicleLog.check;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
public class ScannerView extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;

    Button button,upload;
    TextView textView,serialno,weight,aicode,manufacturer;
    public String s;
    public Spinner spinmm;
    public Spinner spinyyyy;
    ProgressDialog dialog;
    ScrollView scrollView;
    String type = "";
    File photoFile = null;
    ImageView img1 , img2 , img3, img4;
    String ba1 = "",ba2 = "",ba3="",ba4="";
    String Selected,Selectedmm,Selectedyy;
    Spinner spinnerselect;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    String currentPhotoPath;
    private Uri imageUri;

    private String inputHolder = "";

    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("barcode_register")) {
                //Extract your data - better to use constants...
                s = intent.getStringExtra("val");
                if(s==null||s.isEmpty())
                {
                    textView.setText("");
                    //assign some value to result
                }else {
                    textView.setText(s);
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Barcode Registration");
        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
                new IntentFilter("barcode_register"));
        LocalBroadcastManager.getInstance(this).registerReceiver(cameraReceiver,
                new IntentFilter("camera_data"));

        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        button=findViewById(R.id.button);
        textView=findViewById(R.id.textView);
        serialno=findViewById(R.id.edserialnumber);
        weight=findViewById(R.id.edenterweight);
        aicode=findViewById(R.id.edaicode);
        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        img4 = findViewById(R.id.img4);
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "img1";
                Intent intent = new Intent(ScannerView.this,NewCamerActivity.class);
                intent.putExtra("type",type);
                startActivity(intent);                // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image
            }
        });
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "img2";
                Intent intent = new Intent(ScannerView.this,NewCamerActivity.class);
                intent.putExtra("type",type);
                startActivity(intent);                // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image

            }
        });
        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "img3";
                Intent intent = new Intent(ScannerView.this,NewCamerActivity.class);
                intent.putExtra("type",type);
                startActivity(intent);                // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image

            }
        });
        img4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "img4";
                Intent intent = new Intent(ScannerView.this,NewCamerActivity.class);
                intent.putExtra("type",type);
                startActivity(intent);                // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image

            }
        });
        spinmm = (Spinner) findViewById(R.id.spinmm);
        spinyyyy = (Spinner) findViewById(R.id.spinyyyy);
        scrollView=findViewById(R.id.scannerviewmain);
        upload=findViewById(R.id.uploadregitration);
        manufacturer=findViewById(R.id.manufacturerval);
        spinnerselect=findViewById(R.id.spinselect);
        ArrayList aList= new ArrayList(Arrays.asList(SharedPref.getInstance(ScannerView.this).getCycPrefix().split(",")));

        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, aList);
      //  adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerselect.setAdapter(itemsAdapter);
        spinnerselect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
              ///  Log.v("item", (String) parent.getItemAtPosition(position));
                Selected= (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        String[] items1= new String[]{"00","01", "02", "03", "04", "05", "06","07", "08", "09", "10", "11", "12"};

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items1);

        spinmm.setAdapter(adapter1);

        spinmm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Selectedmm = adapter1.getItem(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String[] items2= new String[]{"00000","2010", "2011", "2012", "2013", "2014", "2015","2016", "2017", "2018", "2019","2020", "2021", "2022", "2023", "2024", "2025","2026", "2027", "2028", "2029", "2030","2031"};
        ArrayAdapter<String> adapter2= new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items2);

        spinyyyy.setAdapter(adapter2);

        spinyyyy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Selectedyy = adapter2.getItem(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ScannerView.this, NewScanner.class);
                intent.putExtra("type", "barcode_register");
                startActivity(intent);

            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postUsingVolley();
            }
        });


    }

    private void checkPermission(String camera, int cameraPermissionCode) {
    }


    @Override
public void onBackPressed() {
    Intent intent = new Intent(this, Dashboard.class);
    startActivity(intent);
}

    private void postUsingVolley() {
        dialog = new ProgressDialog(ScannerView.this);
        dialog.setTitle("Login");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(ScannerView.this);
        StringRequest request = new StringRequest(Request.Method.POST,APIClient.barcode_registration, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.dismiss();
                Snackbar.make(scrollView,Selected+aicode.getText().toString().trim()+"हा सिलेंडर नंबर "+textView.getText().toString()+"या बारकोड सोबत रजिस्टर झाला आहे ", Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN).setTextColor(Color.BLACK).show();
                try {
                    JSONObject respObj = new JSONObject(response);
                    String status = respObj.getString("status");
                    String msg = respObj.getString("msg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // method to handle errors.
                Toast.makeText(ScannerView.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("aicode",Selected+aicode.getText().toString().trim());
                params.put("serialno",serialno.getText().toString());
                params.put("weight",weight.getText().toString());
                params.put("hydrodate",Selectedmm+"/"+Selectedyy+"/"+"01");
                params.put("barcodeno",textView.getText().toString().trim());
                params.put("manufacturer",manufacturer.getText().toString());
                params.put("file_path1",ba1);
                params.put("file_path2",ba2);
                params.put("file_path3",ba3);
                params.put("file_path4",ba4);
                params.put("email",SharedPref.getInstance(ScannerView.this).getEmail());
                params.put("username", SharedPref.getInstance(ScannerView.this).FirstName()+" "+SharedPref.getInstance(ScannerView.this).LastName());
                params.put("remarks", "Transaction Through App");
                params.put("db_host",SharedPref.mInstance.getDBHost());
                params.put("db_username",SharedPref.mInstance.getDBUsername());
                params.put("db_password",SharedPref.mInstance.getDBPassword());
                params.put("db_name",SharedPref.mInstance.getDBName());
                return params;
            }
        };
        // below line is to make
        // a json object request.
        queue.add(request);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bao);
                byte[] ba = bao.toByteArray();
                if(type.equalsIgnoreCase("img1")){
                    ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
                    img1.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));
                }else if(type.equalsIgnoreCase("img2")) {
                    ba2 = Base64.encodeToString(ba, Base64.DEFAULT);
                    img2.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));
                }else if(type.equalsIgnoreCase("img3")){
                    ba3 = Base64.encodeToString(ba, Base64.DEFAULT);
                    img3.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));
                }else if(type.equalsIgnoreCase("img4")){
                    ba4 = Base64.encodeToString(ba, Base64.DEFAULT);
                    img4.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));
                }

            }
        }

    }

    private final BroadcastReceiver cameraReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("camera_data")) {
                //Extract your data - better to use constants...
                String url = intent.getStringExtra("url");
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(ScannerView.this.getContentResolver(),Uri.parse(url));
                    bitmap = RotateBitmap(bitmap,90);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bao);
                    byte[] ba = bao.toByteArray();
                    if(type.equalsIgnoreCase("img1")){
                        ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
                        img1.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));
                    }else if(type.equalsIgnoreCase("img2")) {
                        ba2 = Base64.encodeToString(ba, Base64.DEFAULT);
                        img2.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));
                    }else if(type.equalsIgnoreCase("img3")){
                        ba3 = Base64.encodeToString(ba, Base64.DEFAULT);
                        img3.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));
                    }else if(type.equalsIgnoreCase("img4")){
                        ba4 = Base64.encodeToString(ba, Base64.DEFAULT);
                        img4.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));
                    }
                    //   imageView.setImageURI(imageUri);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    };
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }




}