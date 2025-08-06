package com.aaloke.feetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private val clickListener: StudentClickListener
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<Int>()

    fun getSelectedItems(): List<Int> = selectedItems.toList()

    fun clearSelection() {
        isSelectionMode = false
        selectedItems.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.student_item_layout, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = getItem(position)
        holder.bind(student, clickListener, isSelectionMode, selectedItems.contains(student.id))
    }

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.studentNameTextView)
        private val classTextView: TextView = itemView.findViewById(R.id.classNameTextView)
        private val balanceTextView: TextView = itemView.findViewById(R.id.pending_balance_textview)
        private val payFeeButton: Button = itemView.findViewById(R.id.payFeeButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val whatsappIcon: ImageView = itemView.findViewById(R.id.whatsapp_icon)
        private val cardView: CardView = itemView.findViewById(R.id.student_card_view)

        fun bind(
            student: Student,
            listener: StudentClickListener,
            inSelectionMode: Boolean,
            isSelected: Boolean
        ) {
            nameTextView.text = student.name
            classTextView.text = "Class: ${student.className}"

            if (student.pendingBalance > 0) {
                balanceTextView.text = String.format("Balance: ₹%.2f", student.pendingBalance)
                balanceTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.yellow_accent))
            } else {
                balanceTextView.text = "Fully Paid"
                balanceTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.violet_primary))
            }

            itemView.setOnClickListener {
                if (inSelectionMode) {
                    toggleSelection(student.id)
                } else {
                    listener.onItemClicked(student)
                }
            }

            itemView.setOnLongClickListener {
                if (!inSelectionMode) {
                    isSelectionMode = true
                    listener.onSelectionModeStarted()
                }
                toggleSelection(student.id)
                true
            }

            cardView.setCardBackgroundColor(
                if (isSelected) ContextCompat.getColor(itemView.context, R.color.violet_primary_light)
                else ContextCompat.getColor(itemView.context, R.color.white)
            )

            val buttonsVisibility = if (inSelectionMode) View.GONE else View.VISIBLE
            payFeeButton.visibility = buttonsVisibility
            deleteButton.visibility = buttonsVisibility
            whatsappIcon.visibility = buttonsVisibility

            payFeeButton.setOnClickListener { listener.onPayFeeClicked(student) }
            deleteButton.setOnClickListener { listener.onDeleteClicked(student) }
            whatsappIcon.setOnClickListener { listener.onWhatsAppClicked(student) }
        }

        private fun toggleSelection(studentId: Int) {
            if (selectedItems.contains(studentId)) {
                selectedItems.remove(studentId)
            } else {
                selectedItems.add(studentId)
            }
            notifyItemChanged(adapterPosition)
            clickListener.onItemSelected(selectedItems.size)

            if (selectedItems.isEmpty() && isSelectionMode) {
                isSelectionMode = false
                clickListener.onSelectionModeEnded()
            }
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