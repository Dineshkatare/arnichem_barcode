package com.arnichem.arnichem_barcode.FileUpload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arnichem.arnichem_barcode.Barcode.NewCamerActivity;
import com.arnichem.arnichem_barcode.DieselEntry.MainDieselEntry;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.VehicleLog.check;
import com.arnichem.arnichem_barcode.util.BitmapUtils;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.google.android.gms.vision.text.Text;
import com.google.android.material.snackbar.Snackbar;
import com.valdesekamdem.library.mdtoast.MDToast;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileUploadSubActivity extends AppCompatActivity {

    private String type="",path="";
    ProgressDialog dialog;

    private Button uploadBtn;
    private ImageView camera,gallery,uploaded_image_view;
    CardView uploaded_image;
    ConstraintLayout imageCl;
    LinearLayout upload_linear;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 101;
    private FileUploadManager fileUploadManager;
    EditText docNumber;

    private Boolean isCapture= false;
    private File  file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload_sub);
        uploadBtn = findViewById(R.id.uploadBtn);
        camera  = findViewById(R.id.camera);
        gallery = findViewById(R.id.gallery);
        imageCl = findViewById(R.id.image_cl);
        upload_linear = findViewById(R.id.upload_linear);
        uploaded_image =findViewById(R.id.uploaded_image);
        uploaded_image_view = findViewById(R.id.uploaded_image_view);
        docNumber = findViewById(R.id.name);
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
                new IntentFilter("camera_data"));

        getSupportActionBar().setTitle(type +" File Upload");
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) {
                    startCamera();
                } else {
                    requestCameraPermission();
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkGalleryPermission()) {
                    pickImageFromGallery();
                } else {
                    requestGalleryPermission();
                }
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new ProgressDialog(FileUploadSubActivity.this);
                dialog.setTitle("Data Inserting");
                dialog.setMessage("Please wait....");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
                if (docNumber.getText().toString().trim().isEmpty()) {
                    dialog.dismiss();
                    MDToast.makeText(FileUploadSubActivity.this, "Please Enter Number!", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                }else {
                    uploadImageWithText(type,docNumber.getText().toString(),SharedPref.getInstance(FileUploadSubActivity.this).getEmail());
                }

            }

        });


    }

    private void startCamera() {
        Intent intent = new Intent(FileUploadSubActivity.this,NewCamerActivity.class);
        intent.putExtra("type","check");
        startActivity(intent);

    }

    private void uploadImageWithText(String type,String docNumber,String email) {
        fileUploadManager = new FileUploadManager();

        long fileSize = file.length();

        // Convert bytes to kilobytes, megabytes, or gigabytes if needed

        // Convert other parameters to RequestBody
        RequestBody typeBody = RequestBody.create(MediaType.parse("multipart/form-data"), type);
        RequestBody docNumberBody = RequestBody.create(MediaType.parse("multipart/form-data"), docNumber);
        RequestBody sizeBody = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(fileSize));
        RequestBody emailBody = RequestBody.create(MediaType.parse("multipart/form-data"), email);
        RequestBody username = RequestBody.create(MediaType.parse("multipart/form-data"), SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        RequestBody host = RequestBody.create(MediaType.parse("multipart/form-data"), SharedPref.getInstance(this).getDBHost());
        RequestBody db_username = RequestBody.create(MediaType.parse("multipart/form-data"), SharedPref.getInstance(this).getDBUsername());
        RequestBody password = RequestBody.create(MediaType.parse("multipart/form-data"), SharedPref.getInstance(this).getDBPassword());
        RequestBody db_name = RequestBody.create(MediaType.parse("multipart/form-data"), SharedPref.getInstance(this).getDBName());

        FileUploadData data = new FileUploadData(typeBody, docNumberBody, sizeBody, emailBody, file,host,db_username,password,db_name,username);

        fileUploadManager.uploadFile(data, new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                dialog.dismiss();
                MDToast.makeText(FileUploadSubActivity.this, "File uploaded successfully!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                Intent intent = new Intent(FileUploadSubActivity.this, FIleUploadMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                dialog.dismiss();
                MDToast.makeText(FileUploadSubActivity.this, "Failed!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

            }
        });

    }
    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("camera_data")) {
            if(!isCapture){
                isCapture =true;
                path = intent.getStringExtra("url");
                file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "my_image.jpg");

                UCrop.of(Uri.parse(path),Uri.fromFile(file))
                        .start(FileUploadSubActivity.this);
            }
        }}

    };





    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {


            Uri uri = data.getData();
            path = getPath(uri);
            file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "my_image.jpg");

            // Convert the Uri to File

            UCrop.of(uri,Uri.fromFile(file))
                    .start(FileUploadSubActivity.this);

        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            final Uri croppedUri = UCrop.getOutput(data);
            try {
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(FileUploadSubActivity.this.getContentResolver(), croppedUri);

                // Compress the bitmap to 50% quality
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
                byte[] byteArray = outputStream.toByteArray();

                // Save the compressed bitmap to a file
                file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "my_image.jpg");
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(byteArray);
                fileOutputStream.flush();
                fileOutputStream.close();

                // Display the compressed image
                imageCl.setVisibility(View.VISIBLE);
                upload_linear.setVisibility(View.GONE);
                uploaded_image_view.setImageBitmap(originalBitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkGalleryPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    private void requestGalleryPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
            case GALLERY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                }
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}