package com.arnichem.arnichem_barcode.PaymentReceipt;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseClaimActivity extends AppCompatActivity {
    private TextView usernamevalue, date;
    private EditText amountValue, descriptionEt, remarksEt;
    private Spinner categorySpinner;
    private Button submit;

    private ArrayAdapter<CharSequence> adapter;
    private SharedPreferences pref;
    private ProgressDialog dialog;
    private String category;
    private int categoryPos;
    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_claim);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Expense Claim");
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        // Initialize views
        usernamevalue = findViewById(R.id.usernametxtvalue);
        date = findViewById(R.id.date);
        amountValue = findViewById(R.id.amountval);
        categorySpinner = findViewById(R.id.categorySpinner);
        submit = findViewById(R.id.PaymentSubmitBtn);
        descriptionEt = findViewById(R.id.descriptionEt);
        remarksEt = findViewById(R.id.remarksEt);

        // Set username and date
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        date.setText(sdf.format(new Date()));
        date.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ExpenseClaimActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dateStr = String.format(Locale.getDefault(), "%02d/%02d/%d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        date.setText(dateStr);
                    },
                    year, month, day);
            // Disable future dates
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        // Setup category spinner
        adapter = ArrayAdapter.createFromResource(this, R.array.expense_categories,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = (String) parent.getItemAtPosition(position);
                categoryPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup submit button
        submit.setOnClickListener(v -> {
            submit.setEnabled(false);
            postUsingRetrofit();
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void postUsingRetrofit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Data Inserting");
        builder.setMessage("Please wait...");
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Input validation
        if (categoryPos == 0) {
            dialog.dismiss();
            submit.setEnabled(true);
            MDToast.makeText(this, "Please Select Category!", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            return;
        }
        String amountStr = amountValue.getText().toString();
        if (amountStr.isEmpty() || !isValidAmount(amountStr)) {
            dialog.dismiss();
            submit.setEnabled(true);
            MDToast.makeText(this, "Please Enter Valid Amount (Amount cannot be 0)", MDToast.LENGTH_SHORT,
                    MDToast.TYPE_ERROR).show();
            return;
        }
        if (descriptionEt.getText().toString().isEmpty()) {
            dialog.dismiss();
            submit.setEnabled(true);
            MDToast.makeText(this, "Please Enter Description!", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            return;
        }

        // Get values from views and SharedPref
        String dbHost = SharedPref.getInstance(this).getDBHost();
        String dbUsername = SharedPref.getInstance(this).getDBUsername();
        String dbPassword = SharedPref.getInstance(this).getDBPassword();
        String dbName = SharedPref.getInstance(this).getDBName();
        String expenseDate = date.getText().toString();
        String empId = SharedPref.getInstance(this).getEmail(); // API expects user ID in 'email' field
        String description = descriptionEt.getText().toString();

        // Make API call
        Call<ResponseBody> call = apiInterface.uploadExpenseClaim(
                dbHost, dbUsername, dbPassword, dbName, expenseDate, category, amountStr, description, empId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dialog.dismiss();
                submit.setEnabled(true);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseString = response.body().string();
                        Log.d("Retrofit", "Response: " + responseString);
                        JSONObject obj = new JSONObject(responseString);
                        String status = obj.optString("status");
                        String msg = obj.optString("msg");

                        if ("success".equalsIgnoreCase(status)) {
                            MDToast.makeText(ExpenseClaimActivity.this, "Expense Claim Submitted Successfully!",
                                    MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                            finish();
                        } else {
                            MDToast.makeText(ExpenseClaimActivity.this, msg.isEmpty() ? "Unknown error" : msg,
                                    MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                        }
                    } else {
                        String errorBody = response.errorBody() != null ? response.errorBody().string()
                                : "No error body";
                        MDToast.makeText(ExpenseClaimActivity.this,
                                "Server error: " + response.code() + " - " + errorBody, MDToast.LENGTH_SHORT,
                                MDToast.TYPE_ERROR).show();
                    }
                } catch (Exception e) {
                    MDToast.makeText(ExpenseClaimActivity.this, "Error: " + e.getMessage(), MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dialog.dismiss();
                submit.setEnabled(true);
                String message = "Network Error: Please check your internet connection";
                if (t instanceof java.net.SocketTimeoutException) {
                    message = "Request timed out";
                } else if (t instanceof java.io.IOException) {
                    message = "Unable to connect to the server";
                }
                MDToast.makeText(ExpenseClaimActivity.this, message, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                t.printStackTrace();
            }
        });
    }

    private boolean isValidAmount(String amount) {
        try {
            float value = Float.parseFloat(amount);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
