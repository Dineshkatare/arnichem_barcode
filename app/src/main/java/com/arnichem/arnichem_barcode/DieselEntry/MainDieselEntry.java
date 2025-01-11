package com.arnichem.arnichem_barcode.DieselEntry;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.Barcode.NewCamerActivity;
import com.arnichem.arnichem_barcode.GodownView.Closing_stock.NewClosingStock.ClosingPrint;
import com.arnichem.arnichem_barcode.GodownView.Closing_stock.NewClosingStock.ClosingStockMain;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliveryprint;
import com.arnichem.arnichem_barcode.VehicleLog.check;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
import com.google.android.material.snackbar.Snackbar;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainDieselEntry extends AppCompatActivity implements Listener, LocationData.AddressCallBack {

    static final int CAPTURE_IMAGE_REQUEST = 1;
    private static final String IMAGE_DIRECTORY_NAME = "VLEMONN";
    ArrayAdapter<CharSequence> pumpAdapter;
    ProgressDialog dialog;
    TextView vehiclevalue, usernamevalue, date;
    EditText amountEdt, ridingEdt, quantityEdt;
    Spinner pumpSpinner;
    String latitude = "0", logitude = "0", address = "0", pumpName;
    SharedPreferences pref;
    Button dieselSubmitBtn;
    ArrayAdapter<String> pumpListAdapter;
    ImageView ridingImg;
    GetLocationDetail getLocationDetail;
    File photoFile = null;
    String mCurrentPhotoPath;
    private EasyWayLocation easyWayLocation;
    public int pumpPos;
    String ba1;
    static JSONObject object = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_diesel_);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
                new IntentFilter("camera_data"));
        getSupportActionBar().setTitle("Diesel Entry");
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false, true, this);
        pumpSpinner = findViewById(R.id.pumpSpinner);
        vehiclevalue = findViewById(R.id.vno);
        quantityEdt = findViewById(R.id.quantityEdt);
        usernamevalue = findViewById(R.id.usernametxtvalue);
        dieselSubmitBtn = findViewById(R.id.dieselSubmitBtn);
        dieselSubmitBtn.setEnabled(true);
        amountEdt = findViewById(R.id.amountEdt);
        ridingEdt = findViewById(R.id.ridingEdt);
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        pumpAdapter = ArrayAdapter.createFromResource(this, R.array.PUMP, android.R.layout.simple_spinner_item);
        pumpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        vehiclevalue.setText(SharedPref.getInstance(this).getVehicleNo());
        date = findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        pumpSpinner.setAdapter(pumpAdapter);
        loadSpinnerData();
        pumpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pumpName = (String) parent.getItemAtPosition(position);
                pumpPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        dieselSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dieselSubmitBtn.setEnabled(false);
                postUsingVolley();
            }
        });

        ridingImg = findViewById(R.id.ridingImg);
        ridingImg.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainDieselEntry.this, NewCamerActivity.class);
                intent.putExtra("type","diesel");
                startActivity(intent);

            }
        });


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
        latitude = String.valueOf(location.getLatitude());

        logitude = String.valueOf(location.getLongitude());

        getLocationDetail.getAddress(location.getLatitude(), location.getLongitude(), "xyz");
    }

    @Override
    public void locationCancelled() {
        Toast.makeText(this, "Location Cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void locationData(LocationData locationData) {
        address = locationData.getFull_address();


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Bitmap myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            SimpleDateFormat sdf = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                 sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
            }
            String currentDateandTime = sdf.format(new Date());
            Bitmap newBitmap = drawTextToBitmap(MainDieselEntry.this,myBitmap,currentDateandTime);
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 10, bao);
            byte[] ba = bao.toByteArray();
            ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
            ridingImg.setImageBitmap(Bitmap.createScaledBitmap(newBitmap, 100, 100, false));

        } else {
            displayMessage(getBaseContext(), "Request cancelled or something went wrong.");
        }
    }


    private void postUsingVolley() {
        dialog = new ProgressDialog(MainDieselEntry.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (pumpPos == 0) {
            dieselSubmitBtn.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(MainDieselEntry.this, "कृपया पंप लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else if (amountEdt.getText().toString().isEmpty()) {
            dieselSubmitBtn.setEnabled(true);
            dialog.dismiss();
            MDToast.makeText(MainDieselEntry.this, "कृपया Amount टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else if (quantityEdt.getText().toString().isEmpty()) {
            dialog.dismiss();
            dieselSubmitBtn.setEnabled(true);

            MDToast.makeText(MainDieselEntry.this, "कृपया Quantity टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else if (ridingEdt.getText().toString().isEmpty()) {
            dialog.dismiss();
            dieselSubmitBtn.setEnabled(true);

            MDToast.makeText(MainDieselEntry.this, "कृपया वाहन रिडींग टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else if (ba1 == null) {
            dialog.dismiss();
            dieselSubmitBtn.setEnabled(true);

            MDToast.makeText(MainDieselEntry.this, "कृपया वाहन रिडींग चा फोटो टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        } else {
            StringRequest request = new StringRequest(Request.Method.POST, APIClient.diesel_entry, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray array = new JSONArray(response);
                        for (int i = 0; i < 1; i++) {
                            object = array.getJSONObject(i);
                            String status = object.getString("status");
                            String msg = object.getString("msg");

                            if (status.equals("success")) {
                                dialog.dismiss();
                                dieselSubmitBtn.setEnabled(true);

                                MDToast.makeText(MainDieselEntry.this, "Diesel Entry Done !", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                startActivity(new Intent(MainDieselEntry.this, Dashboard.class));
                                finish();
                            } else {
                                dieselSubmitBtn.setEnabled(true);

                                dialog.dismiss();

                            }

                            Log.e("JSON", "> " + status + msg);
                        }

                    } catch (JSONException e) {

                        dialog.dismiss();

                        dieselSubmitBtn.setEnabled(true);

                        e.printStackTrace();
                    }

                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // method to handle errors.
                    dieselSubmitBtn.setEnabled(true);

                    MDToast.makeText(MainDieselEntry.this, "कृपया परत प्रयत्न करा!", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                    dialog.dismiss();

                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("reading", ridingEdt.getText().toString());
                    params.put("filling_station", pumpName);
                    params.put("quantity", quantityEdt.getText().toString());
                    params.put("amount", amountEdt.getText().toString());
                    params.put("file_path", ba1);
                    params.put("lati", latitude);
                    params.put("logi", logitude);
                    params.put("addr", address);
                    params.put("vehicle_no", SharedPref.getInstance(MainDieselEntry.this).getVehicleNo());
                    params.put("driver", SharedPref.getInstance(MainDieselEntry.this).getID());
                    params.put("email", SharedPref.getInstance(MainDieselEntry.this).getEmail());
                    params.put("db_host",SharedPref.mInstance.getDBHost());
                    params.put("db_username",SharedPref.mInstance.getDBUsername());
                    params.put("db_password",SharedPref.mInstance.getDBPassword());
                    params.put("db_name",SharedPref.mInstance.getDBName());
                    return params;
                }
            };
            // below line is to make
            // a json object request.
            request.setRetryPolicy(new DefaultRetryPolicy(
                    60 * 1000, // 60 seconds timeout
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(MainDieselEntry.this).addToRequestQueue(request);

        }
    }



    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void captureImage() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                try {
                    photoFile = createImageFile();
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "com.arnichem.arnichem_barcode.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                    }
                } catch (Exception ex) {
                    // Error occurred while creating the File
                    displayMessage(getBaseContext(), ex.getMessage());
                }


            } else {
                displayMessage(getBaseContext(), "Nullll");
            }
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void captureImage2() {

        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            photoFile = createImageFile4();
            if (photoFile != null) {

                Uri photoURI = Uri.fromFile(photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST);
            }
        } catch (Exception e) {
            displayMessage(getBaseContext(), "Camera is not available." + e.toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private File createImageFile4() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                displayMessage(getBaseContext(), "Unable to create directory.");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
        mCurrentPhotoPath = mediaFile.getAbsolutePath();

        return mediaFile;

    }

    private void displayMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void loadSpinnerData() {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<ItemCode> labels = db.getGasPartners();
        List<String> gasTypes = new ArrayList<>();
        for (ItemCode finalString:labels) {
            if(finalString.getItem_Code().contains("F"))
            {
                gasTypes.add(finalString.getOwner());
            }
        }
        gasTypes.add("Others");


        // Creating adapter for spinner
        pumpListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,gasTypes);

        // Drop down layout style - list view with radio button
        pumpListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        pumpSpinner.setAdapter(pumpListAdapter);
        if(pumpPos!=0)
        {
            pumpSpinner.setSelection(pumpPos);
        }
    }

    public Bitmap drawTextToBitmap(Context mContext, Bitmap bitmap,  String mText) {
        try {
            Resources resources = mContext.getResources();
            float scale = resources.getDisplayMetrics().density;

            android.graphics.Bitmap.Config bitmapConfig =   bitmap.getConfig();
            // set default bitmap config if none
            if(bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            // resource bitmaps are imutable,
            // so we need to convert it to mutable one
            bitmap = bitmap.copy(bitmapConfig, true);

            Canvas canvas = new Canvas(bitmap);
            // new antialised Paint
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            // text color - #3D3D3D
            paint.setColor(Color.rgb(0,0, 0));
            // text size in pixels
            paint.setTextSize((int) (24 * scale));
            // text shadow
            paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);

            // draw text to the Canvas center
            Rect bounds = new Rect();
            paint.getTextBounds(mText, 10, 10, bounds);
            int x = (bitmap.getWidth() - bounds.width())/6;
            int y = (bitmap.getHeight() + bounds.height())/5;

            canvas.drawText(mText, x * scale, y * scale, paint);

            return bitmap;
        } catch (Exception e) {
            // TODO: handle exception

            return null;
        }

    }

    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("camera_data")) {
                //Extract your data - better to use constants...
                String url = intent.getStringExtra("url");
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainDieselEntry.this.getContentResolver(),Uri.parse(url));
                    bitmap = RotateBitmap(bitmap,90);
                    SimpleDateFormat sdf = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
                    }
                    String currentDateandTime = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        currentDateandTime = sdf.format(new Date());
                    }
                    Bitmap newBitmap = drawTextToBitmap(MainDieselEntry.this,bitmap,currentDateandTime);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 10, bao);

                      byte[] ba = bao.toByteArray();
                    ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
                    ridingImg.setImageBitmap(Bitmap.createScaledBitmap(newBitmap, 100, 100, false));

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
