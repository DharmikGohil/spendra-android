package com.dharmikgohil.spendra.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dharmikgohil.spendra.data.local.BudgetDao
import com.dharmikgohil.spendra.data.local.BudgetEntity
import com.dharmikgohil.spendra.data.local.TransactionDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.dharmikgohil.spendra.SpendraApplication
import com.dharmikgohil.spendra.data.local.AppDatabase
import java.util.UUID

data class CategoryBudgetState(
    val categoryId: String,
    val categoryName: String,
    val spent: Double,
    val budget: Double,
    val period: String = "MONTHLY"
)

class BudgetViewModel(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val userId: String = "user_1" // TODO: Get actual userId
) : ViewModel() {

    private val _budgetState = MutableStateFlow<List<CategoryBudgetState>>(emptyList())
    val budgetState: StateFlow<List<CategoryBudgetState>> = _budgetState.asStateFlow()

    init {
        loadBudgets()
    }

    fun loadBudgets() {
        viewModelScope.launch {
            // 1. Get all categories (simulated for now as we don't have a Category table yet, using unique categories from transactions)
            // Ideally we should have a Category table. For now, let's get unique categories from transactions.
            // Wait, we do have categories in transactions.
            // Let's assume a fixed list of categories for MVP or fetch distinct from DB.
            val categories = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Other")
            
            val budgets = budgetDao.getBudgets(userId)
            val budgetMap = budgets.associateBy { it.categoryId }

            val states = categories.map { category ->
                // Calculate spent for this category in current month
                // TODO: Implement date filtering in TransactionDao. For now, getting all time spent for demo.
                // We need a query in TransactionDao to get sum by category.
                // Let's assume 0 for now and add the query later.
                val spent = 0.0 // Placeholder
                
                val budgetEntity = budgetMap[category]
                CategoryBudgetState(
                    categoryId = category,
                    categoryName = category,
                    spent = spent,
                    budget = budgetEntity?.amount ?: 0.0
                )
            }
            _budgetState.value = states
        }
    }

    fun setBudget(categoryId: String, amount: Double) {
        viewModelScope.launch {
            val budget = BudgetEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                categoryId = categoryId,
                amount = amount,
                period = "MONTHLY",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            budgetDao.insertBudget(budget)
            loadBudgets()
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as SpendraApplication)
                val database = AppDatabase.getDatabase(application)
                BudgetViewModel(
                    budgetDao = database.budgetDao(),
                    transactionDao = database.transactionDao()
                )
            }
        }
    }
}
