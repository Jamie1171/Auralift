package com.jamiewardle.auralift.audio

import android.app.*
import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.*
import android.media.audiofx.AudioEffect
import android.os.*
import androidx.core.content.ContextCompat
import com.jamiewardle.auralift.*
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.controls.BoostWidget
import com.jamiewardle.auralift.controls.BoostTile
import com.jamiewardle.auralift.controls.FloatingControls
import com.jamiewardle.auralift.controls.word
import com.jamiewardle.auralift.model.*
import kotlinx.coroutines.*

class BoostService : Service() {
    private val app get() = application as AuraliftApplication
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val handler = Handler(Looper.getMainLooper())
    private val chains = linkedMapOf<Int, EffectChain>()
    private val sessions = linkedSetOf<Int>()
    private lateinit var audio: AudioManager
    private var p = Preferences()
    private var commandedGain = 0f
    private var comparingOriginal = false
    private var deadline = 0L
    private var stopped = false
    private var routeSignature = emptySet<Int>()
    private var rampJob: Job? = null
    private var lastNotification = ""
    private lateinit var floating: FloatingControls
    private var fadeMultiplier = 1f

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) = routeChanged()
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) = routeChanged()
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                if (p.resetOnRouteChange) app.settings.update { it.copy(gainDb = 0f) }
                rebuild(); return
            }
            val id = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, -1)
            if (id <= 0) return
            if (intent.action == AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION) {
                sessions.remove(id); chains.remove(id)?.release()
            } else if (intent.action == AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION && sessions.size < 16) {
                sessions.add(id)
                if (!app.adAudio.blocked.value && p.mode == EffectMode.PLAYERS && id !in chains) chains[id] = newChain(id)
            }
            ramp(); publish()
        }
    }
    private fun newChain(id: Int) = EffectChain(id) { handler.post { if (!stopped) { ramp(); publish() } } }

    override fun onCreate() {
        super.onCreate()
        app.access.refresh()
        if (!app.settings.state.value.rememberBoost) app.settings.update { it.copy(gainDb = 0f) }
        p = app.settings.state.value
        floating = FloatingControls(this, { comparingOriginal = !comparingOriginal; ramp(); publish() }, { finish(word(R.string.boost_off)) })
        audio = getSystemService(AudioManager::class.java)
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, word(R.string.channel_audio), NotificationManager.IMPORTANCE_LOW).apply {
            description = word(R.string.channel_audio_hint); setSound(null, null); enableVibration(false)
        })
        val notification = notification(word(R.string.connecting_effects))
        if (Build.VERSION.SDK_INT >= 34) startForeground(NOTIFICATION, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(NOTIFICATION, notification)
        routeSignature = audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map { it.id }.toSet()
        audio.registerAudioDeviceCallback(deviceCallback, handler)
        val filter = IntentFilter().apply {
            addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        }
        // Public Android audio-session protocol. Registration exists only while user-enabled.
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        app.adAudio.changed = { rebuild() }
        rebuild()
        scope.launch {
            app.settings.state.collect { next ->
                val modeChanged = p.mode != next.mode; p = next
                if (modeChanged) rebuild() else ramp()
                publish()
            }
        }
        scope.launch {
            while (isActive) {
                delay(1000)
                app.access.refresh()
                if (!p.backgroundAudio && !app.activityVisible && !app.adAudio.blocked.value) { finish(word(R.string.boost_off)); break }
                if (deadline > 0L && SystemClock.elapsedRealtime() >= deadline) { finish(word(R.string.sleep_finished)); break }
                val nextFade = if (deadline > 0 && ((p.sleepFade && app.access.state.value.pro) || fadeMultiplier < 1f)) ((deadline - SystemClock.elapsedRealtime()) / 30_000f).coerceIn(0f, 1f) else 1f
                // Once a fade begins it never increases gain again, even if Pro expires.
                val effectiveFade = if (deadline > 0) minOf(fadeMultiplier, nextFade) else 1f
                if (fadeMultiplier != effectiveFade) { fadeMultiplier = effectiveFade; ramp() }
                publish()
            }
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            STOP -> finish(word(R.string.boost_off))
            RECONNECT -> rebuild()
            TIMER -> setTimer(intent.getIntExtra("minutes", 0))
            COMPARE -> { comparingOriginal = !comparingOriginal; ramp(); publish() }
        }
        return START_NOT_STICKY
    }
    private fun routeChanged() {
        if (stopped) return
        val current = audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map { it.id }.toSet()
        if (current == routeSignature) return
        routeSignature = current
        if (p.resetOnRouteChange) app.settings.update { it.copy(gainDb = 0f) }
        rebuild()
    }
    private fun rebuild() {
        rampJob?.cancel(); chains.values.forEach { it.release() }; chains.clear(); commandedGain = 0f
        if (stopped) return
        if (app.adAudio.blocked.value) { publish(); return }
        p = app.settings.state.value
        if (p.mode == EffectMode.SYSTEM) chains[0] = newChain(0)
        else sessions.forEach { chains[it] = newChain(it) }
        ramp(); publish()
    }
    private fun ramp() {
        rampJob?.cancel()
        if (app.adAudio.blocked.value || stopped) return
        rampJob = scope.launch {
            var target: Float
            var lastPublish = 0L
            // Reductions are immediate; increases take small steps to avoid a sudden jump.
            do {
                app.access.refresh()
                if (!isActive || app.adAudio.blocked.value || stopped) break
                val current = app.settings.state.value
                val effectPreferences = current.copy(gainDb = current.gainDb * fadeMultiplier).forComparison(comparingOriginal)
                val cap = chains.values.filter { it.gainAvailable }.minOfOrNull { it.maxGain } ?: 0f
                target = GainMath.clampGain(effectPreferences.gainDb,
                    minOf(current.limitDb, cap, GainMath.allowance(app.access.state.value.pro)))
                commandedGain = minOf(target, commandedGain + 0.25f)
                chains.values.forEach { it.apply(effectPreferences, commandedGain) }
                if (SystemClock.elapsedRealtime() - lastPublish >= 150L) {
                    publish(); lastPublish = SystemClock.elapsedRealtime()
                }
                if (commandedGain < target) delay(25)
            } while (commandedGain < target && isActive)
            publish()
        }
    }
    private fun publish() {
        if (stopped) return
        val connected = chains.values.filter { it.gainAvailable }
        val linked = connected.isNotEmpty()
        val native = linked && connected.all { it.compression }
        val eqOnly = linked && connected.none { it.compression }
        connected.forEach { it.refreshReadback() }
        val reportedValues = connected.mapNotNull { it.reportedGain }
        val reportedGain = if ((native || eqOnly) && reportedValues.size == connected.size) reportedValues.minOrNull() else null
        val cap = connected.minOfOrNull { it.maxGain } ?: GainMath.MAX_GAIN_DB
        val waiting = p.mode == EffectMode.PLAYERS && chains.isEmpty()
        val message = when { app.adAudio.blocked.value -> word(R.string.ad_audio_paused); waiting -> word(R.string.waiting_player); !linked -> word(R.string.effect_unavailable); comparingOriginal -> word(R.string.comparing_sound); native -> word(R.string.effect_connected); else -> word(R.string.eq_connected) }
        val detail = when {
            app.adAudio.blocked.value -> word(R.string.ad_audio_hint)
            waiting -> word(R.string.waiting_player_hint)
            !linked -> word(R.string.effect_unavailable_hint)
            comparingOriginal -> word(R.string.comparing_hint)
            cap < p.gainDb -> word(R.string.effect_limited, cap)
            p.mode == EffectMode.SYSTEM -> word(R.string.system_mix_hint)
            else -> word(R.string.player_sessions_hint, connected.size)
        }
        val readback = when {
            !linked -> word(R.string.no_gain_connected)
            !native && !eqOnly -> word(R.string.mixed_effects)
            reportedGain == null -> word(R.string.readback_unavailable)
            native -> word(R.string.android_target, reportedGain)
            else -> word(R.string.android_eq_peak, reportedGain)
        }
        app.engine.value = EngineState(
            running = true, linked = linked, message = message, detail = detail, gainAvailable = linked,
            eqAvailable = connected.any { it.eqAvailable }, nativeCompression = native, maxGainDb = cap,
            reportedGainDb = reportedGain, comparingOriginal = comparingOriginal, gainReadback = readback,
            sessionCount = connected.size, timerEndsAt = deadline,
            diagnostics = "Auralift ${BuildConfig.VERSION_NAME}\nAndroid ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}\n${Build.MANUFACTURER} ${Build.MODEL}\nMode: ${p.mode}\n" +
                "Requested: +${p.gainDb} dB; range: +${p.limitDb} dB; comparing: $comparingOriginal\n" +
                "Media playing: ${audio.isMusicActive}\nConnected output types: ${audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map { it.type }.distinct()}\n" +
                chains.values.joinToString("\n") { it.diagnostic } +
                "\nReadback is the Android effect setting, not measured loudness. A player can bypass a connected effect.")
        floating.refresh()
        val summary = if (comparingOriginal && linked) word(R.string.comparing_notification) else if (linked) readback else message
        if (summary != lastNotification) {
            lastNotification = summary
            if (Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                getSystemService(NotificationManager::class.java).notify(NOTIFICATION, notification(summary))
            }
            BoostWidget.refresh(this); BoostTile.refresh(this)
        }
    }
    private fun notification(text: String): Notification {
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stop = PendingIntent.getService(this, 1, Intent(this, BoostService::class.java).setAction(STOP), PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_wave)
            .setContentTitle("Auralift").setContentText(text).setContentIntent(open)
            .setOngoing(true).setOnlyAlertOnce(true).setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(Notification.Action.Builder(null, word(R.string.stop_boost), stop).build()).build()
    }
    private fun timerIntent() = PendingIntent.getBroadcast(this, 2, Intent(this, TimerReceiver::class.java), PendingIntent.FLAG_IMMUTABLE)
    private fun setTimer(minutes: Int) {
        getSystemService(AlarmManager::class.java).cancel(timerIntent())
        deadline = if (minutes in 1..120) SystemClock.elapsedRealtime() + minutes * 60_000L else 0L
        fadeMultiplier = 1f; ramp()
        if (deadline > 0) getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, deadline, timerIntent())
        publish()
    }
    private fun finish(message: String) {
        if (stopped) return
        stopped = true; rampJob?.cancel(); chains.values.forEach { it.release() }; chains.clear()
        if (::floating.isInitialized) floating.dismiss()
        app.engine.value = EngineState(message = message, detail = word(R.string.media_keeps_playing), diagnostics = app.engine.value.diagnostics)
        getSystemService(AlarmManager::class.java).cancel(timerIntent())
        BoostWidget.refresh(this); BoostTile.refresh(this)
        stopForeground(STOP_FOREGROUND_REMOVE); stopSelf()
    }
    override fun onDestroy() {
        app.adAudio.changed = null
        finish(word(R.string.boost_off))
        runCatching { unregisterReceiver(receiver) }; audio.unregisterAudioDeviceCallback(deviceCallback)
        handler.removeCallbacksAndMessages(null); scope.cancel(); super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object {
        const val START = "com.jamiewardle.auralift.START"
        const val STOP = "com.jamiewardle.auralift.STOP"
        const val RECONNECT = "com.jamiewardle.auralift.RECONNECT"
        const val TIMER = "com.jamiewardle.auralift.TIMER"
        const val COMPARE = "com.jamiewardle.auralift.COMPARE"
        private const val CHANNEL = "audio_controls"
        private const val NOTIFICATION = 17
        fun start(context: Context) { context.startForegroundService(Intent(context, BoostService::class.java).setAction(START)) }
        fun stop(context: Context) { context.stopService(Intent(context, BoostService::class.java)) }
    }
}

class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val deadline = (context.applicationContext as AuraliftApplication).engine.value.timerEndsAt
        // Ignore an old alarm delivered after cancellation or after a new timer was set.
        if (deadline > 0 && SystemClock.elapsedRealtime() >= deadline) BoostService.stop(context)
    }
}
