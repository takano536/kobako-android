package io.github.takano536.kobako

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import io.github.takano536.kobako.notifications.schedulePaymentWorker

class KobakoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            runCatching {
                getSystemService(NotificationManager::class.java)
                    ?.createNotificationChannel(
                        NotificationChannel(
                            "payments",
                            "支払い",
                            NotificationManager.IMPORTANCE_DEFAULT,
                        ).apply {
                            lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
                        },
                    )
            }
        }
    }
}
