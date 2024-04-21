
package com.arnichem.arnichem_barcode.GetData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class InventoryGa {

    @SerializedName("gasName")
    @Expose
    private String gasName;

    @SerializedName("item_code")
    @Expose
    private String item_code;

    public String getItem_code() {
        return item_code;
    }

    public void setItem_code(String item_code) {
        this.item_code = item_code;
    }

    public String getGasName() {
        return gasName;
    }

    public void setGasName(String gasName) {
        this.gasName = gasName;
    }

}
