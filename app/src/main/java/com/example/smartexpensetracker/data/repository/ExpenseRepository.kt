package com.example.smartexpensetracker.data.repository

import com.example.smartexpensetracker.data.local.ExpenseDao
import com.example.smartexpensetracker.data.model.Expense
import kotlinx.coroutines.delay

class ExpenseRepository(private val dao: ExpenseDao) {
    fun observeByDate(dayMillis: Long) = dao.observeByDate(dayMillis)
    fun observeAll() = dao.observeAll()

    suspend fun insert(expense: Expense): Long {
        return dao.insert(expense)
    }

    suspend fun isDuplicate(expense: Expense): Boolean {
        val count = dao.countDuplicates(expense.title, expense.amount, expense.timestamp)
        return count > 0
    }

    suspend fun totalForDay(dayMillis: Long) = dao.totalForDay(dayMillis) ?: 0.0

    // Mock offline-first sync
    suspend fun syncPending() {
        // pretend to sync: fetch unsynced expenses and mark them synced
        delay(500)
        // this is a mock — real implementation would call API and update DB
    }

    suspend fun range(from: Long, to: Long) = dao.range(from, to)
}