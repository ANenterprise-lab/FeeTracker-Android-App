package com.aaloke.feetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aaloke.feetracker.dao.BatchDatabase
import com.aaloke.feetracker.databinding.ActivityStudentsBinding
import com.aaloke.feetracker.models.Student
import com.aaloke.feetracker.viewmodels.StudentsViewModel
import com.aaloke.feetracker.viewmodels.StudentsViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class StudentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentsBinding
    private lateinit var studentsViewModel: StudentsViewModel
    private var batchId: Int = -1
    private lateinit var studentsAdapter: StudentsAdapter

    companion object {
        const val EXTRA_BATCH_ID = "extra_batch_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the batchId from the Intent
        batchId = intent.getIntExtra(EXTRA_BATCH_ID, -1)
        if (batchId == -1) {
            // Handle error case, e.g., finish the activity or show a message
            finish()
            return
        }

        // Set the title based on the batch name (TODO: We'll fetch the batch name later)
        binding.studentsTitleTextView.text = "Students in Batch #$batchId"

        val studentDao = BatchDatabase.getDatabase(this).studentDao()
        val viewModelFactory = StudentsViewModelFactory(studentDao, batchId)
        studentsViewModel = ViewModelProvider(this, viewModelFactory)[StudentsViewModel::class.java]

        studentsAdapter = StudentsAdapter(
            students = listOf(),
            onPayFeeClick = { student ->
                // TODO: Implement fee payment logic
            },
            onDeleteClick = { student ->
                // TODO: Implement student deletion logic
            }
        )
        binding.studentsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.studentsRecyclerView.adapter = studentsAdapter

        studentsViewModel.studentsForBatch.observe(this) { students ->
            studentsAdapter = StudentsAdapter(
                students = students,
                onPayFeeClick = { student ->
                    // TODO: Implement fee payment logic
                },
                onDeleteClick = { student ->
                    // TODO: Implement student deletion logic
                }
            )
            binding.studentsRecyclerView.adapter = studentsAdapter
        }

        binding.fabAddStudent.setOnClickListener {
            showAddStudentDialog()
        }
    }

    private fun showAddStudentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_student, null)
        val nameEditText = dialogView.findViewById<TextInputEditText>(R.id.student_name_edittext)
        val contactEditText = dialogView.findViewById<TextInputEditText>(R.id.student_contact_edittext)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add New Student")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val contact = contactEditText.text.toString().trim()

                if (name.isNotEmpty()) {
                    val newStudent = Student(
                        batchId = batchId,
                        name = name,
                        contactNumber = contact,
                        profilePicturePath = null,
                        notes = null
                    )
                    studentsViewModel.insert(newStudent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}