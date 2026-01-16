package com.dharmikgohil.spendra

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dharmikgohil.spendra.ui.theme.SpendraTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import com.dharmikgohil.spendra.ui.BudgetScreen
import com.dharmikgohil.spendra.ui.BudgetViewModel
import com.dharmikgohil.spendra.ui.GoalsScreen
import com.dharmikgohil.spendra.ui.GoalsViewModel
import com.dharmikgohil.spendra.ui.HomeViewModel
import com.dharmikgohil.spendra.ui.InsightsScreen
import com.dharmikgohil.spendra.ui.TransactionListScreen
import com.dharmikgohil.spendra.ui.components.AlertBanner
import com.dharmikgohil.spendra.ui.components.SpendraButton
import com.dharmikgohil.spendra.ui.components.SpendraCard
import com.dharmikgohil.spendra.ui.components.SuggestedActionCard

class MainActivity : ComponentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request SMS permissions
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            )
        )
        
        setContent {
            SpendraTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    var currentScreen by remember { mutableStateOf("home") }

    // Navigation Logic
    if (currentScreen == "transactions") {
        TransactionListScreen(onBackClick = { currentScreen = "home" })
        return
    }
    // Insights are now on Home
    // if (currentScreen == "insights") { ... }
    if (currentScreen == "budgets") {
        val budgetViewModel: BudgetViewModel = viewModel(factory = BudgetViewModel.Factory)
        val suggestions by viewModel.suggestions.collectAsState()
        val budgetSuggestions = suggestions.filter { it.type == "BUDGET" }
        BudgetScreen(
            viewModel = budgetViewModel, 
            suggestions = budgetSuggestions,
            onBackClick = { currentScreen = "home" }
        )
        return
    }
    if (currentScreen == "goals") {
        val goalsViewModel: GoalsViewModel = viewModel(factory = GoalsViewModel.Factory)
        val suggestions by viewModel.suggestions.collectAsState()
        val goalSuggestions = suggestions.filter { it.type == "GOAL" }
        GoalsScreen(
            viewModel = goalsViewModel, 
            suggestions = goalSuggestions,
            onBackClick = { currentScreen = "home" }
        )
        return
    }

    val dailySummary by viewModel.dailySummary.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Spendra",
                        style = MaterialTheme.typography.displayMedium
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add Transaction */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Good Morning, Dharmik",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your financial weather is clear ☀️",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // Safe to Spend Hero
            item {
                dailySummary?.let { summary ->
                    SpendraCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(12.dp),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = if (summary.safeToSpend > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {}
                                Text(
                                    text = "Safe to Spend Today",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            
                            Text(
                                text = "₹ ${String.format("%.0f", summary.safeToSpend)}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Spent: ₹${String.format("%.0f", summary.totalSpentToday)}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "${summary.daysRemaining} days left",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                } ?: run {
                    // Loading State
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Alerts
            item {
                dailySummary?.let { summary ->
                    if (summary.safeToSpend <= 0) {
                        com.dharmikgohil.spendra.ui.components.AlertBanner(
                            message = "You've exceeded your daily limit!",
                            isError = true
                        )
                    } else if (summary.safeToSpend < 500) {
                        com.dharmikgohil.spendra.ui.components.AlertBanner(
                            message = "Tight budget today. Spend wisely.",
                            isError = false
                        )
                    }
                }
            }

            // Suggestions Feed
            items(suggestions) { suggestion ->
                com.dharmikgohil.spendra.ui.components.SuggestedActionCard(
                    title = suggestion.title,
                    description = suggestion.description,
                    primaryActionLabel = if (suggestion.type == "BUDGET") "Review Budget" else "Start Saving",
                    onPrimaryClick = {
                        if (suggestion.type == "BUDGET") currentScreen = "budgets"
                        else currentScreen = "goals"
                    },
                    icon = {
                        if (suggestion.type == "BUDGET") {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        } else {
                            Icon(Icons.Default.Star, contentDescription = null)
                        }
                    }
                )
            }

            // Quick Actions (Navigation)
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SpendraButton(
                            onClick = { currentScreen = "transactions" },
                            text = "Transactions",
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                        SpendraButton(
                            onClick = { currentScreen = "budgets" },
                            text = "Budgets",
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SpendraButton(
                            onClick = { currentScreen = "goals" },
                            text = "Goals",
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Sync Button
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val scope = androidx.compose.runtime.rememberCoroutineScope()
                        SpendraButton(
                            onClick = { 
                                val deviceId = android.provider.Settings.Secure.getString(
                                    context.contentResolver,
                                    android.provider.Settings.Secure.ANDROID_ID
                                )
                                android.widget.Toast.makeText(context, "Syncing...", android.widget.Toast.LENGTH_SHORT).show()
                                scope.launch {
                                    viewModel.syncTransactions(deviceId)
                                    android.widget.Toast.makeText(context, "Sync completed", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            text = "Sync Now",
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}