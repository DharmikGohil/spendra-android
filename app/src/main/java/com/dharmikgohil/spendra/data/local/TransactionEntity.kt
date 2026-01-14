package com.dharmikgohil.spendra.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "CREDIT" or "DEBIT"
    val merchant: String,
    val source: String, // "UPI", "SMS", etc.
    val timestamp: Long,
    val balance: Double? = null,
    val rawTextHash: String, // To prevent duplicates
    val category: String? = null, // "Food", "Travel", etc.
    val isSynced: Boolean = false
)
