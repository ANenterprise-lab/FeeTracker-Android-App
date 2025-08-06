package com.aaloke.feetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class ExpensesActivity : AppCompatActivity() {

    private lateinit var expensesViewModel: ExpensesViewModel
    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)
        title = "Expense Tracker"

        expensesViewModel = ViewModelProvider(this).get(ExpensesViewModel::class.java)

        val recyclerView = findViewById<RecyclerView>(R.id.expenses_recycler_view)
        val fab = findViewById<FloatingActionButton>(R.id.fab_add_expense)

        expenseAdapter = ExpenseAdapter()
        recyclerView.adapter = expenseAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        expensesViewModel.allExpenses.observe(this) { expenses ->
            expenseAdapter.submitList(expenses)
        }

        fab.setOnClickListener {
            showAddExpenseDialog()
        }
    }

    private fun showAddExpenseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)
        val descriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.expense_description_edittext)
        val amountEditText = dialogView.findViewById<TextInputEditText>(R.id.expense_amount_edittext)

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setTitle("Add New Expense")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val description = descriptionEditText.text.toString().trim()
                val amount = amountEditText.text.toString().toDoubleOrNull()

                if (description.isNotEmpty() && amount != null && amount > 0) {
                    expensesViewModel.addExpense(description, amount)
                    Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a valid description and amount", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}