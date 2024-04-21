package com.arnichem.arnichem_barcode.Googlepay;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.util.SharedPref;

import java.io.File;

public class GooglepayScreen extends AppCompatActivity {

    Bitmap upiBitmap;
    ImageView upiImg;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlepay_screen);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Google Pay");
        upiImg = findViewById(R.id.upiImg);
        String print_logo = SharedPref.mInstance.getPrintUpi();
        File imgFile = new  File(print_logo);
        if(imgFile.exists()){
            upiBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            upiImg.setImageBitmap(upiBitmap);
        }

    }
}