package com.dharmikgohil.spendra.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("UPDATE transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Int>)
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND timestamp >= :startTime")
    fun getTotalSpentSince(startTime: Long): Flow<Double?>

    @Query("UPDATE transactions SET category = :category WHERE rawTextHash = :rawTextHash")
    suspend fun updateCategory(rawTextHash: String, category: String)
}
