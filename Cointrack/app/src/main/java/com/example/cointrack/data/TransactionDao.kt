package com.example.cointrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow


@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction)


    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>


    @Query("SELECT SUM(amount) FROM transactions WHERE isExpense = 1 AND timestamp >= :startOfDay")
    fun getTotalExpenseSince(startOfDay: Long): Flow<Double?>


    @Query("SELECT SUM(amount) FROM transactions WHERE isExpense = 0 AND timestamp >= :startOfMonth")
    fun getTotalIncomeSince(startOfMonth: Long): Flow<Double?>


    @Query("SELECT SUM(amount) FROM transactions WHERE isExpense = 1 AND timestamp >= :startOfMonth")
    fun getTotalExpenseOfMonth(startOfMonth: Long): Flow<Double?>
}