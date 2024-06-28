package com.arnichem.arnichem_barcode.GetData;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.BusinessPartnersHandler;
import com.arnichem.arnichem_barcode.view.CustomerSearchHandler;
import com.arnichem.arnichem_barcode.view.CylinderSearch;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.DatabaseHandler;
import com.arnichem.arnichem_barcode.view.Delivery_type_liquid_Handler;
import com.arnichem.arnichem_barcode.view.DistributorHelper;
import com.arnichem.arnichem_barcode.view.DurasyncHelper;
import com.arnichem.arnichem_barcode.view.EmployeHandler;
import com.arnichem.arnichem_barcode.view.InventoryGases;
import com.arnichem.arnichem_barcode.view.LocationHandler;
import com.arnichem.arnichem_barcode.view.OtherItemsHandler;
import com.arnichem.arnichem_barcode.view.VehicleHandler;
import com.arnichem.arnichem_barcode.view.bp_contact_handler;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.arnichem.arnichem_barcode.view.loading;
import com.arnichem.arnichem_barcode.view.syncHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Test extends AppCompatActivity {
    APIInterface apiInterface;
    syncHelper sync;
    DatabaseHandler databaseHandlercustomer;
    BusinessPartnersHandler businessPartnersHandler;
    EmployeHandler employeHandler;

    OtherItemsHandler otherItemsHandler;

    DistributorHelper distributorHelper;
    com.arnichem.arnichem_barcode.view.bp_contact_handler bp_contact_handler;
    VehicleHandler vehicleHandler;
    Delivery_type_liquid_Handler delivery_type_liquidHandler;
    com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;
    DurasyncHelper durasyncHelper;
    InventoryGases inventoryGases;

    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        dialog = new ProgressDialog(Test.this);
        dialog.setTitle("Loading Data");
        dialog.setMessage("Please wait....");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        apiInterface = APIClient.getClient().create(APIInterface.class);
        sync = new syncHelper(Test.this);
        durasyncHelper = new DurasyncHelper(Test.this);
        databaseHandlercustomer = new DatabaseHandler(Test.this);
        distributorHelper = new DistributorHelper(Test.this);
        bp_contact_handler = new bp_contact_handler(Test.this);
        vehicleHandler = new VehicleHandler(Test.this);
        fromloccodehandler = new fromloccodehandler(Test.this);
        delivery_type_liquidHandler = new Delivery_type_liquid_Handler(Test.this);
        inventoryGases = new InventoryGases(Test.this);
        employeHandler = new EmployeHandler(Test.this);
        businessPartnersHandler = new BusinessPartnersHandler(Test.this);
        otherItemsHandler = new OtherItemsHandler(Test.this);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                sync.deleteAllData();
                durasyncHelper.deleteAllData();
                databaseHandlercustomer.deleteAllData();
                distributorHelper.deleteAllData();
                vehicleHandler.deleteAllData();
                fromloccodehandler.deleteAllData();
                delivery_type_liquidHandler.deleteAllData();
                bp_contact_handler.deleteAllData();
                inventoryGases.deleteAllData();
                employeHandler.deleteAllData();
                otherItemsHandler.deleteAllData();
                getData();
            }
        });
    }

    private void getData() {
        /**

         } GET List Resources
         **/
        Call<GetDataResponse> call = apiInterface.doCreateUserWithField(SharedPref.mInstance.getDBHost(), SharedPref.mInstance.getDBUsername(), SharedPref.mInstance.getDBPassword(), SharedPref.mInstance.getDBName());
        call.enqueue(new Callback<GetDataResponse>() {
            @Override
            public void onResponse(Call<GetDataResponse> call, Response<GetDataResponse> response) {
                if (response.body().getData() != null)
                    loadData(response.body().getData());
            }

            @Override
            public void onFailure(Call<GetDataResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void loadData(Data data) {
        CountDownLatch latch = new CountDownLatch(12);

        loadInventoryGases(data.getInventoryGas(), latch);
        loadBPContact(data.getBpContact(), latch);
        loadBusinessPartners(data.getBusinessPartners(), latch);
        loadInventoryCylinders(data.getInventoryCylinders(), latch);
        loadInventoryLiquid(data.getInventoryLiquid(), latch);
        loadLocationCode(data.getLocationCode(), latch);
        loadVehicleDetail(data.getVehicleDetails(), latch);
        loadDistributor(data.getDistributor(), latch);
        loadDuraCylinder(data.getDuraCylinder(), latch);
        loadAllBusinessPartner(data.getBusinessPartnerAllList(), latch);
        loadAllEmployee(data.getEmployeList(), latch);
        loadAllOtherData(data.getOtherData().getOtherItems(), latch);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                latch.await(); // Wait until all tasks are completed
                runOnUiThread(() -> navigateToDashboard());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void navigateToDashboard() {
        dialog.dismiss();
        Intent intent = new Intent(Test.this, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadAllOtherData(List<OtherItem> otherItemList, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (OtherItem item : otherItemList) {
                otherItemsHandler.addItems(item.getItem_code(), item.getShort_description());
            }
            latch.countDown();
        });
    }

    private void loadAllBusinessPartner(List<BusinessPartner> businessPartnerAllList, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (BusinessPartner partner : businessPartnerAllList) {
                businessPartnersHandler.add(partner.getName(), partner.getCode());
            }
            latch.countDown();
        });
    }

    private void loadAllEmployee(List<Employe> employeList, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (Employe employee : employeList) {
                employeHandler.addEmployee(employee.getName(), employee.getCode());
            }
            latch.countDown();
        });
    }

    private void loadDuraCylinder(List<DuraCylinder> duraCylinder, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (DuraCylinder cylinder : duraCylinder) {
                durasyncHelper.addBook(cylinder.getItemCode(), cylinder.getBarcode(), cylinder.getWeight(), cylinder.getVolume(), cylinder.getFilledWith());
            }
            latch.countDown();
        });
    }

    private void loadDistributor(List<Distributor> distributor, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (Distributor dist : distributor) {
                distributorHelper.addcust(dist.getName(), dist.getCode());
            }
            latch.countDown();
        });
    }

    private void loadVehicleDetail(List<VehicleDetail> vehicleDetails, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (VehicleDetail detail : vehicleDetails) {
                vehicleHandler.insertLabel(detail.getName());
            }
            latch.countDown();
        });
    }

    private void loadLocationCode(List<LocationCode> locationCode, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (LocationCode code : locationCode) {
                fromloccodehandler.addcust(code.getName(), code.getCode());
            }
            latch.countDown();
        });
    }

    private void loadInventoryLiquid(List<InventoryLiquid> inventoryLiquid, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (InventoryLiquid liquid : inventoryLiquid) {
                delivery_type_liquidHandler.insertLabel(liquid.getName(), liquid.getCode(), liquid.getUnit(), liquid.getConvFactor(), liquid.getHsn(), liquid.getGst());
            }
            latch.countDown();
        });
    }

    private void loadInventoryCylinders(List<InventoryCylinder> inventoryCylinders, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (InventoryCylinder cylinder : inventoryCylinders) {
                sync.addBook(cylinder.getItemCode(), cylinder.getBarcode(), cylinder.getWeight(), cylinder.getVolume(), cylinder.getFilledWith(), cylinder.getSerial_no(), cylinder.getHydrotest_date(), cylinder.getOwner(), cylinder.getStatus(), cylinder.getLocation());
            }
            latch.countDown();
        });
    }

    private void loadBusinessPartners(List<BusinessPartner> businessPartners, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (BusinessPartner partner : businessPartners) {
                databaseHandlercustomer.addcust(partner.getName(), partner.getCode(), partner.getInvoice());
            }
            latch.countDown();
        });
    }

    private void loadBPContact(List<BpContact> bpContact, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (BpContact contact : bpContact) {
                bp_contact_handler.addcust(contact.getName(), contact.getCode());
            }
            latch.countDown();
        });
    }

    private void loadInventoryGases(List<InventoryGa> inventoryGa, CountDownLatch latch) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (InventoryGa gas : inventoryGa) {
                inventoryGases.addGas(gas.getGasName(), gas.getItem_code());
            }
            latch.countDown();
        });

    }
}
