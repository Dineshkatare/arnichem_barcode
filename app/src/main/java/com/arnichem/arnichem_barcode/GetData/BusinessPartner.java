
package com.arnichem.arnichem_barcode.GetData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BusinessPartner {

    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("invoice")
    @Expose
    private String invoice;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

}
