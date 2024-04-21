package com.arnichem.arnichem_barcode.Producation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.DuraDelivery.DuraDeliveryMain;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.DuraUpdateInfo;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;
import org.json.JSONObject;
import java.util.StringTokenizer;

public class DuraScan extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    syncHelper synchelper;
    FrameLayout frameLayout;
    DuraUpdateInfo duraUpdateInfo;
    String s;
    static JSONObject object =null;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dura_scan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        synchelper=new syncHelper(DuraScan.this);
        frameLayout=findViewById(R.id.durascanadd);
        mCodeScanner = new CodeScanner(this, scannerView);
        duraUpdateInfo=new DuraUpdateInfo(this);
        s=getIntent().getStringExtra("id");
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String res= result.getText();
                        Toast.makeText(DuraScan.this, "res"+res, Toast.LENGTH_SHORT).show();
                        if(res==null)
                        {
                            Toast.makeText(DuraScan.this, "null", Toast.LENGTH_SHORT).show();
                            //assign some value to result
                        }
                        else if(res.contains("="))
                        {
                            StringTokenizer tokens = new StringTokenizer(res, "=");
                            String first = tokens.nextToken();// this will contain "Fruit"
                            String second = tokens.nextToken();
//                            title_input.setText(second);
                            Cursor cursor = synchelper.readAllData();
                            Toast.makeText(DuraScan.this, "second"+second, Toast.LENGTH_SHORT).show();

                            if (cursor.getCount() == 0) {
                                //      empty_imageview.setVisibility(View.VISIBLE);
                                //      no_data.setVisibility(View.VISIBLE);
                            } else {
                                while (cursor.moveToNext()) {
                                    String col=cursor.getString(1);
                                    String col1 =cursor.getString(2);
                                    String weight =cursor.getString(3);
                                    if(col1.contentEquals(second))
                                    {
                                        String val=col;
                                        Toast.makeText(DuraScan.this, "val"+val, Toast.LENGTH_SHORT).show();


                                        if(s.equals("duradelivery"))
                                        {

                                            Intent intent=new Intent(DuraScan.this,DuraDeliveryMain.class);
                                            intent.putExtra("result",val);
                                            startActivity(intent);


                                        }
                                        else
                                            {
                                                Intent intent=new Intent(DuraScan.this,Dura.class);
                                                intent.putExtra("result",val);
                                                intent.putExtra("wieght",weight);
                                                startActivity(intent);

                                            }



                                    }
                                }
                            }

                        }
                        else {
                            Cursor cursor = synchelper.readAllData();
                            if (cursor.getCount() == 0) {
                                //      empty_imageview.setVisibility(View.VISIBLE);
                                //      no_data.setVisibility(View.VISIBLE);
                            } else {
                                while (cursor.moveToNext()) {
                                    String col=cursor.getString(1);
                                    String col1 =cursor.getString(2);
                                    String weight =cursor.getString(3);
                                    if(col1.contentEquals(res))
                                    {
                                        String val=col;
                                        Intent intent=new Intent(DuraScan.this,DuraDeliveryMain.class);
                                                                        intent.putExtra("result",val);
                                                                        startActivity(intent);
                                        if(s.equals("duradelivery"))
                                        {
                                            Intent intent1=new Intent(DuraScan.this,DuraDeliveryMain.class);
                                            intent.putExtra("result",val);
                                            startActivity(intent1);

                                        }
                                        else
                                        {
                                            Intent intent1=new Intent(DuraScan.this,Dura.class);
                                            intent1.putExtra("result",val);
                                            intent1.putExtra("wieght",weight);
                                            startActivity(intent1);
                                        }

                                    }
                                    else
                                    {
                                        Snackbar.make(frameLayout, "तुमचा स्कॅन आमच्या सिलेंडर नंबर शी मिळत नाही आहे कृपाया परत स्कॅन करा ", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                                    }
                                }
                            }


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
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }
}