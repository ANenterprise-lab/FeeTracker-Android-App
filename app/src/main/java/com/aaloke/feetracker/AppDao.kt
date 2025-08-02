package com.aaloke.feetracker

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Student Queries ---
    @Insert
    suspend fun insertStudent(student: Student)

    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>
    @Update
    suspend fun updateStudent(student: Student) // <-- Add this line

    // --- Fee Queries ---
    @Insert
    suspend fun insertFee(fee: Fee)

    @Query("SELECT * FROM fees WHERE studentId = :studentId")
    fun getFeesForStudent(studentId: Int): Flow<List<Fee>>

    // In AppDao.kt, inside the interface

    @Query("SELECT * FROM fees")
    fun getAllFees(): Flow<List<Fee>> // <-- Add this new function

    @Delete
    suspend fun deleteStudent(student: Student) // <-- ADD THIS FUNCTION
}