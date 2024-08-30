package com.example.mytracking.retrofit


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("location")
    fun sendLocation(@Body locationRequest: LocationRequest): Call<Void>
}
