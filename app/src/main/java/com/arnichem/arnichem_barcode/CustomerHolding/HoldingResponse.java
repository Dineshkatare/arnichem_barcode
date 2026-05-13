package com.arnichem.arnichem_barcode.CustomerHolding;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HoldingResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("msg")
    private String msg;

    @SerializedName("customer_name")
    private String customerName;

    @SerializedName("next_entry_no")
    private int nextEntryNo;

    @SerializedName("cylinders")
    private List<HoldingCylinder> cylinders;

    public String getStatus() { return status; }
    public String getMsg() { return msg; }
    public String getCustomerName() { return customerName; }
    public int getNextEntryNo() { return nextEntryNo; }
    public List<HoldingCylinder> getCylinders() { return cylinders; }
}
