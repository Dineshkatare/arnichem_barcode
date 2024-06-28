package com.example.myapplication.reset

import com.arnichem.arnichem_barcode.GetData.GetDataResponse
import com.arnichem.arnichem_barcode.Reset.APIClient
import com.example.myapplication.data.request.LoginRequest
import com.example.myapplication.data.response.LoginResponse
import com.example.myapplication.data.response.Task
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterface {

    @Headers("Content-Type: application/json")
    @POST("task_login") // Replace with your actual login endpoint URL
    suspend fun login(@Body user: LoginRequest): LoginResponse

    @GET("search_tasks") // Replace with your actual search tasks endpoint URL
    suspend fun searchTasks(@Query("user") user: String, @Query("q") query: String): List<Task>


    @FormUrlEncoded
    @POST("task_login")
    fun login1(
        @Body user: LoginRequest
    ): LoginResponse


}
