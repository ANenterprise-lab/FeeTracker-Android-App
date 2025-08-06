package com.aaloke.feetracker

import androidx.room.Entity

@Entity(tableName = "student_batch_cross_ref", primaryKeys = ["id", "batchId"])
data class StudentBatchCrossRef(
    val id: Int, // This is the student's ID
    val batchId: Int  // This is the batch's ID
)