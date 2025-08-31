package com.example.smartexpensetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.smartexpensetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Query("SELECT * FROM expenses WHERE date = :dayStart ORDER BY timestamp DESC")
    fun observeByDate(dayStart: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<Expense>>

    @Query("SELECT COUNT(*) FROM expenses WHERE title = :title AND amount = :amount AND abs(timestamp - :timestamp) < :windowMs")
    suspend fun countDuplicates(
        title: String,
        amount: Double,
        timestamp: Long,
        windowMs: Long = 5 * 60 * 1000
    ): Int

    @Query("SELECT SUM(amount) FROM expenses WHERE date = :dayStart")
    suspend fun totalForDay(dayStart: Long): Double?

    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    suspend fun range(from: Long, to: Long): List<Expense>
}
