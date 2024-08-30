package com.example.mytracking.retrofit


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitController private constructor() {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    companion object {
        private const val BASE_URL = "http://123.253.10.229:8080/"

        @Volatile
        private var instance: RetrofitController? = null

        fun getInstance(): RetrofitController {
            return instance ?: synchronized(this) {
                instance ?: RetrofitController().also { instance = it }
            }
        }
    }

    fun getApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
