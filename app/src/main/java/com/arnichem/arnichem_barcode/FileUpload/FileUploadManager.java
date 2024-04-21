package com.arnichem.arnichem_barcode.FileUpload;

import com.arnichem.arnichem_barcode.Reset.APIInterface;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FileUploadManager {
    private static final String BASE_URL = "http://arnichem.co.in/intranet/barcode/APP/app_apis/"; // Adjust your base URL
    private final APIInterface apiService;

    public FileUploadManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit  retrofit = new Retrofit.Builder()
                .baseUrl("http://arnichem.co.in/intranet/barcode/APP/app_apis/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();


        apiService = retrofit.create(APIInterface.class);
    }

    public void uploadFile(FileUploadData data, final Callback<ApiResponse> callback) {
        Call<ApiResponse> call = apiService.uploadFile(
                data.getDb_host(),
                data.getDb_username(),
                data.getDb_password(),
                data.getDb_name(),
                data.getType(),
                data.getDoc_number(),
                data.getSize(),
                data.getEmail(),
                data.getUsername(),
                data.getFilePart()
                );

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, new Throwable("Failed to upload file"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }
}
