package com.aaloke.feetracker

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Queries for Lists
    @Query("SELECT * FROM students WHERE (:className = 'All' OR className = :className) AND (name LIKE '%' || :searchQuery || '%') ORDER BY name ASC LIMIT 200")
    fun getAllStudents(className: String, searchQuery: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE feeStatus = :status AND (:className = 'All' OR className = :className) AND (name LIKE '%' || :searchQuery || '%') ORDER BY name ASC")
    fun getStudentsByStatusAndClass(status: String, className: String, searchQuery: String): Flow<List<Student>>

    @Query("SELECT name FROM students ORDER BY name ASC")
    fun getAllStudentNames(): Flow<List<String>>

    // Queries for Dashboard
    @Query("SELECT SUM(feeAmount) FROM students WHERE feeStatus = :status")
    fun getTotalAmountByStatus(status: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM students WHERE feeStatus = :status")
    fun getStudentCountByStatus(status: String): Flow<Int>

    // Queries for Student Profile
    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY paymentDate DESC")
    fun getPaymentsForStudent(studentId: Int): Flow<List<Payment>>

    // Insert, Update, Delete
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)

    // Queries for Backup/Restore
    @Query("SELECT * FROM students")
    suspend fun getAllStudentsList(): List<Student>

    @Query("SELECT * FROM payments")
    suspend fun getAllPaymentsList(): List<Payment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudents(students: List<Student>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPayments(payments: List<Payment>)

    @Transaction
    suspend fun clearAllTables() {
        clearStudents()
        clearPayments()
    }

    @Query("DELETE FROM students")
    suspend fun clearStudents()

    @Query("DELETE FROM payments")
    suspend fun clearPayments()

    @Query("SELECT * FROM students WHERE feeStatus = 'Pending'")
    suspend fun getPendingStudentsList(): List<Student>
    @Query("SELECT * FROM payments WHERE month = :month AND year = :year ORDER BY paymentDate DESC")
    fun getPaymentsForMonth(month: String, year: Int): Flow<List<Payment>>
}