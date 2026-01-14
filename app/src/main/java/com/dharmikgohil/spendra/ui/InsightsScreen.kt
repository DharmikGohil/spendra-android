package com.dharmikgohil.spendra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dharmikgohil.spendra.ui.components.SpendraCard
import com.dharmikgohil.spendra.SpendingItem
import com.dharmikgohil.spendra.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val context = LocalContext.current
    val spendingState by viewModel.spendingState.collectAsState()
    val safeToSpend by viewModel.safeToSpend.collectAsState()
    val totalSpent by viewModel.totalSpentThisMonth.collectAsState()
    
    // Calculate Health Score (Simple logic for MVP)
    // 30000 is hardcoded budget in ViewModel. 
    // Score = 100 - (% of budget spent). If spent > budget, score is 0.
    val budget = 30000.0
    val healthScore = remember(totalSpent) {
        val percentSpent = (totalSpent / budget) * 100
        (100 - percentSpent).coerceIn(0.0, 100.0).toInt()
    }

    LaunchedEffect(Unit) {
        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        viewModel.getSpendingInsights(deviceId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Financial Coach",
                        style = MaterialTheme.typography.displayMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Financial Health Summary
            item {
                HealthSummaryCard(score = healthScore, totalSpent = totalSpent)
            }

            // 2. Smart Category Spending
            item {
                Text(
                    "Top Spending",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                spendingState?.let { response ->
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(response.data.sortedByDescending { it.total }) { item ->
                            CategoryInsightCard(item = item, totalSpent = totalSpent)
                        }
                    }
                } ?: Text("Loading categories...")
            }

            // 3. Trend & Behavior Changes
            item {
                Text(
                    "Trends",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                // Mock trends for MVP
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TrendInsightCard(
                        title = "Food Delivery",
                        change = "+28% vs last month",
                        isPositive = false // Spending went up, so negative impact
                    )
                    TrendInsightCard(
                        title = "Groceries",
                        change = "-₹400 saved this week",
                        isPositive = true
                    )
                }
            }

            // 4. AI Insights
            item {
                Text(
                    "Coach Says",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                PersonalizedInsightCard(
                    message = "You ordered food 5 times this week.",
                    impact = "Reducing to 3 could save ~₹800/month.",
                    actionLabel = "Set Food Budget"
                )
            }

            // 5. Quick Actions
            item {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionButton(text = "Set Budget")
                    ActionButton(text = "Alerts")
                }
            }
        }
    }
}

@Composable
fun HealthSummaryCard(score: Int, totalSpent: Double) {
    val status = when {
        score > 80 -> "Excellent!"
        score > 50 -> "On Track"
        else -> "Spending High"
    }
    
    val color = when {
        score > 80 -> Color(0xFFE8F5E9) // Light Green
        score > 50 -> Color(0xFFFFF8E1) // Light Yellow
        else -> Color(0xFFFFEBEE) // Light Red
    }

    SpendraCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = color
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Health Score",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "$score/100",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Simple visual for score
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (score > 50) "😊" else "😬",
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }
    }
}

@Composable
fun CategoryInsightCard(item: SpendingItem, totalSpent: Double) {
    val percent = if (totalSpent > 0) (item.total / totalSpent * 100).toInt() else 0
    
    SpendraCard(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = item.categoryName.take(2).uppercase(), // Placeholder icon
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = item.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = "₹${item.total.toInt()}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column {
                Text(
                    text = "$percent% of total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                LinearProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
fun TrendInsightCard(title: String, change: String, isPositive: Boolean) {
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
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = change,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
            Text(
                text = if (isPositive) "📉" else "📈",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun PersonalizedInsightCard(message: String, impact: String, actionLabel: String) {
    SpendraCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFFE3F2FD) // Light Blue
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Insight",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = impact,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun ActionButton(text: String) {
    OutlinedButton(
        onClick = { /* TODO */ },
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text)
    }
}
