package com.aaloke.feetracker.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import com.aaloke.feetracker.models.Batch // <-- The fix is here

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = Batch::class,
            parentColumns = ["batchId"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val studentId: Int = 0,
    val batchId: Int,
    val name: String,
    val contactNumber: String,
    val profilePicturePath: String?,
    val notes: String?
)