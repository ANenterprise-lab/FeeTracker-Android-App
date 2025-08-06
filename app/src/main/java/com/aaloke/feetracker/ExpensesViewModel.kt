package com.aaloke.feetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Date

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    val allExpenses: LiveData<List<Expense>>

    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
        allExpenses = repository.getAllExpenses().asLiveData()
    }

    fun addExpense(description: String, amount: Double) = viewModelScope.launch {
        val newExpense = Expense(description = description, amount = amount, date = Date())
        repository.insertExpense(newExpense)
    }
}