package com.aaloke.feetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aaloke.feetracker.models.Student

class StudentsAdapter(
    private val students: List<Student>,
    private val onPayFeeClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentsAdapter.StudentViewHolder>() {

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.studentNameTextView)
        val classNameTextView: TextView = itemView.findViewById(R.id.classNameTextView)
        val phoneNumberTextView: TextView = itemView.findViewById(R.id.phoneNumberTextView)
        val feeStatusTextView: TextView = itemView.findViewById(R.id.feeStatusTextView)
        val payFeeButton: Button = itemView.findViewById(R.id.payFeeButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(
            student: Student,
            onPayFeeClick: (Student) -> Unit,
            onDeleteClick: (Student) -> Unit
        ) {
            nameTextView.text = student.name
            // We'll update the classNameTextView once we have a way to get the batch name
            phoneNumberTextView.text = student.contactNumber
            // We'll update the feeStatusTextView later with the fee status logic

            payFeeButton.setOnClickListener {
                onPayFeeClick(student)
            }
            deleteButton.setOnClickListener {
                onDeleteClick(student)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student, onPayFeeClick, onDeleteClick)
    }

    override fun getItemCount(): Int = students.size
}