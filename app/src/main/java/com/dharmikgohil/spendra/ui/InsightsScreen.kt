package com.dharmikgohil.spendra.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val previousSpendingState by viewModel.previousSpendingState.collectAsState()
    val totalSpent by viewModel.totalSpentThisMonth.collectAsState()
    
    // Calculate Health Score
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
            // 1. Financial Pulse
            item {
                FinancialPulseCard(score = healthScore, totalSpent = totalSpent)
            }

            // 2. Smart Category Spending (Horizontal)
            item {
                Text(
                    "Where your money went",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                spendingState?.let { response ->
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(response.data.sortedByDescending { it.total }) { item ->
                            // Find previous month data for this category
                            val prevItem = previousSpendingState?.data?.find { it.categoryId == item.categoryId }
                            SmartCategoryCard(
                                item = item, 
                                totalSpent = response.total,
                                prevTotal = prevItem?.total ?: 0.0
                            )
                        }
                    }
                } ?: Text("Analyzing spending...")
            }

            // 3. Intelligent Coach (Carousel)
            item {
                Text(
                    "Coach Insights",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                // Generate insights based on data
                val insights = remember(spendingState, previousSpendingState) {
                    generateInsights(spendingState?.data ?: emptyList(), previousSpendingState?.data ?: emptyList())
                }
                
                if (insights.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(insights) { insight ->
                            InsightCard(insight)
                        }
                    }
                } else {
                    Text("No insights yet. Keep spending to get tips!")
                }
            }

            // 4. Suggested Actions
            item {
                Text(
                    "Suggested Actions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionChip(text = "Set Budget", icon = "💰")
                    ActionChip(text = "Review Subs", icon = "📅")
                }
            }
        }
    }
}

@Composable
fun FinancialPulseCard(score: Int, totalSpent: Double) {
    val status = when {
        score > 80 -> "Excellent!"
        score > 50 -> "On Track"
        else -> "Needs Attention"
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
                    text = "Financial Pulse",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Based on this month's activity",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Progress Ring Visual
            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    strokeWidth = 8.dp,
                )
                CircularProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = if (score > 50) Color(0xFF4CAF50) else Color(0xFFF44336),
                    strokeWidth = 8.dp,
                )
                Text(
                    text = if (score > 80) "😎" else if (score > 50) "🙂" else "😬",
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

@Composable
fun SmartCategoryCard(item: SpendingItem, totalSpent: Double, prevTotal: Double) {
    val percent = if (totalSpent > 0) (item.total / totalSpent * 100).toInt() else 0
    val diff = item.total - prevTotal
    val trendText = if (prevTotal > 0) {
        if (diff > 0) "⬆️ ₹${diff.toInt()}" else "⬇️ ₹${kotlin.math.abs(diff.toInt())}"
    } else {
        "New"
    }
    val trendColor = if (diff > 0) Color(0xFFD32F2F) else Color(0xFF388E3C) // Red if spent more, Green if saved
    
    SpendraCard(
        modifier = Modifier
            .width(160.dp)
            .height(190.dp),
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
                    text = item.categoryName.take(2).uppercase(),
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
                if (prevTotal > 0) {
                    Text(
                        text = trendText,
                        style = MaterialTheme.typography.labelSmall,
                        color = trendColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
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

data class Insight(
    val title: String,
    val message: String,
    val type: InsightType,
    val action: String
)

enum class InsightType { ALERT, SAVING, HABIT }

@Composable
fun InsightCard(insight: Insight) {
    val bgColor = when (insight.type) {
        InsightType.ALERT -> Color(0xFFFFEBEE) // Red
        InsightType.SAVING -> Color(0xFFE8F5E9) // Green
        InsightType.HABIT -> Color(0xFFE3F2FD) // Blue
    }
    
    SpendraCard(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp),
        backgroundColor = bgColor
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (insight.type) {
                            InsightType.ALERT -> "⚠️"
                            InsightType.SAVING -> "🎉"
                            InsightType.HABIT -> "💡"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp).align(Alignment.End)
            ) {
                Text(text = insight.action, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun ActionChip(text: String, icon: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.clickable { /* TODO */ }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// Simple logic to generate insights
fun generateInsights(current: List<SpendingItem>, previous: List<SpendingItem>): List<Insight> {
    val insights = mutableListOf<Insight>()
    
    // 1. Check for high spending categories
    current.forEach { item ->
        val prevItem = previous.find { it.categoryId == item.categoryId }
        if (prevItem != null) {
            val increase = item.total - prevItem.total
            if (increase > 1000) { // Threshold
                insights.add(Insight(
                    title = "Spending Spike",
                    message = "${item.categoryName} is ₹${increase.toInt()} higher than last month.",
                    type = InsightType.ALERT,
                    action = "Check Transactions"
                ))
            } else if (increase < -500) {
                insights.add(Insight(
                    title = "Great Saving!",
                    message = "You spent ₹${kotlin.math.abs(increase.toInt())} less on ${item.categoryName}.",
                    type = InsightType.SAVING,
                    action = "Keep it up"
                ))
            }
        }
    }
    
    // Fallback insight
    if (insights.isEmpty() && current.isNotEmpty()) {
        insights.add(Insight(
            title = "Spending Habit",
            message = "Your top category is ${current.maxBy { it.total }.categoryName}.",
            type = InsightType.HABIT,
            action = "Set Budget"
        ))
    }
    
    return insights
}
