package com.aaloke.feetracker.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.aaloke.feetracker.models.Batch
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {

    @Insert
    suspend fun insertBatch(batch: Batch)

    @Query("SELECT * FROM batches ORDER BY name ASC")
    fun getAllBatches(): Flow<List<Batch>>

    @Delete
    suspend fun deleteBatch(batch: Batch)

}