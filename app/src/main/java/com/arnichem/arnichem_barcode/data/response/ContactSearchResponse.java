package com.arnichem.arnichem_barcode.data.response;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class ContactSearchResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private List<ContactData> data;

    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public List<ContactData> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public static class ContactData {
        @SerializedName("code")
        private String code;

        @SerializedName("name")
        private String name;

        @SerializedName("city")
        private String city;

        @SerializedName("phone1")
        private String phone1;

        @SerializedName("phone2")
        private String phone2;

        @SerializedName("company_email")
        private String companyEmail;

        @SerializedName("contacts")
        private List<ContactPerson> contacts;

        // Getters
        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getCity() {
            return city;
        }

        public String getPhone1() {
            return phone1;
        }

        public String getPhone2() {
            return phone2;
        }

        public String getCompanyEmail() {
            return companyEmail;
        }

        public List<ContactPerson> getContacts() {
            return contacts;
        }
    }

    public static class ContactPerson {
        @SerializedName("name")
        private String name;

        @SerializedName("designation")
        private String designation;

        @SerializedName("mobile")
        private String mobile;

        @SerializedName("phone")
        private String phone;

        @SerializedName("email")
        private String email;

        public String getName() {
            return name;
        }

        public String getDesignation() {
            return designation;
        }

        public String getMobile() {
            return mobile;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }
    }
}
