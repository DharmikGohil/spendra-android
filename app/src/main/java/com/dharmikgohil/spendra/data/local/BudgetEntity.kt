package com.dharmikgohil.spendra.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["userId", "categoryId", "period"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val categoryId: String,
    val amount: Double,
    val period: String, // MONTHLY, WEEKLY, YEARLY
    val createdAt: Long,
    val updatedAt: Long
)
