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
    // ADD THESE TWO NEW FIELDS
    val phoneNumber: String?, // '?' makes it optional (can be empty)
    val feeStatus: String = "Pending" // Default new students to "Pending"
) : Serializable