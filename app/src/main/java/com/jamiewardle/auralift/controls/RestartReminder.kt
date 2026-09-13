package com.jamiewardle.auralift.controls

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.jamiewardle.auralift.*

/** Opt-in notification after reboot. Never starts a foreground service or raises volume. */
class RestartReminder : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as AuraliftApplication
        if (!app.settings.state.value.restartReminder) return
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, context.word(R.string.restart_reminder), NotificationManager.IMPORTANCE_LOW).apply { setSound(null, null) })
        val open = PendingIntent.getActivity(context, 18, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        manager.notify(ID, Notification.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_wave)
            .setContentTitle(context.word(R.string.restart_title)).setContentText(context.word(R.string.restart_body))
            .setContentIntent(open).setAutoCancel(true).build())
    }
    companion object { const val ID = 18; private const val CHANNEL = "restart_reminders" }
}
