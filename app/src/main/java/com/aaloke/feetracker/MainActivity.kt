package com.aaloke.feetracker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var studentAdapter: StudentAdapter
    private val db by lazy { AppDatabase.getDatabase(this) }

    // --- NEW: Modern way to handle saving files ---
    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    exportDataToUri(uri)
                }
            } else {
                Toast.makeText(this, "File saving cancelled.", Toast.LENGTH_SHORT).show()
            }
        }

    // --- NEW: Modern way to handle asking for permissions ---
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission is recommended for reminders.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ask for notification permission on startup
        requestNotificationPermission()

        setupRecyclerView()

        lifecycleScope.launch {
            db.appDao().getAllStudents().combine(db.appDao().getAllFees()) { students, fees ->
                Pair(students, fees)
            }.collect { (students, fees) ->
                studentAdapter.updateData(students, fees)
            }
        }

        findViewById<Button>(R.id.syncButton).setOnClickListener {
            // This now launches the system file picker
            launchFileSaver()
        }

        findViewById<ImageButton>(R.id.openCalendarButton).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        findViewById<FloatingActionButton>(R.id.addStudentFab).setOnClickListener {
            showAddStudentDialog()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun launchFileSaver() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain" // We are saving a text file
            putExtra(Intent.EXTRA_TITLE, "FeeTracker_Backup.txt")
        }
        createFileLauncher.launch(intent)
    }

    private fun exportDataToUri(uri: Uri) {
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

                // Write the content to the location the user selected
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toString().toByteArray())
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Backup saved successfully!", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        studentAdapter = StudentAdapter(emptyList(), emptyList()) { student ->
            startActivity(Intent(this, StudentDetailsActivity::class.java).apply {
                putExtra("STUDENT_ID", student.id)
            })
        }
        val recyclerView = findViewById<RecyclerView>(R.id.studentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = studentAdapter
    }

    private fun showAddStudentDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Student")
        val input = EditText(this).apply {
            hint = "Enter student name"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        builder.setView(input)
        builder.setPositiveButton("Add") { _, _ ->
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
}