package com.dharmikgohil.spendra.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    suspend fun getBudgets(userId: String): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryId = :categoryId")
    suspend fun getBudgetByCategory(userId: String, categoryId: String): BudgetEntity?
}
