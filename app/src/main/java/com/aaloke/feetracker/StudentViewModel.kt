package com.aaloke.feetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations // This will work after the fix
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val classNameFilter = MutableLiveData<String>("All")

    val allStudents: LiveData<List<Student>>
    val pendingStudents: LiveData<List<Student>>
    val paidStudents: LiveData<List<Student>>
    val distinctClassNames: LiveData<List<String>>

    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
        distinctClassNames = repository.distinctClassNames

        allStudents = Transformations.switchMap(classNameFilter) { className ->
            repository.getAllStudents(className)
        }

        pendingStudents = Transformations.switchMap(classNameFilter) { className ->
            repository.getStudentsByStatus("Pending", className)
        }

        paidStudents = Transformations.switchMap(classNameFilter) { className ->
            repository.getStudentsByStatus("Paid", className)
        }
    }

    fun setClassNameFilter(className: String) {
        classNameFilter.value = className
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
}