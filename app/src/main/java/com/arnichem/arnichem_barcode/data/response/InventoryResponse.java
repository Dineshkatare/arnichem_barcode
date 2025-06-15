package com.arnichem.arnichem_barcode.data.response;

import com.arnichem.arnichem_barcode.GetData.InventoryCylinder;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InventoryResponse {
    @SerializedName("inventory_cylinders")
    private List<InventoryCylinder> inventoryCylinders;

    public List<InventoryCylinder> getInventoryCylinders() {
        return inventoryCylinders;
    }
}

