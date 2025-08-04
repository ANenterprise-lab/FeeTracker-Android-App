package com.aaloke.feetracker

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    fun getAllStudents(className: String, searchQuery: String): Flow<List<Student>> = appDao.getAllStudents(className, searchQuery)
    fun getStudentsByStatus(status: String, className: String, searchQuery: String): Flow<List<Student>> = appDao.getStudentsByStatusAndClass(status, className, searchQuery)
    fun getAllStudentNames(): Flow<List<String>> = appDao.getAllStudentNames()
    fun getTotalAmountByStatus(status: String): Flow<Double?> = appDao.getTotalAmountByStatus(status)
    fun getStudentCountByStatus(status: String): Flow<Int> = appDao.getStudentCountByStatus(status)
    fun getPaymentsForStudent(studentId: Int): Flow<List<Payment>> = appDao.getPaymentsForStudent(studentId)
    suspend fun getPendingStudentsList(): List<Student> = appDao.getPendingStudentsList()

    suspend fun insertStudent(student: Student) = appDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = appDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = appDao.deleteStudent(student)
    suspend fun insertPayment(payment: Payment) = appDao.insertPayment(payment)
    fun getPaymentsForMonth(month: String, year: Int): Flow<List<Payment>> {
        return appDao.getPaymentsForMonth(month, year)
    }
}