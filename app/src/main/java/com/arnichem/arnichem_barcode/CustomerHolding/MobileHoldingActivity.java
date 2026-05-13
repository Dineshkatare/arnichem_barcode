package com.arnichem.arnichem_barcode.CustomerHolding;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MobileHoldingActivity extends AppCompatActivity {

    private String customerCode;
    private String dcNo; // Passed from Print Screen (deliveryprint.java)
    private int nextEntryNo;
    private List<HoldingCylinder> holdingList = new ArrayList<>();
    private List<HoldingCylinder> masterHoldingList = new ArrayList<>();
    private HoldingCylinderAdapter adapter;
    
    private TextView customerNameTxt, totalCylCount;
    private TextView sortDaysBtn, sortCylBtn, filterGasBtn, filterScanBtn;
    private Button saveBtn;
    private RecyclerView recyclerView;
    private ProgressDialog dialog;

    private boolean sortByDays = true;
    private String selectedGasFilter = "All";
    private String selectedScanFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_holding);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        customerCode = getIntent().getStringExtra("code");
        dcNo = getIntent().getStringExtra("dc_no"); // Passed from deliveryprint or MainHoldingScreen

        if (customerCode == null) {
            Toast.makeText(this, "Customer code missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        fetchData();
    }

    private void initViews() {
        customerNameTxt = findViewById(R.id.customer_name_txt);
        totalCylCount = findViewById(R.id.total_cyl_count);
        sortDaysBtn = findViewById(R.id.sort_days_btn);
        sortCylBtn = findViewById(R.id.sort_cyl_btn);
        filterGasBtn = findViewById(R.id.filter_gas_btn);
        filterScanBtn = findViewById(R.id.filter_scan_btn);
        recyclerView = findViewById(R.id.holding_recycler_view);
        saveBtn = findViewById(R.id.save_report_btn);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HoldingCylinderAdapter(this, holdingList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());

        sortDaysBtn.setOnClickListener(v -> {
            sortByDays = true;
            updateSortUI();
            sortAndRefresh();
        });

        sortCylBtn.setOnClickListener(v -> {
            sortByDays = false;
            updateSortUI();
            sortAndRefresh();
        });

        saveBtn.setOnClickListener(v -> saveReport());

        filterGasBtn.setOnClickListener(v -> showGasFilterDialog());
        filterScanBtn.setOnClickListener(v -> showScanFilterDialog());
    }

    private void showGasFilterDialog() {
        List<String> gases = new ArrayList<>();
        gases.add("All");
        for (HoldingCylinder cyl : masterHoldingList) {
            if (cyl.getFilledWith() != null && !gases.contains(cyl.getFilledWith())) {
                gases.add(cyl.getFilledWith());
            }
        }

        String[] gasArray = gases.toArray(new String[0]);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Filter by Gas")
                .setItems(gasArray, (dialog, which) -> {
                    selectedGasFilter = gasArray[which];
                    filterGasBtn.setText("Gas: " + selectedGasFilter);
                    applyFilters();
                })
                .show();
    }

    private void showScanFilterDialog() {
        String[] options = {"All", "Scanned (B/C)", "Not Scanned (A)"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Filter by Scan Status")
                .setItems(options, (dialog, which) -> {
                    selectedScanFilter = options[which];
                    filterScanBtn.setText("Scan: " + selectedScanFilter);
                    applyFilters();
                })
                .show();
    }

    private void applyFilters() {
        List<HoldingCylinder> filteredList = new ArrayList<>();
        for (HoldingCylinder cyl : masterHoldingList) {
            boolean gasMatch = selectedGasFilter.equals("All") || selectedGasFilter.equals(cyl.getFilledWith());
            boolean scanMatch = true;
            if (selectedScanFilter.equals("Scanned (B/C)")) {
                scanMatch = "B".equals(cyl.getIsScanned()) || "C".equals(cyl.getIsScanned());
            } else if (selectedScanFilter.equals("Not Scanned (A)")) {
                scanMatch = "A".equals(cyl.getIsScanned());
            }

            if (gasMatch && scanMatch) {
                filteredList.add(cyl);
            }
        }
        holdingList.clear();
        holdingList.addAll(filteredList);
        sortAndRefresh();
        totalCylCount.setText(String.valueOf(holdingList.size()));
    }

    private void updateSortUI() {
        if (sortByDays) {
            sortDaysBtn.setBackgroundResource(R.drawable.toggle_selected);
            sortDaysBtn.setTextColor(getResources().getColor(android.R.color.white));
            sortCylBtn.setBackground(null);
            sortCylBtn.setTextColor(getResources().getColor(R.color.black));
        } else {
            sortCylBtn.setBackgroundResource(R.drawable.toggle_selected);
            sortCylBtn.setTextColor(getResources().getColor(android.R.color.white));
            sortDaysBtn.setBackground(null);
            sortDaysBtn.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void sortAndRefresh() {
        if (sortByDays) {
            Collections.sort(holdingList, (a, b) -> Integer.compare(b.getPendingDays(), a.getPendingDays()));
        } else {
            Collections.sort(holdingList, (a, b) -> a.getItemCode().compareTo(b.getItemCode()));
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchData() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Fetching holding data...");
        dialog.setCancelable(false);
        dialog.show();

        APIInterface api = APIClient.getClient().create(APIInterface.class);
        api.getCustomerHolding(
                customerCode,
                SharedPref.mInstance.getDBHost(),
                SharedPref.mInstance.getDBUsername(),
                SharedPref.mInstance.getDBPassword(),
                SharedPref.mInstance.getDBName()
        ).enqueue(new Callback<HoldingResponse>() {
            @Override
            public void onResponse(Call<HoldingResponse> call, Response<HoldingResponse> response) {
                dialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    HoldingResponse data = response.body();
                    if ("success".equals(data.getStatus())) {
                        customerNameTxt.setText(data.getCustomerName());
                        nextEntryNo = data.getNextEntryNo();
                        totalCylCount.setText(String.valueOf(data.getCylinders().size()));
                        
                        masterHoldingList.clear();
                        masterHoldingList.addAll(data.getCylinders());
                        
                        holdingList.clear();
                        holdingList.addAll(data.getCylinders());
                        sortAndRefresh();
                    } else {
                        Toast.makeText(MobileHoldingActivity.this, data.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<HoldingResponse> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(MobileHoldingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveReport() {
        // Use the passed DC number (from Print screen or Search screen)
        String finalDelNo = (dcNo != null && !dcNo.isEmpty()) ? dcNo : "N/A";
        
        String remarks = "Cylinder Holding Checked on Print Screen for " + finalDelNo;

        Map<String, Object> payload = new HashMap<>();
        payload.put("cust_code", customerCode);
        payload.put("email", SharedPref.getInstance(this).getEmail());
        payload.put("remarks", remarks);

        String jsonData = new Gson().toJson(payload);

        dialog.setMessage("Saving report...");
        dialog.show();

        APIInterface api = APIClient.getClient().create(APIInterface.class);
        api.saveHoldingReport(
                jsonData,
                SharedPref.mInstance.getDBHost(),
                SharedPref.mInstance.getDBUsername(),
                SharedPref.mInstance.getDBPassword(),
                SharedPref.mInstance.getDBName()
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(MobileHoldingActivity.this, "Report saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(MobileHoldingActivity.this, "Failed to save report", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(MobileHoldingActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
