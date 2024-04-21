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
        durasyncHelper=new DurasyncHelper(Test.this);
        databaseHandlercustomer=new DatabaseHandler(Test.this);
        distributorHelper=new DistributorHelper(Test.this);
        bp_contact_handler= new bp_contact_handler(Test.this);
        vehicleHandler=new VehicleHandler(Test.this);
        fromloccodehandler=new fromloccodehandler(Test.this);
        delivery_type_liquidHandler=new Delivery_type_liquid_Handler(Test.this);
        inventoryGases =new InventoryGases(Test.this);
        employeHandler =new EmployeHandler(Test.this);
        businessPartnersHandler = new BusinessPartnersHandler(Test.this);
        otherItemsHandler = new OtherItemsHandler(Test.this);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                sync.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {

                durasyncHelper.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                databaseHandlercustomer.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                distributorHelper.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                vehicleHandler.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                fromloccodehandler.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                delivery_type_liquidHandler.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                bp_contact_handler.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                inventoryGases.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                employeHandler.deleteAllData();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                otherItemsHandler.deleteAllData();
            }
        });



        getData();
    }

    private void getData() {
        /**

    } GET List Resources
         **/
    Call<GetDataResponse> call = apiInterface.doCreateUserWithField(SharedPref.mInstance.getDBHost(),SharedPref.mInstance.getDBUsername(),SharedPref.mInstance.getDBPassword(),SharedPref.mInstance.getDBName());
        call.enqueue(new Callback<GetDataResponse>() {
        @Override
        public void onResponse(Call<GetDataResponse> call, Response<GetDataResponse> response) {
           if(response.body().getData()!=null)
                loadData(response.body().getData());
        }

        @Override
        public void onFailure(Call<GetDataResponse> call, Throwable t) {
            call.cancel();
        }
    });
    }

    private void loadData(Data data) {
        if(data.getInventoryGas().size()!=0&&data.getInventoryGas()!=null)
            loadInventoryGases(data.getInventoryGas());

        if(data.getBpContact().size()!=0&&data.getBpContact()!=null)
            loadBPContact(data.getBpContact());


        if(data.getBusinessPartners().size()!=0&&data.getBusinessPartners()!=null)
            loadBusinessPartners(data.getBusinessPartners());

        if(data.getInventoryCylinders().size()!=0&&data.getInventoryCylinders()!=null)
            loadInventoryCylinders(data.getInventoryCylinders());
        

        if(data.getInventoryLiquid().size()!=0&&data.getInventoryLiquid()!=null)
            loadInventoryLiquid(data.getInventoryLiquid());

        if(data.getLocationCode().size()!=0&&data.getLocationCode()!=null)
            loadLocationCode(data.getLocationCode());

        if(data.getVehicleDetails().size()!=0&&data.getVehicleDetails()!=null)
            loadVehicleDetail(data.getVehicleDetails());

        if(data.getDistributor().size()!=0&&data.getDistributor()!=null)
            loadDistributor(data.getDistributor());

        if(data.getDuraCylinder().size()!=0&&data.getDuraCylinder()!=null)
            loadDuraCylinder(data.getDuraCylinder());

        if(data.getBusinessPartnerAllList().size()!=0&&data.getBusinessPartnerAllList()!=null)
            loadAllBusinessPartener(data.getBusinessPartnerAllList());

        if(data.getEmployeList().size()!=0&&data.getEmployeList()!=null)
            loadAllEmployee(data.getEmployeList());

        if(data.getOtherData().getOtherItems().size()!=0&&data.getOtherData().getOtherItems()!=null)
            loadAllOtherData(data.getOtherData().getOtherItems());


        loin();

    }

    private void loadAllOtherData(List<OtherItem> otherItemList) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for(int i=0;i<otherItemList.size();i++)
                {
                    otherItemsHandler.addItems(otherItemList.get(i).getItem_code(),otherItemList.get(i).getShort_description());
                }
            }
        });
    }

    private void loadAllBusinessPartener(List<BusinessPartner> businessPartnerAllList) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for(int i=1;i<businessPartnerAllList.size();i++)
                {
                    businessPartnersHandler.add(businessPartnerAllList.get(i).getName(),businessPartnerAllList.get(i).getCode());
                }
            }
        });
    }

    private void loadAllEmployee(List<Employe> employeList) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for(int i=0;i<employeList.size();i++)
                {
                    employeHandler.addEmployee(employeList.get(i).getName(),employeList.get(i).getCode());
                }
            }
        });
    }


    private void loadDuraCylinder(List<DuraCylinder> duraCylinder) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for(int i=1;i<duraCylinder.size();i++)
                {
                    durasyncHelper.addBook(duraCylinder.get(i).getItemCode(),duraCylinder.get(i).getBarcode(),duraCylinder.get(i).getWeight(),duraCylinder.get(i).getVolume(),duraCylinder.get(i).getFilledWith());
                }
            }
        });
    }


    private void loadDistributor(List<Distributor> distributor) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {

                for(int i=1;i<distributor.size();i++)
                {
                    distributorHelper.addcust(distributor.get(i).getName(),distributor.get(i).getCode());
                }
            }
        });
    }

    private void loadVehicleDetail(List<VehicleDetail> vehicleDetails) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for(int i=1;i<vehicleDetails.size();i++)
                {
                    vehicleHandler.insertLabel(vehicleDetails.get(i).getName());
                }
            }
        });
    }

    private void loadLocationCode(List<LocationCode> locationCode) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for(int i=1;i<locationCode.size();i++)
                {
                    fromloccodehandler.addcust(locationCode.get(i).getName(),locationCode.get(i).getCode());
                }
            }
        });
    }

    private void loadInventoryLiquid(List<InventoryLiquid> inventoryLiquid) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for(int i=1;i<inventoryLiquid.size();i++)
                {
                    delivery_type_liquidHandler.insertLabel(inventoryLiquid.get(i).getName(),inventoryLiquid.get(i).getCode(),inventoryLiquid.get(i).getUnit(),inventoryLiquid.get(i).getConvFactor(),inventoryLiquid.get(i).getHsn(),inventoryLiquid.get(i).getGst());
                }
            }
        });

    }

    private void loadInventoryCylinders(List<InventoryCylinder> inventoryCylinders) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for(int i=1;i<inventoryCylinders.size();i++)
                {
                    sync.addBook(inventoryCylinders.get(i).getItemCode(),
                            inventoryCylinders.get(i).getBarcode(),
                            inventoryCylinders.get(i).getWeight(),
                            inventoryCylinders.get(i).getVolume(),
                            inventoryCylinders.get(i).getFilledWith(),
                            inventoryCylinders.get(i).getSerial_no(),
                            inventoryCylinders.get(i).getHydrotest_date(),
                            inventoryCylinders.get(i).getOwner(),
                            inventoryCylinders.get(i).getStatus(),
                            inventoryCylinders.get(i).getLocation());

                }
            }
        });
    }

    private void loadBusinessPartners(List<BusinessPartner> businessPartners) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for(int i=1;i<businessPartners.size();i++)
                {
                    databaseHandlercustomer.addcust(businessPartners.get(i).getName(),businessPartners.get(i).getCode(),businessPartners.get(i).getInvoice());
                }
            }
        });
    }

    private void loadBPContact(List<BpContact> bpContact) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for(int i=1;i<bpContact.size();i++)
                {
                    bp_contact_handler.addcust(bpContact.get(i).getName(),bpContact.get(i).getCode());
                }
            }
        });
    }


    private void loadInventoryGases(List<InventoryGa> inventoryGa) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                for(int i=1;i<inventoryGa.size();i++)
                {
                    inventoryGases.addGas(inventoryGa.get(i).getGasName(),inventoryGa.get(i).getItem_code());
                }
            }
        });
    }
    private void loin() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                Intent intent=new Intent(Test.this, Dashboard.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();            }
        }, 5000);

    }
}