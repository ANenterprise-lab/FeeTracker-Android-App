package com.aaloke.feetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val classNameFilter = MutableStateFlow("All")
    private val searchQuery = MutableStateFlow("")

    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
    }

    val allStudentNames: LiveData<List<String>> = repository.getAllStudentNames().asLiveData()

    private val filtersFlow = combine(classNameFilter, searchQuery) { className, query ->
        Pair(className, query)
    }

    val allStudents: LiveData<List<Student>> = filtersFlow.flatMapLatest { (className, query) ->
        repository.getAllStudents(className, query)
    }.asLiveData()
    val pendingStudents: LiveData<List<Student>> = filtersFlow.flatMapLatest { (className, query) ->
        repository.getStudentsByStatus("Pending", className, query)
    }.asLiveData()
    val paidStudents: LiveData<List<Student>> = filtersFlow.flatMapLatest { (className, query) ->
        repository.getStudentsByStatus("Paid", className, query)
    }.asLiveData()

    val totalCollected: LiveData<Double?> = repository.getTotalAmountByStatus("Paid").asLiveData()
    val totalPending: LiveData<Double?> = repository.getTotalAmountByStatus("Pending").asLiveData()
    val paidStudentCount: LiveData<Int> = repository.getStudentCountByStatus("Paid").asLiveData()
    val pendingStudentCount: LiveData<Int> = repository.getStudentCountByStatus("Pending").asLiveData()

    suspend fun getPendingStudentsList(): List<Student> {
        return repository.getPendingStudentsList()
    }

    fun setClassNameFilter(className: String) {
        classNameFilter.value = className
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun insertStudent(student: Student) = viewModelScope.launch {
        repository.insertStudent(student)
    }

    fun updateStudent(student: Student) = viewModelScope.launch {
        repository.updateStudent(student)
    }

    fun deleteStudent(student: Student) = viewModelScope.launch {
        repository.deleteStudent(student)
    }

    fun markFeeAsPaid(student: Student) = viewModelScope.launch {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val currentMonth = monthFormat.format(calendar.time)
        val currentYear = calendar.get(Calendar.YEAR)

        val payment = Payment(
            studentId = student.id,
            amount = student.feeAmount,
            paymentDate = Date(),
            month = currentMonth,
            year = currentYear
        )
        repository.insertPayment(payment)
        updateStudent(student.copy(feeStatus = "Paid"))
    }
    fun recordPartialPayment(student: Student, amountPaid: Double) = viewModelScope.launch {
        // 1. Calculate the new balance
        val newBalance = student.pendingBalance - amountPaid

        // 2. Determine the new fee status
        val newStatus = if (newBalance <= 0) "Paid" else "Pending"

        // 3. Create a payment record for the amount that was paid
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val payment = Payment(
            studentId = student.id,
            amount = amountPaid,
            paymentDate = Date(),
            month = monthFormat.format(calendar.time),
            year = calendar.get(Calendar.YEAR)
        )
        repository.insertPayment(payment)

        // 4. Update the student with the new balance and status
        updateStudent(student.copy(
            pendingBalance = newBalance,
            feeStatus = newStatus
        ))
    }
}