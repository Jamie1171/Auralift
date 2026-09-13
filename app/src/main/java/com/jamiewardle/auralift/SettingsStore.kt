package com.jamiewardle.auralift

import android.content.Context
import androidx.core.content.edit
import com.jamiewardle.auralift.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context,
                    private val allowedGain: () -> Float = { GainMath.FREE_MAX_GAIN_DB },
                    private val refreshAccess: () -> Unit = {}) {
    private val disk = context.getSharedPreferences("auralift", Context.MODE_PRIVATE)
    init {
        if (disk.getInt("gainRangeVersion", 0) < 3) {
            val version = disk.getInt("gainRangeVersion", 0)
            val oldLimit = disk.getFloat("limit", if (version < 2) 6f else 30f)
                .let { if (it in listOf(6f, 12f, 18f, 24f, 30f)) it else 6f }
            val oldGain = GainMath.clampGain(disk.getFloat("gain", 0f), oldLimit)
            // Existing full-range users gain a new selectable ceiling, never extra volume.
            // Respect a manually reduced ceiling by rounding it DOWN to a 5 dB step.
            val requested = if (version < 2 || oldLimit == 30f) GainMath.MAX_GAIN_DB
                else GainMath.ranges.last { it <= oldLimit }
            disk.edit { putFloat("limit", requested); putFloat("gain", oldGain); putInt("gainRangeVersion", 3) }
        }
    }
    private var requestedLimit = disk.getFloat("limit", GainMath.MAX_GAIN_DB)
        .let { if (it in GainMath.ranges) it else GainMath.MAX_GAIN_DB }
    val maximumGain: Float get() = GainMath.clampGain(allowedGain(), GainMath.MAX_GAIN_DB)
    private inline fun <reified T : Enum<T>> enumValue(key: String, fallback: T): T =
        runCatching { enumValueOf<T>(disk.getString(key, fallback.name)!!) }.getOrDefault(fallback)
    private fun read(): Preferences {
        val limit = minOf(requestedLimit, maximumGain)
        return Preferences(
            gainDb = GainMath.clampGain(disk.getFloat("gain", 0f), limit), limitDb = limit,
            preset = enumValue("preset", SoundPreset.BALANCED),
            customEq = GainMath.cleanCurve(List(5) { disk.getFloat("eq$it", 0f) }),
            mode = enumValue("mode", EffectMode.SYSTEM),
            resetOnRouteChange = disk.getBoolean("routeReset", true),
            onboarded = disk.getBoolean("onboarded", false), lightTheme = disk.getBoolean("light", false),
            profile = enumValue("profile", OutputProfile.SPEAKER),
            accent = enumValue("accent", Accent.MINT), haptics = enumValue("haptics", Haptics.LIGHT),
            rememberBoost = disk.getBoolean("rememberBoost", true),
            floatingControls = disk.getBoolean("floating", false), spectrum = disk.getBoolean("spectrum", false),
            sleepFade = disk.getBoolean("sleepFade", false), backgroundAudio = disk.getBoolean("background", true), restartReminder = disk.getBoolean("restartReminder", false)
        )
    }
    private val mutable = MutableStateFlow(read())
    val state = mutable.asStateFlow()
    init { enforceGainLimit() }
    /** Persist a reduction so renewing Pro never restores a previously louder sound. */
    fun enforceGainLimit() = update { it }
    fun setLimit(limit: Float) {
        refreshAccess()
        if (limit !in GainMath.ranges || limit > maximumGain) return
        requestedLimit = limit
        update { it.copy(limitDb = minOf(limit, maximumGain)) }
    }
    fun update(change: (Preferences) -> Preferences) {
        refreshAccess()
        val previous = mutable.value
        val p = change(mutable.value).let {
            if (it.limitDb != previous.limitDb && it.limitDb in GainMath.ranges)
                requestedLimit = minOf(it.limitDb, maximumGain)
            val limit = minOf(requestedLimit, maximumGain)
            it.copy(limitDb = limit, gainDb = GainMath.clampGain(it.gainDb, limit), customEq = GainMath.cleanCurve(it.customEq))
        }
        mutable.value = p
        disk.edit { putFloat("gain", p.gainDb).putFloat("limit", requestedLimit)
            .putString("preset", p.preset.name).putString("mode", p.mode.name)
            .putBoolean("routeReset", p.resetOnRouteChange).putBoolean("onboarded", p.onboarded)
            .putBoolean("light", p.lightTheme).putString("profile", p.profile.name)
            .putString("accent", p.accent.name).putString("haptics", p.haptics.name)
            .putBoolean("rememberBoost", p.rememberBoost).putBoolean("floating", p.floatingControls)
            .putBoolean("spectrum", p.spectrum).putBoolean("sleepFade", p.sleepFade).putBoolean("background", p.backgroundAudio).putBoolean("restartReminder", p.restartReminder)
            .also { e -> p.customEq.forEachIndexed { i, f -> e.putFloat("eq$i", f) } } }
    }
    fun saveProfile() {
        val p = mutable.value; val key = "saved_${p.profile.name}_"
        disk.edit { putBoolean(key + "exists", true).putFloat(key + "gain", p.gainDb)
            .putString(key + "preset", p.preset.name)
            .also { e -> p.customEq.forEachIndexed { i, f -> e.putFloat(key + "eq$i", f) } } }
    }
    fun loadProfile(profile: OutputProfile): Boolean {
        val key = "saved_${profile.name}_"
        if (!disk.getBoolean(key + "exists", false)) { update { it.copy(profile = profile) }; return false }
        update { it.copy(profile = profile, gainDb = disk.getFloat(key + "gain", 0f),
            preset = enumValue(key + "preset", SoundPreset.BALANCED),
            customEq = List(5) { i -> disk.getFloat(key + "eq$i", 0f) }) }
        return true
    }
}
