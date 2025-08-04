package com.aaloke.feetracker

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StudentProfileActivity : AppCompatActivity() {

    // Use the modern 'by viewModels()' delegate to create the ViewModel
    private val viewModel: StudentProfileViewModel by viewModels()
    private lateinit var paymentAdapter: PaymentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)

        val student = intent.getSerializableExtra("STUDENT_EXTRA") as? Student
        if (student == null) {
            finish()
            return
        }

        // --- Populate the UI ---
        findViewById<TextView>(R.id.profile_student_name).text = student.name
        findViewById<TextView>(R.id.profile_class_name).text = "Class: ${student.className}"
        findViewById<TextView>(R.id.profile_phone_number).text = "Phone: ${student.phoneNumber ?: "N/A"}"

        // --- Setup RecyclerView ---
        val recyclerView = findViewById<RecyclerView>(R.id.payment_history_recycler_view)
        paymentAdapter = PaymentAdapter()
        recyclerView.adapter = paymentAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // --- Observe payment history ---
        viewModel.paymentHistory.observe(this) { payments ->
            paymentAdapter.submitList(payments)
        }
    }
}