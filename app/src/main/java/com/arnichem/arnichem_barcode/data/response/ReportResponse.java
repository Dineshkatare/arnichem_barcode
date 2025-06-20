package com.arnichem.arnichem_barcode.data.response;

public class ReportResponse {

    private String user;
    private String company;
    private int reports_access;

    // Constructor
    public ReportResponse(String user, String company, int reports_access) {
        this.user = user;
        this.company = company;
        this.reports_access = reports_access;
    }

    // Getters
    public String getUser() {
        return user;
    }

    public String getCompany() {
        return company;
    }

    public int getReportsAccess() {
        return reports_access;
    }

    // Setters
    public void setUser(String user) {
        this.user = user;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setReportsAccess(int reports_access) {
        this.reports_access = reports_access;
    }
}
