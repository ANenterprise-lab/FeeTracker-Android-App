package com.aaloke.feetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private val onPayFeeClicked: (Student) -> Unit,
    private val onDeleteClicked: (Student) -> Unit,
    private val onItemClicked: (Student) -> Unit,
    private val onWhatsAppClicked: (Student) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.student_item_layout, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = getItem(position)
        holder.bind(student, onPayFeeClicked, onDeleteClicked, onItemClicked, onWhatsAppClicked)
    }

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.studentNameTextView)
        private val classTextView: TextView = itemView.findViewById(R.id.classNameTextView)
        private val phoneTextView: TextView = itemView.findViewById(R.id.phoneNumberTextView)
        private val balanceTextView: TextView = itemView.findViewById(R.id.pending_balance_textview)
        private val payFeeButton: Button = itemView.findViewById(R.id.payFeeButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val whatsappIcon: ImageView = itemView.findViewById(R.id.whatsapp_icon)

        fun bind(
            student: Student,
            onPayFeeClicked: (Student) -> Unit,
            onDeleteClicked: (Student) -> Unit,
            onItemClicked: (Student) -> Unit,
            onWhatsAppClicked: (Student) -> Unit
        ) {
            nameTextView.text = student.name
            classTextView.text = "Class: ${student.className}"
            phoneTextView.text = "Phone: ${student.phoneNumber ?: "N/A"}"

            if (student.pendingBalance > 0) {
                balanceTextView.text = String.format("Balance: ₹%.2f", student.pendingBalance)
                balanceTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.yellow_accent))
                payFeeButton.visibility = View.VISIBLE
            } else {
                balanceTextView.text = "Fully Paid"
                balanceTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.violet_primary))
                payFeeButton.visibility = View.GONE
            }

            payFeeButton.setOnClickListener { onPayFeeClicked(student) }
            deleteButton.setOnClickListener { onDeleteClicked(student) }
            itemView.setOnClickListener { onItemClicked(student) }
            whatsappIcon.setOnClickListener { onWhatsAppClicked(student) }
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