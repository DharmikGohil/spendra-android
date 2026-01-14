package com.dharmikgohil.spendra.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dharmikgohil.spendra.SpendraApplication
import com.dharmikgohil.spendra.data.local.TransactionDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.dharmikgohil.spendra.TransactionDto
import java.time.LocalDate
import java.time.ZoneId

class HomeViewModel(private val dao: TransactionDao) : ViewModel() {

    // Hardcoded monthly budget for MVP
    private val monthlyBudget = 30000.0

    private val startOfMonth = LocalDate.now().withDayOfMonth(1)
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val totalSpentThisMonth: StateFlow<Double> = dao.getTotalSpentSince(startOfMonth)
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val safeToSpend: StateFlow<Double> = totalSpentThisMonth.map { spent ->
        val remaining = monthlyBudget - spent
        val daysRemaining = LocalDate.now().lengthOfMonth() - LocalDate.now().dayOfMonth + 1
        if (daysRemaining > 0) remaining / daysRemaining else 0.0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = monthlyBudget / LocalDate.now().lengthOfMonth()
    )

    suspend fun syncTransactions(deviceId: String) {
        try {
            // Force full resync to fix backend state
            dao.markAllAsUnsynced()
            
            val unsynced = dao.getUnsyncedTransactions()
            if (unsynced.isEmpty()) return

            val dtos = unsynced.map { entity ->
                TransactionDto(
                    amount = entity.amount,
                    type = entity.type,
                    merchant = entity.merchant,
                    source = entity.source,
                    timestamp = java.time.Instant.ofEpochMilli(entity.timestamp).toString(),
                    rawTextHash = entity.rawTextHash,
                    balance = entity.balance,
                    category = null // Backend ignores this
                )
            }

            val response = com.dharmikgohil.spendra.ApiClient.api.syncTransactions(
                com.dharmikgohil.spendra.SyncRequest(deviceId, dtos)
            )

            if (response.success) {
                // Mark as synced
                dao.markAsSynced(unsynced.map { it.id })
                
                // Update categories from response
                response.data?.forEach { dto ->
                    if (dto.category != null && dto.rawTextHash != null) {
                        dao.updateCategory(dto.rawTextHash, dto.category.name)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val _spendingState = kotlinx.coroutines.flow.MutableStateFlow<com.dharmikgohil.spendra.SpendingSummaryResponse?>(null)
    val spendingState: StateFlow<com.dharmikgohil.spendra.SpendingSummaryResponse?> = _spendingState

    private val _previousSpendingState = kotlinx.coroutines.flow.MutableStateFlow<com.dharmikgohil.spendra.SpendingSummaryResponse?>(null)
    val previousSpendingState: StateFlow<com.dharmikgohil.spendra.SpendingSummaryResponse?> = _previousSpendingState

    suspend fun getSpendingInsights(deviceId: String) {
        try {
            val now = LocalDate.now()
            
            // Current Month
            val startOfMonth = now.withDayOfMonth(1).toString()
            val endOfMonth = now.plusMonths(1).withDayOfMonth(1).minusDays(1).toString()

            // Previous Month
            val startOfPrevMonth = now.minusMonths(1).withDayOfMonth(1).toString()
            val endOfPrevMonth = now.withDayOfMonth(1).minusDays(1).toString()

            // Fetch Current
            val response = com.dharmikgohil.spendra.ApiClient.api.getSpendingSummary(
                deviceId = deviceId,
                startDate = "${startOfMonth}T00:00:00Z",
                endDate = "${endOfMonth}T23:59:59Z"
            )
            _spendingState.value = response

            // Fetch Previous
            val prevResponse = com.dharmikgohil.spendra.ApiClient.api.getSpendingSummary(
                deviceId = deviceId,
                startDate = "${startOfPrevMonth}T00:00:00Z",
                endDate = "${endOfPrevMonth}T23:59:59Z"
            )
            _previousSpendingState.value = prevResponse

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SpendraApplication)
                HomeViewModel(application.database.transactionDao())
            }
        }
    }
}
