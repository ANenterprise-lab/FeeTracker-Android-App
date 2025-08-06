package com.aaloke.feetracker

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val classNameFilter = MutableStateFlow("All")
    private val searchQuery = MutableStateFlow("")
    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
    }
    private val filtersFlow = combine(classNameFilter, searchQuery) { className, query -> Pair(className, query) }
    private val filteredStudents: LiveData<List<Student>> = filtersFlow.flatMapLatest { (className, query) ->
        repository.getStudentsFiltered(className, query)
    }.asLiveData()
    val allStudents: LiveData<List<Student>> = filteredStudents
    val pendingStudents: LiveData<List<Student>> = filteredStudents.map { list -> list.filter { it.feeStatus == "Pending" } }
    val paidStudents: LiveData<List<Student>> = filteredStudents.map { list -> list.filter { it.feeStatus == "Paid" } }
    val allStudentNames: LiveData<List<String>> = repository.getAllStudentNames().asLiveData()
    val distinctClassNames: LiveData<List<String>> = repository.getDistinctClassNames().asLiveData()
    val totalCollected: LiveData<Double?> = repository.getTotalCollected().asLiveData()
    val totalPending: LiveData<Double?> = repository.getTotalPending().asLiveData()
    val paidStudentCount: LiveData<Int> = repository.getPaidStudentCount().asLiveData()
    val pendingStudentCount: LiveData<Int> = repository.getPendingStudentCount().asLiveData()
    val totalExpenses: LiveData<Double?> = repository.getTotalExpenses().asLiveData()
    suspend fun getPendingStudentsList(): List<Student> = repository.getPendingStudentsList()
    fun setClassNameFilter(className: String) { classNameFilter.value = className }
    fun setSearchQuery(query: String) { searchQuery.value = query }
    fun insertStudent(student: Student) = viewModelScope.launch { repository.insertStudent(student) }
    fun updateStudent(student: Student) = viewModelScope.launch { repository.updateStudent(student) }
    fun deleteStudent(student: Student) = viewModelScope.launch { repository.deleteStudent(student) }
    fun deleteSelectedStudents(studentIds: List<Int>) = viewModelScope.launch { repository.deleteStudentsByIds(studentIds) }
    fun markSelectedAsPaid(studentIds: List<Int>) = viewModelScope.launch {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val studentsToUpdate = repository.getStudentsByIds(studentIds)
        studentsToUpdate.forEach { student ->
            if (student.pendingBalance > 0) {
                val payment = Payment(studentId = student.id, amount = student.pendingBalance, paymentDate = Date(), month = monthFormat.format(calendar.time), year = calendar.get(Calendar.YEAR))
                repository.insertPayment(payment)
                repository.updateStudent(student.copy(feeStatus = "Paid", pendingBalance = 0.0))
            }
        }
    }
    fun recordPartialPayment(student: Student, amountPaid: Double) = viewModelScope.launch {
        val newBalance = student.pendingBalance - amountPaid
        val newStatus = if (newBalance <= 0) "Paid" else "Pending"
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val payment = Payment(studentId = student.id, amount = amountPaid, paymentDate = Date(), month = monthFormat.format(calendar.time), year = calendar.get(Calendar.YEAR))
        repository.insertPayment(payment)
        updateStudent(student.copy(pendingBalance = newBalance, feeStatus = newStatus))
    }
}