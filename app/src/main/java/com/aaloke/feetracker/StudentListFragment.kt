package com.aaloke.feetracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

// Implement the new click listener interface
class StudentListFragment : Fragment(), StudentClickListener {

    private lateinit var studentViewModel: StudentViewModel
    private lateinit var studentAdapter: StudentAdapter
    private var listType: String? = null

    // This will hold our contextual action bar
    private var actionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.studentRecyclerView)

        studentViewModel = ViewModelProvider(requireActivity())[StudentViewModel::class.java]

        // Pass 'this' fragment as the single, powerful click listener
        studentAdapter = StudentAdapter(this)

        recyclerView.adapter = studentAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Observe the correct LiveData for each tab
        when (listType) {
            "Pending" -> studentViewModel.pendingStudents.observe(viewLifecycleOwner) { studentAdapter.submitList(it) }
            "Paid" -> studentViewModel.paidStudents.observe(viewLifecycleOwner) { studentAdapter.submitList(it) }
            else -> studentViewModel.allStudents.observe(viewLifecycleOwner) { studentAdapter.submitList(it) }
        }

        return view
    }

    // --- Implementation of the StudentClickListener Interface ---

    override fun onPayFeeClicked(student: Student) {
        showPaymentDialog(student)
    }

    override fun onDeleteClicked(student: Student) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Student")
            .setMessage("Are you sure you want to permanently delete ${student.name}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                studentViewModel.deleteStudent(student)
                Toast.makeText(context, "${student.name} deleted", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onItemClicked(student: Student) {
        val intent = Intent(requireContext(), StudentProfileActivity::class.java).apply {
            putExtra("STUDENT_EXTRA", student)
            putExtra("studentId", student.id)
        }
        startActivity(intent)
    }

    override fun onWhatsAppClicked(student: Student) {
        sendWhatsAppMessage(student)
    }

    override fun onSelectionModeStarted() {
        if (actionMode == null) {
            actionMode = (activity as? AppCompatActivity)?.startSupportActionMode(actionModeCallback)
        }
    }

    override fun onSelectionModeEnded() {
        actionMode?.finish()
    }

    override fun onItemSelected(count: Int) {
        actionMode?.title = "$count selected"
    }

    // --- This is the logic for the contextual action bar ---

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            // Show the special menu when selection starts
            mode?.menuInflater?.inflate(R.menu.contextual_action_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val selectedStudentIds = studentAdapter.getSelectedItems()
            if (selectedStudentIds.isEmpty()) return false

            when (item?.itemId) {
                R.id.action_pay_selected -> {
                    // Add the confirmation dialog you requested
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Confirm Payment")
                        .setMessage("Are you sure you want to mark fees as paid for ${selectedStudentIds.size} students?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Confirm") { _, _ ->
                            studentViewModel.markSelectedAsPaid(selectedStudentIds)
                            Toast.makeText(context, "${selectedStudentIds.size} students marked as paid", Toast.LENGTH_SHORT).show()
                            mode?.finish() // Close the bar after action
                        }
                        .show()
                    return true
                }
                R.id.action_delete_selected -> {
                    // Add the confirmation dialog you requested
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to permanently delete ${selectedStudentIds.size} students?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete") { _, _ ->
                            studentViewModel.deleteSelectedStudents(selectedStudentIds)
                            Toast.makeText(context, "${selectedStudentIds.size} students deleted", Toast.LENGTH_SHORT).show()
                            mode?.finish() // Close the bar after action
                        }
                        .show()
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            // Clear selection when the bar is closed
            studentAdapter.clearSelection()
            actionMode = null
        }
    }

    // --- (Helper functions are unchanged) ---

    private fun showPaymentDialog(student: Student) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_make_payment, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.payment_dialog_title)
        val balanceTextView = dialogView.findViewById<TextView>(R.id.payment_dialog_balance)
        val amountEditText = dialogView.findViewById<TextInputEditText>(R.id.payment_amount_edittext)
        titleTextView.text = "Pay Fee for ${student.name}"
        balanceTextView.text = String.format("Pending Balance: ₹%.2f", student.pendingBalance)
        amountEditText.setText(student.pendingBalance.toString())
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setTitle("Record Payment")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm") { _, _ ->
                val amountText = amountEditText.text.toString()
                val amountPaid = amountText.toDoubleOrNull()
                if (amountPaid == null || amountPaid <= 0) {
                    Toast.makeText(context, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                } else if (amountPaid > student.pendingBalance) {
                    Toast.makeText(context, "Amount cannot be greater than the pending balance.", Toast.LENGTH_SHORT).show()
                } else {
                    studentViewModel.recordPartialPayment(student, amountPaid)
                    Toast.makeText(context, "Payment recorded successfully!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun sendWhatsAppMessage(student: Student) {
        val phoneNumber = student.phoneNumber
        if (phoneNumber.isNullOrBlank()) {
            Toast.makeText(context, "No phone number for this student.", Toast.LENGTH_SHORT).show()
            return
        }
        val template = SettingsManager.getSmsTemplate(requireContext())
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val currentMonth = monthFormat.format(Calendar.getInstance().time)
        val message = template.replace("{student_name}", student.name, ignoreCase = true).replace("{month}", currentMonth, ignoreCase = true)
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val formattedNumber = phoneNumber.replace("+", "").replace(" ", "")
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=${Uri.encode(message)}")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(listType: String?): StudentListFragment {
            val fragment = StudentListFragment()
            val args = Bundle()
            args.putString("LIST_TYPE", listType)
            fragment.arguments = args
            return fragment
        }
    }
}