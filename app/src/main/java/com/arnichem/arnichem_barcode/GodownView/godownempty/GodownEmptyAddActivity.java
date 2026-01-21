package com.arnichem.arnichem_barcode.GodownView.godownempty;

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
import com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardMain;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;

import java.util.StringTokenizer;

public class GodownEmptyAddActivity extends AppCompatActivity {
    Button add_button, finish;
    private CodeScanner mCodeScanner;
    GodownEmptyHelper myDB;
    syncHelper synchelper;
    FrameLayout frameLayout;
    int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_godown_delivery_add2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        frameLayout = findViewById(R.id.frameinward);
        myDB = new GodownEmptyHelper(GodownEmptyAddActivity.this);
        synchelper = new syncHelper(GodownEmptyAddActivity.this);
        // title_input = findViewById(R.id.title_input);
        // add_button = findViewById(R.id.add_button);
        finish = findViewById(R.id.go);
        CodeScannerView scannerView = findViewById(R.id.outscannerview);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String res = result.getText();
                        if (res == null) {
                            Toast.makeText(GodownEmptyAddActivity.this, "null", Toast.LENGTH_SHORT).show();
                            // assign some value to result
                        } else if (res.contains("=")) {
                            StringTokenizer tokens = new StringTokenizer(res, "=");
                            String first = tokens.nextToken();// this will contain "Fruit"
                            String second = tokens.nextToken();
                            // title_input.setText(second);
                            Cursor cursor = synchelper.readAllData();
                            if (cursor.getCount() == 0) {
                                // empty_imageview.setVisibility(View.VISIBLE);
                                // no_data.setVisibility(View.VISIBLE);
                            } else {
                                while (cursor.moveToNext()) {
                                    String col = cursor.getString(1);
                                    String volume = cursor.getString(4); // Get volume
                                    String filledWith = cursor.getString(5); // Get filled_with
                                    if (col.contentEquals(second)) {
                                        if (i == 1) {
                                            myDB.addBook(col, filledWith, volume, "no");
                                            Snackbar.make(frameLayout,
                                                    "तुमचा बारकोड नंबर सिलेंडर नंबर " + col + " शी जोडला आहे ",
                                                    Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN)
                                                    .setTextColor(Color.BLACK).show();
                                            i++;
                                        }
                                    }
                                }
                            }

                        } else {
                            Snackbar.make(frameLayout,
                                    "तुमचा स्कॅन आमच्या सिलेंडर नंबर नंबर शी मिळत नाही आहे कृपाया परत स्कॅन करा ",
                                    Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();

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
        // add_button.setOnClickListener(new View.OnClickListener() {
        // @Override
        // public void onClick(View view) {
        //
        // Cursor cursor = synchelper.readAllData();
        // if (cursor.getCount() == 0) {
        // // empty_imageview.setVisibility(View.VISIBLE);
        // // no_data.setVisibility(View.VISIBLE);
        // } else {
        // while (cursor.moveToNext()) {
        //// book_id.add(cursor.getString(0));
        //// book_title.add(cursor.getString(1));
        // String col=cursor.getString(1);
        // String col1 =cursor.getString(2);
        // if(col1.contentEquals(title_input.getText().toString()))
        // {
        //
        // if(i==1)
        // {
        //
        // myDB.addBook(col);
        // i++;
        // }
        //
        // }
        //
        //
        // }
        //
        // }
        //
        //
        //
        // }
        // });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GodownEmptyAddActivity.this, InWardMain.class));
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
        if (myDB != null)
            myDB.close();

        if (synchelper != null)
            synchelper.close();

    }

}