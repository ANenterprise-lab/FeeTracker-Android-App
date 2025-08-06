package com.aaloke.feetracker

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class ReportsFragment : Fragment(R.layout.fragment_reports) {

    private lateinit var reportsViewModel: ReportsViewModel
    private lateinit var paymentAdapter: PaymentAdapter
    private lateinit var summaryTextView: TextView
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportsViewModel = ViewModelProvider(this).get(ReportsViewModel::class.java)

        summaryTextView = view.findViewById(R.id.report_summary_textview)
        monthSpinner = view.findViewById(R.id.month_spinner)
        yearSpinner = view.findViewById(R.id.year_spinner)
        val recyclerView = view.findViewById<RecyclerView>(R.id.report_recycler_view)

        // THIS IS THE FIX: We pass a do-nothing lambda for the receipt click
        paymentAdapter = PaymentAdapter { /* Do nothing on receipt click from this screen */ }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = paymentAdapter

        setupSpinners()
        setupObservers()
    }

    private fun setupSpinners() {
        val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, months)
        monthSpinner.adapter = monthAdapter

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear + 5).map { it.toString() }.toTypedArray()
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, years)
        yearSpinner.adapter = yearAdapter

        monthSpinner.setSelection(Calendar.getInstance().get(Calendar.MONTH))
        yearSpinner.setSelection(years.indexOf(currentYear.toString()))

        val spinnerListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                reportsViewModel.setMonth(monthSpinner.selectedItem as String)
                reportsViewModel.setYear((yearSpinner.selectedItem as String).toInt())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        monthSpinner.onItemSelectedListener = spinnerListener
        yearSpinner.onItemSelectedListener = spinnerListener
    }

    private fun setupObservers() {
        reportsViewModel.monthlyPayments.observe(viewLifecycleOwner) { payments ->
            paymentAdapter.submitList(payments)

            val total = payments.sumOf { it.amount }
            summaryTextView.text = String.format("Total Collected: ₹%.2f", total)
        }
    }
}