package com.aaloke.feetracker.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "batches")
data class Batch(
    @PrimaryKey(autoGenerate = true)
    val batchId: Int = 0,
    val name: String,
    val description: String?
)