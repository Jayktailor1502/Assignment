package com.example.smartexpensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val notes: String? = null,
    val receiptUri: String? = null,
    val synced: Boolean = false,
    val timestamp: Long, // exact time
    val date: Long
)
