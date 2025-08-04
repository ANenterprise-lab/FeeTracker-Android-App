package com.aaloke.feetracker

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

// This data class will hold all our database content for backup
data class AppBackup(
    val students: List<Student>,
    val payments: List<Payment>
)

object BackupManager {

    private val gson = Gson()

    // Function to create a JSON string from the database
    suspend fun createBackupJson(context: Context): String {
        val db = AppDatabase.getDatabase(context)
        val students = db.appDao().getAllStudentsList() // A new function we will add to the DAO
        val payments = db.appDao().getAllPaymentsList() // A new function we will add to the DAO

        val backupData = AppBackup(students, payments)
        return gson.toJson(backupData)
    }

    // Function to write the JSON to a file chosen by the user
    fun writeBackupToFile(context: Context, uri: Uri, jsonData: String) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(jsonData)
            }
        }
    }

    // Function to restore data from a JSON file
    suspend fun restoreBackupFromJson(context: Context, uri: Uri) {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        } ?: return

        val type = object : TypeToken<AppBackup>() {}.type
        val backupData: AppBackup = gson.fromJson(jsonString, type)

        val db = AppDatabase.getDatabase(context)
        db.appDao().clearAllTables() // A new function to clear old data
        db.appDao().insertAllStudents(backupData.students) // A new function to insert all students
        db.appDao().insertAllPayments(backupData.payments) // A new function to insert all payments
    }
}