package com.arnichem.arnichem_barcode.view;

import com.google.gson.annotations.SerializedName;

public class NotificationModel {
    @SerializedName("id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("body")
    private String body;
    @SerializedName("sent_at")
    private String sentAt;
    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private String data;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getSentAt() { return sentAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getData() { return data; }
}
