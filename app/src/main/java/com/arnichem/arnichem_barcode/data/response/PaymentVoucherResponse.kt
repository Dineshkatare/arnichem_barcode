package com.arnichem.arnichem_barcode.data.response

import com.google.gson.annotations.SerializedName


class PaymentVoucherResponse {
    @SerializedName("data")
    val data: List<PaymentVoucher>? = null

    class PaymentVoucher {
        // Getters
        @SerializedName("srno")
        val srno: Int = 0

        @SerializedName("series")
        val series: String? = null

        @SerializedName("vch_no")
        val vchNo: String? = null

        @SerializedName("date")
        val date: String? = null

        @SerializedName("cust_code")
        val custCode: String? = null

        @SerializedName("mode")
        val mode: String? = null

        @SerializedName("amount")
        val amount: String? = null

        @SerializedName("description")
        val description: String? = null

        @SerializedName("transaction_id")
        val transactionId: String? = null

        @SerializedName("remarks")
        val remarks: String? = null

        @SerializedName("file_path")
        val filePath: String? = null

        @SerializedName("user")
        val user: String? = null
    }
}