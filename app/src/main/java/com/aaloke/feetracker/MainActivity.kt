package com.aaloke.feetracker

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var studentAdapter: StudentAdapter
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()

        lifecycleScope.launch {
            db.appDao().getAllStudents().collect { students ->
                studentAdapter.updateData(students)
            }
        }

        // The sync button now saves to a local file
        findViewById<Button>(R.id.syncButton).setOnClickListener {
            exportDataToLocalFile()
        }
    }

    private fun exportDataToLocalFile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val students = db.appDao().getAllStudents().first()
                val fees = db.appDao().getAllFees().first()

                val content = StringBuilder()
                content.append("--- Students ---\n")
                content.append("ID,Name,DefaultFee\n")
                students.forEach {
                    content.append("${it.id},${it.name},${it.defaultFeeAmount}\n")
                }

                content.append("\n--- Fees ---\n")
                content.append("FeeID,StudentID,Month,Year,Amount,Paid,Date\n")
                fees.forEach {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.paymentDate))
                    content.append("${it.id},${it.studentId},${it.month},${it.year},${it.amount},${it.isPaid},${date}\n")
                }

                // Save to the Downloads folder
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, "FeeTracker_Backup.txt")
                file.writeText(content.toString())

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Backup saved to Downloads folder", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        studentAdapter = StudentAdapter(emptyList()) { student ->
            val intent = Intent(this, StudentDetailsActivity::class.java).apply {
                putExtra("STUDENT_ID", student.id)
            }
            startActivity(intent)
        }
        val recyclerView = findViewById<RecyclerView>(R.id.studentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = studentAdapter
    }
}