package com.example.smartexpensetracker.ui.vm

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensetracker.data.local.AppDatabase
import com.example.smartexpensetracker.data.model.Expense
import com.example.smartexpensetracker.data.repository.ExpenseRepository
import com.example.smartexpensetracker.utils.startOfDayMillis
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Collections.emptyList

open class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).expenseDao()
    private val repo = ExpenseRepository(dao)

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis().startOfDayMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    val expensesForDate: StateFlow<List<Expense>> = selectedDate
        .flatMapLatest { dayMillis -> repo.observeByDate(dayMillis) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _toast = MutableSharedFlow<String>()
    val toast = _toast.asSharedFlow()

    init {
        viewModelScope.launch {
            repo.syncPending()
        }
    }

    fun selectDate(millis: Long) {
        _selectedDate.value = millis
        Log.i("TAG", "ExpenseViewModel: ${selectedDate.value}")
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            if (expense.title.isEmpty() || expense.amount <= 0.0) {
                _toast.emit("Validation failed: title and amount required")
                return@launch
            }

            val isDup = repo.isDuplicate(expense)
            if (isDup) {
                _toast.emit("Possible duplicate detected — not added")
                return@launch
            }

            repo.insert(expense)
            _toast.emit("Expense added")
        }
    }

    suspend fun totalForSelectedDate(): Double = repo.totalForDay(_selectedDate.value)

    // Export last 7 days as CSV (suspend)
    suspend fun exportLast7DaysCsvFile(context: Context): File {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000
        val list = repo.range(sevenDaysAgo, now)

        val csv = StringBuilder()
        csv.append("id,title,amount,category,notes,timestamp\n")
        list.forEach { e ->
            val line = listOf(
                e.id,
                e.title,
                e.amount,
                e.category,
                (e.notes ?: ""),
                e.timestamp
            ).joinToString(",")
            csv.append(line).append("\n")
        }

        val file = File(context.cacheDir, "expenses_last7days.csv")
        file.writeText(csv.toString())
        return file
    }
}