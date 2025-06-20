package com.arnichem.arnichem_barcode.leave;

public class LeaveResponse {

    // Variables to map the API response fields
    private String status;
    private String message;

    public String getSrno() {
        return srno;
    }

    public void setSrno(String srno) {
        this.srno = srno;
    }

    private String srno;


    // Constructor
    public LeaveResponse(String status, String message,String srno) {
        this.status = status;
        this.message = message;
        this.srno = srno;

    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // toString method (optional for debugging)
    @Override
    public String toString() {
        return "LeaveResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
