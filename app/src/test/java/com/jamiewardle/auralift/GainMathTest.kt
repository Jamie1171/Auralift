package com.jamiewardle.auralift

import com.jamiewardle.auralift.model.*
import org.junit.Assert.*
import org.junit.Test

class GainMathTest {
    @Test fun correctUnitsAndAmplitude() {
        assertEquals(600, GainMath.millibels(6f))
        assertEquals(100, GainMath.amplitudePercent(0f))
        assertEquals(200, GainMath.amplitudePercent(6f))
        assertEquals(794, GainMath.amplitudePercent(18f))
        assertEquals(3000, GainMath.millibels(30f))
        assertEquals(3162, GainMath.amplitudePercent(30f))
        assertEquals(3500, GainMath.millibels(35f))
        assertEquals(5623, GainMath.amplitudePercent(35f))
    }
    @Test fun malformedAndOutOfRangeGainCannotReachAudioApi() {
        assertEquals(0f, GainMath.clampGain(Float.NaN, 6f), 0f)
        assertEquals(0f, GainMath.clampGain(Float.POSITIVE_INFINITY, 6f), 0f)
        assertEquals(0f, GainMath.clampGain(-4f, 6f), 0f)
        assertEquals(6f, GainMath.clampGain(18f, 6f), 0f)
        assertEquals(3500, GainMath.millibels(999f))
    }
    @Test fun comparisonZeroesBoostAndEqWithoutLosingChosenSound() {
        val chosen = Preferences(gainDb = 30f, preset = SoundPreset.VOICE)
        val original = chosen.forComparison(true)
        assertEquals(0f, original.gainDb)
        assertEquals(List(5) { 0f }, original.curve)
        assertEquals(30f, chosen.gainDb)
        assertEquals(SoundPreset.VOICE, chosen.preset)
        assertEquals(chosen, chosen.forComparison(false))
    }
    @Test fun equalizerNeverAddsHiddenPositiveGain() {
        SoundPreset.entries.forEach { preset ->
            val normalized = GainMath.normalizedCurve(preset.curve)
            assertTrue(normalized.all { it <= 0f })
            assertTrue(normalized.all { it >= -12f })
        }
    }
    @Test fun interpolationPreservesShapeAcrossDeviceBandCounts() {
        val voice = SoundPreset.VOICE.curve
        assertEquals(-9f, GainMath.atFrequency(60f, voice), 0.001f)
        assertEquals(0f, GainMath.atFrequency(3600f, voice), 0.001f)
        val half = kotlin.math.sqrt(910f * 3600f)
        assertEquals(-1.5f, GainMath.atFrequency(half, voice), 0.001f)
        assertEquals(-9f, GainMath.atFrequency(20f, voice), 0.001f)
        assertEquals(-2f, GainMath.atFrequency(20000f, voice), 0.001f)
    }
    @Test fun malformedCurveIsNormalizedToFiveFiniteBands() {
        val result = GainMath.normalizedCurve(listOf(Float.NaN, 999f))
        assertEquals(5, result.size)
        assertTrue(result.all { it.isFinite() && it in -12f..0f })
    }
}
