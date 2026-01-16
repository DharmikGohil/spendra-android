package com.dharmikgohil.spendra.data.remote

import com.dharmikgohil.spendra.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = BuildConfig.API_BASE_URL

    val api: SpendraApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpendraApi::class.java)
    }
}
