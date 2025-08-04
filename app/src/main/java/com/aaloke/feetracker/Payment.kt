package com.aaloke.feetracker

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE // If a student is deleted, their payments are also deleted
        )
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: Int, // Links this payment to a student
    val amount: Double,
    val paymentDate: Date,
    val month: String, // e.g., "August"
    val year: Int      // e.g., 2025
)