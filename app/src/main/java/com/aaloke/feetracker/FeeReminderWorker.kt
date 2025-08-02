package com.aaloke.feetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Locale

class FeeReminderWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val calendar = Calendar.getInstance()
        // Only run if it's after the 8th of the month
        if (calendar.get(Calendar.DAY_OF_MONTH) <= 8) return Result.success()

        val db = AppDatabase.getDatabase(applicationContext)
        val students = db.appDao().getAllStudents().first()
        val allFees = db.appDao().getAllFees().first()

        val currentMonthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val currentYear = calendar.get(Calendar.YEAR)

        val overdueStudents = students.filter { student ->
            allFees.none { fee ->
                fee.studentId == student.id &&
                        fee.month.equals(currentMonthName, ignoreCase = true) &&
                        fee.year == currentYear
            }
        }

        if (overdueStudents.isNotEmpty()) {
            sendNotification(overdueStudents.size)
        }

        return Result.success()
    }

    private fun sendNotification(overdueCount: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fee_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Fee Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open NotificationActivity
        val intent = Intent(applicationContext, NotificationActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Pending Fee Reminders")
            .setContentText("$overdueCount student(s) have pending fees for this month.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent) // Set the intent
            .setAutoCancel(true) // Dismiss notification on tap
            .build()

        notificationManager.notify(1, notification)
    }
}