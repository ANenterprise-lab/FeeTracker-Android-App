package com.aaloke.feetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aaloke.feetracker.models.Batch

class BatchesAdapter(
    private val batches: List<Batch>,
    private val onClick: (Batch) -> Unit,
    private val onLongClick: (Batch) -> Boolean
) : RecyclerView.Adapter<BatchesAdapter.BatchViewHolder>() {

    class BatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.batch_name_textview)
        val descriptionTextView: TextView = itemView.findViewById(R.id.batch_description_textview)

        fun bind(batch: Batch, onClick: (Batch) -> Unit, onLongClick: (Batch) -> Boolean) {
            nameTextView.text = batch.name
            descriptionTextView.text = batch.description

            itemView.setOnClickListener {
                onClick(batch)
            }
            itemView.setOnLongClickListener {
                onLongClick(batch)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_batch, parent, false)
        return BatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: BatchViewHolder, position: Int) {
        val batch = batches[position]
        holder.bind(batch, onClick, onLongClick)
    }

    override fun getItemCount(): Int = batches.size
}