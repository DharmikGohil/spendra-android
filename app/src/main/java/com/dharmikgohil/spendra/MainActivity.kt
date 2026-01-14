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
import com.dharmikgohil.spendra.ui.TransactionListScreen
import com.dharmikgohil.spendra.ui.components.SpendraButton
import com.dharmikgohil.spendra.ui.components.SpendraCard
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dharmikgohil.spendra.ui.HomeViewModel
import com.dharmikgohil.spendra.ui.InsightsScreen

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

    if (currentScreen == "transactions") {
        TransactionListScreen(onBackClick = { currentScreen = "home" })
        return
    }

    if (currentScreen == "insights") {
        InsightsScreen(onBackClick = { currentScreen = "home" })
        return
    }

    val safeToSpend by viewModel.safeToSpend.collectAsState()
    val totalSpent by viewModel.totalSpentThisMonth.collectAsState()

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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Hero Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
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

            // Status Card (Neo-Brutalist)
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
                        // Simple circle indicator
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = if (safeToSpend > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {}
                        Text(
                            text = "Safe to Spend",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Text(
                        text = "₹ ${String.format("%.0f", safeToSpend)}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    )
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.dp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Spent: ₹${String.format("%.0f", totalSpent)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Budget: ₹30,000",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SpendraButton(
                        onClick = { currentScreen = "transactions" },
                        text = "Transactions",
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                    
                    SpendraButton(
                        onClick = { currentScreen = "insights" },
                        text = "Insights",
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                }
                
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
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}