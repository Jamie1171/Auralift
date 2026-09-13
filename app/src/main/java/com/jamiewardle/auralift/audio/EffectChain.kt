package com.jamiewardle.auralift.audio

import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import com.jamiewardle.auralift.model.GainMath
import com.jamiewardle.auralift.model.Preferences
import kotlin.math.roundToInt

/** Exactly one chain per session; system and player-session chains never coexist. */
@Suppress("DEPRECATION")
class EffectChain(val session: Int, private val controlChanged: () -> Unit) {
    private var loudness: LoudnessEnhancer? = null
    private var equalizer: Equalizer? = null
    private var gainControl: GainController? = null
    private val notes = mutableListOf<String>()
    private var eqLevels = emptyList<Float>()

    init {
        loudness = open("Loudness", { LoudnessEnhancer(session) }) { it.setTargetGain(0) }
        gainControl = loudness?.let { fx -> GainController({ fx.setTargetGain(it) }, { fx.targetGain }) }
        equalizer = open("Equalizer", { Equalizer(0, session) }) { eq ->
            repeat(eq.numberOfBands.toInt()) { eq.setBandLevel(it.toShort(), 0) }
        }
    }
    private fun <T : AudioEffect> open(name: String, create: () -> T, initialize: (T) -> Unit): T? {
        var effect: T? = null
        return try {
            effect = create()
            check(effect.hasControl()) { "controlled by another app" }
            initialize(effect)
            check(effect.setEnabled(true) == AudioEffect.SUCCESS && effect.enabled) { "enable rejected" }
            effect.setControlStatusListener { _, _ -> controlChanged() }
            effect.setEnableStatusListener { _, _ -> controlChanged() }
            notes += "$name connected to session $session: ${effect.descriptor.name} (${effect.descriptor.implementor})"
            effect
        } catch (e: RuntimeException) {
            effect?.let { runCatching { it.release() } }
            notes += "$name: ${e.javaClass.simpleName} ${e.message.orEmpty().take(160)}"
            null
        }
    }
    private fun controlled(effect: AudioEffect?) = runCatching { effect?.hasControl() == true && effect.enabled }.getOrDefault(false)
    val compression get() = controlled(loudness)
    val eqAvailable get() = controlled(equalizer)
    val gainAvailable get() = compression || eqAvailable
    val maxGain: Float get() = if (compression) gainControl?.maxGainDb ?: 0f else if (eqAvailable)
        runCatching { (equalizer!!.bandLevelRange[1] / 100f).coerceIn(0f, GainMath.MAX_GAIN_DB) }.getOrDefault(0f) else 0f
    val reportedGain: Float? get() = if (compression) gainControl?.reportedGainDb else if (eqAvailable) eqLevels.maxOrNull() else null
    val diagnostic get() = notes.joinToString("\n") +
        "\nSession $session: loudness controlled/enabled=$compression; EQ controlled/enabled=$eqAvailable" +
        "\nEffect gain limit: +${"%.2f".format(maxGain)} dB" +
        (if (compression) "\nAndroid loudness target: ${gainControl?.reportedGainDb?.let { "%+.2f dB".format(it) } ?: "unavailable"}" else "") +
        (gainControl?.note?.let { "\n$it" } ?: "") +
        (if (eqLevels.isNotEmpty()) "\nAndroid EQ bands (dB): ${eqLevels.joinToString { "%+.2f".format(it) }}" else "")

    fun refreshReadback() {
        if (compression) gainControl?.refresh()
        eqLevels = if (eqAvailable) runCatching {
            val eq = equalizer!!
            List(eq.numberOfBands.toInt()) { eq.getBandLevel(it.toShort()) / 100f }
        }.getOrDefault(emptyList()) else emptyList()
    }

    fun apply(p: Preferences, gainDb: Float): Boolean {
        var applied = false
        if (controlled(loudness)) {
            try { gainControl!!.apply(gainDb); applied = true }
            catch (e: RuntimeException) { notes += "Gain update failed: ${e.javaClass.simpleName}"; releaseLoudness() }
        }
        if (controlled(equalizer)) {
            try {
                val eq = equalizer!!; val range = eq.bandLevelRange
                repeat(eq.numberOfBands.toInt()) { i ->
                    val band = i.toShort()
                    val shape = GainMath.atFrequency(eq.getCenterFreq(band) / 1000f, p.curve)
                    val preamp = if (compression) 0f else gainDb.coerceAtMost(maxGain)
                    eq.setBandLevel(band, ((shape + preamp) * 100).roundToInt().coerceIn(range[0].toInt(), range[1].toInt()).toShort())
                }
                applied = true
            } catch (e: RuntimeException) {
                notes += "EQ update failed: ${e.javaClass.simpleName}"; releaseEqualizer()
            }
        }
        refreshReadback()
        return applied
    }
    private fun releaseLoudness() {
        loudness?.let { fx ->
            runCatching { fx.setControlStatusListener(null); fx.setEnableStatusListener(null) }
            runCatching { if (fx.hasControl()) { fx.setTargetGain(0); fx.enabled = false } }
            runCatching { fx.release() }
        }; loudness = null; gainControl = null
    }
    private fun releaseEqualizer() {
        equalizer?.let { fx ->
            runCatching { fx.setControlStatusListener(null); fx.setEnableStatusListener(null) }
            runCatching { if (fx.hasControl()) { repeat(fx.numberOfBands.toInt()) { fx.setBandLevel(it.toShort(), 0) }; fx.enabled = false } }
            runCatching { fx.release() }
        }; equalizer = null; eqLevels = emptyList()
    }
    fun release() { releaseLoudness(); releaseEqualizer() }
}
