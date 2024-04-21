
package com.arnichem.arnichem_barcode.GetData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DuraCylinder {

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
