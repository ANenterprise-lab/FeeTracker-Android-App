package com.aaloke.feetracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class NotificationActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val pendingFeesTextView = findViewById<TextView>(R.id.pendingFeesTextView)

        lifecycleScope.launch {
            val students = db.appDao().getAllStudents().first()
            val fees = db.appDao().getAllFees().first()

            val calendar = Calendar.getInstance()
            val currentMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            val currentYear = calendar.get(Calendar.YEAR)

            val overdueStudents = students.filter { student ->
                fees.none { fee ->
                    fee.studentId == student.id &&
                            fee.month.equals(currentMonth, ignoreCase = true) &&
                            fee.year == currentYear
                }
            }

            if (overdueStudents.isEmpty()) {
                pendingFeesTextView.text = "No pending fees for the current month."
            } else {
                pendingFeesTextView.text = overdueStudents.joinToString("\n") { "- ${it.name}" }
            }
        }
    }
}