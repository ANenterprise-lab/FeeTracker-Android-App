package com.aaloke.feetracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val className: String,
    val feeAmount: Double,
    val phoneNumber: String?,
    val feeStatus: String = "Pending",
    // The previous version was missing a comma after feeStatus
    val pendingBalance: Double = feeAmount
) : Serializable