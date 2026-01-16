package com.dharmikgohil.spendra.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dharmikgohil.spendra.SpendraApplication
import com.dharmikgohil.spendra.data.local.TransactionDao
import com.dharmikgohil.spendra.data.model.DailySummary
import com.dharmikgohil.spendra.data.model.Suggestion
import com.dharmikgohil.spendra.data.remote.NetworkModule
import com.dharmikgohil.spendra.TransactionDto
import com.dharmikgohil.spendra.SyncRequest
import com.dharmikgohil.spendra.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val dao: TransactionDao) : ViewModel() {

    private val _dailySummary = MutableStateFlow<DailySummary?>(null)
    val dailySummary: StateFlow<DailySummary?> = _dailySummary.asStateFlow()

    private val _suggestions = MutableStateFlow<List<Suggestion>>(emptyList())
    val suggestions: StateFlow<List<Suggestion>> = _suggestions.asStateFlow()

    private val deviceId = "android_emulator_1" // TODO: Get real device ID

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                // Fetch Daily Summary
                val summary = NetworkModule.api.getDailySummary(deviceId)
                _dailySummary.value = summary
                
                // Fetch Suggestions
                val suggestionList = NetworkModule.api.getSuggestions(deviceId)
                _suggestions.value = suggestionList
                
                // Trigger Sync in background
                syncTransactions(deviceId)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error (e.g. show offline state)
            }
        }
    }

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

            // Using the old ApiClient for sync for now, or migrate to NetworkModule if possible.
            // Assuming ApiClient still exists and works for sync.
            val response = ApiClient.api.syncTransactions(
                SyncRequest(deviceId, dtos)
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
                
                // Reload insights after sync
                val summary = NetworkModule.api.getDailySummary(deviceId)
                _dailySummary.value = summary
            }
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
