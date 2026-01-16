package com.dharmikgohil.spendra.data.model

import com.google.gson.annotations.SerializedName

data class DailySummary(
    @SerializedName("safeToSpend") val safeToSpend: Double,
    @SerializedName("totalSpentToday") val totalSpentToday: Double,
    @SerializedName("daysRemaining") val daysRemaining: Int,
    @SerializedName("monthlyIncome") val monthlyIncome: Double,
    @SerializedName("totalBudgeted") val totalBudgeted: Double,
    @SerializedName("totalGoalSavings") val totalGoalSavings: Double,
    @SerializedName("message") val message: String
)

data class Suggestion(
    @SerializedName("type") val type: String, // "BUDGET" or "GOAL"
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("data") val data: SuggestionData,
    @SerializedName("priority") val priority: String // "HIGH", "MEDIUM", "LOW"
)

data class SuggestionData(
    // Budget specific
    @SerializedName("categoryId") val categoryId: String?,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("suggestedAmount") val suggestedAmount: Double?,
    @SerializedName("averageSpend") val averageSpend: Double?,
    
    // Goal specific
    @SerializedName("suggestedName") val suggestedName: String?
)
