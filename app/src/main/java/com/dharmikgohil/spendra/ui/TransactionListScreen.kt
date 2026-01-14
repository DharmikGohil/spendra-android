package com.dharmikgohil.spendra.ui

import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dharmikgohil.spendra.ApiClient
import com.dharmikgohil.spendra.ApiTransaction
import com.dharmikgohil.spendra.ui.components.SpendraCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.lifecycle.viewModelScope

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dharmikgohil.spendra.SpendraApplication
import com.dharmikgohil.spendra.data.local.TransactionDao
import com.dharmikgohil.spendra.data.local.TransactionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TransactionViewModel(private val dao: TransactionDao) : ViewModel() {
    
    val transactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // We don't need manual fetch anymore as we observe the DB
    // But we can keep a sync function if needed later
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SpendraApplication)
                TransactionViewModel(application.database.transactionDao())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onBackClick: () -> Unit,
    viewModel: TransactionViewModel = viewModel(factory = TransactionViewModel.Factory)
) {
    // We don't need deviceId anymore for fetching, as we observe local DB
    
    val transactions by viewModel.transactions.collectAsState()
    // isLoading is removed for now as Flow is instant (or we can add it back with Resource wrapper)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Transactions",
                        style = MaterialTheme.typography.displayMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Refresh not needed for local DB, but maybe for sync later
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (transactions.isEmpty()) {
             Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No transactions yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionCard(transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: TransactionEntity) {
    SpendraCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon Placeholder (Square with border)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = transaction.merchant.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = transaction.merchant,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Category Tag
                        if (transaction.category != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = transaction.category ?: "Uncategorized",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Text(
                            text = formatDate(transaction.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Right: Amount
            Text(
                text = "₹${transaction.amount}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (transaction.type == "CREDIT") 
                    MaterialTheme.colorScheme.tertiary // Green
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return try {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        "Unknown"
    }
}
