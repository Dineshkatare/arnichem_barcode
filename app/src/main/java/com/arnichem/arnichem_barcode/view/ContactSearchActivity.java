package com.arnichem.arnichem_barcode.view;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.data.response.ContactSearchResponse;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactSearchActivity extends AppCompatActivity {

    private TextInputEditText etCustName, etContactName, etPhone, etEmail;
    private Button btnSearch;
    private TextView tvResultCount, tvNoResults;
    private RecyclerView rvResults;
    private ContactSearchAdapter adapter;
    private List<ContactSearchResponse.ContactData> contactList = new ArrayList<>();
    private APIInterface apiInterface;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        apiInterface = APIClient.getClient().create(APIInterface.class);

        btnSearch.setOnClickListener(v -> performSearch());
    }

    private void initViews() {
        etCustName = findViewById(R.id.etCustName);
        etContactName = findViewById(R.id.etContactName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        btnSearch = findViewById(R.id.btnSearch);
        tvResultCount = findViewById(R.id.tvResultCount);
        tvNoResults = findViewById(R.id.tvNoResults);
        rvResults = findViewById(R.id.rvResults);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactSearchAdapter(contactList, this);
        rvResults.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Searching...");
        progressDialog.setCancelable(false);
    }

    private void performSearch() {
        String custName = etCustName.getText().toString().trim();
        String contactName = etContactName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (custName.isEmpty() && contactName.isEmpty() && phone.isEmpty() && email.isEmpty()) {
            Toast.makeText(this, "Please enter at least one search criteria", Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();
        progressDialog.show();

        String dbHost = SharedPref.mInstance.getDBHost();
        String dbUsername = SharedPref.mInstance.getDBUsername();
        String dbPassword = SharedPref.mInstance.getDBPassword();
        String dbName = SharedPref.mInstance.getDBName();

        Call<ContactSearchResponse> call = apiInterface.searchContacts(
                dbHost, dbUsername, dbPassword, dbName,
                custName, contactName, phone, email);

        call.enqueue(new Callback<ContactSearchResponse>() {
            @Override
            public void onResponse(Call<ContactSearchResponse> call, Response<ContactSearchResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    ContactSearchResponse searchResponse = response.body();
                    if ("success".equals(searchResponse.getStatus())) {
                        contactList.clear();
                        if (searchResponse.getData() != null) {
                            contactList.addAll(searchResponse.getData());
                        }
                        updateUI();
                    } else {
                        Toast.makeText(ContactSearchActivity.this, searchResponse.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    Toast.makeText(ContactSearchActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ContactSearchResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ContactSearchActivity.this, "Connection Failed: " + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void updateUI() {
        if (contactList.isEmpty()) {
            tvResultCount.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
            rvResults.setVisibility(View.GONE);
        } else {
            tvResultCount.setVisibility(View.VISIBLE);
            tvResultCount.setText("Found " + contactList.size() + " matches");
            tvNoResults.setVisibility(View.GONE);
            rvResults.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
