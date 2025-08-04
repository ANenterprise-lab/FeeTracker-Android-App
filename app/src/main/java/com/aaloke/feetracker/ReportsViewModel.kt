package com.aaloke.feetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val _selectedMonth = MutableLiveData<String>()
    private val _selectedYear = MutableLiveData<Int>()

    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
    }

    // This is a bit complex, but it creates a LiveData that automatically
    // re-fetches the payment list whenever the selected month or year changes.
    val monthlyPayments: LiveData<List<Payment>> = _selectedMonth.switchMap { month ->
        _selectedYear.switchMap { year ->
            repository.getPaymentsForMonth(month, year).asLiveData()
        }
    }

    // Functions to be called from the UI when a new month or year is selected
    fun setMonth(month: String) {
        _selectedMonth.value = month
    }

    fun setYear(year: Int) {
        _selectedYear.value = year
    }
}