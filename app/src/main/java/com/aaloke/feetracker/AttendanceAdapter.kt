package com.aaloke.feetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// A data class to hold the student and their attendance status together
data class AttendanceItem(val student: Student, var status: String)

class AttendanceAdapter(
    private val attendanceItems: MutableList<AttendanceItem>
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val item = attendanceItems[position]
        holder.bind(item) { newStatus ->
            // When a radio button is clicked, update the status in our list
            attendanceItems[position].status = newStatus
        }
    }

    override fun getItemCount(): Int = attendanceItems.size

    // A function to get the final list of attendance records to be saved
    fun getAttendanceRecords(): List<AttendanceItem> {
        return attendanceItems
    }

    class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val studentName: TextView = itemView.findViewById(R.id.student_name_attendance)
        private val statusGroup: RadioGroup = itemView.findViewById(R.id.attendance_status_group)
        private val presentRadio: RadioButton = itemView.findViewById(R.id.radio_present)
        private val absentRadio: RadioButton = itemView.findViewById(R.id.radio_absent)

        fun bind(item: AttendanceItem, onStatusChanged: (String) -> Unit) {
            studentName.text = item.student.name

            // Prevent the listener from firing when we programmatically set the checked state
            statusGroup.setOnCheckedChangeListener(null)

            // Set the radio button based on the item's status
            when (item.status) {
                "Present" -> presentRadio.isChecked = true
                "Absent" -> absentRadio.isChecked = true
                else -> statusGroup.clearCheck()
            }

            // Now, set the listener to capture user clicks
            statusGroup.setOnCheckedChangeListener { _, checkedId ->
                val newStatus = if (checkedId == R.id.radio_present) "Present" else "Absent"
                onStatusChanged(newStatus)
            }
        }
    }
}