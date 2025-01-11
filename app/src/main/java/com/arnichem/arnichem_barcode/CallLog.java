package com.arnichem.arnichem_barcode;

import com.google.gson.annotations.SerializedName;

public class CallLog {
    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("contact_name")
    private String contactName;

    @SerializedName("call_type")
    private int callType;

    @SerializedName("call_date")
    private long callDate;

    @SerializedName("call_duration")
    private int callDuration;

    @SerializedName("call_unique_id")
    private String callUniqueId;

    @SerializedName("sim_slot")
    private int simSlot;

    @SerializedName("sim_serial_number")
    private String simSerialNumber;

    // Add getters and setters
    // ...
}
