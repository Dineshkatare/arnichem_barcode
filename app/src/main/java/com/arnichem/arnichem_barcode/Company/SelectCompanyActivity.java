package com.arnichem.arnichem_barcode.Company;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.CompanyUpdatedEvent;
import com.arnichem.arnichem_barcode.view.login;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;

public class SelectCompanyActivity extends AppCompatActivity {

    ArrayAdapter<String> comapanyAdapter;
    Spinner comapySpiner;
    List<String> labels  = new ArrayList<>();
    Button submitBtn;
    String selectedCompanyName;
    int poslocfixdel=0;
    ProgressDialog dialog,dialog_company_logo,dialog_print_logo,dialog_phone_number,dialog_upi;
    CompanyHelper companyHelper;
    File logoFile,printlogoFile,phonenumberFile,upi_paymentFile;
    String dirPath;
    String companylogo = "companylogo.jpeg";
    String printlogo = "print_logo.jpeg";
    String  phonenumber = "phone_number.jpeg";
    String  upi_payment = "upi_payment.jpeg";

    private int EXTERNAL_STORAGE_PERMISSION_CODE = 23;
//    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            loadSpinnerData();
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_company);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
//                new IntentFilter("custom-event-name"));
        companyHelper = new CompanyHelper(SelectCompanyActivity.this);
        // Initialization Of DownLoad Button
        AndroidNetworking.initialize(getApplicationContext());

        //Folder Creating Into Phone Storage
        File outputDir = getApplicationContext().getCacheDir();
        //file Creating With Folder & Fle Name
        dirPath= outputDir.getAbsolutePath();

        String defaultComapny ="Select Your Company";
        labels.add(defaultComapny);
        // Requesting Permission to access External Storage
        hasWriteStoragePermission();
        logoFile = new File(dirPath, companylogo);
        printlogoFile = new File(dirPath, printlogo);
        phonenumberFile = new File(dirPath, phonenumber);
        upi_paymentFile = new File(dirPath, upi_payment);

        companyHelper.deleteAllData();
        comapySpiner = findViewById(R.id.companySpinner);
        submitBtn = findViewById(R.id.companySubmitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(poslocfixdel==0){
                    Toast.makeText(SelectCompanyActivity.this, "Please select your company", Toast.LENGTH_SHORT).show();
                }else{
                    SharedPref.getInstance(getApplicationContext()).setSelectedCompany(true);
                    Intent intent = new Intent(SelectCompanyActivity.this, login.class);
                    startActivity(intent);
                }
            }
        });

        if(isInternetAvailable()){
            fetchAllInventoryGases();
        } else{

                try {
                    AlertDialog alertDialog = new AlertDialog.Builder(SelectCompanyActivity.this).create();

                    alertDialog.setTitle("Info");
                    alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again");
                    alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                    alertDialog.setButton("Refresh", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Refresh();
                            alertDialog.dismiss();

                        }
                    });

                    alertDialog.show();
                } catch (Exception e) {
                    Log.d("fkcdvf", "Show Dialog: " + e.getMessage());
                }
        }

        comapySpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    selectedCompanyName = comapanyAdapter.getItem(position);
                    poslocfixdel = position;
                    SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(poslocfixdel));
                    Cursor cursor = companyHelper.readAllData();
                    if (cursor.getCount() == 0) {
                        //      empty_imageview.setVisibility(View.VISIBLE);
                        //      no_data.setVisibility(View.VISIBLE);
                    } else {
                        while (cursor.moveToNext()) {
                            String company_id = cursor.getString(1);
                            String company_short_name = cursor.getString(2);
                            String company_full_name = cursor.getString(3);
                            String db_host = cursor.getString(4);
                            String db_username = cursor.getString(5);
                            String db_password = cursor.getString(6);
                            String base_url = cursor.getString(7);
                            String db_name = cursor.getString(8);
                            String terms_text = cursor.getString(9);
                            String own_code = cursor.getString(10);
                            String batch_prefix = cursor.getString(11);
                            String cyc_prefix = cursor.getString(12);
                            String login_msg = cursor.getString(14);
                            String bg_color = cursor.getString(13);





                            if (company_full_name.contentEquals(selectedCompanyName)) {
                                SharedPref.getInstance(getApplicationContext()).setCompanyID(company_id);
                                SharedPref.getInstance(getApplicationContext()).setCompanyShortName(company_short_name);
                                SharedPref.getInstance(getApplicationContext()).setCompanyFullName(company_full_name);
                                SharedPref.getInstance(getApplicationContext()).setBgColor(bg_color);
                                SharedPref.getInstance(getApplicationContext()).setDBHost(db_host);
                                SharedPref.getInstance(getApplicationContext()).setDBUsername(db_username);
                                SharedPref.getInstance(getApplicationContext()).setDBPassword(db_password);
                                SharedPref.getInstance(getApplicationContext()).setDBName(db_name);
                                SharedPref.getInstance(getApplicationContext()).setBaseUrl(base_url);
                                SharedPref.getInstance(getApplicationContext()).setTermsText(terms_text);
                                SharedPref.getInstance(getApplicationContext()).setOwnCode(own_code);
                                SharedPref.getInstance(getApplicationContext()).setBatchPrefix(batch_prefix);
                                SharedPref.getInstance(getApplicationContext()).setCycPrefix(cyc_prefix);
                                SharedPref.getInstance(getApplicationContext()).setLoginMsg(login_msg);



                                try {
                                    saveCompanyLogo(base_url);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }


    private void hasWriteStoragePermission() {

            if (ActivityCompat.checkSelfPermission(SelectCompanyActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SelectCompanyActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION_CODE);


            }

    }
    private void saveCompanyLogo(String base_url) throws IOException {
        dialog_company_logo = new ProgressDialog(SelectCompanyActivity.this);
        dialog_company_logo.setTitle("Data Inserting");
        dialog_company_logo.setMessage("Please wait....");
        dialog_company_logo.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog_company_logo.show();

        String hellWrld = base_url.replace("/public_html/","");// this will contain "Fruit"

        Log.d("logo", "first: "+hellWrld);


        StringBuilder logoUrl = new StringBuilder("http://"+hellWrld);
        logoUrl.append("/images/logo.png");


        String finalLogoUrl = logoUrl.toString();

        Log.d("logo", "saveImage: "+finalLogoUrl);
        drawable_from_url(finalLogoUrl,companylogo,"logo");
        savePrintLogo(base_url);
    }

    private void savePrintLogo(String base_url) throws IOException {
        dialog_print_logo = new ProgressDialog(SelectCompanyActivity.this);
        dialog_print_logo.setTitle("Data Inserting");
        dialog_print_logo.setMessage("Please wait....");
        dialog_print_logo.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog_print_logo.show();

        String hellWrld = base_url.replace("/public_html/","");// this will contain "Fruit"

        Log.d("logo", "first: "+hellWrld);


        StringBuilder logoUrl = new StringBuilder("http://"+hellWrld);
        logoUrl.append("/barcode/APP/app_images/print_logo.png");

        String finalLogoUrl = logoUrl.toString();
        Log.d("logo", "saveImage: "+finalLogoUrl);
        drawable_from_url(finalLogoUrl,printlogo,"print_logo");

        savePhoneNumberLogo(base_url);
    }

    private void savePhoneNumberLogo(String base_url) throws IOException {
        dialog_phone_number = new ProgressDialog(SelectCompanyActivity.this);
        dialog_phone_number.setTitle("Data Inserting");
        dialog_phone_number.setMessage("Please wait....");
        dialog_phone_number.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog_phone_number.show();

        String hellWrld = base_url.replace("/public_html/","");// this will contain "Fruit"

        Log.d("logo", "first: "+hellWrld);


        StringBuilder logoUrl = new StringBuilder("http://"+hellWrld);
        logoUrl.append("/barcode/APP/app_images/phone_number.png");

        String finalLogoUrl = logoUrl.toString();

        Log.d("logo", "saveImage: "+finalLogoUrl);
        drawable_from_url(finalLogoUrl,phonenumber,"phone_number");
        save_upi_payment(base_url);
    }

    private void


    save_upi_payment(String base_url) throws IOException {
        dialog_upi = new ProgressDialog(SelectCompanyActivity.this);
        dialog_upi.setTitle("Data Inserting");
        dialog_upi.setMessage("Please wait....");
        dialog_upi.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog_upi.show();

        String hellWrld = base_url.replace("/public_html/","");// this will contain "Fruit"

        Log.d("logo", "first: "+hellWrld);


        StringBuilder logoUrl = new StringBuilder("http://"+hellWrld);
        logoUrl.append("/barcode/APP/app_images/upi_payment.png");

        String finalLogoUrl = logoUrl.toString();
        Log.d("logo", "saveImage: "+finalLogoUrl);
        drawable_from_url(finalLogoUrl,upi_payment,"upi");

    }


    public void drawable_from_url(String path,String fileName,String type){
       AndroidNetworking.download(path, dirPath, fileName)
               .build()
               .startDownload(new DownloadListener() {
                   @Override
                   public void onDownloadComplete() {

                       if(type.equalsIgnoreCase("print_logo")){
                           SharedPref.getInstance(getApplicationContext()).setPrintLogo(printlogoFile.getPath());
                           dialog_print_logo.dismiss();
                       }else if(type.equalsIgnoreCase("logo")){
                           SharedPref.getInstance(getApplicationContext()).setLogo(logoFile.getPath());
                           dialog_company_logo.dismiss();

                       }else  if(type.equalsIgnoreCase("phone_number")){
                           SharedPref.getInstance(getApplicationContext()).setPhoneNumber(phonenumberFile.getPath());
                           dialog_phone_number.dismiss();

                       }else  if(type.equalsIgnoreCase("upi")){
                           SharedPref.getInstance(getApplicationContext()).setPrintUpi(upi_paymentFile.getPath());
                           dialog_upi.dismiss();

                       }
                   }

                   @Override
                   public void onError(ANError anError) {
                       dialog_print_logo.dismiss();
                       dialog_company_logo.dismiss();
                       dialog_phone_number.dismiss();
                       dialog_upi.dismiss();
                   }
               });
        }

    private void Refresh() {
        if(isInternetAvailable()){
            fetchAllInventoryGases();
        } else{

            try {
                AlertDialog alertDialog = new AlertDialog.Builder(SelectCompanyActivity.this).create();

                alertDialog.setTitle("Info");
                alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again");
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.setButton("Refresh", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();

                    }
                });

                alertDialog.show();
            } catch (Exception e) {
                Log.d("fkcdvf", "Show Dialog: " + e.getMessage());
            }
        }
    }


    private void loadSpinnerData() {
        CompanyHelper db = new CompanyHelper(getApplicationContext());
        List<String> newlabels = db.getAllLabels();

        labels.addAll(newlabels);

        // Creating adapter for spinner
        comapanyAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        comapanyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        comapySpiner.setAdapter(comapanyAdapter);
        if (poslocfixdel != 0) {
            comapySpiner.setSelection(poslocfixdel);
        }
    }

    private void fetchAllInventoryGases() {
        dialog = new ProgressDialog(SelectCompanyActivity.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
 //       String url = "http://arnichem.co.in/intranet/barcode/APP/getCompanies.php";
        final JsonArrayRequest request = new JsonArrayRequest(APIClient.getCompanies, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                dialog.dismiss();
                try {
                    JSONObject ob = response.getJSONObject(0);
                    String msg = ob.getString("status");
                    if (msg.equalsIgnoreCase("success")) {
                        runOnUiThread(() -> {
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject ob1 = response.getJSONObject(i);

                                    companyHelper.addCompany(
                                            ob1.getString("company_id"),
                                            ob1.getString("company_short_name"),
                                            ob1.getString("company_long_name"),
                                            ob1.getString("db_host"),
                                            ob1.getString("db_username"),
                                            ob1.getString("db_password"),
                                            ob1.getString("db_name"),
                                            ob1.getString("base_url"),
                                            ob1.getString("terms_text"),
                                            ob1.getString("own_cyl_code"),
                                            ob1.getString("batch_prefix"),
                                            ob1.getString("cyc_prefix"),
                                            ob1.optString("login_msg", ""),
                                            ob1.getString("bg_color"));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            onCompanyUpdated(new CompanyUpdatedEvent(""));

//                            Intent intent = new Intent("custom-event-name");
//                            // You can also include some extra data.
//                            LocalBroadcastManager.getInstance(SelectCompanyActivity.this).sendBroadcast(intent);
                        });


                    }
                } catch (Exception e) {
                    Toast.makeText(SelectCompanyActivity.this, "catch" + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Toast.makeText(SelectCompanyActivity.this, "ERROR RESPONSE" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(request);
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCompanyUpdated(CompanyUpdatedEvent event) {
        loadSpinnerData(); // Your method to update the spinner
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


}