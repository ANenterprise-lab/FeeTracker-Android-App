package com.aaloke.feetracker.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aaloke.feetracker.dao.StudentDao
import com.aaloke.feetracker.models.Student
import kotlinx.coroutines.launch

class StudentsViewModel(
    private val studentDao: StudentDao,
    private val batchId: Int
) : ViewModel() {

    val studentsForBatch: LiveData<List<Student>> =
        studentDao.getStudentsForBatch(batchId).asLiveData()

    fun insert(student: Student) {
        viewModelScope.launch {
            studentDao.insertStudent(student)
        }
    }

    fun delete(student: Student) {
        viewModelScope.launch {
            studentDao.deleteStudent(student)
        }
    }
}

class StudentsViewModelFactory(
    private val studentDao: StudentDao,
    private val batchId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentsViewModel(studentDao, batchId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}