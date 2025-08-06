package com.aaloke.feetracker.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aaloke.feetracker.dao.BatchDao
import com.aaloke.feetracker.models.Batch
import kotlinx.coroutines.launch

class BatchesViewModel(private val batchDao: BatchDao) : ViewModel() {

    // A LiveData to hold all the batches from the database, updated automatically.
    val allBatches: LiveData<List<Batch>> = batchDao.getAllBatches().asLiveData()

    // Function to insert a new batch into the database.
    fun insert(batch: Batch) {
        viewModelScope.launch {
            batchDao.insertBatch(batch)
        }
    }
    fun delete(batch: Batch) {
        viewModelScope.launch {
            batchDao.deleteBatch(batch)
        }
    }
}

class BatchesViewModelFactory(private val batchDao: BatchDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BatchesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BatchesViewModel(batchDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}