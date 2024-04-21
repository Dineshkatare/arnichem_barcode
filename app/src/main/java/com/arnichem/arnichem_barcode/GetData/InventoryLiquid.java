
package com.arnichem.arnichem_barcode.GetData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class InventoryLiquid {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("unit")
    @Expose
    private String unit;
    @SerializedName("conv_factor")
    @Expose
    private String convFactor;
    @SerializedName("HSN")
    @Expose
    private String hsn;
    @SerializedName("GST")
    @Expose
    private String gst;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getConvFactor() {
        return convFactor;
    }

    public void setConvFactor(String convFactor) {
        this.convFactor = convFactor;
    }

    public String getHsn() {
        return hsn;
    }

    public void setHsn(String hsn) {
        this.hsn = hsn;
    }

    public String getGst() {
        return gst;
    }

    public void setGst(String gst) {
        this.gst = gst;
    }

}
