
package com.arnichem.arnichem_barcode.GetData;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class VehicleDetail {

    @SerializedName("srno")
    @Expose
    private String srno;
    @SerializedName("name")
    @Expose
    private String name;

    public String getSrno() {
        return srno;
    }

    public void setSrno(String srno) {
        this.srno = srno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
