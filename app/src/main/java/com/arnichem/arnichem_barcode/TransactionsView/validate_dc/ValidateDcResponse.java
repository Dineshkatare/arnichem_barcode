package com.arnichem.arnichem_barcode.TransactionsView.validate_dc;

public class ValidateDcResponse {
    private String status;
    private String msg;
    private String dcno;

    // Default constructor
    public ValidateDcResponse() {
    }

    // Parameterized constructor
    public ValidateDcResponse(String status, String msg, String dcno) {
        this.status = status;
        this.msg = msg;
        this.dcno = dcno;
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDcno() {
        return dcno;
    }

    public void setDcno(String dcno) {
        this.dcno = dcno;
    }

    @Override
    public String toString() {
        return "ValidateDcResponse{" +
                "status='" + status + '\'' +
                ", msg='" + msg + '\'' +
                ", dcno='" + dcno + '\'' +
                '}';
    }
}
