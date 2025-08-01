package com.aaloke.feetracker

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class StudentDetailsActivity : AppCompatActivity() {

    private var studentId: Int = -1
    private var currentStudent: Student? = null // Will hold the student's data

    private val db by lazy { AppDatabase.getDatabase(this) }
    private val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    private lateinit var feeHistoryLayout: LinearLayout
    private lateinit var addFeeButton: Button
    private lateinit var studentNameTextView: TextView
    private lateinit var editStudentNameButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_details)

        studentId = intent.getIntExtra("STUDENT_ID", -1)
        if (studentId == -1) {
            finish()
            return
        }

        // --- Initialize Views ---
        feeHistoryLayout = findViewById(R.id.feeHistoryLayout)
        addFeeButton = findViewById(R.id.addFeeButton)
        studentNameTextView = findViewById(R.id.detailsStudentNameTextView)
        editStudentNameButton = findViewById(R.id.editStudentNameButton)

        // --- Fetch Data and Set Up UI ---
        lifecycleScope.launch {
            // Fetch student data ONCE and store it
            currentStudent = db.appDao().getAllStudents().first().find { it.id == studentId }

            // Now that we have the data, update the UI
            studentNameTextView.text = currentStudent?.name ?: "Unknown"

            // Also, start collecting fee updates
            db.appDao().getFeesForStudent(studentId).collect { feeList ->
                updateFeeHistoryUI(feeList)
                updateAddFeeButtonLogic(feeList)
            }
        }

        // --- Set Click Listeners ---
        addFeeButton.setOnClickListener {
            addNewFee()
        }

        editStudentNameButton.setOnClickListener {
            showEditStudentInfoDialog()
        }
    }

    private fun showEditStudentInfoDialog() {
        // Use the 'currentStudent' variable which is already loaded
        val student = currentStudent ?: return // Safety check

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Student Info")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val padding = 50
            setPadding(padding, padding, padding, padding)
        }

        val nameInput = EditText(this).apply {
            hint = "Student Name"
            setText(student.name)
        }

        val feeInput = EditText(this).apply {
            hint = "Default Fee Amount"
            setText(student.defaultFeeAmount.toString())
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        layout.addView(nameInput)
        layout.addView(feeInput)
        builder.setView(layout)

        builder.setPositiveButton("Save") { _, _ ->
            val newName = nameInput.text.toString().trim()
            val newFeeAmount = feeInput.text.toString().toDoubleOrNull() ?: student.defaultFeeAmount

            if (newName.isNotEmpty()) {
                val updatedStudent = student.copy(name = newName, defaultFeeAmount = newFeeAmount)
                lifecycleScope.launch(Dispatchers.IO) {
                    db.appDao().updateStudent(updatedStudent)
                }
                // Update the UI immediately
                currentStudent = updatedStudent
                studentNameTextView.text = newName
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun addNewFee() {
        // Use the 'currentStudent' variable, which is ready
        val student = currentStudent ?: return // Safety check

        lifecycleScope.launch {
            val fees = db.appDao().getFeesForStudent(studentId).first()
            val nextFeeDate = getNextFeeDate(fees)

            AlertDialog.Builder(this@StudentDetailsActivity)
                .setTitle("Confirm Fee")
                .setMessage("Add fee of \$${student.defaultFeeAmount} for ${nextFeeDate.first} ${nextFeeDate.second}?")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val newFee = Fee(
                            studentId = studentId,
                            month = nextFeeDate.first,
                            year = nextFeeDate.second,
                            amount = student.defaultFeeAmount,
                            isPaid = true,
                            paymentDate = System.currentTimeMillis() // <-- Add/confirm this line is here
                        )
                        db.appDao().insertFee(newFee)
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    // --- Helper Functions (No changes needed below) ---

    private fun updateFeeHistoryUI(fees: List<Fee>) {
        feeHistoryLayout.removeAllViews()
        if (fees.isEmpty()) {
            val textView = TextView(this).apply { text = "No fee history yet."; textSize = 16f }
            feeHistoryLayout.addView(textView)
        } else {
            fees.sortedWith(compareBy({ it.year }, { months.indexOf(it.month) })).forEach { fee ->
                val textView = TextView(this).apply {
                    val status = if (fee.isPaid) "PAID" else "PENDING"
                    text = "${fee.month} ${fee.year}: \$${fee.amount} - $status"
                    textSize = 16f
                }
                feeHistoryLayout.addView(textView)
            }
        }
    }

    private fun updateAddFeeButtonLogic(fees: List<Fee>) {
        val nextFeeDate = getNextFeeDate(fees)
        addFeeButton.text = "Add Fee for ${nextFeeDate.first} ${nextFeeDate.second}"
    }

    private fun getNextFeeDate(fees: List<Fee>): Pair<String, Int> {
        val lastPaidFee = fees.filter { it.isPaid }.maxByOrNull { it.year * 12 + months.indexOf(it.month) }
        return if (lastPaidFee == null) {
            val calendar = Calendar.getInstance()
            "January" to calendar.get(Calendar.YEAR)
        } else {
            val lastMonthIndex = months.indexOf(lastPaidFee.month)
            if (lastMonthIndex == 11) "January" to lastPaidFee.year + 1
            else months[lastMonthIndex + 1] to lastPaidFee.year
        }
    }
}