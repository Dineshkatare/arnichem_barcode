package com.arnichem.arnichem_barcode.attendance

import com.google.gson.annotations.SerializedName

data class MyResponseModel(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("srno") val srno: String

) {
}