package com.aaloke.feetracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
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

        // Combine student and fee flows to update the adapter together
        lifecycleScope.launch {
            db.appDao().getAllStudents().combine(db.appDao().getAllFees()) { students, fees ->
                Pair(students, fees)
            }.collect { (students, fees) ->
                studentAdapter.updateData(students, fees)
            }
        }

        findViewById<Button>(R.id.syncButton).setOnClickListener {
            exportDataToLocalFile()
        }

        findViewById<ImageButton>(R.id.openCalendarButton).setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        findViewById<FloatingActionButton>(R.id.addStudentFab).setOnClickListener {
            showAddStudentDialog()
        }
        findViewById<ImageButton>(R.id.openCalendarButton).setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
        findViewById<FloatingActionButton>(R.id.addStudentFab).setOnClickListener {
            showAddStudentDialog()
        }
    }

    private fun showAddStudentDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Student")
        val input = EditText(this).apply {
            hint = "Enter student name"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        builder.setView(input)
        builder.setPositiveButton("Add") { dialog, _ ->
            val studentName = input.text.toString().trim()
            if (studentName.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    db.appDao().insertStudent(Student(name = studentName, color = ColorUtils.getNextColor()))
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun exportDataToLocalFile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val students = db.appDao().getAllStudents().first()
                val fees = db.appDao().getAllFees().first()
                val content = StringBuilder()
                content.append("--- Students ---\nID,Name,DefaultFee\n")
                students.forEach { content.append("${it.id},${it.name},${it.defaultFeeAmount}\n") }
                content.append("\n--- Fees ---\nFeeID,StudentID,Month,Year,Amount,Paid,Date\n")
                fees.forEach {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.paymentDate))
                    content.append("${it.id},${it.studentId},${it.month},${it.year},${it.amount},${it.isPaid},${date}\n")
                }
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, "FeeTracker_Backup.txt")
                file.writeText(content.toString())
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Backup saved to Downloads folder", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        // Initialize adapter with empty lists
        studentAdapter = StudentAdapter(emptyList(), emptyList()) { student ->
            val intent = Intent(this, StudentDetailsActivity::class.java).apply {
                putExtra("STUDENT_ID", student.id)
            }
            startActivity(intent)
        }
        val recyclerView = findViewById<RecyclerView>(R.id.studentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = studentAdapter
    }
    private fun showAddStudentDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Student")

        val input = EditText(this)
        input.hint = "Enter student name"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, _ ->
            val studentName = input.text.toString().trim()
            if (studentName.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val newStudent = Student(
                        name = studentName,
                        color = ColorUtils.getNextColor()
                    )
                    db.appDao().insertStudent(newStudent)
                }
                Toast.makeText(this, "$studentName added.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}