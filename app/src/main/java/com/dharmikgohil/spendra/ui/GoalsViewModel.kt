package com.dharmikgohil.spendra.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dharmikgohil.spendra.data.local.GoalDao
import com.dharmikgohil.spendra.data.local.GoalEntity
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

class GoalsViewModel(
    private val goalDao: GoalDao,
    private val userId: String = "user_1" // TODO: Get actual userId
) : ViewModel() {

    private val _goals = MutableStateFlow<List<GoalEntity>>(emptyList())
    val goals: StateFlow<List<GoalEntity>> = _goals.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch {
            _goals.value = goalDao.getGoals(userId)
        }
    }

    fun addGoal(name: String, targetAmount: Double, icon: String = "🎯") {
        viewModelScope.launch {
            val goal = GoalEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                deadline = null,
                icon = icon,
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            goalDao.insertGoal(goal)
            loadGoals()
        }
    }

    fun addSavings(goalId: String, amount: Double) {
        viewModelScope.launch {
            val goal = goalDao.getGoalById(goalId) ?: return@launch
            val updatedGoal = goal.copy(
                currentAmount = goal.currentAmount + amount,
                updatedAt = System.currentTimeMillis()
            )
            goalDao.insertGoal(updatedGoal)
            loadGoals()
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as SpendraApplication)
                val database = AppDatabase.getDatabase(application)
                GoalsViewModel(
                    goalDao = database.goalDao()
                )
            }
        }
    }
}
