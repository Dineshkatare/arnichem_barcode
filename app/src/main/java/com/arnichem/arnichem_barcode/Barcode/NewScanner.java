package com.arnichem.arnichem_barcode.Barcode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnichem.arnichem_barcode.GodownView.Closing_stock.closing_helper;
import com.arnichem.arnichem_barcode.GodownView.GodownFullRecipt.GodownFullReciptHelper;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryHelper;
import com.arnichem.arnichem_barcode.GodownView.godownempty.GodownEmptyHelper;
import com.arnichem.arnichem_barcode.Producation.Co2.Co2Helper;
import com.arnichem.arnichem_barcode.Producation.Nitrogen.NitrogenHelper;
import com.arnichem.arnichem_barcode.Producation.Oxygen.OxygenHelper;
import com.arnichem.arnichem_barcode.Producation.ZeroAir.ZeroAirHelper;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Duraempty.DuraemptyHelper;
import com.arnichem.arnichem_barcode.TransactionsView.Empty.AddClyHelper;
import com.arnichem.arnichem_barcode.TransactionsView.FullRecipt.FullReciptHelper;
import com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardDatabaseHelper;
import com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardMain;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.MyDatabaseHelper;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliDB;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.scanner.BarcodeReader;
import com.arnichem.arnichem_barcode.view.BusinessPartnersHandler;
import com.arnichem.arnichem_barcode.view.DistributorHelper;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.google.android.gms.vision.barcode.Barcode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewScanner extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {
    String type = "";
    String dis = "";
    deliDB deli_db;
    AddClyHelper empty_db;
    FullReciptHelper full_receipt_db;
    MyDatabaseHelper outward_db;
    DuraemptyHelper duraEmptyHelper;
    InWardDatabaseHelper inward_db;
    BusinessPartnersHandler businessPartnersHandler;
    Co2Helper co2_db;
    OxygenHelper o2db;
    ZeroAirHelper zero_air_db;
    NitrogenHelper no2_db;
    GodownDeliveryHelper godownDeliveryHelper;
    GodownEmptyHelper godownEmptyHelper;
    closing_helper closingHelper;
    EditText editText;
    GodownFullReciptHelper godownFullReciptHelper;
    syncHelper synchelper;
    private BarcodeReader barcodeReader;
    boolean status = true;
    private Button finishBtn;
    Vibrator vibrate;
    ImageView flashImg;
    private String inputHolder = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_scanner);
        finishBtn = findViewById(R.id.finishBtn);
        flashImg = findViewById(R.id.flash);
        getIntentData();
        registerFun();
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        editText = findViewById(R.id.newScan);
        editText.requestFocus();

        // Fix: Use OnKeyListener instead of dispatchKeyEvent
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                        && event.getAction() == KeyEvent.ACTION_UP) {

                    String text = editText.getText().toString().trim();
                    if (!text.isEmpty()) {
                        Log.d("ScannerFix", "NewScanner Enter detected: " + text);
                        scanned(text, true);
                    }
                    editText.setText("");
                    editText.requestFocus();
                    return true;
                }
                return false;
            }
        });

        // Focus protection
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    editText.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            editText.requestFocus();
                        }
                    }, 50);
                }
            }
        });

        flashImg.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                if (barcodeReader.useFlash) {
                    barcodeReader.useFlash = false;
                    barcodeReader.mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    Resources resources = getResources();
                    flashImg.setImageDrawable(resources.getDrawable(R.drawable.flash));
                } else {
                    barcodeReader.useFlash = true;
                    barcodeReader.mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    Resources resources = getResources();
                    flashImg.setImageDrawable(resources.getDrawable(R.drawable.flash_on));

                }
            }
        });
    }

    private void customDailog(String cyl_no, String barcode_no, String weight, String volume, String fillwith,
            String serial_no, String Hydrotest_date, String Owner, String Status, String location, boolean val) {
        Cursor cursor = businessPartnersHandler.readAllData();
        if (cursor.getCount() == 0) {
            // empty_imageview.setVisibility(View.VISIBLE);
            // no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                String cust_code = cursor.getString(2);
                if (cust_code.contentEquals(location)) {
                    location = cursor.getString(1);
                }
            }
        }
        String finalLocation = location;
        String finalLocation1 = location;
        NewScanner.this.runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = new Dialog(NewScanner.this);
                TextView okay_text, cancel_text, cylNo, barcodeTxtVal, ownerTxtVal, serialTxtVal, hydroTestDateVal,
                        gasTxtVal, statusTxtVal, locationTxtVal;
                dialog.setContentView(R.layout.custom_dailog);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(false);
                dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

                okay_text = dialog.findViewById(R.id.ok_text);
                cancel_text = dialog.findViewById(R.id.cancel_text);
                cylNo = dialog.findViewById(R.id.cylNo);
                barcodeTxtVal = dialog.findViewById(R.id.barcodeTxtVal);
                ownerTxtVal = dialog.findViewById(R.id.ownerTxtVal);
                serialTxtVal = dialog.findViewById(R.id.serialTxtVal);
                hydroTestDateVal = dialog.findViewById(R.id.hydroTestDateVal);
                gasTxtVal = dialog.findViewById(R.id.gasTxtVal);
                statusTxtVal = dialog.findViewById(R.id.statusTxtVal);
                locationTxtVal = dialog.findViewById(R.id.locationTxtVal);
                cylNo.setText(cyl_no);
                barcodeTxtVal.setText(barcode_no);
                serialTxtVal.setText(serial_no);
                gasTxtVal.setText(fillwith);
                ownerTxtVal.setText(Owner);
                hydroTestDateVal.setText(Hydrotest_date);
                locationTxtVal.setText(finalLocation1);
                statusTxtVal.setText(Status);

                SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
                Date d1 = null;
                try {
                    d1 = sdformat.parse(Hydrotest_date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date date = new Date();

                if (d1.before(date)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        hydroTestDateVal.setTextColor(getColor(R.color.red));
                    }
                }

                okay_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        status = true;
                        insertData(cyl_no, weight, barcode_no, volume, fillwith);
                        dialog.dismiss();
                    }
                });

                cancel_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        status = true;
                        barcodeReader.onResume();
                        dialog.dismiss();
                    }
                });
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();
                // if(val){
                // okay_text.setVisibility(View.GONE);
                // cancel_text.setVisibility(View.GONE);
                //// status = true;
                // insertData(cyl_no,weight,barcode_no,volume,fillwith);
                // }

            }
        });

    }

    private void insertData(String col, String weight, String col1, String volume, String fillwith) {
        smallVibarate();
        if (type.equalsIgnoreCase("delivery")) {
            deli_db.addBook(col, fillwith, volume, "C");
        } else if (type.equalsIgnoreCase("empty")) {
            empty_db.addBook(col, fillwith, volume, "C");
        } else if (type.equalsIgnoreCase("dura_delivery")) {
            Intent intent = new Intent("dura_delivery");
            // You can also include some extra data.
            intent.putExtra("dura_no", col);
            LocalBroadcastManager.getInstance(NewScanner.this).sendBroadcast(intent);
            finish();

        } else if (type.equalsIgnoreCase("full_receipt")) {
            full_receipt_db.addBook(col, fillwith, volume, "C");
        } else if (type.equalsIgnoreCase("dura_empty")) {
            duraEmptyHelper.addBook(col);
            finish();
        } else if (type.equalsIgnoreCase("dura_production")) {
            Intent intent = new Intent("dura_production");
            // You can also include some extra data.
            intent.putExtra("dura_no", col);
            intent.putExtra("wieght", weight);
            LocalBroadcastManager.getInstance(NewScanner.this).sendBroadcast(intent);
            finish();

        } else if (type.equalsIgnoreCase("inward")) {
            inward_db.addBook(col, "C");
        } else if (type.equalsIgnoreCase("outward")) {
            outward_db.addBook(col, fillwith, volume, "C");
        } else if (type.equalsIgnoreCase("closing_stock")) {
            closingHelper.addBook(col, "C");
        } else if (type.equalsIgnoreCase("godown_delivery")) {
            godownDeliveryHelper.addBook(col, fillwith, volume, "C");
        } else if (type.equalsIgnoreCase("godown_empty")) {
            godownEmptyHelper.addBook(col, fillwith, volume, "C");
        } else if (type.equalsIgnoreCase("godown_fullreceipt")) {
            godownFullReciptHelper.addBook(col, fillwith, volume, "C");
        } else if (type.equalsIgnoreCase("liquid_delivery")) {

        } else if (type.equalsIgnoreCase("o2")) {
            o2db.addBook(col, dis, volume, "C");
        } else if (type.equalsIgnoreCase("no2")) {
            no2_db.addBook(col, dis, volume, "C");
        } else if (type.equalsIgnoreCase("zero_air")) {
            zero_air_db.addBook(col, dis, volume, "C");
        } else if (type.equalsIgnoreCase("co2")) {
            co2_db.addBook(col, dis, volume, "C");
        } else if (type.equalsIgnoreCase("ammonia_delivery")) {
            Intent intent = new Intent("ammonia_delivery");
            intent.putExtra("ammonia_no", col);
            intent.putExtra("volume", volume);
            intent.putExtra("fill_with", fillwith);
            LocalBroadcastManager.getInstance(NewScanner.this).sendBroadcast(intent);
            finish();
        }

    }

    private void registerFun() {
        vibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        businessPartnersHandler = new BusinessPartnersHandler(NewScanner.this);
        godownDeliveryHelper = new GodownDeliveryHelper(NewScanner.this);

        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
        assert barcodeReader != null;
        barcodeReader.setBeepSoundFile("beep.mp3");

        if (type.equalsIgnoreCase("delivery")) {
            deli_db = new deliDB(NewScanner.this);
        } else if (type.equalsIgnoreCase("empty")) {
            empty_db = new AddClyHelper(NewScanner.this);
        } else if (type.equalsIgnoreCase("dura_delivery")) {

        } else if (type.equalsIgnoreCase("full_receipt")) {
            full_receipt_db = new FullReciptHelper(NewScanner.this);
        } else if (type.equalsIgnoreCase("dura_empty")) {
            duraEmptyHelper = new DuraemptyHelper(NewScanner.this);
        } else if (type.equalsIgnoreCase("inward")) {
            inward_db = new InWardDatabaseHelper(NewScanner.this);
        } else if (type.equalsIgnoreCase("outward")) {
            outward_db = new MyDatabaseHelper(NewScanner.this);
        } else if (type.equalsIgnoreCase("closing_stock")) {
            closingHelper = new closing_helper(NewScanner.this);
        } else if (type.equalsIgnoreCase("godown_delivery")) {
            godownDeliveryHelper = new GodownDeliveryHelper(NewScanner.this);
        } else if (type.equalsIgnoreCase("godown_empty")) {
            godownEmptyHelper = new GodownEmptyHelper(NewScanner.this);
        } else if (type.equalsIgnoreCase("godown_fullreceipt")) {
            godownFullReciptHelper = new GodownFullReciptHelper(NewScanner.this);
        } else if (type.equalsIgnoreCase("o2")) {
            o2db = new OxygenHelper(NewScanner.this);
        } else if (type.equalsIgnoreCase("no2")) {
            no2_db = new NitrogenHelper(NewScanner.this);
        } else if (type.equalsIgnoreCase("zero_air")) {
            zero_air_db = new ZeroAirHelper(NewScanner.this);
        } else if (type.equalsIgnoreCase("co2")) {
            co2_db = new Co2Helper(NewScanner.this);
        }

        synchelper = new syncHelper(NewScanner.this);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        type = intent.getExtras().getString("type", "");
        dis = intent.getExtras().getString("dis", "");

        if (type.equalsIgnoreCase("barcode_register")) {
            // finishBtn.setVisibility(View.GONE);
        }

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

        scanned(barcode.displayValue, false);

    }

    private void scanned(String displayValue, boolean val) {
        if (type.equalsIgnoreCase("barcode_register")) {
            Intent intent = new Intent("barcode_register");
            // You can also include some extra data.
            intent.putExtra("val", displayValue);
            editText.setText("");
            LocalBroadcastManager.getInstance(NewScanner.this).sendBroadcast(intent);
            finish();
        }
        if (status) {
            Cursor cursor = synchelper.readAllData();
            if (cursor.getCount() == 0) {
            } else {
                while (cursor.moveToNext()) {
                    String col = cursor.getString(1);
                    String col1 = cursor.getString(2);
                    String weight = cursor.getString(3);
                    String volume = cursor.getString(4);
                    String Fillwith = cursor.getString(5);
                    String Serial_no = cursor.getString(6);
                    String Hydrotest_date = cursor.getString(7);
                    String Owner = cursor.getString(8);
                    String Status = cursor.getString(9);
                    String Location = cursor.getString(10);

                    if (col1.contentEquals(displayValue)) {
                        status = false;
                        editText.setText("");
                        editText.requestFocus();
                        barcodeReader.playBeep();
                        vibarate();
                        customDailog(col, col1, weight, volume, Fillwith, Serial_no, Hydrotest_date, Owner, Status,
                                Location, val);
                        break;
                    }
                }
                editText.setText("");
                editText.requestFocus();
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

    private void vibarate() {

        if (vibrate.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                VibrationEffect ve = VibrationEffect.createOneShot(1000L,
                        VibrationEffect.DEFAULT_AMPLITUDE);
                vibrate.vibrate(ve, audioAttributes);
            } else {
                vibrate.vibrate(1000L);
            }
        }

    }

    private void smallVibarate() {
        if (vibrate.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                VibrationEffect ve = VibrationEffect.createOneShot(400L,
                        VibrationEffect.DEFAULT_AMPLITUDE);
                vibrate.vibrate(ve, audioAttributes);
            } else {
                vibrate.vibrate(400L);
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event == null) {
            return false;
        }

        int action = event.getAction();

        try {
            if (action == KeyEvent.ACTION_DOWN) {
                Log.d("KEYDOWN", event.getKeyCode() + "");
            } else if (action == KeyEvent.ACTION_UP) {
                char pressedKey = (char) event.getUnicodeChar();
                Log.d("pressedKey != 0", pressedKey + "");
                if (pressedKey != 0) {
                    if (pressedKey == ',' || pressedKey == 10) {
                        Log.d("pressedKey != ','", "inputHolder " + this.inputHolder);
                        // Perform action based on inputHolder value
                        // registerID(this.inptHolder);
                        // this.inputHolder = "";
                    } else {
                        this.inputHolder += pressedKey;
                        Log.d("pressedKey", pressedKey + "");
                    }
                }
                Log.d("KEYUP", event.getKeyCode() + "");
            }

            Log.d("load", editText.getText().toString() + "");

            // Perform action based on EditText input
            String text = editText.getText().toString();
            if (type.equalsIgnoreCase("inward")) {
                if (!text.isEmpty()) {
                    scanned(text, true);
                }
            } else if (type.equalsIgnoreCase("outward")) {
                if (!text.isEmpty()) {
                    scanned(text, true);
                }
            }

            // Delay the call to the `call()` method by 1000 milliseconds

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DispatchKeyEvent", "An exception occurred: " + e.getMessage());
        }

        Log.d("KEY", event.getKeyCode() + "");
        return false;
    }

}