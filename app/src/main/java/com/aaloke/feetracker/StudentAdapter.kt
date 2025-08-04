package com.aaloke.feetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private val onPayFeeClicked: (Student) -> Unit,
    private val onDeleteClicked: (Student) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = getItem(position)
        holder.bind(student, onPayFeeClicked, onDeleteClicked)
    }

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.studentNameTextView)
        private val classTextView: TextView = itemView.findViewById(R.id.classNameTextView)
        private val phoneTextView: TextView = itemView.findViewById(R.id.phoneNumberTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.feeStatusTextView)
        private val payFeeButton: Button = itemView.findViewById(R.id.payFeeButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(student: Student, onPayFeeClicked: (Student) -> Unit, onDeleteClicked: (Student) -> Unit) {
            nameTextView.text = student.name
            classTextView.text = "Class: ${student.className}"
            phoneTextView.text = "Phone: ${student.phoneNumber ?: "N/A"}" // Show N/A if no number
            statusTextView.text = "Status: ${student.feeStatus}"

            // Show "Pay Fee" button only if status is "Pending"
            payFeeButton.visibility = if (student.feeStatus == "Pending") View.VISIBLE else View.GONE
            payFeeButton.setOnClickListener { onPayFeeClicked(student) }

            deleteButton.setOnClickListener { onDeleteClicked(student) }
        }
    }
}

class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
    override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
        return oldItem == newItem
    }
}