package com.example.cointrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget)


    @Query("SELECT * FROM budget_settings WHERE id = 1")
    fun getBudget(): Flow<Budget?>
}