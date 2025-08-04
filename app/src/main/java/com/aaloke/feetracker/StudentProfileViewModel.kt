package com.aaloke.feetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.flatMapLatest

// No more factory needed!
class StudentProfileViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val repository: AppRepository

    // Get the student ID safely from the saved state handle
    private val studentIdFlow = savedStateHandle.getStateFlow("studentId", 0)

    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
    }

    // The payment history will automatically update if the student ID ever changes
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val paymentHistory: LiveData<List<Payment>> = studentIdFlow.flatMapLatest { studentId ->
        repository.getPaymentsForStudent(studentId)
    }.asLiveData()
}