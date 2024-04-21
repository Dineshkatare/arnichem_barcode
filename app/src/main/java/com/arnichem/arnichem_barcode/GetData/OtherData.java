package com.arnichem.arnichem_barcode.GetData;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OtherData {
    @SerializedName("other_data")
    private List<OtherItem> otherItems;

    public List<OtherItem> getOtherItems() {
        return otherItems;
    }

    public void setOtherItems(List<OtherItem> otherItems) {
        this.otherItems = otherItems;
    }
// Getters and setters...
}

