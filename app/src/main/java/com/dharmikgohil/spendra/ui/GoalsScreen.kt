package com.dharmikgohil.spendra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dharmikgohil.spendra.data.local.GoalEntity
import com.dharmikgohil.spendra.ui.theme.NeoBlack
import com.dharmikgohil.spendra.ui.theme.NeoWhite

import androidx.compose.material.icons.filled.ArrowBack

import com.dharmikgohil.spendra.data.model.Suggestion
import com.dharmikgohil.spendra.ui.components.SuggestedActionCard

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
    suggestions: List<Suggestion> = emptyList(),
    onBackClick: () -> Unit
) {
    val goals by viewModel.goals.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    // State for pre-filling dialog from suggestion
    var initialName by remember { mutableStateOf("") }
    var initialTarget by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoWhite)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NeoBlack)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Savings Goals",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoBlack
                )
            }
            FloatingActionButton(
                onClick = { 
                    initialName = ""
                    initialTarget = ""
                    showAddDialog = true 
                },
                containerColor = NeoBlack,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Suggestions Section
            if (suggestions.isNotEmpty()) {
                item {
                    Text(
                        text = "Suggested Goals",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeoBlack,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(suggestions) { suggestion ->
                    if (suggestion.type == "GOAL") {
                        SuggestedActionCard(
                            title = suggestion.title,
                            description = suggestion.description,
                            primaryActionLabel = "Start Saving",
                            onPrimaryClick = {
                                initialName = suggestion.data.suggestedName ?: "New Goal"
                                initialTarget = suggestion.data.suggestedAmount?.toString() ?: ""
                                showAddDialog = true
                            },
                            icon = { Icon(Icons.Default.Star, contentDescription = null) }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Goals",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeoBlack,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            items(goals) { goal ->
                GoalCard(
                    goal = goal,
                    onAddSavings = { amount -> viewModel.addSavings(goal.id, amount) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            initialName = initialName,
            initialTarget = initialTarget,
            onDismiss = { showAddDialog = false },
            onSave = { name, target ->
                viewModel.addGoal(name, target)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: GoalEntity,
    onAddSavings: (Double) -> Unit
) {
    var showSavingsDialog by remember { mutableStateOf(false) }

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
                Column {
                    Text(
                        text = "${goal.icon} ${goal.name}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoBlack
                    )
                    Text(
                        text = "Target: ₹${goal.targetAmount.toInt()}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Button(
                    onClick = { showSavingsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoBlack)
                ) {
                    Text("Save", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = NeoBlack,
                trackColor = Color.LightGray
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${goal.currentAmount.toInt()} saved",
                    fontSize = 12.sp,
                    color = NeoBlack
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoBlack
                )
            }
        }
    }

    if (showSavingsDialog) {
        AddSavingsDialog(
            goalName = goal.name,
            onDismiss = { showSavingsDialog = false },
            onSave = { amount ->
                onAddSavings(amount)
                showSavingsDialog = false
            }
        )
    }
}

@Composable
fun AddGoalDialog(
    initialName: String = "",
    initialTarget: String = "",
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var target by remember { mutableStateOf(initialTarget) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") }
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, target.toDoubleOrNull() ?: 0.0) },
                colors = ButtonDefaults.buttonColors(containerColor = NeoBlack)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NeoBlack)
            }
        }
    )
}

@Composable
fun AddSavingsDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Savings to $goalName") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(amount.toDoubleOrNull() ?: 0.0) },
                colors = ButtonDefaults.buttonColors(containerColor = NeoBlack)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NeoBlack)
            }
        }
    )
}
