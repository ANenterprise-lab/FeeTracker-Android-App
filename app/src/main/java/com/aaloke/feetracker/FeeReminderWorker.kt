package com.aaloke.feetracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class FeeReminderWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // --- We will rebuild the notification logic later ---
        // For now, we just tell the system the work succeeded.
        return Result.success()
    }
}