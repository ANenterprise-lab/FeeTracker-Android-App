package com.aaloke.feetracker

import androidx.lifecycle.LiveData

class AppRepository(private val appDao: AppDao) {

    // Gets the unique class names for the dropdown
    val distinctClassNames: LiveData<List<String>> = appDao.getDistinctClassNames()

    // Gets a filtered list of all students
    fun getAllStudents(className: String): LiveData<List<Student>> {
        return appDao.getAllStudents(className)
    }

    // Gets a filtered list of students by their status
    fun getStudentsByStatus(status: String, className: String): LiveData<List<Student>> {
        return appDao.getStudentsByStatusAndClass(status, className)
    }

    // --- Unchanged Functions ---
    suspend fun insertStudent(student: Student) {
        appDao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) {
        appDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        appDao.deleteStudent(student)
    }
}