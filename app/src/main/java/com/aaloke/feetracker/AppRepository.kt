package com.aaloke.feetracker

import kotlinx.coroutines.flow.Flow
import java.util.Date

class AppRepository(private val appDao: AppDao) {
    fun getStudentsFiltered(className: String, searchQuery: String): Flow<List<Student>> = appDao.getStudentsFiltered(className, searchQuery)
    fun getAllStudentNames(): Flow<List<String>> = appDao.getAllStudentNames()
    fun getDistinctClassNames(): Flow<List<String>> = appDao.getDistinctClassNames()
    fun getPaymentsForStudent(studentId: Int): Flow<List<Payment>> = appDao.getPaymentsForStudent(studentId)
    fun getTotalCollected(): Flow<Double?> = appDao.getTotalCollected()
    fun getTotalPending(): Flow<Double?> = appDao.getTotalPending()
    fun getPaidStudentCount(): Flow<Int> = appDao.getPaidStudentCount()
    fun getPendingStudentCount(): Flow<Int> = appDao.getPendingStudentCount()
    fun getPaymentsForMonth(month: String, year: Int): Flow<List<Payment>> = appDao.getPaymentsForMonth(month, year)
    suspend fun insertExpense(expense: Expense) = appDao.insertExpense(expense)
    fun getAllExpenses(): Flow<List<Expense>> = appDao.getAllExpenses()
    fun getTotalExpenses(): Flow<Double?> = appDao.getTotalExpenses()
    suspend fun getPendingStudentsList(): List<Student> = appDao.getPendingStudentsList()
    suspend fun insertStudent(student: Student) = appDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = appDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = appDao.deleteStudent(student)
    suspend fun insertPayment(payment: Payment) = appDao.insertPayment(payment)
    suspend fun getStudentsByIds(studentIds: List<Int>): List<Student> = appDao.getStudentsByIds(studentIds)
    suspend fun deleteStudentsByIds(studentIds: List<Int>) = appDao.deleteStudentsByIds(studentIds)
    suspend fun getAllStudentsList(): List<Student> = appDao.getAllStudentsList()
    suspend fun getAllPaymentsList(): List<Payment> = appDao.getAllPaymentsList()
    suspend fun clearAllTables() = appDao.clearAllTables()
    suspend fun insertAllStudents(students: List<Student>) = appDao.insertAllStudents(students)
    suspend fun insertAllPayments(payments: List<Payment>) = appDao.insertAllPayments(payments)
    fun getAttendanceForStudentsOnDate(studentIds: List<Int>, date: Date): Flow<List<Attendance>> = appDao.getAttendanceForStudentsOnDate(studentIds, date)
    suspend fun insertOrUpdateAttendance(attendanceRecords: List<Attendance>) = appDao.insertOrUpdateAttendance(attendanceRecords)
}