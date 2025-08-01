package com.aaloke.feetracker

// In Fee.kt
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fees")
data class Fee(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: Int,
    val month: String,
    val year: Int,
    val amount: Double,
    val isPaid: Boolean = false,
    val paymentDate: Long = System.currentTimeMillis() // <-- Add this new line
)