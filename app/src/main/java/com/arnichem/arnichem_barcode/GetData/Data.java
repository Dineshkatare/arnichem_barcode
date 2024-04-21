
package com.arnichem.arnichem_barcode.GetData;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Data {

    @SerializedName("inventory_cylinders")
    @Expose
    private List<InventoryCylinder> inventoryCylinders = null;
    @SerializedName("inventory_gas")
    @Expose
    private List<InventoryGa> inventoryGas = null;
    @SerializedName("bp_contact")
    @Expose
    private List<BpContact> bpContact = null;
    @SerializedName("business_partners")
    @Expose
    private List<BusinessPartner> businessPartners = null;
    @SerializedName("vehicle_details")
    @Expose
    private List<VehicleDetail> vehicleDetails = null;
    @SerializedName("inventory_liquid")
    @Expose
    private List<InventoryLiquid> inventoryLiquid = null;
    @SerializedName("location_code")
    @Expose
    private List<LocationCode> locationCode = null;
    @SerializedName("distributor")
    @Expose
    private List<Distributor> distributor = null;
    @SerializedName("dura_cylinder")
    @Expose
    private List<DuraCylinder> duraCylinder = null;
    @SerializedName("business_partners_all")
    @Expose
    private List<BusinessPartner> businessPartnerAllList = null;

    @SerializedName("employe_all")
    @Expose
    private List<Employe> employeList = null;

    @SerializedName("other_data")
    private OtherData otherData;

    public OtherData getOtherData() {
        return otherData;
    }

    public void setOtherData(OtherData otherData) {
        this.otherData = otherData;
    }

    public List<BusinessPartner> getBusinessPartnerAllList() {
        return businessPartnerAllList;
    }

    public void setBusinessPartnerAllList(List<BusinessPartner> businessPartnerAllList) {
        this.businessPartnerAllList = businessPartnerAllList;
    }

    public List<LocationCode> getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(List<LocationCode> locationCode) {
        this.locationCode = locationCode;
    }

    public List<Distributor> getDistributor() {
        return distributor;
    }

    public void setDistributor(List<Distributor> distributor) {
        this.distributor = distributor;
    }

    public List<DuraCylinder> getDuraCylinder() {
        return duraCylinder;
    }

    public void setDuraCylinder(List<DuraCylinder> duraCylinder) {
        this.duraCylinder = duraCylinder;
    }

    public List<InventoryCylinder> getInventoryCylinders() {
        return inventoryCylinders;
    }


    public void setInventoryCylinders(List<InventoryCylinder> inventoryCylinders) {
        this.inventoryCylinders = inventoryCylinders;
    }

    public List<InventoryGa> getInventoryGas() {
        return inventoryGas;
    }

    public void setInventoryGas(List<InventoryGa> inventoryGas) {
        this.inventoryGas = inventoryGas;
    }

    public List<BpContact> getBpContact() {
        return bpContact;
    }

    public void setBpContact(List<BpContact> bpContact) {
        this.bpContact = bpContact;
    }

    public List<BusinessPartner> getBusinessPartners() {
        return businessPartners;
    }

    public void setBusinessPartners(List<BusinessPartner> businessPartners) {
        this.businessPartners = businessPartners;
    }


    public List<VehicleDetail> getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(List<VehicleDetail> vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    public List<InventoryLiquid> getInventoryLiquid() {
        return inventoryLiquid;
    }

    public void setInventoryLiquid(List<InventoryLiquid> inventoryLiquid) {
        this.inventoryLiquid = inventoryLiquid;
    }

    public List<Employe> getEmployeList() {
        return employeList;
    }

    public void setEmployeList(List<Employe> employeList) {
        this.employeList = employeList;
    }


}
