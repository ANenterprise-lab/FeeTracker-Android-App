package com.aaloke.feetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class PaymentAdapter(
    // The adapter now takes a function to handle receipt icon clicks
    private val onReceiptClicked: (Payment) -> Unit
) : ListAdapter<Payment, PaymentAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = getItem(position)
        // Pass the click handler function to the ViewHolder
        holder.bind(payment, onReceiptClicked)
    }

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthYearTextView: TextView = itemView.findViewById(R.id.payment_month_year)
        private val dateTextView: TextView = itemView.findViewById(R.id.payment_date)
        private val amountTextView: TextView = itemView.findViewById(R.id.payment_amount)
        // Find the new receipt icon
        private val receiptIcon: ImageView = itemView.findViewById(R.id.generate_receipt_icon)

        private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        fun bind(payment: Payment, onReceiptClicked: (Payment) -> Unit) {
            monthYearTextView.text = "${payment.month} ${payment.year}"
            dateTextView.text = "Paid on: ${dateFormat.format(payment.paymentDate)}"
            amountTextView.text = "₹${"%.2f".format(payment.amount)}"

            // Set the click listener for the receipt icon
            receiptIcon.setOnClickListener {
                onReceiptClicked(payment)
            }
        }
    }
}

class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>() {
    override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
        return oldItem == newItem
    }
}