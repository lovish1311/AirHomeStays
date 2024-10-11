package com.airhomestays.app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: RazorPayService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.airhomestays.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RazorPayService::class.java)
    }
}