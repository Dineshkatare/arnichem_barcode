package com.arnichem.arnichem_barcode.leave;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.EmployeHandler;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaveApplicationActivity extends AppCompatActivity {
    ProgressDialog dialog;

    private SearchableSpinner  typeSpinner;
    private TextView fromDate, toDate, returningDate, numberOfDays,employeeNameTxt;
    private EditText reasonEditText;

    private Button applyButton;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    APIInterface apiInterface;
    private ArrayAdapter<String> empAdapter;
    private EmployeHandler employeeHandler;
    private SearchableSpinner empSpinner;
    private String empName="";
    private String empId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_apply);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        employeeHandler = new EmployeHandler(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Initialize Views
        empSpinner = findViewById(R.id.emp_spinner);
        typeSpinner = findViewById(R.id.typeSpinner);
        fromDate = findViewById(R.id.fromDate);
        toDate = findViewById(R.id.toDate);
        returningDate = findViewById(R.id.returningDate);
        numberOfDays = findViewById(R.id.numberOfDaysTxt);
        reasonEditText = findViewById(R.id.reasonEditText);
        applyButton = findViewById(R.id.applyButton);
       // employeeNameTxt.setText(SharedPref.mInstance.FirstName()+" "+SharedPref.mInstance.LastName());
        // Set up DatePicker Dialogs
        setDatePickers();

        List<String> leaveTypes = Arrays.asList("Select Leave Type", "Leave", "Camp Off", "LOP");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, leaveTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        // Handle Apply Button
        applyButton.setOnClickListener(v -> submitLeaveApplication());
        empSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                empName = empAdapter.getItem(position);

                Cursor cursor = employeeHandler.readAllData();
                if (cursor.getCount() == 0) {
                    // Handle empty case
                } else {
                    if (empName.equals("Select Employee")) {
                        empId = "0";
                    } else {
                        while (cursor.moveToNext()) {
                            String col = cursor.getString(1);
                            String col1 = cursor.getString(0);
                            if (col1.equals(empName)) {
                                empId = col;
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        loadSpinnerData();

    }
    private void loadSpinnerData() {
        EmployeHandler db = new EmployeHandler(getApplicationContext());
        List<String> labels = db.getAllLabels();

        // Get the value to match
        String fullNameToMatch = SharedPref.mInstance.FirstName()+" "+SharedPref.mInstance.LastName();

        // Find the index of the matching value in the labels list
        int index = labels.indexOf(fullNameToMatch);

        // Creating adapter for spinner
        empAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        empAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        empSpinner.setAdapter(empAdapter);

        // Set the selection of the spinner to the found index
        if (index != -1) {
            empSpinner.setSelection(index);
        }
    }


    private void setDatePickers() {
        fromDate.setOnClickListener(v -> openDatePicker(fromDate));
        toDate.setOnClickListener(v -> openDatePicker(toDate));
        returningDate.setOnClickListener(v -> openDatePicker(returningDate));
    }

    private void openDatePicker(TextView dateView) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String dateString = dateFormat.format(calendar.getTime());
            dateView.setText(dateString);

            // Auto-calculate number of days
            if (!fromDate.getText().toString().isEmpty() && !toDate.getText().toString().isEmpty()) {
                calculateNumberOfDays();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();

    }

    private void calculateNumberOfDays() {
        try {
            String fromDateText = fromDate.getText().toString();
            String toDateText = toDate.getText().toString();

            // Check if the dates are valid and not the placeholder text
            if (fromDateText.equals("Select Date") || toDateText.equals("Select Date")) {
                Toast.makeText(this, "Please select valid dates", Toast.LENGTH_SHORT).show();
                return;
            }

            Date from = dateFormat.parse(fromDateText);
            Date to = dateFormat.parse(toDateText);
            long differenceInMillis = to.getTime() - from.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(differenceInMillis) + 1; // Add 1 to include both days
            numberOfDays.setText(String.valueOf(days));
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
        }
    }



    private void submitLeaveApplication() {
        String leaveType = typeSpinner.getSelectedItem().toString();
        String fromDateValue = fromDate.getText().toString();
        String toDateValue = toDate.getText().toString();
        String returningOn = returningDate.getText().toString();
        String reason = reasonEditText.getText().toString();

        // Validation checks
        if (empName.isEmpty()) { // Assuming first item is a hint like "Select Employee"
            Toast.makeText(this, "Please select an employee", Toast.LENGTH_SHORT).show();
            return;
        }

        if (leaveType.equals("Select Leave Type")) { // Assuming first item is a hint like "Select Leave Type"
            Toast.makeText(this, "Please select a leave type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fromDateValue.isEmpty()) {
            Toast.makeText(this, "Please select a from date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (toDateValue.isEmpty()) {
            Toast.makeText(this, "Please select a to date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (returningOn.isEmpty()) {
            Toast.makeText(this, "Please select a returning date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reason.isEmpty()) {
            Toast.makeText(this, "Please provide a reason for the leave", Toast.LENGTH_SHORT).show();
            return;
        }

        // Date validation logic (optional) - Check if fromDate is before toDate
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date fromDateParsed = sdf.parse(fromDateValue);
            Date toDateParsed = sdf.parse(toDateValue);
            if (fromDateParsed != null && toDateParsed != null && fromDateParsed.after(toDateParsed)) {
                Toast.makeText(this, "From date cannot be after To date", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        // All validations passed, proceed with API call
        // Dummy values for DB connection, replace with actual ones
        String dbHost = SharedPref.mInstance.getDBHost();
        String dbUsername = SharedPref.mInstance.getDBUsername();
        String dbPassword = SharedPref.mInstance.getDBPassword();
        String dbName = SharedPref.mInstance.getDBName();

        dialog = new ProgressDialog(LeaveApplicationActivity.this);
        dialog.setTitle("Data Inserting");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();


        // Make API call
        Call<LeaveResponse> call = apiInterface.submitLeaveApplication(
                dbHost, dbUsername, dbPassword, dbName,
                empId , fromDateValue, toDateValue, leaveType, reason, returningOn
        );

        // Execute the call
        call.enqueue(new Callback<LeaveResponse>() {
            @Override
            public void onResponse(Call<LeaveResponse> call, Response<LeaveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LeaveResponse leaveResponse = response.body();
                    if (leaveResponse.getStatus().equals("success")) {
                        MDToast.makeText(LeaveApplicationActivity.this, leaveResponse.getMessage(), MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                        finish();
                    } else {
                        Toast.makeText(LeaveApplicationActivity.this, leaveResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();

                } else {
                    dialog.dismiss();

                    Toast.makeText(LeaveApplicationActivity.this, "Failed to submit leave", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LeaveResponse> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(LeaveApplicationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
