package com.aaloke.feetracker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.ArrayList

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var studentViewModel: StudentViewModel
    private lateinit var totalCollectedTextView: TextView
    private lateinit var totalPendingTextView: TextView
    private lateinit var totalExpensesTextView: TextView
    private lateinit var summaryTitleTextView: TextView
    private lateinit var pieChart: PieChart

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find all the UI elements from the layout
        totalCollectedTextView = view.findViewById(R.id.total_collected_textview)
        totalPendingTextView = view.findViewById(R.id.total_pending_textview)
        totalExpensesTextView = view.findViewById(R.id.total_expenses_textview)
        summaryTitleTextView = view.findViewById(R.id.summary_title_textview)
        pieChart = view.findViewById(R.id.pie_chart)

        // Get the shared ViewModel from the Activity
        studentViewModel = ViewModelProvider(requireActivity()).get(StudentViewModel::class.java)

        setupPieChartStyle()
        setupObservers()
    }

    private fun setupObservers() {
        // Observe all data points and call a single function to update the summary
        studentViewModel.totalCollected.observe(viewLifecycleOwner) { updateTotalSummary() }
        studentViewModel.totalPending.observe(viewLifecycleOwner) { updateTotalSummary() }
        studentViewModel.totalExpenses.observe(viewLifecycleOwner) { updateTotalSummary() }

        // Observe student counts to update the pie chart
        studentViewModel.paidStudentCount.observe(viewLifecycleOwner) { updatePieChart() }
        studentViewModel.pendingStudentCount.observe(viewLifecycleOwner) { updatePieChart() }
    }

    private fun updateTotalSummary() {
        val collected = studentViewModel.totalCollected.value ?: 0.0
        val pending = studentViewModel.totalPending.value ?: 0.0
        val expenses = studentViewModel.totalExpenses.value ?: 0.0
        val profit = collected - expenses

        totalCollectedTextView.text = String.format("Fees Collected: ₹%.2f", collected)
        totalPendingTextView.text = String.format("Fees Pending: ₹%.2f", pending)
        totalExpensesTextView.text = String.format("Total Expenses: ₹%.2f", expenses)
        summaryTitleTextView.text = String.format("Financial Summary (Profit: ₹%.2f)", profit)
    }

    private fun setupPieChartStyle() {
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.legend.textSize = 14f
    }

    private fun updatePieChart() {
        val paidCount = studentViewModel.paidStudentCount.value ?: 0
        val pendingCount = studentViewModel.pendingStudentCount.value ?: 0

        if (paidCount == 0 && pendingCount == 0) {
            pieChart.visibility = View.GONE // Hide chart if there is no data
            return
        }
        pieChart.visibility = View.VISIBLE

        val entries = ArrayList<PieEntry>()
        if (paidCount > 0) entries.add(PieEntry(paidCount.toFloat(), "Paid"))
        if (pendingCount > 0) entries.add(PieEntry(pendingCount.toFloat(), "Pending"))

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.violet_primary),
            ContextCompat.getColor(requireContext(), R.color.yellow_accent)
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 16f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.animateY(1000) // Add a simple animation
        pieChart.invalidate() // Refresh the chart
    }
}