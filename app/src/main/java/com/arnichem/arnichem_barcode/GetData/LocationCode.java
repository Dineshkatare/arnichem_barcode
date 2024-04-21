
package com.arnichem.arnichem_barcode.GetData;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class LocationCode {

    @SerializedName("name")
    @Expose
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @SerializedName("code")
    @Expose
    private String code;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
