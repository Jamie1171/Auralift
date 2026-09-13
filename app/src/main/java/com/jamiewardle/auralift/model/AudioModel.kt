package com.jamiewardle.auralift.model

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

enum class SoundPreset(val title: String, val subtitle: String, val curve: List<Float>) {
    BALANCED("Balanced", "Keep the original character", listOf(0f, 0f, 0f, 0f, 0f)),
    VOICE("Voice", "Bring dialogue forward", listOf(-6f, -3f, 0f, 3f, 1f)),
    WARM("Warm", "Fuller lows, softer highs", listOf(3f, 2f, 0f, -2f, -3f)),
    DETAIL("Detail", "A little more definition", listOf(-2f, -1f, 0f, 2f, 3f)),
    CUSTOM("Custom", "Your own sound", listOf(0f, 0f, 0f, 0f, 0f))
}

enum class EffectMode { SYSTEM, PLAYERS }
enum class OutputProfile(val title: String) { SPEAKER("Phone speaker"), HEADPHONES("Wired / USB"), BLUETOOTH("Bluetooth") }
enum class Accent { MINT, OCEAN, AMBER, ORCHID }
enum class Haptics { OFF, LIGHT, MEDIUM, STRONG }

data class Preferences(
    val gainDb: Float = 0f,
    val limitDb: Float = GainMath.MAX_GAIN_DB,
    val preset: SoundPreset = SoundPreset.BALANCED,
    val customEq: List<Float> = List(5) { 0f },
    val mode: EffectMode = EffectMode.SYSTEM,
    val resetOnRouteChange: Boolean = true,
    val onboarded: Boolean = false,
    val lightTheme: Boolean = false,
    val profile: OutputProfile = OutputProfile.SPEAKER,
    val accent: Accent = Accent.MINT,
    val haptics: Haptics = Haptics.LIGHT,
    val rememberBoost: Boolean = true,
    val floatingControls: Boolean = false,
    val spectrum: Boolean = false,
    val sleepFade: Boolean = false,
    val backgroundAudio: Boolean = true,
    val restartReminder: Boolean = false
) {
    val curve get() = if (preset == SoundPreset.CUSTOM) customEq else preset.curve
    fun forComparison(comparing: Boolean) = if (comparing) copy(gainDb = 0f, preset = SoundPreset.BALANCED) else this
}

data class EngineState(
    val running: Boolean = false,
    val linked: Boolean = false,
    val message: String = "Ready when you are",
    val detail: String = "Start your audio, then enable Auralift.",
    val gainAvailable: Boolean = false,
    val eqAvailable: Boolean = false,
    val nativeCompression: Boolean = false,
    val maxGainDb: Float = GainMath.MAX_GAIN_DB,
    val reportedGainDb: Float? = null,
    val comparingOriginal: Boolean = false,
    val gainReadback: String = "Enable boost to check the audio effect.",
    val sessionCount: Int = 0,
    val timerEndsAt: Long = 0L,
    val diagnostics: String = "No audio effects have been opened yet."
)

object GainMath {
    const val FREE_MAX_GAIN_DB = 15f
    const val MAX_GAIN_DB = 35f
    fun allowance(pro: Boolean) = if (pro) MAX_GAIN_DB else FREE_MAX_GAIN_DB
    val ranges = listOf(5f, 10f, 15f, 20f, 25f, 30f, MAX_GAIN_DB)
    val frequencies = listOf(60f, 230f, 910f, 3600f, 14000f)
    fun clampGain(value: Float, limit: Float): Float = if (!value.isFinite() || !limit.isFinite()) 0f else value.coerceIn(0f, limit.coerceIn(0f, MAX_GAIN_DB))
    fun cleanCurve(curve: List<Float>) = List(5) { curve.getOrElse(it) { 0f }.let { v -> if (v.isFinite()) v.coerceIn(-6f, 6f) else 0f } }
    fun millibels(db: Float) = (clampGain(db, MAX_GAIN_DB) * 100).roundToInt()
    fun amplitudePercent(db: Float) = (100.0 * 10.0.pow(db / 20.0)).roundToInt()
    // Normalize positive EQ peaks to zero before loudness gain. EQ never silently adds gain.
    fun normalizedCurve(curve: List<Float>): List<Float> {
        val clean = cleanCurve(curve)
        val peak = maxOf(0f, clean.max())
        return clean.map { it - peak }
    }
    fun atFrequency(hz: Float, curve: List<Float>): Float {
        val shaped = normalizedCurve(curve)
        if (hz <= frequencies.first()) return shaped.first()
        if (hz >= frequencies.last()) return shaped.last()
        val i = frequencies.indexOfFirst { it > hz } - 1
        val t = (log10(hz) - log10(frequencies[i])) / (log10(frequencies[i + 1]) - log10(frequencies[i]))
        return shaped[i] + t * (shaped[i + 1] - shaped[i])
    }
}
