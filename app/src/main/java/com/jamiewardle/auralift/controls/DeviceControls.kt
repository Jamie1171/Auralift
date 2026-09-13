package com.jamiewardle.auralift.controls

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.os.*
import android.provider.Settings
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.model.Haptics

fun Context.localized(): Context {
    val locales = AppCompatDelegate.getApplicationLocales()
    if (locales.isEmpty) return this
    return createConfigurationContext(Configuration(resources.configuration).apply { setLocales(locales.unwrap() as android.os.LocaleList) })
}
fun Context.word(@StringRes id: Int, vararg args: Any) = localized().getString(id, *args)
fun Context.openSettings(intent: Intent): Boolean = try {
    startActivity(intent); true
} catch (_: RuntimeException) {
    Toast.makeText(this, word(R.string.settings_unavailable), Toast.LENGTH_SHORT).show(); false
}
object MediaActions {
    fun send(context: Context, key: Int) {
        try {
            val audio = context.getSystemService(AudioManager::class.java)
            audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, key))
            audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, key))
        } catch (_: RuntimeException) { Toast.makeText(context, context.word(R.string.open_player_hint), Toast.LENGTH_SHORT).show() }
    }
}
object HapticTick {
    private var last = 0L
    fun play(context: Context, level: Haptics) {
        if (level == Haptics.OFF || SystemClock.elapsedRealtime() - last < 65 ||
            Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 0) return
        last = SystemClock.elapsedRealtime()
        val vibrator = if (Build.VERSION.SDK_INT >= 31) context.getSystemService(VibratorManager::class.java).defaultVibrator
            else @Suppress("DEPRECATION") context.getSystemService(android.os.Vibrator::class.java)
        if (!vibrator.hasVibrator()) return
        val amplitude = when (level) { Haptics.LIGHT -> 40; Haptics.MEDIUM -> 85; Haptics.STRONG -> 140; else -> 0 }
        runCatching { vibrator.vibrate(VibrationEffect.createOneShot(12, if (vibrator.hasAmplitudeControl()) amplitude else VibrationEffect.DEFAULT_AMPLITUDE)) }
    }
}
