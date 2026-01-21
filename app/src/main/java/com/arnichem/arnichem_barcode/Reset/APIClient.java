package com.arnichem.arnichem_barcode.Reset;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static Retrofit retrofit = null;
    public static final String data_fetch = "data_fetch_v8.4.php";
    public static final String sync_barcode = "inventory_cylinders_fetch.php";

    public static final String sync_bp_contact = "sync_bp_contact.php";
    public static final String vehicle_login = "http://arnichem.co.in/intranet/barcode/APP/app_apis/vehicle_login.php";
    public static final String getCompanies = "http://arnichem.co.in/intranet/barcode/APP/app_apis/getCompanies1.php";
    public static final String access_login = "http://arnichem.co.in/intranet/barcode/APP/app_apis/access_login.php";
    public static final String barcode_registration = "http://arnichem.co.in/intranet/barcode/APP/app_apis/barcode_registration_v8.5.php"; // DONE
    public static final String crm_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/crm_entry.php";
    public static final String bpcontact_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/bpcontact_entry.php";

    public static final String diesel_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/diesel_entry.php";
    public static final String closing_stock_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/closing_stock_entry.php";
    public static final String godown_delivery_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/godown_delivery_entry_v8.5.php";// DONE
    public static final String other_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/other_entry_v6.2.php";// DONE

    public static final String godown_empty_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/godown_empty_entry_v8.5.php";// DONE

    public static final String full_recipt_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/full_recipt_entry_v4.5.php"; // DONE
    public static final String godown_fullrecipt_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/godown_fullrecipt_entry_v4.5.php";// DONE
    public static final String payment_recipt_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/payment_recipt_entry.php";
    public static final String voucher_payment_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/voucher_payment_entry.php";
    public static final String voucher_payment_names = "http://arnichem.co.in/intranet/barcode/APP/app_apis/voucher_payment_names.php";

    public static final String NO2_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/NO2_entry_5.6.php";
    public static final String CO2_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/CO2_entry_5.6.php";
    public static final String O2_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/O2_entry_5.6.php";
    public static final String ammonia_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/ammonia_entry_5.6.php";
    public static final String zero_air_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/zero_air_entry_5.6.php";

    public static final String ammonia_delivery = "http://arnichem.co.in/intranet/barcode/APP/app_apis/ammonia_delivery_v6.2.php";
    public static final String ammonia_del_update = "http://arnichem.co.in/intranet/barcode/APP/app_apis/ammonia_get_details.php";
    public static final String fill_dura_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/fill_dura_entry_4.7.php";
    public static final String outward_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/outward_entry_v4.5.php"; // DONE
                                                                                                                             // //DONE
    public static final String inward_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/inward_entry_v4.5.php"; // DONE
    public static final String delivery_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/delivery_entry_v5.7.php"; // DONE
    public static final String empty_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/empty_entry_v5.7.php"; // DONE
    public static final String dura_delivery_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/new_dura_delivery_entry.php";
    public static final String dura_empty_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/new_dura_empty_entry.php";
    public static final String Liquid_delivery_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/liquid_delivery_entry_v6.2.php";
    public static final String fetch_print_data_cylinder_transactions = "http://arnichem.co.in/intranet/barcode/APP/app_apis/fetch_print_data_cylinder_transactions.php";
    public static final String fetch_print_data_delivery_main = "http://arnichem.co.in/intranet/barcode/APP/app_apis/fetch_print_data_delivery_main.php";
    public static final String fetch_print_data_empty_main = "http://arnichem.co.in/intranet/barcode/APP/app_apis/fetch_print_data_empty_main.php";
    public static final String change_password = "http://arnichem.co.in/intranet/barcode/APP/app_apis/change_password.php";
    public static final String check_dual_delivery = "http://arnichem.co.in/intranet/barcode/APP/app_apis/check_dual_delivery.php";

    public static final String check_attendance_status = "http://arnichem.co.in/intranet/barcode/APP/app_apis/get_emp_attendance_status.php";

    public static final String fetch_username = "http://arnichem.co.in/intranet/barcode/APP/app_apis/fetch_username.php";
    public static final String get_dura_fill_details = "http://arnichem.co.in/intranet/barcode/APP/app_apis/get_dura_fill_details.php";
    public static final String fetch_sign = "http://arnichem.co.in/intranet/barcode/APP/app_apis/fetch_digital_sign.php";
    public static final String hydro_test = "http://arnichem.co.in/intranet/barcode/APP/app_apis/hydrotest_app_entry.php"; // DONE

    public static final String dry_ice_production_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/dry_ice_production.php";

    public static final String dry_ice_delivery_entry = "http://arnichem.co.in/intranet/barcode/APP/app_apis/dry_ice_delivery_v6.2.php"; // DONE

    public static final String get_invoice_php = "http://arnichem.co.in/intranet/barcode/APP/app_apis/invoice.php"; // DONE

    public static final String delivery_validation = "http://arnichem.co.in/intranet/barcode/APP/app_apis/delivery_validation.php"; // DONE

    public static Retrofit getClient() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor)
                .connectTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES) // write timeout
                .readTimeout(2, TimeUnit.MINUTES)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://arnichem.co.in/intranet/barcode/APP/app_apis/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }

}