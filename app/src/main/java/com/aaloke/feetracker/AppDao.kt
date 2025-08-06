package com.aaloke.feetracker

import androidx.room.*
import com.aaloke.feetracker.models.Batch
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AppDao {
    @Query("SELECT * FROM students WHERE (:className = 'All' OR className = :className) AND (name LIKE '%' || :searchQuery || '%') ORDER BY name ASC")
    fun getStudentsFiltered(className: String, searchQuery: String): Flow<List<Student>>
    @Query("SELECT name FROM students ORDER BY name ASC")
    fun getAllStudentNames(): Flow<List<String>>
    @Query("SELECT DISTINCT className FROM students ORDER BY className ASC")
    fun getDistinctClassNames(): Flow<List<String>>
    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY paymentDate DESC")
    fun getPaymentsForStudent(studentId: Int): Flow<List<Payment>>
    @Query("SELECT SUM(amount) FROM payments")
    fun getTotalCollected(): Flow<Double?>
    @Query("SELECT SUM(pendingBalance) FROM students")
    fun getTotalPending(): Flow<Double?>
    @Query("SELECT COUNT(*) FROM students WHERE feeStatus = 'Paid'")
    fun getPaidStudentCount(): Flow<Int>
    @Query("SELECT COUNT(*) FROM students WHERE feeStatus = 'Pending'")
    fun getPendingStudentCount(): Flow<Int>
    @Query("SELECT * FROM payments WHERE month = :month AND year = :year ORDER BY paymentDate DESC")
    fun getPaymentsForMonth(month: String, year: Int): Flow<List<Payment>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>
    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Double?>
    @Query("SELECT * FROM students")
    suspend fun getAllStudentsList(): List<Student>
    @Query("SELECT * FROM payments")
    suspend fun getAllPaymentsList(): List<Payment>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudents(students: List<Student>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPayments(payments: List<Payment>)
    @Transaction
    suspend fun clearAllTables() { clearStudents(); clearPayments(); clearExpenses() }
    @Query("DELETE FROM students")
    suspend fun clearStudents()
    @Query("DELETE FROM payments")
    suspend fun clearPayments()
    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()
    @Query("SELECT * FROM students WHERE feeStatus = 'Pending'")
    suspend fun getPendingStudentsList(): List<Student>
    @Query("SELECT * FROM students WHERE id IN (:studentIds)")
    suspend fun getStudentsByIds(studentIds: List<Int>): List<Student>
    @Query("DELETE FROM students WHERE id IN (:studentIds)")
    suspend fun deleteStudentsByIds(studentIds: List<Int>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)
    @Update
    suspend fun updateStudent(student: Student)
    @Delete
    suspend fun deleteStudent(student: Student)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)
    @Query("SELECT * FROM attendance_records WHERE studentId IN (:studentIds) AND date = :date")
    fun getAttendanceForStudentsOnDate(studentIds: List<Int>, date: Date): Flow<List<Attendance>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendanceRecords: List<Attendance>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: Batch)
    @Query("SELECT * FROM batches ORDER BY name ASC")
    fun getAllBatches(): Flow<List<Batch>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addStudentToBatch(crossRef: StudentBatchCrossRef)
    @Query("SELECT * FROM students INNER JOIN student_batch_cross_ref ON students.id = student_batch_cross_ref.id WHERE student_batch_cross_ref.batchId = :batchId")
    fun getStudentsInBatch(batchId: Int): Flow<List<Student>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: Fee)
    @Query("SELECT * FROM fees WHERE studentId = :studentId")
    fun getFeesForStudent(studentId: Int): Flow<List<Fee>>

}