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

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var studentViewModel: StudentViewModel
    private lateinit var totalCollectedTextView: TextView
    private lateinit var totalPendingTextView: TextView
    private lateinit var pieChart: PieChart

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find all the UI elements from the layout
        totalCollectedTextView = view.findViewById(R.id.total_collected_textview)
        totalPendingTextView = view.findViewById(R.id.total_pending_textview)
        pieChart = view.findViewById(R.id.pie_chart)

        // Get the shared ViewModel
        studentViewModel = ViewModelProvider(requireActivity()).get(StudentViewModel::class.java)

        // Observe the data and update the UI
        setupObservers()
        setupPieChart()
    }

    private fun setupObservers() {
        // Observe total collected amount
        studentViewModel.totalCollected.observe(viewLifecycleOwner) { collected ->
            totalCollectedTextView.text = String.format("Total Collected: ₹%.2f", collected ?: 0.0)
        }

        // Observe total pending amount
        studentViewModel.totalPending.observe(viewLifecycleOwner) { pending ->
            totalPendingTextView.text = String.format("Total Pending: ₹%.2f", pending ?: 0.0)
        }

        // Observe student counts for the pie chart
        studentViewModel.paidStudentCount.observe(viewLifecycleOwner) { updatePieChart() }
        studentViewModel.pendingStudentCount.observe(viewLifecycleOwner) { updatePieChart() }
    }

    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.legend.isEnabled = true
    }

    private fun updatePieChart() {
        val paidCount = studentViewModel.paidStudentCount.value ?: 0
        val pendingCount = studentViewModel.pendingStudentCount.value ?: 0

        if (paidCount == 0 && pendingCount == 0) {
            pieChart.visibility = View.GONE // Hide chart if there's no data
            return
        }
        pieChart.visibility = View.VISIBLE

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(paidCount.toFloat(), "Paid"))
        entries.add(PieEntry(pendingCount.toFloat(), "Pending"))

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.violet_primary),
            ContextCompat.getColor(requireContext(), R.color.yellow_accent)
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 16f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate() // Refresh the chart
    }
}