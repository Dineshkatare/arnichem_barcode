package com.arnichem.arnichem_barcode;
// CombinedRequest.java

import java.util.List;

public class CombinedRequest {
    private List<CallLogManager.CallLogEntry> call_logs;
    private String db_host;
    private String db_username;
    private String db_password;
    private String db_name;

    public CombinedRequest(List<CallLogManager.CallLogEntry> call_logs, String db_host, String db_username, String db_password, String db_name) {
        this.call_logs = call_logs;
        this.db_host = db_host;
        this.db_username = db_username;
        this.db_password = db_password;
        this.db_name = db_name;
    }

    public List<CallLogManager.CallLogEntry> getCall_logs() {
        return call_logs;
    }

    public String getDb_host() {
        return db_host;
    }

    public String getDb_username() {
        return db_username;
    }

    public String getDb_password() {
        return db_password;
    }

    public String getDb_name() {
        return db_name;
    }
}
