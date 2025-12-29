package com.example.cointrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.cointrack.data.AppDatabase
import com.example.cointrack.data.Budget
import com.example.cointrack.data.Transaction
import com.example.cointrack.data.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale


class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository


    private val searchQuery = MutableStateFlow("")


    val filteredTransactions: LiveData<List<Transaction>>

    val allTransactions: LiveData<List<Transaction>>
    val todayTotalExpense: LiveData<Double>
    val currentBudget: LiveData<Budget?>
    val monthRemaining: LiveData<Double>

    init {
        val database = AppDatabase.getDatabase(application)
        val transactionDao = database.transactionDao()
        val budgetDao = database.budgetDao()

        repository = TransactionRepository(transactionDao, budgetDao)

        currentBudget = repository.currentBudget.asLiveData()

        allTransactions = liveData {
            emitSource(repository.allTransactions.asLiveData())
        }

        todayTotalExpense = liveData {
            emit(0.0)
            emitSource(repository.getTodayTotalExpense().map { it ?: 0.0 }.asLiveData())
        }

        monthRemaining = liveData {
            emit(0.0)
            emitSource(calculateMonthRemaining().asLiveData())
        }


        filteredTransactions = searchQuery.flatMapLatest { query ->
            if (query.isBlank()) {

                repository.allTransactions
            } else {

                repository.allTransactions.map { transactions ->
                    val normalizedQuery = query.trim().lowercase(Locale.getDefault())
                    transactions.filter {

                        it.category.lowercase(Locale.getDefault()).contains(normalizedQuery) ||

                                String.format(Locale.getDefault(), "%.2f", it.amount).contains(normalizedQuery)
                    }
                }
            }
        }.asLiveData()
    }


    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }


    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }


    private fun calculateMonthRemaining(): Flow<Double> {
        val (totalIncomeFlow, totalExpenseFlow) = repository.getMonthTransactionFlows()
        val budgetFlow = repository.currentBudget.map { it?.amount ?: 0.0 }

        return combine(totalIncomeFlow, totalExpenseFlow, budgetFlow) { income, expense, budget ->
            val totalIncome = income ?: 0.0
            val totalExpense = expense ?: 0.0

            val remaining = budget + totalIncome - totalExpense
            remaining
        }
    }

    fun getMonthlyCategoryExpense(): LiveData<Map<String, Double>> = liveData {
        val startOfMonth = getStartOfMonthTimestamp()

        val monthlyExpensesFlow = repository.allTransactions
            .map { transactions ->
                transactions.filter {
                    it.isExpense && it.timestamp >= startOfMonth
                }
            }

        emitSource(monthlyExpensesFlow.map { expenses ->
            expenses.groupBy { it.category }
                .mapValues { entry ->
                    entry.value.sumOf { it.amount }
                }
        }.asLiveData())
    }


    fun saveBudget(amount: Double) = viewModelScope.launch {
        repository.saveBudget(Budget(amount = amount))
    }


    fun addTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }


    private fun getStartOfMonthTimestamp(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}