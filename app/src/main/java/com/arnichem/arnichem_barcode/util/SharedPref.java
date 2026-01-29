package com.arnichem.arnichem_barcode.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;

public class SharedPref {

    // Storage File
    public static final String SHARED_PREF_NAME = "larntech";

    // Username
    public static final String from_loc = "from_loc";
    public static final String cutomer_sel = "cutomer_sel";
    public static final String report_status = "report_status";
    public static final String showed_msg_status = "msg_status";

    public static final String item_Sel = "item_Sel";

    public static final String call_log_access = "call_log_access";

    public static final String distshare = "dist";
    public static final String manifold = "manifold";
    public static final String USER_NAME = "username";
    public static final String FNAME = "firstname";
    public static final String LNAME = "lastname";
    public static final String loginDate = "loginDate";
    public static final String STATUS = "status";
    public static final String VSTATUS = "vstatus";
    public static final String VehicleNo = "VehicleNo";
    public static final String EMAIL = "email";
    public static final String ID = "id";

    public static final String COMPANY_ID = "COMPANY_ID";
    public static final String COMPANY_SHORT_NAME = "COMPANY_SHORT_NAME";
    public static final String COMPANY_FULL_NAME = "COMPANY_FULL_NAME";
    public static final String BG_COLOR = "BG_COLOR";
    public static final String DB_HOST = "DB_HOST";
    public static final String DB_USERNAME = "DB_USERNAME";
    public static final String DB_PASSWORD = "DB_PASSWORD";
    public static final String DB_NAME = "DB_NAME";
    public static final String SELECT_COMAPANY = "SELECT_COMAPANY";
    public static final String BASE_URL = "BASE_URL";
    public static final String LOGO = "LOGO";
    public static final String PRINT_LOGO = "PRINT_LOGO";

    public static final String LOGIN_MSG = "LOGIN_MSG";

    public static final String PRINT_NUMBER = "PRINT_NUMBER";

    public static final String SM = "sm";
    public static final String EM = "em";
    public static final String after_tank_pressure = "after_tank_pressure";
    public static final String after_tank_liquid_liter = "after_tank_liquid_liter";
    public static final String before_tank_pressure = "before_tank_pressure";
    public static final String before_tank_liquid_liter = "before_tank_liquid_liter";

    public static final String fill_gap_pressure = "fill_gap_pressure";

    public static final String PRINT_UPI = "PRINT_UPI";
    public static final String double_entry = "double_entry";

    public static final String TERMS_TEXT = "TERMS_TEXT";
    public static final String OWN_CODE = "OWN_CODE";
    public static final String SIGN = "SIGN";
    public static final String SIGNED = "SIGNED";
    public static final String batch_prefix = "batch_prefix";

    public static final String cyc_prefix = "cyc_prefix";

    public static SharedPref mInstance;
    public static Context mCtx;

    public SharedPref(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPref getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPref(context);
        }
        return mInstance;
    }
    // public void statusget(String st) {
    // SharedPreferences sharedPreferences =
    // mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    // SharedPreferences.Editor editor = sharedPreferences.edit();
    // editor.putString(status, st);
    // editor.commit();
    // }

    // method to store user data
    public void storeUserName(String names) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_NAME, names);
        editor.commit();
    }

    public void storeFName(String names) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FNAME, names);
        editor.commit();
    }

    public void storeLoginDate(String date) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(loginDate, date);
        editor.commit();
    }

    public String getLoginDate() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(loginDate, "");
    }

    public void storeLName(String names) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LNAME, names);
        editor.commit();
    }

    public void storeStatus(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(STATUS, sta);
        editor.commit();
    }

    public void storeEmail(String email) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EMAIL, email);
        editor.commit();
    }

    public void storefrom_loc(String email) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(from_loc, email);
        editor.commit();
    }

    public String getfrom_loc() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(from_loc, "");

    }

    public void store_dist(String dist) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(distshare, dist);
        editor.commit();
    }

    public String get_dist() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(distshare, "");
    }

    public void store_manifold(String manifold) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(manifold, manifold);
        editor.commit();
    }

    public String get_manifold() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(manifold, "");

    }

    public void store_customersel(String email) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(cutomer_sel, email);
        editor.commit();
    }

    public String getcustomersel() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(cutomer_sel, "0");

    }

    public void store_report_status(String email) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(report_status, email);
        editor.commit();
    }

    public String get_report_status() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(report_status, "0");

    }

    public void store_show_msg_status(String status) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(showed_msg_status, status);
        editor.commit();
    }

    public String get_show_msg_status() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(showed_msg_status, "0");

    }

    public void store_item_sel(String email) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(item_Sel, email);
        editor.commit();
    }

    public String get_item_sel() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(item_Sel, "0");

    }

    public void storesuperId(String id) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ID, id);
        editor.commit();
    }

    public String get_call_log_access() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(call_log_access, "N");

    }

    public void store_call_log_access(String access) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(call_log_access, access);
        editor.commit();
    }

    public String getEmail() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(EMAIL, "");

    }

    public String Id() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ID, "");

    }

    public void storeVStatus(String vsta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(VSTATUS, vsta);
        editor.commit();
    }

    public void storeVehicleNumber(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(VehicleNo, sta);
        editor.commit();
    }

    // check if user is logged in
    public String isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return String.valueOf(sharedPreferences.getString(STATUS, null) != null);
    }

    // find logged in user
    public String LoggedInUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(STATUS, "");

    }

    public String vLoggedInUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(VSTATUS, "");

    }

    public String UserName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_NAME, "");

    }

    public String FirstName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(FNAME, "");

    }

    public String LastName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LNAME, "");

    }

    public String getVehicleNo() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(VehicleNo, "");

    }

    public String getID() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ID, "");
    }

    public void setCompanyID(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(COMPANY_ID, sta);
        editor.commit();
    }

    public String getCompanyID() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(COMPANY_ID, "");

    }

    public void setCompanyShortName(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(COMPANY_SHORT_NAME, sta);
        editor.commit();
    }

    public String getCompanyShortName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(COMPANY_SHORT_NAME, "");

    }

    public void setCompanyFullName(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(COMPANY_FULL_NAME, sta);
        editor.commit();
    }

    public String getCompanyFullName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(COMPANY_FULL_NAME, "");

    }

    public void setBgColor(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BG_COLOR, sta);
        editor.commit();
    }

    public String getBgColor() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(BG_COLOR, "");

    }

    public void setDBHost(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DB_HOST, sta);
        editor.commit();
    }

    public String getDBHost() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DB_HOST, "");

    }

    public void setDBUsername(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DB_USERNAME, sta);
        editor.commit();
    }

    public String getDBUsername() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DB_USERNAME, "");

    }

    public void setDBPassword(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DB_PASSWORD, sta);
        editor.commit();
    }

    public String getDBPassword() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DB_PASSWORD, "");

    }

    public void setDBName(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DB_NAME, sta);
        editor.commit();
    }

    public String getDBName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DB_NAME, "");
    }

    public void setSelectedCompany(Boolean sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SELECT_COMAPANY, sta);
        editor.commit();
    }

    public Boolean isSelectedCompany() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(SELECT_COMAPANY, false);

    }

    public void setBaseUrl(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BASE_URL, sta);
        editor.commit();
    }

    public String getBaseUrl() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(BASE_URL, "");
    }

    public void setOwnCode(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(OWN_CODE, sta);
        editor.commit();
    }

    public String getOwnCode() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(OWN_CODE, "");
    }

    public void setBatchPrefix(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(batch_prefix, sta);
        editor.commit();
    }

    public String getBatchPrefix() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(batch_prefix, "");
    }

    public void setCycPrefix(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(cyc_prefix, sta);
        editor.commit();
    }

    public String getCycPrefix() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(cyc_prefix, "");
    }

    public void setTermsText(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TERMS_TEXT, sta);
        editor.commit();
    }

    public String getTermsText() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(TERMS_TEXT, "");
    }

    public void setSign(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SIGN, sta);
        editor.commit();
    }

    public String getSign() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SIGN, "");
    }

    public void setIsSigned(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SIGNED, sta);
        editor.commit();
    }

    public String getIsSigned() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SIGNED, "");
    }

    public void setLogo(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LOGO, sta);
        editor.commit();
    }

    public String getLogo() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LOGO, "");
    }

    public void setPhoneNumber(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PRINT_NUMBER, sta);
        editor.commit();
    }

    public String getPhoneNumber() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PRINT_NUMBER, "");
    }

    public void setPrintUpi(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PRINT_UPI, sta);
        editor.commit();
    }

    public String getPrintUpi() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PRINT_UPI, "");
    }

    public void setPrintLogo(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PRINT_LOGO, sta);
        editor.commit();
    }

    public String getPrintLogo() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PRINT_LOGO, "");
    }

    public void setLoginMsg(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LOGIN_MSG, sta);
        editor.commit();
    }

    public String getLoginMsg() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LOGIN_MSG, "");
    }

    // Logout user
    public void logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        // mCtx.startActivity(new Intent(mCtx, MainActivity.class));
    }

    public void setDoubleEntry(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(double_entry, sta);
        editor.commit();
    }

    public String getDoubleEntry() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(double_entry, "");
    }

    public void setSm(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SM, sta);
        editor.commit();
    }

    public String getSm() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SM, "");
    }

    public void setEm(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EM, sta);
        editor.commit();
    }

    public String getEm() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(EM, "");
    }

    public void setAfter_tank_pressure(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(after_tank_pressure, sta);
        editor.commit();
    }

    public String getAfter_tank_pressure() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(after_tank_pressure, "");
    }

    public void setAfter_tank_liquid_liter(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(after_tank_liquid_liter, sta);
        editor.commit();
    }

    public String getAfter_tank_liquid_liter() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(after_tank_liquid_liter, "");
    }

    public void setBefore_tank_pressure(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(before_tank_pressure, sta);
        editor.commit();
    }

    public String getBefore_tank_pressure() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(before_tank_pressure, "");
    }

    public void setBefore_tank_liquid_liter(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(before_tank_liquid_liter, sta);
        editor.commit();
    }

    public String getBefore_tank_liquid_liter() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(before_tank_liquid_liter, "");
    }

    public void setFillGapPressure(String sta) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(fill_gap_pressure, sta);
        editor.commit();
    }

    public String getFillGapPressure() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(fill_gap_pressure, "");
    }

    public static final String PERM_PREF_NAME = "arnichem_perm";
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_NO = "device_no";

    public void setPersistentDevice(String name, String number) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PERM_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_NAME, name);
        editor.putString(DEVICE_NO, number);
        editor.apply();
    }

    public String getPersistentDeviceName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PERM_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DEVICE_NAME, "");
    }

    public String getPersistentDeviceNumber() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PERM_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DEVICE_NO, "");
    }
}
