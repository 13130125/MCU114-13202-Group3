package com.example.cointrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "budget_settings")
data class Budget(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val amount: Double
)