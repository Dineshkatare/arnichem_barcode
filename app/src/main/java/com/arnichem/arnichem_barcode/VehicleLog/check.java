package com.arnichem.arnichem_barcode.VehicleLog;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arnichem.arnichem_barcode.Barcode.NewCamerActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.Main;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.VehicleHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.login;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class check extends AppCompatActivity implements Spinner.OnItemSelectedListener, Listener, LocationData.AddressCallBack
{

    private EasyWayLocation easyWayLocation;
    GetLocationDetail getLocationDetail;
    ImageView imageView;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    String currentPhotoPath;
    private Uri imageUri;
    Button button;
    File photoFile = null;
    Spinner spinner;
    EditText riding;
    public String selected;
    public  int pos;
    SharedPreferences pref;
    ProgressDialog dialog;
    public String path,latitude="0",logitude="0",address="0";;
    String ba1;
    ScrollView scrollView;
    ArrayAdapter<String> dataAdapter;
    APIInterface apiInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
                new IntentFilter("camera_data"));
        apiInterface = APIClient.getClient().create(APIInterface.class);

        getSupportActionBar().setTitle("Vehicle Login");
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false,true,this);
        spinner=findViewById(R.id.dynamic_spinner);
        fetchData();
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        riding=findViewById(R.id.ed1resdingtype);
        scrollView=findViewById(R.id.checkid);
        imageView =  findViewById(R.id.image_viewfor);
        button=findViewById(R.id.UploadBtn);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(check.this,NewCamerActivity.class);
                intent.putExtra("type","check");
                startActivity(intent);
               // askCameraPermissions();

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //postUsingRetrofit();
                postUsingVolley();


            }

        });



        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               selected = dataAdapter.getItem(position);
               pos=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


    }

    @Override
    protected void onResume() {
        super.onResume();
//        easyWayLocation.startLocation();
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
    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            dispatchTakePictureIntent();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
//                File  imageFile = new File(
//                (imageUri));
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bao);
                    byte[] ba = bao.toByteArray();
                    ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));

                 //   imageView.setImageURI(imageUri);

                } catch (IOException e) {
                    e.printStackTrace();
                }



//                File f = new File(currentPhotoPath);
//                imageView.setImageURI(Uri.fromFile(imageFile));
//                Log.d("tag", "ABsolute Url of Image is " + Uri.fromFile(f));
//
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                Uri contentUri = Uri.fromFile(f);
//                mediaScanIntent.setData(contentUri);
//                this.sendBroadcast(mediaScanIntent);
            }

        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        }
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        photoFile = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = photoFile.getAbsolutePath();
        return photoFile;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this,
                        "com.arnichem.arnichem_barcode.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }


    private void fetchData() {
        VehicleHandler db = new VehicleHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();
        dataAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);
        // Creating adapter for spinner
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }



    private void displayMessage(Context context, String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }


    private void postUsingVolley() {
        dialog = new ProgressDialog(check.this);
        dialog.setTitle("Uploading");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if(pos==0) {
            dialog.dismiss();
            Snackbar.make(scrollView, "कृपया वाहन क्रमांक निवडा !", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
        }
        else if(riding.getText().toString().isEmpty())
        {
            dialog.dismiss();
            MDToast.makeText(check.this, "कृपया वाहन रिडींग टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


        }
        else if(ba1==null)
        {
            dialog.dismiss();
            MDToast.makeText(check.this, "कृपया वाहन रिडींग चा फोटो टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


        }else
            {
            StringRequest request = new StringRequest(Request.Method.POST, APIClient.vehicle_login, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    SharedPref.getInstance(getApplicationContext()).storeVStatus("success");
                    SharedPref.getInstance(getApplicationContext()).storeVehicleNumber(selected);
                    Snackbar.make(scrollView, SharedPref.getInstance(check.this).FirstName()+" "+SharedPref.getInstance(check.this).LastName()+" तुमचा  गाडी  नंबर "+selected+" सोबत रजिस्टर झाला आहे ", Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN).setTextColor(Color.WHITE).show();
                    dialog.dismiss();
                    startActivity(new Intent(check.this, Dashboard.class));

                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // method to handle errors.
                    Snackbar.make(scrollView, "कृपया परत प्रयत्न करा!", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();

                    dialog.dismiss();

                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("reading", riding.getText().toString());
                    params.put("file_path",ba1);
                    params.put("vehicle_no",selected);
                    params.put("lati",latitude);
                    params.put("logi",logitude);
                    params.put("addr",address);
                    params.put("username",SharedPref.getInstance(check.this).FirstName()+" "+SharedPref.getInstance(check.this).LastName());
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
            VolleySingleton.getInstance(check.this).addToRequestQueue(request);
        }
    }
    private void postUsingRetrofit() {
        dialog = new ProgressDialog(check.this);
        dialog.setTitle("Uploading");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        if (pos == 0) {
            dialog.dismiss();
            Snackbar.make(scrollView, "कृपया वाहन क्रमांक निवडा !", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.RED)
                    .setTextColor(Color.WHITE)
                    .show();
        } else if (riding.getText().toString().isEmpty()) {
            dialog.dismiss();
            MDToast.makeText(check.this, "कृपया वाहन रिडींग टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else if (ba1 == null) {
            dialog.dismiss();
            MDToast.makeText(check.this, "कृपया वाहन रिडींग चा फोटो टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else {
            Call<ResponseBody> call = apiInterface.postVehicleDetails(
                    riding.getText().toString(),
                    ba1,
                    selected,
                    latitude,
                    logitude,
                    address,
                    SharedPref.getInstance(check.this).FirstName() + " " + SharedPref.getInstance(check.this).LastName(),
                    SharedPref.mInstance.getDBHost(),
                    SharedPref.mInstance.getDBUsername(),
                    SharedPref.mInstance.getDBPassword(),
                    SharedPref.mInstance.getDBName()
            );

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        SharedPref.getInstance(getApplicationContext()).storeVStatus("success");
                        SharedPref.getInstance(getApplicationContext()).storeVehicleNumber(selected);
                        Snackbar.make(scrollView, SharedPref.getInstance(check.this).FirstName() + " " + SharedPref.getInstance(check.this).LastName() + " तुमचा  गाडी  नंबर " + selected + " सोबत रजिस्टर झाला आहे ", Snackbar.LENGTH_LONG)
                                .setBackgroundTint(Color.GREEN)
                                .setTextColor(Color.WHITE)
                                .show();
                        dialog.dismiss();
                        startActivity(new Intent(check.this, Dashboard.class));
                    } else {
                        Snackbar.make(scrollView, "कृपया परत प्रयत्न करा!", Snackbar.LENGTH_LONG)
                                .setBackgroundTint(Color.RED)
                                .setTextColor(Color.WHITE)
                                .show();
                        dialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Snackbar.make(scrollView, "कृपया परत प्रयत्न करा!", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.RED)
                            .setTextColor(Color.WHITE)
                            .show();
                    dialog.dismiss();
                }
            });
        }
    }


    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        // On selecting a spinner item
        String label = dataAdapter.getItem(position);
        pos=position;
        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "You selected: " + label+pos,
                Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("camera_data")) {
                //Extract your data - better to use constants...
                String url = intent.getStringExtra("url");
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(check.this.getContentResolver(),Uri.parse(url));
                    bitmap = RotateBitmap(bitmap,90);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bao);
                    byte[] ba = bao.toByteArray();
                    ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));

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


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}