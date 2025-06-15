
package com.arnichem.arnichem_barcode.GetData;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class InventoryCylinder {

    @SerializedName("item_code")
    @Expose
    private String itemCode;
    @SerializedName("barcode")
    @Expose
    private String barcode;
    @SerializedName("weight")
    @Expose
    private String weight;
    @SerializedName("volume")
    @Expose
    private String volume;
    @SerializedName("filled_with")
    @Expose
    private String filledWith;
    @SerializedName("serial_no")
    @Expose
    private String serial_no;

    @SerializedName("owner")
    @Expose
    private String owner;

    @SerializedName("hydrotest_date")
    @Expose
    private String hydrotest_date;

    @SerializedName("water_capacity")
    @Expose
    private String water_capacity;

    public String getWater_capacity() {
        return water_capacity;
    }

    public void setWater_capacity(String water_capacity) {
        this.water_capacity = water_capacity;
    }

    public String getMfg() {
        return mfg;
    }

    public void setMfg(String mfg) {
        this.mfg = mfg;
    }

    @SerializedName("mfg")
    @Expose
    private String mfg;


    public String getSerial_no() {
        return serial_no;
    }

    public void setSerial_no(String serial_no) {
        this.serial_no = serial_no;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getHydrotest_date() {
        return hydrotest_date;
    }

    public void setHydrotest_date(String hydrotest_date) {
        this.hydrotest_date = hydrotest_date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @SerializedName("location")
    @Expose
    private String location;

    @SerializedName("status")
    @Expose
    private String status;



    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getFilledWith() {
        return filledWith;
    }

    public void setFilledWith(String filledWith) {
        this.filledWith = filledWith;
    }

}
