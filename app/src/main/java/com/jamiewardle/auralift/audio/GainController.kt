package com.jamiewardle.auralift.audio

import com.jamiewardle.auralift.model.GainMath
import kotlin.math.abs

/** Writes millibels and reads the platform target back; neither is an acoustic measurement. */
class GainController(private val writeMillibels: (Int) -> Unit, private val readMillibels: () -> Float) {
    var maxGainDb = GainMath.MAX_GAIN_DB
        private set
    var reportedGainDb: Float? = null
        private set
    var note: String? = null
        private set
    private var lastAcceptedMb = 0

    fun apply(gainDb: Float) {
        val targetMb = GainMath.millibels(GainMath.clampGain(gainDb, maxGainDb))
        try {
            writeMillibels(targetMb)
            lastAcceptedMb = targetMb
        } catch (e: IllegalArgumentException) {
            // A device can reject the expanded range. Restore the last accepted value;
            // failure at zero or during a reduction is a broken effect, not a range limit.
            if (targetMb <= lastAcceptedMb) throw e
            writeMillibels(lastAcceptedMb)
            maxGainDb = minOf(maxGainDb, lastAcceptedMb / 100f)
        }
        refresh()
    }

    fun refresh() {
        reportedGainDb = runCatching { readMillibels() / 100f }.getOrNull()
            ?.takeIf { it.isFinite() && it in 0f..GainMath.MAX_GAIN_DB }
        note = when {
            reportedGainDb == null -> "Android gain readback unavailable"
            abs(reportedGainDb!! - lastAcceptedMb / 100f) > 0.05f ->
                "Android reports a different target from the requested gain"
            maxGainDb < GainMath.MAX_GAIN_DB -> "Device rejected higher gain; limited to +${"%.2f".format(maxGainDb)} dB"
            else -> null
        }
    }
}
