package com.arnichem.arnichem_barcode.TransactionsView.validate_dc


data class CylinderData(
    val cyl_code: String,
    val barcode_no: String,
    val filled_with: String,
    var isValidated: Boolean = false
)