package com.aaloke.feetracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AttendanceActivity : AppCompatActivity() {

    private lateinit var viewModel: AttendanceViewModel
    private lateinit var studentViewModel: StudentViewModel // For getting class list
    private lateinit var attendanceAdapter: AttendanceAdapter
    private lateinit var classSpinner: Spinner
    private lateinit var datePickerButton: Button
    private lateinit var saveButton: Button
    private lateinit var recyclerView: RecyclerView

    private val selectedDate = Calendar.getInstance()
    private val attendanceItems = mutableListOf<AttendanceItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)
        title = "Take Attendance"

        viewModel = ViewModelProvider(this)[AttendanceViewModel::class.java]
        studentViewModel = ViewModelProvider(this)[StudentViewModel::class.java]

        // Find Views
        classSpinner = findViewById(R.id.class_spinner_attendance)
        datePickerButton = findViewById(R.id.date_picker_button)
        saveButton = findViewById(R.id.save_attendance_button)
        recyclerView = findViewById(R.id.attendance_recycler_view)

        setupRecyclerView()
        setupClassSpinner()
        setupDatePicker()
        setupSaveButton()
        setupObservers()
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceAdapter(attendanceItems)
        recyclerView.adapter = attendanceAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClassSpinner() {
        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        classSpinner.adapter = spinnerAdapter

        studentViewModel.allStudentNames.observe(this) { names ->
            // We get class names by getting all student names and finding the unique ones
            val classNames = mutableListOf("Select a Class")
            classNames.addAll(names.distinct())
            spinnerAdapter.clear()
            spinnerAdapter.addAll(classNames)
        }

        classSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // Ignore the "Select a Class" hint
                    val selectedClass = parent?.getItemAtPosition(position) as String
                    viewModel.fetchStudentsByClass(selectedClass)
                } else {
                    // Clear the list if "Select a Class" is chosen
                    attendanceItems.clear()
                    attendanceAdapter.notifyDataSetChanged()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDatePicker() {
        updateDateButtonText()
        datePickerButton.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateButtonText()
                // Re-fetch attendance for the new date
                viewModel.allStudents.value?.let { students ->
                    viewModel.fetchAttendanceForDate(students.map { it.id }, selectedDate.time)
                }
            }
            DatePickerDialog(this, dateSetListener,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            val records = attendanceAdapter.getAttendanceRecords()
            viewModel.saveAttendance(records, selectedDate.time)
            Toast.makeText(this, "Attendance Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewModel.allStudents.observe(this) { students ->
            // When the list of students for a class is fetched,
            // immediately fetch any existing attendance records for them on the selected date.
            viewModel.fetchAttendanceForDate(students.map { it.id }, selectedDate.time)
        }

        viewModel.attendanceForDate.observe(this) { attendanceRecords ->
            val students = viewModel.allStudents.value ?: return@observe

            // This is the core logic: merge the student list with their attendance status
            attendanceItems.clear()
            students.forEach { student ->
                val existingRecord = attendanceRecords.find { it.studentId == student.id }
                // If a record exists, use its status. If not, default to "Present".
                val status = existingRecord?.status ?: "Present"
                attendanceItems.add(AttendanceItem(student, status))
            }
            attendanceAdapter.notifyDataSetChanged()
        }
    }

    private fun updateDateButtonText() {
        val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
        datePickerButton.text = dateFormat.format(selectedDate.time)
    }
}