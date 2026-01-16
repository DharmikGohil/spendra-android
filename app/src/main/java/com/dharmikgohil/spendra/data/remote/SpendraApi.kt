package com.dharmikgohil.spendra.data.remote

import com.dharmikgohil.spendra.data.model.DailySummary
import com.dharmikgohil.spendra.data.model.Suggestion
import retrofit2.http.GET
import retrofit2.http.Header

interface SpendraApi {
    @GET("insights/daily")
    suspend fun getDailySummary(
        @Header("x-device-id") deviceId: String
    ): DailySummary

    @GET("suggestions")
    suspend fun getSuggestions(
        @Header("x-device-id") deviceId: String
    ): List<Suggestion>
}
