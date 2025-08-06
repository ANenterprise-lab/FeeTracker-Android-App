package com.aaloke.feetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData

class StudentProfileViewModel(application: Application, studentId: Int) : AndroidViewModel(application) {

    private val repository: AppRepository
    val paymentHistory: LiveData<List<Payment>>

    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
        paymentHistory = repository.getPaymentsForStudent(studentId).asLiveData()
    }
}

// This Factory is required to pass the studentId to the ViewModel
class StudentProfileViewModelFactory(private val application: Application, private val studentId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentProfileViewModel(application, studentId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}