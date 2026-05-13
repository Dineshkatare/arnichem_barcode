package com.arnichem.arnichem_barcode.CustomerHolding;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class HoldingCylinder implements Serializable {
    @SerializedName("item_code")
    private String itemCode;

    @SerializedName("item_description")
    private String itemDescription;

    @SerializedName("filled_with")
    private String filledWith;

    @SerializedName("trans_ref_no")
    private String transRefNo;

    @SerializedName("date")
    private String date;

    @SerializedName("pending_days")
    private int pendingDays;

    @SerializedName("is_scanned")
    private String isScanned;

    private String selectedStatus = "DEFAULT";

    // Getters and Setters
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

    public String getFilledWith() { return filledWith; }
    public void setFilledWith(String filledWith) { this.filledWith = filledWith; }

    public String getTransRefNo() { return transRefNo; }
    public void setTransRefNo(String transRefNo) { this.transRefNo = transRefNo; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getPendingDays() { return pendingDays; }
    public void setPendingDays(int pendingDays) { this.pendingDays = pendingDays; }

    public String getIsScanned() { return isScanned; }
    public void setIsScanned(String isScanned) { this.isScanned = isScanned; }

    public String getSelectedStatus() { return selectedStatus; }
    public void setSelectedStatus(String selectedStatus) { this.selectedStatus = selectedStatus; }
}
