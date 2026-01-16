package com.dharmikgohil.spendra.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: Long?,
    val icon: String?,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
