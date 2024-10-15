package com.arnichem.arnichem_barcode.Reset;


import com.arnichem.arnichem_barcode.FileUpload.ApiResponse;
import com.arnichem.arnichem_barcode.FileUpload.FileUploadData;
import com.arnichem.arnichem_barcode.GetData.GetDataResponse;
import com.arnichem.arnichem_barcode.PaymentReceipt.GasTypeResponse;
import com.arnichem.arnichem_barcode.attendance.MyResponseModel;
import com.arnichem.arnichem_barcode.data.response.FetchItemAndQuantityVolume;
import com.arnichem.arnichem_barcode.data.response.ReportResponse;
import com.arnichem.arnichem_barcode.leave.LeaveResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface APIInterface {

//    @GET("/api/unknown")
//    Call<MultipleResource> doGetListResources();
//
//    @POST("/api/users")
//    Call<User> createUser(@Body User user);
//
//    @GET("/api/users?")
//    Call<UserList> doGetUserList(@Query("page") String page);


//
    @FormUrlEncoded
    @POST(APIClient.data_fetch)
    Call<GetDataResponse> doCreateUserWithField(@Field("db_host") String db_host,
                                                @Field("db_username") String db_username,
                                                @Field("db_password") String db_password,
                                                @Field("db_name") String db_name);

    @FormUrlEncoded
    @POST(APIClient.sync_bp_contact)
    Call<GetDataResponse> sync_bp_contact(@Field("db_host") String db_host,
                                                @Field("db_username") String db_username,
                                                @Field("db_password") String db_password,
                                                @Field("db_name") String db_name);

    @Multipart
    @POST("attendance_log6.0.php") // Update with your actual endpoint
    Call<MyResponseModel> uploadImageWithTextData(
            @Part("time") RequestBody time,
            @Part("remarks") RequestBody remarks,
            @Part("in_out") RequestBody spinnerTxt,
            @Part("emp_name") RequestBody empName,
            @Part("emp_id") RequestBody empId,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("address") RequestBody addressText,
            @Part("entered_by") RequestBody email,
            @Part("db_host") RequestBody dbHost,
            @Part("db_username") RequestBody dbUsername,
            @Part("db_password") RequestBody dbPasswordText,
            @Part("db_name") RequestBody dbName,
            @Part MultipartBody.Part image
    );


    @Multipart
    @POST("delivery_entry_v6.2.php") // Replace with your actual endpoint
    Call<MyResponseModel> uploadDeliveryData(
            @Part("dura_code") RequestBody duraCode,
            @Part("item") RequestBody item,
            @Part("itemq") RequestBody itemq,
            @Part("item_volume") RequestBody itemVolume,
            @Part("from_warehouse") RequestBody fromWarehouse,
            @Part("to_warehouse") RequestBody toWarehouse,
            @Part("transport_type") RequestBody transportType,
            @Part("cust_code") RequestBody custCode,
            @Part("from_code") RequestBody fromCode,
            @Part("lati") RequestBody lati,
            @Part("logi") RequestBody logi,
            @Part("addr") RequestBody addr,
            @Part("is_scan") RequestBody isScan,
            @Part("transport_no") RequestBody transportNo,
            @Part("driver") RequestBody driver,
            @Part("email") RequestBody email,
            @Part("count") RequestBody count,
            @Part("db_host") RequestBody dbHost,
            @Part("db_username") RequestBody dbUsername,
            @Part("db_password") RequestBody dbPassword,
            @Part("db_name") RequestBody dbName,
            @Part MultipartBody.Part sign // Multipart for image upload
    );

    @Multipart
    @POST("empty_entry_v6.1.php") // Replace with your actual endpoint
    Call<MyResponseModel> uploadEmptyData(
            @Part("dura_code") RequestBody duraCode,
            @Part("is_scan") RequestBody isScan,
            @Part("from_warehouse") RequestBody fromWarehouse,
            @Part("to_warehouse") RequestBody toWarehouse,
            @Part("transport_type") RequestBody transportType,
            @Part("cust_code") RequestBody custCode,
            @Part("from_code") RequestBody fromCode,
            @Part("lati") RequestBody lati,
            @Part("logi") RequestBody logi,
            @Part("addr") RequestBody addr,
            @Part("transport_no") RequestBody transportNo,
            @Part("driver") RequestBody driver,
            @Part("email") RequestBody email,
            @Part("count") RequestBody count,
            @Part("db_host") RequestBody dbHost,
            @Part("db_username") RequestBody dbUsername,
            @Part("db_password") RequestBody dbPassword,
            @Part("db_name") RequestBody dbName,
            @Part MultipartBody.Part sign
    );

    @Multipart
    @POST("dura_delivery_entry_v6.2.php") // Replace with your actual endpoint
    Call<MyResponseModel> uploadDeliveryData(
            @Part("dura_code") RequestBody duraCode,
            @Part("is_scan") RequestBody isScan,
            @Part("item") RequestBody item,
            @Part("itemq") RequestBody itemq,
            @Part("item_volume") RequestBody itemVolume,
            @Part("from_warehouse") RequestBody fromWarehouse,
            @Part("from_code") RequestBody fromCode,
            @Part("to_warehouse") RequestBody toWarehouse,
            @Part("transport_type") RequestBody transportType,
            @Part("cust_code") RequestBody custCode,
            @Part("lati") RequestBody lati,
            @Part("logi") RequestBody logi,
            @Part("addr") RequestBody addr,
            @Part("transport_no") RequestBody transportNo,
            @Part("driver") RequestBody driver,
            @Part("email") RequestBody email,
            @Part("db_host") RequestBody dbHost,
            @Part("db_username") RequestBody dbUsername,
            @Part("db_password") RequestBody dbPassword,
            @Part("db_name") RequestBody dbName,
            @Part MultipartBody.Part sign
    );

    @Multipart
    @POST("dura_empty_entry_v6.1.php") // Replace with your actual API endpoint
    Call<MyResponseModel> uploadEmptyEntry(
            @Part("dura_code") RequestBody duraCode,
            @Part("from_warehouse") RequestBody fromWarehouse,
            @Part("to_warehouse") RequestBody toWarehouse,
            @Part("transport_type") RequestBody transportType,
            @Part("cust_code") RequestBody custCode,
            @Part("lati") RequestBody latitude,
            @Part("logi") RequestBody longitude,
            @Part("addr") RequestBody address,
            @Part("sign") RequestBody digitalSign,
            @Part("from_code") RequestBody fromCode,
            @Part("transport_no") RequestBody transportNo,
            @Part("driver") RequestBody driver,
            @Part("email") RequestBody email,
            @Part("count") RequestBody count,
            @Part("db_host") RequestBody dbHost,
            @Part("db_username") RequestBody dbUsername,
            @Part("db_password") RequestBody dbPassword,
            @Part("db_name") RequestBody dbName,
            @Part MultipartBody.Part sign
    );


    @Multipart
    @POST("file_upload_api.php") // Adjust the endpoint according to your backend
    Call<ApiResponse> uploadFile(
            @Part("db_host") RequestBody dbHost,
            @Part("db_username") RequestBody dbUsername,
            @Part("db_password") RequestBody dbPassword,
            @Part("db_name") RequestBody dbName,
            @Part("type") RequestBody type,
            @Part("doc_number") RequestBody docNumber,
            @Part("size") RequestBody size,
            @Part("email") RequestBody email,
            @Part("username") RequestBody username,
            @Part MultipartBody.Part file
    );



    @FormUrlEncoded
    @POST("fetch_report.php")
    Call<ReportResponse> postReport(
            @Field("user") String user,
            @Field("company") String company,
            @Field("db_host") String dbHost,
            @Field("db_username") String dbUsername,
            @Field("db_password") String dbPassword,
            @Field("db_name") String dbName
    );

    @FormUrlEncoded
    @POST("fetch_delivery_item_quantity_volume.php") // Replace with your actual API URL
    Call<FetchItemAndQuantityVolume> getDeliveryItems(
            @Field("db_host") String dbHost,
            @Field("db_username") String dbUsername,
            @Field("db_password") String dbPassword,
            @Field("db_name") String dbName,
            @Field("dcno") String dcno

    );

    @FormUrlEncoded
    @POST("upload_call_logs.php")
    Call<ApiResponse> uploadCallLogs(
            @Field("db_host") String dbHost,
            @Field("db_username") String dbUsername,
            @Field("db_password") String dbPassword,
            @Field("db_name") String dbName,
            @Field("call_logs") String callLogsJson
    );


    @FormUrlEncoded
    @POST("apply_leave.php") // Example: "leave/submit_leave.php"
    Call<LeaveResponse> submitLeaveApplication(
            @Field("db_host") String dbHost,
            @Field("db_username") String dbUsername,
            @Field("db_password") String dbPassword,
            @Field("db_name") String dbName,
            @Field("emp_id") String empId,
            @Field("from_date") String fromDate,
            @Field("to_date") String toDate,
            @Field("type") String leaveType,
            @Field("reason") String reason,
            @Field("joining_date") String joiningDate
    );


    @FormUrlEncoded
    @POST("print_setting.php") // Update with your PHP API endpoint
    Call<GasTypeResponse> fetchGasTypes(
            @Field("db_host") String dbHost,
            @Field("db_username") String dbUsername,
            @Field("db_password") String dbPassword,
            @Field("db_name") String dbName
    );



}