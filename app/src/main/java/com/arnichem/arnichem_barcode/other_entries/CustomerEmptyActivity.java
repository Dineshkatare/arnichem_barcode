package com.arnichem.arnichem_barcode.other_entries;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.location.Location;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.GetLocationDetail;
import com.example.easywaylocation.Listener;
import com.example.easywaylocation.LocationData;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerEmptyActivity extends AppCompatActivity implements Listener, LocationData.AddressCallBack {

    com.toptoche.searchablespinnerlibrary.SearchableSpinner spinLoc, spinCust;
    EditText edtDescription, edtRemarks;
    Button btnSubmit;
    TextView date, usernametxtvalue, vno;

    GetLocationDetail getLocationDetail;
    EasyWayLocation easyWayLocation;

    APIInterface apiInterface;
    DatabaseHandler databaseHandlercustomer;
    fromloccodehandler fromLocHelper;

    ArrayAdapter<String> locAdapter;
    ArrayAdapter<String> custAdapter;

    String from_warehouse, to_warehouse, cust_code, latitude = "0", logitude = "0", address = "0";
    // Using 0 as default selection positions if needed, though we will track
    // selection by index if necessary

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_empty);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Customer Empty");
        }

        apiInterface = APIClient.getClient().create(APIInterface.class);
        getLocationDetail = new GetLocationDetail(this, this);
        easyWayLocation = new EasyWayLocation(this, false, true, this);

        spinLoc = findViewById(R.id.spinLoc);
        spinCust = findViewById(R.id.spinCust);
        edtDescription = findViewById(R.id.edtDescription);
        edtRemarks = findViewById(R.id.edtRemarks);
        btnSubmit = findViewById(R.id.btnSubmit);
        date = findViewById(R.id.date);
        usernametxtvalue = findViewById(R.id.usernametxtvalue);
        vno = findViewById(R.id.vno);

        spinLoc.setTitle("Select Location");
        spinCust.setTitle("Select Customer");

        databaseHandlercustomer = new DatabaseHandler(this);
        fromLocHelper = new fromloccodehandler(this);

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        usernametxtvalue
                .setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        vno.setText(SharedPref.getInstance(this).getVehicleNo());

        loadLocationSpinner();
        loadCustomerSpinner();

        spinLoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                from_warehouse = locAdapter.getItem(position);
                // Logic to get code if needed, similar to GodownEmpty
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinCust.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                to_warehouse = custAdapter.getItem(position);
                // Fetch cust_code
                Cursor cursor = databaseHandlercustomer.readAllData();
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1); // Name
                        String col1 = cursor.getString(2); // Code
                        if (col.contentEquals(to_warehouse)) {
                            cust_code = col1;
                            break;
                        }
                    }
                    cursor.close();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtDescription.getText().toString().trim().isEmpty()) {
                    MDToast.makeText(CustomerEmptyActivity.this, "Please enter description", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();
                    return;
                }
                if (to_warehouse == null || to_warehouse.isEmpty()) {
                    MDToast.makeText(CustomerEmptyActivity.this, "Please select customer", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();
                    return;
                }

                uploadData();
            }
        });
    }

    private void loadLocationSpinner() {
        List<String> labels = fromLocHelper.getAllLabels();
        locAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        locAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinLoc.setAdapter(locAdapter);
    }

    private void loadCustomerSpinner() {
        List<String> labels = databaseHandlercustomer.getAllLabels();
        custAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        custAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCust.setAdapter(custAdapter);
    }

    private void uploadData() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Submitting...");
        dialog.setCancelable(false);
        dialog.show();

        String dbHost = SharedPref.mInstance.getDBHost();
        String dbUsername = SharedPref.mInstance.getDBUsername();
        String dbPassword = SharedPref.mInstance.getDBPassword();
        String dbName = SharedPref.mInstance.getDBName();
        String email = SharedPref.getInstance(this).getEmail();
        String driver = SharedPref.getInstance(this).getID();
        String transportNo = SharedPref.getInstance(this).getVehicleNo();

        // Convert Strings to RequestBody
        RequestBody rCustCode = RequestBody.create(MediaType.parse("text/plain"), cust_code != null ? cust_code : "");
        RequestBody rMetaOwn = RequestBody.create(MediaType.parse("text/plain"), "OWN");
        RequestBody rTransportNo = RequestBody.create(MediaType.parse("text/plain"), transportNo);
        RequestBody rDriver = RequestBody.create(MediaType.parse("text/plain"), driver);
        RequestBody rDesc = RequestBody.create(MediaType.parse("text/plain"), edtDescription.getText().toString());
        RequestBody rRemarks = RequestBody.create(MediaType.parse("text/plain"), edtRemarks.getText().toString());
        RequestBody rAddr = RequestBody.create(MediaType.parse("text/plain"), address);
        RequestBody rLat = RequestBody.create(MediaType.parse("text/plain"), latitude);
        RequestBody rLogi = RequestBody.create(MediaType.parse("text/plain"), logitude);
        RequestBody rEmail = RequestBody.create(MediaType.parse("text/plain"), email);

        // DB Config
        RequestBody rDbHost = RequestBody.create(MediaType.parse("text/plain"), dbHost);
        RequestBody rDbUser = RequestBody.create(MediaType.parse("text/plain"), dbUsername);
        RequestBody rDbPass = RequestBody.create(MediaType.parse("text/plain"), dbPassword);
        RequestBody rDbName = RequestBody.create(MediaType.parse("text/plain"), dbName);

        Call<ResponseBody> call = apiInterface.uploadCustEmpty(
                rCustCode,
                rMetaOwn,
                rTransportNo,
                rDriver,
                rDriver, // empb same as driver/user id
                rDesc,
                rRemarks,
                rAddr,
                rLat,
                rLogi,
                rEmail,
                rDbHost,
                rDbUser,
                rDbPass,
                rDbName);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    try {
                        String res = response.body().string();
                        // Assuming simplistic JSON check or just success based on 200 OK and "success"
                        // string in body
                        if (res.contains("success")) {
                            MDToast.makeText(CustomerEmptyActivity.this, "Entry submitted successfully!",
                                    MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                            Intent intent = new Intent(CustomerEmptyActivity.this, CustomerEmptyPrintActivity.class);
                            intent.putExtra("customer", to_warehouse);
                            intent.putExtra("location", from_warehouse);
                            intent.putExtra("description", edtDescription.getText().toString());
                            intent.putExtra("remarks", edtRemarks.getText().toString());
                            intent.putExtra("vehicle",
                                    SharedPref.getInstance(CustomerEmptyActivity.this).getVehicleNo());
                            startActivity(intent);
                            finish();
                        } else {
                            MDToast.makeText(CustomerEmptyActivity.this, "Failed: " + res, MDToast.LENGTH_LONG,
                                    MDToast.TYPE_ERROR).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        MDToast.makeText(CustomerEmptyActivity.this, "Error parsing response", MDToast.LENGTH_SHORT,
                                MDToast.TYPE_ERROR).show();
                    }
                } else {
                    MDToast.makeText(CustomerEmptyActivity.this, "Server Error", MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dialog.dismiss();
                MDToast.makeText(CustomerEmptyActivity.this, "Connection Error: " + t.getMessage(),
                        MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        easyWayLocation.startLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        easyWayLocation.endUpdates();
    }

    @Override
    public void locationOn() {
    }

    @Override
    public void currentLocation(Location location) {
        latitude = String.valueOf(location.getLatitude());
        logitude = String.valueOf(location.getLongitude());
        getLocationDetail.getAddress(location.getLatitude(), location.getLongitude(), "xyz");
    }

    @Override
    public void locationCancelled() {
    }

    @Override
    public void locationData(LocationData locationData) {
        address = locationData.getFull_address();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
