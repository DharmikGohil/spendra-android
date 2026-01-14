package com.dharmikgohil.spendra

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Retrofit API interface
interface SpendraApi {
    @POST("transactions/sync")
    suspend fun syncTransactions(@Body request: SyncRequest): SyncResponse

    @retrofit2.http.GET("transactions")
    suspend fun getTransactions(
        @retrofit2.http.Header("x-device-id") deviceId: String,
        @retrofit2.http.Query("limit") limit: Int = 50,
        @retrofit2.http.Query("offset") offset: Int = 0
    ): GetTransactionsResponse

    @retrofit2.http.GET("insights/spending")
    suspend fun getSpendingSummary(
        @retrofit2.http.Header("x-device-id") deviceId: String,
        @retrofit2.http.Query("startDate") startDate: String,
        @retrofit2.http.Query("endDate") endDate: String
    ): SpendingSummaryResponse
}

// API Response Models
data class GetTransactionsResponse(
    val data: List<ApiTransaction>,
    val pagination: Pagination
)

data class Pagination(
    val limit: Int,
    val offset: Int,
    val count: Int
)

data class ApiTransaction(
    val id: String,
    val amount: Double,
    val type: String,
    val merchant: String,
    val source: String,
    val category: CategoryDto?,
    val timestamp: String,
    val balance: Double?
)

data class CategoryDto(
    val id: String,
    val name: String,
    val slug: String,
    val icon: String?,
    val color: String?
)

data class SpendingSummaryResponse(
    val data: List<SpendingItem>,
    val total: Double,
    val period: Period
)

data class SpendingItem(
    val categoryId: String,
    val categoryName: String,
    val total: Double,
    val count: Int
)

data class Period(
    val start: String,
    val end: String
)

// Singleton API client
object ApiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
        )
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: SpendraApi = retrofit.create(SpendraApi::class.java)
}
