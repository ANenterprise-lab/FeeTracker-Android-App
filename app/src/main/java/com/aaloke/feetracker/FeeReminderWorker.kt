package com.aaloke.feetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class FeeReminderWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val today = LocalDate.now()

        // Only run if it's after the 10th of the month
        if (today.dayOfMonth <= 10) return Result.success()

        val currentMonthName = today.month.name.lowercase().capitalize()
        val students = db.appDao().getAllStudents().first()
        val allFees = db.appDao().getAllFees().first()

        val overdueStudents = students.filter { student ->
            allFees.none { fee ->
                fee.studentId == student.id &&
                        fee.month.equals(currentMonthName, ignoreCase = true) &&
                        fee.year == today.year
            }
        }

        if (overdueStudents.isNotEmpty()) {
            sendNotification(overdueStudents.joinToString(", ") { it.name })
        }

        return Result.success()
    }

    private fun sendNotification(overdueNames: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fee_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Fee Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Overdue Student Fees")
            .setContentText("Fees may be overdue for: $overdueNames")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()

        notificationManager.notify(1, notification)
    }
}