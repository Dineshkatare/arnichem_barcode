package com.arnichem.arnichem_barcode.TransactionsView.Duraempty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;

import java.util.StringTokenizer;

public class Duraemptyadd extends AppCompatActivity {
    Button add_button,finish;
    private CodeScanner mCodeScanner;
    DuraemptyHelper myDB;
    syncHelper synchelper;
    FrameLayout frameLayout;
    int i=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duraemptyadd);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        frameLayout=findViewById(R.id.framelayoutempty);
        myDB=new DuraemptyHelper(Duraemptyadd.this);
        synchelper=new syncHelper(Duraemptyadd.this);
//        title_input = findViewById(R.id.title_input);
//        add_button = findViewById(R.id.add_button);

        CodeScannerView scannerView = findViewById(R.id.outscannerview);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String res= result.getText();
                        if(res==null)
                        {
                            Toast.makeText(Duraemptyadd.this, "null", Toast.LENGTH_SHORT).show();
                            //assign some value to result
                        }
                        else if(res.contains("="))
                        {
                            StringTokenizer tokens = new StringTokenizer(res, "=");
                            String first = tokens.nextToken();// this will contain "Fruit"
                            String second = tokens.nextToken();
//                            title_input.setText(second);
                            Cursor cursor = synchelper.readAllData();
                            if (cursor.getCount() == 0) {
                                //      empty_imageview.setVisibility(View.VISIBLE);
                                //      no_data.setVisibility(View.VISIBLE);
                            } else {
                                while (cursor.moveToNext()) {
                                    String col=cursor.getString(1);
                                    String col1 =cursor.getString(2);
                                    if(col1.contentEquals(second))
                                    {
                                        if(i==1)
                                        {
                                            myDB.addBook(col);
                                            startActivity(new Intent(Duraemptyadd.this,duraemptymain.class));
                                            i++;
                                        }
                                    }
                                }
                            }

                        }
                        else {
                            Snackbar.make(frameLayout, "तुमचा स्कॅन आमच्या सिलेंडर नंबर शी मिळत नाही आहे कृपाया परत स्कॅन करा ", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();

                        }
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });




    }



    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        onResume();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myDB != null)
            myDB.close();

        if(synchelper != null)
            synchelper.close();


    }


}