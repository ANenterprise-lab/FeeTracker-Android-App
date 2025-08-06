package com.aaloke.feetracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aaloke.feetracker.models.Batch // This is the new import
import com.aaloke.feetracker.dao.BatchDao
import com.aaloke.feetracker.dao.StudentDao // Check this import
import com.aaloke.feetracker.models.Student // Check this import

@Database(entities = [Batch::class, Student::class], version = 2, exportSchema = false)
abstract class BatchDatabase : RoomDatabase() {

    abstract fun batchDao(): BatchDao
    abstract fun studentDao(): StudentDao // New function for the student DAO

    companion object {
        @Volatile
        private var INSTANCE: BatchDatabase? = null

        fun getDatabase(context: Context): BatchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BatchDatabase::class.java,
                    "batch_database"
                ).fallbackToDestructiveMigration() // Added this for simplicity during development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}