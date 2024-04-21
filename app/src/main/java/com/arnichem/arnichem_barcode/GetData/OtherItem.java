package com.arnichem.arnichem_barcode.GetData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtherItem {
    @SerializedName("item_code")
    @Expose
    private String item_code;

    public String getItem_code() {
        return item_code;
    }

    public void setItem_code(String item_code) {
        this.item_code = item_code;
    }

    public String getShort_description() {
        return short_description;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    @SerializedName("short_description")
    @Expose
    private String short_description;


}
