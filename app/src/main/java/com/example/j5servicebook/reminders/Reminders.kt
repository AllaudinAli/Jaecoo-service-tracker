package com.example.j5servicebook.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.j5servicebook.J5App
import com.example.j5servicebook.MainActivity
import com.example.j5servicebook.R
import com.example.j5servicebook.domain.Notice
import com.example.j5servicebook.domain.ReminderPlanner
import com.example.j5servicebook.domain.ScheduleEngine
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/** Runs the schedule engine once a day and posts any reminder stage not yet sent. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as J5App).container
        val data = container.repository.snapshot() ?: return Result.success()
        if (!Notifier.canPost(applicationContext)) return Result.success()

        val settings = container.settings.settings.first()
        val engine = ScheduleEngine(data.profile.purchaseDate, data.readings, data.records)
        val notices = ReminderPlanner.plan(engine, LocalDate.now(), settings.leadDays, container.settings.sentKeys())
        notices.forEach { Notifier.post(applicationContext, it) }
        if (notices.isNotEmpty()) container.settings.addSentKeys(notices.map { it.key })
        return Result.success()
    }
}

object ReminderScheduler {
    private const val DAILY = "daily-reminders"
    private const val HOUR = 9

    fun schedule(context: Context) {
        val now = ZonedDateTime.now()
        var next = now.withHour(HOUR).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(Duration.between(now, next).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(DAILY, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /** Runs the check right away, for testing from Settings. */
    fun runNow(context: Context) {
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<ReminderWorker>().build())
    }
}

object Notifier {
    private const val CHANNEL = "reminders"
    const val EXTRA_ACTION = "com.example.j5servicebook.ACTION"
    const val ACTION_LOG_SERVICE = "log_service"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL, "Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Service, maintenance and warranty reminders"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canPost(context: Context): Boolean =
        (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
            NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun post(context: Context, notice: Notice) {
        if (!canPost(context)) return
        val builder = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_service)
            .setContentTitle(notice.title)
            .setContentText(notice.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notice.text))
            .setContentIntent(activityIntent(context, null, 0))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
        if (notice.logAction) {
            builder.addAction(0, "Log service", activityIntent(context, ACTION_LOG_SERVICE, 1))
        }
        try {
            NotificationManagerCompat.from(context).notify(notice.tag, 0, builder.build())
        } catch (_: SecurityException) {
            // Permission revoked between the check and the post.
        }
    }

    private fun activityIntent(context: Context, action: String?, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .apply { if (action != null) putExtra(EXTRA_ACTION, action) }
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
