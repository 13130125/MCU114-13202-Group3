package com.example.cointrack.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import com.example.cointrack.data.Budget
import com.example.cointrack.data.BudgetDao


class TransactionRepository(private val transactionDao: TransactionDao, private val budgetDao: BudgetDao) {

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()


    val currentBudget: Flow<Budget?> = budgetDao.getBudget()
    suspend fun saveBudget(budget: Budget) {
        budgetDao.insert(budget)
    }

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }


    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    fun getTodayTotalExpense(): Flow<Double?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        return transactionDao.getTotalExpenseSince(startOfDay)
    }


    fun getMonthTransactionFlows(): Pair<Flow<Double?>, Flow<Double?>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = calendar.timeInMillis

        val totalIncomeFlow = transactionDao.getTotalIncomeSince(startOfMonth)
        val totalExpenseFlow = transactionDao.getTotalExpenseOfMonth(startOfMonth)

        return Pair(totalIncomeFlow, totalExpenseFlow)
    }
}