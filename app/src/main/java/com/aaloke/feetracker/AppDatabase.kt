package com.aaloke.feetracker

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aaloke.feetracker.models.Batch

// 1. Add Batch::class and StudentBatchCrossRef::class to the entities list
// 2. Increment the version number
@Database(entities = [
    Student::class,
    Payment::class,
    Expense::class,
    Attendance::class,
    Fee::class,
    Batch::class, // New
    StudentBatchCrossRef::class // New
], version = 2, exportSchema = false) // Assuming current version is 1, new is 2
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fee_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}