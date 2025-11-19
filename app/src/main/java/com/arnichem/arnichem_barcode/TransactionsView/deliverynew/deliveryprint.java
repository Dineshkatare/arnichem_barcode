package com.arnichem.arnichem_barcode.TransactionsView.deliverynew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint.DeliveryPrintDB;
import com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint.ViewDeliveryPrint;
import com.arnichem.arnichem_barcode.Producation.Nitrogen.FirstNitrogen;
import com.arnichem.arnichem_barcode.Producation.Nitrogen.NitrogenPrint;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CylinderNamePrintAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.OutwardPrint;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncBluetoothEscPosPrint;
import com.arnichem.arnichem_barcode.finalprint.aysnc.AsyncEscPosPrinter;
import com.arnichem.arnichem_barcode.finalprint.finalprint;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class deliveryprint extends AppCompatActivity {
 //   FilledWithAdapter filledWithAdapter;
    CylinderNamePrintAdapter cylinderNamePrintAdapter;
    FilledWithAdapter filledWithAdapter;


    ArrayList<String> name, tot,fillWithList;
    ArrayList<String> cylIdList;
    APIInterface apiInterface;


    RecyclerView Filled_with_Recycle_View,cyclinderNames;
    Button duradelprint;
    String durano,custname,empb,custcode,count,sign_path;
    TextView empbid,dateid,custnameid,vehicleno,arnichemdignprint,counttxt,tvcode;
    deliDB deliDB;
    ArrayList<String> newlist;
    DeliveryPrintDB deliveryPrintDB;
    Bitmap printLogoDr,phoneNumberDr,digital_sign;
    ImageView printImg,phoneImg,custnamesign;
    TextView arnichemsignTxt,termsTxt;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliveryprint);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        empbid=findViewById(R.id.cdcnoid);
        dateid=findViewById(R.id.cddateid);
        deliDB = new deliDB(deliveryprint.this);
        custnameid=findViewById(R.id.cdcustnameid);
        arnichemdignprint=findViewById(R.id.cdarnichemdignprint);
        tvcode=findViewById(R.id.codeid);
        counttxt=findViewById(R.id.totalq);
        vehicleno=findViewById(R.id.cdvehicleno);
        duradelprint=findViewById(R.id.delyprint);
        printImg = findViewById(R.id.printImg);
        phoneImg = findViewById(R.id.phoneImg);
        arnichemsignTxt = findViewById(R.id.arnichemsignTxt);
        custnamesign = findViewById(R.id.custnamesign);
        termsTxt = findViewById(R.id.termsTxt);
        deliveryPrintDB=new DeliveryPrintDB(this);
        Intent i=getIntent();
        custname=i.getStringExtra("custname");
        empb=i.getStringExtra("empb");
        durano= i.getStringExtra("cylinder");
        custcode=i.getStringExtra("custcode");
        count=i.getStringExtra("count");
        sign_path=i.getExtras().getString("sign_path","");
        name = new ArrayList<>();
        tot = new ArrayList<>();
        newlist = new ArrayList<String>();
        fillWithList = new ArrayList<>();
        cylIdList = new ArrayList<>();
        apiInterface = APIClient.getClient().create(APIInterface.class);

        check();


        cylinder();

        empbid.setText(empb);
        deliDB.deleteAllData();
        tvcode.setText(custcode);
        counttxt.setText(count);
        custnameid.setText(custname);

        dateid.setText(DateFormat.getDateTimeInstance().format(new Date()));
        vehicleno.setText(SharedPref.getInstance(this).getVehicleNo());
        cyclinderNames = findViewById(R.id.cyclinderNames);
        Filled_with_Recycle_View=findViewById(R.id.fillwithrec);
        filledWithAdapter = new FilledWithAdapter(deliveryprint.this, this, name, tot);
        Filled_with_Recycle_View.setAdapter(filledWithAdapter);
        cylinderNamePrintAdapter = new CylinderNamePrintAdapter(deliveryprint.this, this, cylIdList, newlist, fillWithList);
        cyclinderNames.setAdapter(cylinderNamePrintAdapter);
        cyclinderNames.setLayoutManager(new LinearLayoutManager(deliveryprint.this));
        Filled_with_Recycle_View.setLayoutManager(new LinearLayoutManager(deliveryprint.this));
        arnichemdignprint.setText(SharedPref.getInstance(this).FirstName()+SharedPref.getInstance(this).LastName());
        duradelprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFullScrollViewImage();
                printBluetooth();

            }
        });
        String print_logo = SharedPref.mInstance.getPrintLogo();
        File imgFile = new  File(print_logo);
        if(imgFile.exists()){
             printLogoDr = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
             printImg.setImageBitmap(printLogoDr);
        }

        String phoneNumber = SharedPref.mInstance.getPhoneNumber();
        File imgFilePhoneNumber = new  File(phoneNumber);
        if(imgFile.exists()){
            phoneNumberDr = BitmapFactory.decodeFile(imgFilePhoneNumber.getAbsolutePath());
            phoneImg.setImageBitmap(phoneNumberDr);
        }
        arnichemsignTxt.setText(SharedPref.mInstance.getOwnCode());
        termsTxt.setText(SharedPref.mInstance.getTermsText());
        if(!sign_path.isEmpty()){
            File signFile = new File(sign_path);
            if(signFile.exists()){
                digital_sign = BitmapFactory.decodeFile(signFile.getAbsolutePath());
                digital_sign = Bitmap.createScaledBitmap(digital_sign,200, 200, true);

                custnamesign.setImageBitmap(digital_sign);
            }
        }else {
            digital_sign = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
            digital_sign.eraseColor(Color.WHITE);
            custnamesign.setImageBitmap(digital_sign);
        }

    }

    public static final int PERMISSION_BLUETOOTH = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
           if (requestCode == finalprint.PERMISSION_BLUETOOTH) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted for Bluetooth, continue with the Bluetooth operation
            new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
           // secondPrint();
        } else {
            // Permission denied, inform the user
            Toast.makeText(this, "Bluetooth permission denied. Cannot print.", Toast.LENGTH_SHORT).show();
        }
    } else if (requestCode == 1) {
        // This handles the Bluetooth device selection permission for Android 12 and above
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed to select Bluetooth device
            selectBluetoothDevice();
        } else {
            // Permission denied, inform the user
            Toast.makeText(this, "Bluetooth connect permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

        if (requestCode == 101) { // storage permission request
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // 👍 Permission granted → call again
                saveFullScrollViewImage();

            } else {
                Toast.makeText(this, "Storage permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void selectBluetoothDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) and above
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                return;
            }
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            final List<BluetoothDevice> deviceList = new ArrayList<>(pairedDevices);
            final CharSequence[] deviceNames = new CharSequence[deviceList.size()];

            for (int i = 0; i < deviceList.size(); i++) {
                deviceNames[i] = deviceList.get(i).getName();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a Bluetooth Device");
            builder.setItems(deviceNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BluetoothDevice device = deviceList.get(which);
                    selectedDevice = new BluetoothConnection(device);

                    printBluetooth();
                    // Toast.makeText(getApplicationContext(), "Selected Device: " + device.getName(), Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        } else {
            Toast.makeText(this, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
        }
    }

    private BluetoothConnection selectedDevice;
    BitmapDrawable flip(BitmapDrawable d)
    {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap src = d.getBitmap();
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return new BitmapDrawable(dst);
    }

    public void printBluetooth() {
        if (selectedDevice == null) {
            selectBluetoothDevice();
            
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        finalprint.PERMISSION_BLUETOOTH);
            } else {
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
                //secondPrint();
            }
        } else {
            // Below Android 12
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, finalprint.PERMISSION_BLUETOOTH);
            } else {
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
               // secondPrint();
            }
        }

    }
    public void secondPrint() {
        if (selectedDevice == null) {
            selectBluetoothDevice();
            
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        finalprint.PERMISSION_BLUETOOTH);
            } else {
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
            }
        } else {
            // Below Android 12
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, finalprint.PERMISSION_BLUETOOTH);
            } else {
                new AsyncBluetoothEscPosPrint(this).execute(getAsyncEscPosPrinter(selectedDevice));
            }
        }

    }


    public void cylinder() {

        Cursor cursor = deliDB.readAllData();
        if (cursor.getCount() == 0) {


        } else {

            while (cursor.moveToNext()) {
                cylIdList.add(cursor.getString(0));
                newlist.add(cursor.getString(1));
                fillWithList.add(cursor.getString(2));
                deliveryPrintDB.addBook(cursor.getString(1),DateFormat.getDateTimeInstance().format(new Date()),custcode,custname,count,empb);

            }


        }
    }


    void check(){
        Cursor cursor = deliDB.readcount();
        if(cursor.getCount() == 0){
        }else{
            while (cursor.moveToNext()){
                name.add(cursor.getString(3));
                tot.add(cursor.getString(2));
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 5);
        return printer.setTextToPrint(
                        "[C]        Delivery challan  \n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,phoneNumberDr)+"</img>\n" +
                        "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,printLogoDr)+"</img>\n\n" +
                        "[C]<font size='small'>DCNO -  "+empb+"</font>\n" +
                        "[C]<font size='small'>Date -  "+DateFormat.getDateTimeInstance().format(new Date())+"</font>\n" +
                        "[C]<font size='small'>Code -  "+custcode+"</font>\n" +
                        "[C]<font size='small'>Name -  "+custname+"</font>\n" +
                        "[C]<font size='small'><b>       Cylinder Numbers </b></font>\n" +
                        "[C]<font size='small'><b>      "+foreaching()+"</b></font>\n" +
                        "[C]<font size='extra-small'>        "+foreachname()+"</font>\n" +
                        "[C]<font size='small'>Total Quantity : "+count+"</font>\n" +
                        "[C]<font size='small'>Vehicle No    :  "+ SharedPref.getInstance(this).getVehicleNo()+"</font>\n" +
                                "[L]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer,digital_sign)+"</img>\n" +
                                "[R]               [R]"+SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName()+"\n" +
                                "[R]Customer Sign [R]"+"For "+SharedPref.getInstance(this).getOwnCode()+"\n\n"+
                                "[R]<font size='small'>"+SharedPref.getInstance(this).getTermsText()+"\n"

        );
    }
    public Serializable foreaching() {
        StringBuffer text = new StringBuffer();

        for (int i = 0; i < newlist.size(); i++) {
            text.append(newlist.get(i)).append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append(fillWithList.get(i)).append('\n').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020');

        }
        return text;
    }


    public Serializable foreachname() {
        StringBuffer text = new StringBuffer();

        for(int i=0;i<name.size();i++) {
            text.append(name.get(i).toString()).append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append(tot.get(i).toString()).append('\n').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020').append('\u0020');
        }

        return text;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(deliveryprint.this, Transactions.class));
    }

    private void saveFullScrollViewImage() {
        if (sign_path == null || sign_path.isEmpty() ) {
            Log.e("SAVE_SCROLL", "Digital sign missing → Upload skipped");
            return;
        }
        // Check and request permission for Android < 10
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);

                return; // wait for permission result
            }
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Preparing receipt for upload...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {

                ScrollView scrollView = findViewById(R.id.receipt_scroll_view);
                View childView = scrollView.getChildAt(0);

                // Measure properly
                int widthSpec = View.MeasureSpec.makeMeasureSpec(scrollView.getWidth(), View.MeasureSpec.EXACTLY);
                int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                childView.measure(widthSpec, heightSpec);
                childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());

                Bitmap bitmap = Bitmap.createBitmap(
                        childView.getMeasuredWidth(),
                        childView.getMeasuredHeight(),
                        Bitmap.Config.ARGB_8888
                );

                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                childView.draw(canvas);

                // ---------- FIXED STORAGE SECTION ----------
                File storageDir = new File(
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "ArnichemReceipts"
                );

                if (!storageDir.exists()) {
                    boolean created = storageDir.mkdirs();
                    if (!created) {
                        Toast.makeText(this, "Failed to create folder!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        return;
                    }
                }

                // Create file
                String fileName = "ScrollView_" + empb + "_" + System.currentTimeMillis() + ".jpg";
                File imageFile = new File(storageDir, fileName);

                FileOutputStream out = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

                // Make it visible in Gallery
                sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(imageFile)
                ));

                // Upload automatically
                uploadAndInsertPod(imageFile.getAbsolutePath(), fileName);

                progressDialog.dismiss();
                Toast.makeText(this, "Receipt saved and uploaded!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                progressDialog.dismiss();
                e.printStackTrace();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, 1000);
    }


    private void uploadAndInsertPod(String localPath, String fileName) {
        ProgressDialog uploadDialog = new ProgressDialog(this);
        uploadDialog.setTitle("Uploading POD");
        uploadDialog.setMessage("Please wait...");
        uploadDialog.setCancelable(false);
        uploadDialog.show();

        File podFile = new File(localPath);
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), podFile);

        // ✅ Server expects "print_image"
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("print_image", fileName, fileBody);

        // ✅ Text form data
        RequestBody dcnoBody   = RequestBody.create(MediaType.parse("text/plain"), empb);
        RequestBody emailBody  = RequestBody.create(MediaType.parse("text/plain"), SharedPref.getInstance(this).getEmail());
        RequestBody dbHost     = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBHost());
        RequestBody dbUser     = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBUsername());
        RequestBody dbPass     = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBPassword());
        RequestBody dbName     = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getDBName());
        RequestBody transType  = RequestBody.create(MediaType.parse("text/plain"), "DC"); // or dynamic if available
        RequestBody vehicleNo  = RequestBody.create(MediaType.parse("text/plain"), SharedPref.mInstance.getVehicleNo()); // optional

        // ✅ Ensure the same parameter order as your PHP expects
        Call<ResponseBody> call = apiInterface.uploadPod(
                filePart,
                dcnoBody,
                emailBody,
                dbHost,
                dbUser,
                dbPass,
                dbName,
                transType,
                vehicleNo
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                uploadDialog.dismiss();

                try {
                    String resp = "";
                    if (response.body() != null)
                        resp = response.body().string();
                    else if (response.errorBody() != null)
                        resp = response.errorBody().string();

                    if (resp.isEmpty()) {
                        Toast.makeText(deliveryprint.this, "Empty server response", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONObject obj = new JSONObject(resp);
                    String status = obj.optString("status", "error");
                    String msg = obj.optString("msg", "Unknown response");

                    if (status.equalsIgnoreCase("success")) {
                        Toast.makeText(deliveryprint.this, msg, Toast.LENGTH_SHORT).show();
                        Log.d("POD_UPLOAD", "Server OK: " + msg);
                    } else {
                        Toast.makeText(deliveryprint.this, "Upload failed: " + msg, Toast.LENGTH_LONG).show();
                        Log.e("POD_UPLOAD", "Error response: " + resp);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(deliveryprint.this, "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                uploadDialog.dismiss();
                Toast.makeText(deliveryprint.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("POD_UPLOAD", "Upload failed", t);
            }
        });
    }


}