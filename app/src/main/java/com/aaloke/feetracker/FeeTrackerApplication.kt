package com.aaloke.feetracker

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class FeeTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val workRequest = PeriodicWorkRequestBuilder<FeeReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FeeReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}