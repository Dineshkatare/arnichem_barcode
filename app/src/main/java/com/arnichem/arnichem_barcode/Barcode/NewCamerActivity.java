package com.arnichem.arnichem_barcode.Barcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NewCamerActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private boolean isPreviewing = false;
    private Button button;
    private String type = "";
    private ConstraintLayout progressCl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_camer);
        getIntentData();
        // Get the SurfaceView and Button from the layout
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        progressCl = findViewById(R.id.progressCl);
        button = findViewById(R.id.button);
        progressCl.setVisibility(View.GONE);
        // Add a click listener to the Button to capture the image
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        // Get the SurfaceHolder and add a callback to it
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        type = intent.getExtras().getString("type","");
    }

    // SurfaceHolder.Callback methods

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(type.equalsIgnoreCase("front")){
            int numCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            for (int cameraId = 0; cameraId < numCameras; cameraId++) {
                Camera.getCameraInfo(cameraId, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        camera = Camera.open(cameraId);
                        camera.setPreviewDisplay(surfaceHolder);
                        camera.setDisplayOrientation(90);
                        Camera.Parameters params = camera.getParameters();
                        params.setRotation(90); // Adjust orientation
                        camera.setParameters(params); // Set parameters
                        camera.startPreview();
                        isPreviewing = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break; // Found the front camera, so break out of the loop
                }
            }
        } else {
            // Open the camera when the SurfaceHolder is created
            camera = Camera.open();
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.setDisplayOrientation(90);
                Camera.Parameters params = camera.getParameters();
                params.setRotation(90); // Adjust orientation
                camera.setParameters(params); // Set parameters
                camera.startPreview();
                isPreviewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Update the camera preview when the SurfaceHolder is changed
        if (isPreviewing) {
            camera.stopPreview();
            isPreviewing = false;
        }
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Release the camera when the SurfaceHolder is destroyed
        camera.stopPreview();
        camera.release();
        camera = null;
        isPreviewing = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }


    private void captureImage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressCl.setVisibility(View.VISIBLE);
            }
        });
        button.setVisibility(View.GONE);

        // Take a picture and save it to a file
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // Create a file to save the image
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "my_image.jpg");
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    progressCl.setVisibility(View.GONE);
                }

                // Notify the MediaScanner to scan the saved image
                MediaScannerConnection.scanFile(NewCamerActivity.this,
                        new String[]{file.getPath()},
                        new String[]{"image/jpeg"},
                        null);

                // Create a content URI for the saved image using a FileProvider
                Uri contentUri = FileProvider.getUriForFile(NewCamerActivity.this, "com.arnichem.arnichem_barcode.fileprovider", file);
                Intent intent = new Intent("camera_data");
                intent.putExtra("url", contentUri.toString());
                intent.putExtra("type", type);
                LocalBroadcastManager.getInstance(NewCamerActivity.this).sendBroadcast(intent);
                finish();

                // Restart the camera preview
                camera.startPreview();
            }
        });
    }
}