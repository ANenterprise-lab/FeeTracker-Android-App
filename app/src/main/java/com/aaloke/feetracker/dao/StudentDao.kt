package com.aaloke.feetracker.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.aaloke.feetracker.models.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Insert
    suspend fun insertStudent(student: Student)

    @Query("SELECT * FROM students WHERE batchId = :batchId")
    fun getStudentsForBatch(batchId: Int): Flow<List<Student>>

    @Delete
    suspend fun deleteStudent(student: Student)

}