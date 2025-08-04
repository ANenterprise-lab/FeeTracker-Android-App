package com.aaloke.feetracker

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AppDao {
    // --- NEW & UPDATED QUERIES ---

    // Gets a list of unique class names for the spinner
    @Query("SELECT DISTINCT className FROM students ORDER BY className ASC")
    fun getDistinctClassNames(): LiveData<List<String>>

    // Gets ALL students, optionally filtered by class
    @Query("SELECT * FROM students WHERE (:className = 'All' OR className = :className) ORDER BY name ASC")
    fun getAllStudents(className: String): LiveData<List<Student>>

    // Gets students by STATUS, optionally filtered by class
    @Query("SELECT * FROM students WHERE feeStatus = :status AND (:className = 'All' OR className = :className) ORDER BY name ASC")
    fun getStudentsByStatusAndClass(status: String, className: String): LiveData<List<Student>>

    // ---UNCHANGED FUNCTIONS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: Fee)

    @Query("SELECT * FROM fees WHERE studentId = :studentId")
    fun getFeesForStudent(studentId: Int): LiveData<List<Fee>>
}