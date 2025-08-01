package com.aaloke.feetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private var students: List<Student>,
    private val onItemClicked: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.student_item_layout, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student, position + 1)
        holder.itemView.setOnClickListener { onItemClicked(student) }
    }

    override fun getItemCount() = students.size

    fun updateData(newStudents: List<Student>) {
        students = newStudents
        notifyDataSetChanged()
    }

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serialNumberTextView: TextView = itemView.findViewById(R.id.serialNumberTextView)
        private val studentNameTextView: TextView = itemView.findViewById(R.id.studentNameTextView)

        fun bind(student: Student, serialNumber: Int) {
            serialNumberTextView.text = "$serialNumber."
            studentNameTextView.text = student.name
            itemView.findViewById<View>(R.id.colorIndicatorView).setBackgroundColor(student.color) // <-- Add
        }
    }
}