package com.aaloke.feetracker

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE // If a student is deleted, their attendance records are also deleted
        )
    ],
    // This index makes looking up records faster
    indices = [Index(value = ["studentId", "date"], unique = true)]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: Int,
    val date: Date,
    val status: String // "Present" or "Absent"
)