package com.aaloke.feetracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class StudentAdapter(
    private var students: List<Student>,
    private var fees: List<Fee>, // Add fees list
    private val onItemClicked: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.student_item_layout, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student, fees) // Pass fees to the bind function
        holder.itemView.setOnClickListener { onItemClicked(student) }
    }

    override fun getItemCount() = students.size

    fun updateData(newStudents: List<Student>, newFees: List<Fee>) {
        students = newStudents
        fees = newFees
        notifyDataSetChanged()
    }

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val studentNameTextView: TextView = itemView.findViewById(R.id.studentNameTextView)
        private val statusIndicatorView: View = itemView.findViewById(R.id/statusIndicatorView)

        fun bind(student: Student, fees: List<Fee>) {
            studentNameTextView.text = student.name

            // Logic to set dot color
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            val currentYear = calendar.get(Calendar.YEAR)

            val feeForCurrentMonth = fees.find {
                it.studentId == student.id &&
                        it.month.equals(currentMonth, ignoreCase = true) &&
                        it.year == currentYear
            }

            if (feeForCurrentMonth != null && feeForCurrentMonth.isPaid) {
                statusIndicatorView.setBackgroundColor(Color.GREEN) // Paid
            } else {
                statusIndicatorView.setBackgroundColor(Color.RED) // Pending
            }
        }
    }
}