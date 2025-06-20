package com.arnichem.arnichem_barcode.Settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.arnichem.arnichem_barcode.GetData.InventoryCylinder;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.data.response.InventoryResponse;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.syncHelper;

import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncInventoryActivity  extends AppCompatActivity {
    APIInterface apiInterface;
    syncHelper sync;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_inventory);

        dialog = new ProgressDialog(SyncInventoryActivity.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        apiInterface = APIClient.getClient().create(APIInterface.class);
        sync = new syncHelper(SyncInventoryActivity.this);

        Executors.newSingleThreadExecutor().execute(() -> {
            sync.deleteAllData(); // Clear only cylinder-related data
            getData();
        });
    }

    private void getData() {
        Call<InventoryResponse> call = apiInterface.syncBarocde(

                SharedPref.mInstance.getDBHost(),
                SharedPref.mInstance.getDBUsername(),
                SharedPref.mInstance.getDBPassword(),
                SharedPref.mInstance.getDBName()
        );

        call.enqueue(new Callback<InventoryResponse>() {
            @Override
            public void onResponse(Call<InventoryResponse> call, Response<InventoryResponse> response) {
                if (response.body() != null && response.body().getInventoryCylinders() != null) {
                    List<InventoryCylinder> inventoryCylinders = response.body().getInventoryCylinders();
                    loadInventoryCylinders(inventoryCylinders);
                } else {
                    showErrorAndExit("No data found.");
                }

            }

            @Override
            public void onFailure(Call<InventoryResponse> call, Throwable t) {
                showErrorAndExit("API call failed: " + t.getMessage());
            }
        });
    }

    private void loadInventoryCylinders(List<InventoryCylinder> inventoryCylinders) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (InventoryCylinder cylinder : inventoryCylinders) {
                sync.addBook(
                        cylinder.getItemCode(),
                        cylinder.getBarcode(),
                        cylinder.getWeight(),
                        cylinder.getVolume(),
                        cylinder.getFilledWith(),
                        cylinder.getSerial_no(),
                        cylinder.getHydrotest_date(),
                        cylinder.getOwner(),
                        cylinder.getStatus(),
                        cylinder.getWater_capacity(),
                        cylinder.getMfg(),
                        cylinder.getLocation()
                );
            }

            runOnUiThread(this::navigateToDashboard);
        });
    }

    private void navigateToDashboard() {
        dialog.dismiss();
        Intent intent = new Intent(SyncInventoryActivity.this, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showErrorAndExit(String message) {
        runOnUiThread(() -> {
            dialog.dismiss();
            Log.e("TestActivity", message);
            // Show toast or alert dialog here if needed
        });
    }

}
