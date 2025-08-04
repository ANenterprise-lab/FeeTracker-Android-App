package com.aaloke.feetracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StudentListFragment : Fragment() {

    private lateinit var studentViewModel: StudentViewModel
    private lateinit var studentAdapter: StudentAdapter
    private var listType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listType = it.getString("LIST_TYPE")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.studentRecyclerView)

        studentViewModel = ViewModelProvider(requireActivity())[StudentViewModel::class.java]

        studentAdapter = StudentAdapter(
            onPayFeeClicked = { student ->
                showPaymentDialog(student)
            },
            onDeleteClicked = { student ->
                studentViewModel.deleteStudent(student)
            },
            onItemClicked = { student ->
                val intent = Intent(requireContext(), StudentProfileActivity::class.java).apply {
                    putExtra("STUDENT_EXTRA", student)
                    putExtra("studentId", student.id)
                }
                startActivity(intent)
            },
            onWhatsAppClicked = { student ->
                sendWhatsAppMessage(student)
            }
        )

        recyclerView.adapter = studentAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        when (listType) {
            "Pending" -> studentViewModel.pendingStudents.observe(viewLifecycleOwner) { studentAdapter.submitList(it) }
            "Paid" -> studentViewModel.paidStudents.observe(viewLifecycleOwner) { studentAdapter.submitList(it) }
            else -> studentViewModel.allStudents.observe(viewLifecycleOwner) { studentAdapter.submitList(it) }
        }

        return view
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

        val message = template
            .replace("{student_name}", student.name, ignoreCase = true)
            .replace("{month}", currentMonth, ignoreCase = true)

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val formattedNumber = phoneNumber.replace("+", "").replace(" ", "")
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=${Uri.encode(message)}")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
        }
    }

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