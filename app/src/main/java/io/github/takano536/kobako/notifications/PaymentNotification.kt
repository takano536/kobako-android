package io.github.takano536.kobako.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.takano536.kobako.data.api.createApi
import io.github.takano536.kobako.data.local.SettingsStore
import io.github.takano536.kobako.data.repository.ApiRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

fun shouldNotify(dueOn: LocalDate, today: LocalDate): Boolean = dueOn == today || dueOn == today.plusDays(1)
class NotificationDeduplicator { private val sent = mutableSetOf<String>(); fun markIfNew(cardId: Long, dueOn: LocalDate): Boolean = sent.add("$cardId:$dueOn") }
fun schedulePaymentWorker(context: Context) { val request = PeriodicWorkRequestBuilder<PaymentWorker>(1, TimeUnit.DAYS).setInitialDelay(1, TimeUnit.HOURS).build(); WorkManager.getInstance(context).enqueueUniquePeriodicWork("kobako-payment", ExistingPeriodicWorkPolicy.UPDATE, request) }
class PaymentWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val settings = SettingsStore(context).state.first()
        if (!settings.notifications || settings.demo || settings.url.isBlank()) return Result.success()
        val token = SettingsStore(context).token() ?: return Result.success()
        val balances = ApiRepository(createApi(settings.url) { token }).balances().getOrNull() ?: return Result.success()
        val today = LocalDate.now(); balances.groups.flatMap { it.accounts }.forEach { account -> account.nextPayment?.let { payment -> if (shouldNotify(payment.dueOn, today) && markIfNew(account.id, payment.dueOn)) notify(account.id) } }
        return Result.success()
    }
    private fun markIfNew(cardId: Long, dueOn: LocalDate): Boolean { val key = "$cardId:$dueOn"; val prefs = context.getSharedPreferences("notification_records", Context.MODE_PRIVATE); if (prefs.getBoolean(key, false)) return false; prefs.edit().putBoolean(key, true).apply(); return true }
    @SuppressLint("MissingPermission", "NotificationPermission")
    private fun notify(id: Long) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        if (Build.VERSION.SDK_INT >= 26) context.getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("payments", "支払い", NotificationManager.IMPORTANCE_DEFAULT).apply { lockscreenVisibility = Notification.VISIBILITY_PRIVATE })
        context.getSystemService(NotificationManager::class.java).notify(id.toInt(), NotificationCompat.Builder(context, "payments").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("支払い日のお知らせ").setContentText("カードの支払い日が近づいています").setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setAutoCancel(true).build())
    }
}