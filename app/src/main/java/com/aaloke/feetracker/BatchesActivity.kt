package com.aaloke.feetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aaloke.feetracker.database.BatchDatabase
import com.aaloke.feetracker.databinding.ActivityBatchesBinding
import com.aaloke.feetracker.models.Batch
import com.aaloke.feetracker.viewmodels.BatchesViewModel
import com.aaloke.feetracker.viewmodels.BatchesViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class BatchesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBatchesBinding
    private lateinit var batchViewModel: BatchesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBatchesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val batchDao = BatchDatabase.getDatabase(this).batchDao()

        val viewModelFactory = BatchesViewModelFactory(batchDao)
        batchViewModel = ViewModelProvider(this, viewModelFactory)[BatchesViewModel::class.java]

        // Setup the RecyclerView with an empty list initially
        val batchAdapter = BatchesAdapter(
            batches = listOf(),
            onClick = { batch ->
                // Handle a click on a batch here. We'll implement this later.
            },
            onLongClick = { batch ->
                showDeleteConfirmationDialog(batch)
                true // Return true to consume the long click event
            }
        )
        binding.batchesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.batchesRecyclerView.adapter = batchAdapter

        // Observe the batches from the ViewModel
        batchViewModel.allBatches.observe(this) { batches ->
            // Create a new adapter instance with the latest data and listeners
            val newAdapter = BatchesAdapter(
                batches = batches,
                onClick = { batch ->
                    // Handle a click on a batch here
                },
                onLongClick = { batch ->
                    showDeleteConfirmationDialog(batch)
                    true // Consume the long click
                }
            )
            binding.batchesRecyclerView.adapter = newAdapter
        }

        binding.fabAddBatch.setOnClickListener {
            showAddBatchDialog()
        }
    }

    private fun showAddBatchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_batch, null)
        val batchNameEditText = dialogView.findViewById<TextInputEditText>(R.id.batch_name_edittext)
        val batchDescriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.batch_description_edittext)

        MaterialAlertDialogBuilder(this)
            .setTitle("Create New Batch")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = batchNameEditText.text.toString().trim()
                val description = batchDescriptionEditText.text.toString().trim()

                if (name.isNotEmpty()) {
                    val newBatch = Batch(name = name, description = description)
                    batchViewModel.insert(newBatch)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(batch: Batch) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Batch")
            .setMessage("Are you sure you want to delete the batch '${batch.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                batchViewModel.delete(batch)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}