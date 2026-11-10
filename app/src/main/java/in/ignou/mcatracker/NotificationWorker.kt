package in.ignou.mcatracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        return try {
            val prefs = applicationContext.getSharedPreferences("tracker", Context.MODE_PRIVATE)
            val oldIds = prefs.getStringSet("seen_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
            val items = AnnouncementRepository().fetch()
            val fresh = items.filter { it.id !in oldIds }
            if (oldIds.isNotEmpty() && fresh.isNotEmpty()) {
                createChannel()
                val body = fresh.take(4).joinToString("\n") { it.title }
                val notification = NotificationCompat.Builder(applicationContext, CHANNEL)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("New IGNOU MCA_NEW update")
                    .setContentText(fresh.first().title)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true).build()
                try { NotificationManagerCompat.from(applicationContext).notify(1001, notification) }
                catch (_: SecurityException) { }
            }
            prefs.edit().putStringSet("seen_ids", (oldIds + items.map { it.id }).takeLast(500).toSet()).apply()
            androidx.work.ListenableWorker.Result.success()
        } catch (_: Exception) {
            androidx.work.ListenableWorker.Result.retry()
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(CHANNEL, "IGNOU announcements", NotificationManager.IMPORTANCE_DEFAULT))
        }
    }
    companion object { const val CHANNEL = "ignou_updates" }
}
