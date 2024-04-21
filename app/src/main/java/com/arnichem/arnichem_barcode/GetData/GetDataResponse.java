
package com.arnichem.arnichem_barcode.GetData;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class GetDataResponse {

    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

}
