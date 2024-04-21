package com.arnichem.arnichem_barcode.TransactionsView.deliverynew;

import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.scanner.BarcodeReader;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;

public class deliAdd extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {

    Button add_button,finish;
    deliDB delidb;
    syncHelper synchelper;
    FrameLayout frameLayout;
    int i=1;
    private BarcodeReader barcodeReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deli_add);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);

      //  frameLayout=findViewById(R.id.framelayoutdeli);
        delidb=new deliDB(deliAdd.this);
        synchelper=new syncHelper(deliAdd.this);
//        title_input = findViewById(R.id.title_input);
//        add_button = findViewById(R.id.add_button);
     //   finish = findViewById(R.id.go);
//        CodeScannerView scannerView = findViewById(R.id.outscannerview);
//        mCodeScanner = new CodeScanner(this, scannerView);
//        mCodeScanner.setDecodeCallback(new DecodeCallback() {
//            @Override
//            public void onDecoded(@NonNull final Result result) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String res= result.getText();
//                        if(res==null)
//                        {
//                            Toast.makeText(deliAdd.this, "null", Toast.LENGTH_SHORT).show();
//                            //assign some value to result
//                        }
//                        else if(res.contains("="))
//                        {
//                            StringTokenizer tokens = new StringTokenizer(res, "=");
//                            String first = tokens.nextToken();// this will contain "Fruit"
//                            String second = tokens.nextToken();
////                            title_input.setText(second);
//                            Cursor cursor = synchelper.readAllData();
//                            if (cursor.getCount() == 0) {
//                                //      empty_imageview.setVisibility(View.VISIBLE);
//                                //      no_data.setVisibility(View.VISIBLE);
//                            } else {
//                                while (cursor.moveToNext()) {
//                                    String col=cursor.getString(1);
//                                    String col1 =cursor.getString(2);
//                                    String fill =cursor.getString(5);
//                                    String vol =cursor.getString(4);
//                                    if(col1.contentEquals(second))
//                                    {
//                                        if(i==1)
//                                        {
//                                            delidb.addBook(col,fill,vol);
//                                            Snackbar.make(frameLayout, "तुमचा बारकोड नंबर सिलेंडर नंबर "+col+" शी जोडला आहे ", Snackbar.LENGTH_LONG).setBackgroundTint(Color.GREEN).setTextColor(Color.WHITE).show();
//                                            i++;
//                                        }
//                                    }
//                                }
//                            }
//
//                        }
//                        else {
//                            Snackbar.make(frameLayout, "तुमचा स्कॅन आमच्या सिलेंडर नंबर नंबर शी मिळत नाही आहे कृपाया परत स्कॅन करा ", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
//
//                        }
//                    }
//                });
//            }
//        });
//        scannerView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mCodeScanner.startPreview();
//            }
//        });
//        add_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//

//
//
//
//            }
//        });
//        finish.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(deliAdd.this, InWardMain.class));
//            }
//        });


    }



    @Override
    protected void onResume() {
        barcodeReader.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        barcodeReader.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        barcodeReader.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onScanned(Barcode barcode) {
                Cursor cursor = synchelper.readAllData();
                if (cursor.getCount() == 0) {
                } else {
                    while (cursor.moveToNext()) {
                        String col=cursor.getString(1);
                        String col1 =cursor.getString(2);
                        String volume=cursor.getString(4);
                        String Fillwith=cursor.getString(5);
                        if(col1.contentEquals(barcode.displayValue))
                        {
                            if(i==1)
                            {
                                delidb.addBook(col,Fillwith,volume,"no");
                                i++;
                            }
                        }
                    }
                }

    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {

    }

    @Override
    public void onCameraPermissionDenied() {

    }


}