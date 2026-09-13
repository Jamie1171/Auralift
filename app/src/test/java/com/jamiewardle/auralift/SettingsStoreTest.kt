package com.jamiewardle.auralift

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.model.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.Config

// These framework/logic checks do not need Robolectric native font loading.
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsStoreTest {
    private lateinit var context: Context
    @Before fun reset() { context = ApplicationProvider.getApplicationContext(); context.getSharedPreferences("auralift", 0).edit().clear().commit() }
    @Test fun profileRoundTripIncludesCurveAndClampsToCurrentLimit() {
        val store = SettingsStore(context, { GainMath.MAX_GAIN_DB })
        store.update { it.copy(profile = OutputProfile.BLUETOOTH, limitDb = 20f, gainDb = 15f, preset = SoundPreset.CUSTOM, customEq = listOf(-3f, -1f, 0f, 2f, 3f)) }
        store.saveProfile()
        store.update { Preferences(limitDb = 5f) }
        assertTrue(store.loadProfile(OutputProfile.BLUETOOTH))
        assertEquals(5f, store.state.value.gainDb)
        assertEquals(SoundPreset.CUSTOM, store.state.value.preset)
        assertEquals(listOf(-3f, -1f, 0f, 2f, 3f), store.state.value.customEq)
        assertEquals(store.state.value, SettingsStore(context, { GainMath.MAX_GAIN_DB }).state.value)
    }
    @Test fun loadingMissingProfileDoesNotDiscardCurrentSound() {
        val store = SettingsStore(context, { GainMath.MAX_GAIN_DB })
        store.update { it.copy(gainDb = 3f, preset = SoundPreset.VOICE) }
        assertFalse(store.loadProfile(OutputProfile.HEADPHONES))
        assertEquals(3f, store.state.value.gainDb)
        assertEquals(SoundPreset.VOICE, store.state.value.preset)
    }
    @Test fun reducingRangeImmediatelyClampsSavedGain() {
        val store = SettingsStore(context, { GainMath.MAX_GAIN_DB })
        store.update { it.copy(limitDb = 20f, gainDb = 16f) }
        store.update { it.copy(limitDb = 5f) }
        assertEquals(5f, SettingsStore(context, { GainMath.MAX_GAIN_DB }).state.value.gainDb)
    }
    @Test fun upgradeExposesFullRangeWithoutIncreasingExistingGain() {
        context.getSharedPreferences("auralift", 0).edit().putFloat("limit", 6f).putFloat("gain", 3f).commit()
        val store = SettingsStore(context, { GainMath.MAX_GAIN_DB })
        assertEquals(35f, store.state.value.limitDb)
        assertEquals(3f, store.state.value.gainDb)
        store.update { it.copy(limitDb = 10f) }
        assertEquals(10f, SettingsStore(context, { GainMath.MAX_GAIN_DB }).state.value.limitDb)
    }
    @Test fun fullGainAndProfileSurvivePersistence() {
        val store = SettingsStore(context, { GainMath.MAX_GAIN_DB })
        assertEquals(0f, store.state.value.gainDb)
        assertEquals(35f, store.state.value.limitDb)
        store.update { it.copy(gainDb = 35f) }
        store.saveProfile()
        val restored = SettingsStore(context, { GainMath.MAX_GAIN_DB })
        assertEquals(35f, restored.state.value.gainDb)
        restored.update { it.copy(gainDb = 0f) }
        assertTrue(restored.loadProfile(OutputProfile.SPEAKER))
        assertEquals(35f, restored.state.value.gainDb)
    }
}
