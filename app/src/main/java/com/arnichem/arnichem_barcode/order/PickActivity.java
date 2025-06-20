package com.arnichem.arnichem_barcode.order;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.leave.LeaveResponse;
import com.arnichem.arnichem_barcode.util.DateHandler;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickActivity extends AppCompatActivity {
    private SearchableSpinner customerspinnerdelivery;
    private ArrayAdapter<String> customerdataAdapter;
    private TextView usernametxtvalue, fromDate;
    private EditText orderMsgEditText, remarksEditText;
    private Button submitOrderButton;
    private int poscustfixdel;
    ProgressDialog progressDialog;

    private String to_warehouse, cust_code;
    private final boolean checkInvoice = false; // Add your logic for this variable
    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Pick Holding");

        usernametxtvalue = findViewById(R.id.usernametxtvalue);
        customerspinnerdelivery = findViewById(R.id.cutomerdelivery);
        fromDate = findViewById(R.id.fromDate);
        orderMsgEditText = findViewById(R.id.orderMsgEditText);
        remarksEditText = findViewById(R.id.remarks_edt);
        submitOrderButton = findViewById(R.id.submitButton);

        // Set username
        usernametxtvalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());

        // Load spinner data
        loadSpinnerData();

        // Set up spinner item selection
        customerspinnerdelivery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                to_warehouse = customerdataAdapter.getItem(position);
                poscustfixdel = position;
                SharedPref.getInstance(getApplicationContext()).store_customersel(String.valueOf(poscustfixdel));

                DatabaseHandler databaseHandlercustomer = new DatabaseHandler(getApplicationContext());
                Cursor cursor = databaseHandlercustomer.readAllData();
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        String invoice = cursor.getString(3);

                        if (col.contentEquals(to_warehouse)) {
                            if (invoice.equalsIgnoreCase("Y") && !checkInvoice) {
                                showAlertDialogButtonClicked(view);
                            }
                            cust_code = col1;
                            //   checkdual(cust_code, view);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set up date picker for fromDate TextView
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

// Set the default date to current date on the TextView
        String defaultDate = day + "-" + (month + 1) + "-" + year; // Format as DD-MM-YYYY
        fromDate.setText(defaultDate);

        fromDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(PickActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear;
                        fromDate.setText(date);
                    }, year, month, day);

            // Restrict to future dates only
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
        // Set up submit order button
        submitOrderButton.setOnClickListener(v -> submitOrder());
    }
    private void loadSpinnerData() {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        customerdataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        customerdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        customerspinnerdelivery.setAdapter(customerdataAdapter);
        if (poscustfixdel != 0) {
            customerspinnerdelivery.setSelection(poscustfixdel);
        }
    }

    private void submitOrder() {
        String dbHost = SharedPref.mInstance.getDBHost();
        String dbUsername = SharedPref.mInstance.getDBUsername();
        String dbPassword = SharedPref.mInstance.getDBPassword();
        String dbName = SharedPref.mInstance.getDBName();

        String orderMsg = orderMsgEditText.getText().toString().trim();
        String remarks = remarksEditText.getText().toString().trim();
        String dateAdded = fromDate.getText().toString().trim();
        String user = SharedPref.mInstance.getID();

        if (cust_code == null||cust_code.equalsIgnoreCase("val")) {
            MDToast.makeText(this, "Please Select Customer Name !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            return;
        }

        if (orderMsg.isEmpty()) {
            MDToast.makeText(this, "Please enter a message !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            return;
        }


        progressDialog = new ProgressDialog(PickActivity.this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please wait....");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        String originalDate = dateAdded; // Input date in "dd/MM/yyyy" format
        String inputFormat = "dd-MM-yyyy";

        // Call the utility function
        String formattedDate = DateHandler.formatToYYYYMMDD(originalDate, inputFormat);

        Call<OrderResponse> call = apiInterface.submitPick(dbHost, dbUsername, dbPassword, dbName, cust_code, orderMsg, user, remarks, formattedDate);

        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(PickActivity.this, PickViewActivity.class);
                    intent.putExtra("srno", response.body().getSrno());
                    intent.putExtra("date_added", dateAdded);
                    intent.putExtra("code", cust_code);
                    intent.putExtra("name", to_warehouse);
                    intent.putExtra("message",orderMsgEditText.getText().toString() );
                    intent.putExtra("remarks", remarksEditText.getText().toString());
                    intent.putExtra("link", "https://arnisol.com/intranet/bpview.php?code="+cust_code);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    Toast.makeText(PickActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PickActivity.this, "Pick submission failed", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();

            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(PickActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAlertDialogButtonClicked(View view) {
        // Implement dialog display logic here
        Toast.makeText(this, "Invoice check alert", Toast.LENGTH_SHORT).show();
    }

}