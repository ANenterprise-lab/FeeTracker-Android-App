package com.aaloke.feetracker

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter

object ExportManager {
    fun exportToCsv(context: Context, students: List<Student>, fileName: String) {
        if (students.isEmpty()) {
            Toast.makeText(context, "No students to export.", Toast.LENGTH_SHORT).show()
            return
        }

        val csvHeader = "ID,Name,Class,Phone Number,Fee Amount,Fee Status\n"
        val stringBuilder = StringBuilder().append(csvHeader)

        students.forEach { student ->
            stringBuilder.append("${student.id},\"${student.name}\",\"${student.className}\",\"${student.phoneNumber ?: "N/A"}\",${student.feeAmount},${student.feeStatus}\n")
        }

        try {
            val file = File(context.cacheDir, fileName)
            FileWriter(file).use { writer ->
                writer.write(stringBuilder.toString())
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export CSV"))

        } catch (e: Exception) {
            Toast.makeText(context, "Error exporting file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}