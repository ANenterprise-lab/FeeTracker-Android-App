package com.aaloke.feetracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StudentProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: StudentProfileViewModel
    private lateinit var paymentAdapter: PaymentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)

        val student = intent.getSerializableExtra("STUDENT_EXTRA") as? Student
        if (student == null) {
            finish() // Exit if student data is missing
            return
        }
        title = "${student.name}'s Profile"

        // --- THIS IS THE FIX: Correctly create the ViewModel using the Factory ---
        val viewModelFactory = StudentProfileViewModelFactory(application, student.id)
        viewModel = ViewModelProvider(this, viewModelFactory).get(StudentProfileViewModel::class.java)

        // --- Populate UI ---
        findViewById<TextView>(R.id.profile_student_name).text = student.name
        findViewById<TextView>(R.id.profile_class_name).text = "Class: ${student.className}"
        findViewById<TextView>(R.id.profile_phone_number).text = "Phone: ${student.phoneNumber ?: "N/A"}"

        // --- Setup RecyclerView ---
        val recyclerView = findViewById<RecyclerView>(R.id.payment_history_recycler_view)
        paymentAdapter = PaymentAdapter { payment ->
            // This is where the PDF generation is triggered
            PdfManager.generateReceipt(this, student, payment)
        }
        recyclerView.adapter = paymentAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // --- Observe Data ---
        viewModel.paymentHistory.observe(this) { payments ->
            paymentAdapter.submitList(payments)
        }
    }
}