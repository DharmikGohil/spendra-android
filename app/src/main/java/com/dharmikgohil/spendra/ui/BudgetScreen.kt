package com.dharmikgohil.spendra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dharmikgohil.spendra.ui.theme.NeoBlack
import com.dharmikgohil.spendra.ui.theme.NeoWhite

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

import com.dharmikgohil.spendra.data.model.Suggestion
import com.dharmikgohil.spendra.ui.components.BudgetRecommendationCard

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    suggestions: List<Suggestion> = emptyList(),
    onBackClick: () -> Unit
) {
    val budgetStates by viewModel.budgetState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoWhite)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NeoBlack)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Smart Budgets",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NeoBlack
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recommendations Section
            if (suggestions.isNotEmpty()) {
                item {
                    Text(
                        text = "Recommended for You",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeoBlack,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(suggestions) { suggestion ->
                    if (suggestion.type == "BUDGET" && suggestion.data.categoryId != null && suggestion.data.suggestedAmount != null) {
                        BudgetRecommendationCard(
                            categoryName = suggestion.data.categoryName ?: "Unknown",
                            suggestedAmount = suggestion.data.suggestedAmount,
                            averageSpend = suggestion.data.averageSpend ?: 0.0,
                            onApply = {
                                viewModel.setBudget(suggestion.data.categoryId, suggestion.data.suggestedAmount)
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeoBlack,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Existing Budgets
            items(budgetStates) { state ->
                BudgetCard(
                    state = state,
                    onSave = { amount -> viewModel.setBudget(state.categoryId, amount) }
                )
            }
        }
    }
}

@Composable
fun BudgetCard(
    state: CategoryBudgetState,
    onSave: (Double) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf(state.budget.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.categoryName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoBlack
                )
                if (isEditing) {
                    Button(
                        onClick = {
                            onSave(amountText.toDoubleOrNull() ?: 0.0)
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeoBlack)
                    ) {
                        Text("Save", color = Color.White)
                    }
                } else {
                    TextButton(onClick = { isEditing = true }) {
                        Text("Edit", color = NeoBlack)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Spent: ₹${state.spent.toInt()}", color = Color.Gray)
                    Text("Limit: ₹${state.budget.toInt()}", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val progress = if (state.budget > 0) (state.spent / state.budget).toFloat() else 0f
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (progress > 1f) Color.Red else NeoBlack,
                    trackColor = Color.LightGray
                )
            }
        }
    }
}
