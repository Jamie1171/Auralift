package com.jamiewardle.auralift.device

import android.content.Context
import android.media.AudioManager
import android.media.AudioTrack
import android.os.SystemClock
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jamiewardle.auralift.SettingsStore
import com.jamiewardle.auralift.audio.BoostService
import com.jamiewardle.auralift.model.EffectMode
import com.jamiewardle.auralift.model.SoundPreset
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreDeviceTest : DeviceHarness() {
    @Test fun firstLaunchRequiresAcknowledgementAndLaterDoesNotStartAudio() {
        onMain { app.settings.update { it.copy(onboarded = false) } }
        launch()
        assertFalse(app.engine.value.running)
        assertEquals(0f, app.settings.state.value.gainDb, 0f)
        click("Enable boost")
        compose.onNodeWithText("A little lift. Start low.").assertIsDisplayed()
        assertFalse(app.engine.value.running)
        compose.onNodeWithText("Later").performClick()
        assertFalse(app.engine.value.running)
        assertFalse(app.settings.state.value.onboarded)
    }

    @Test fun freeControlsPersistAcrossActivityRecreationWithoutEnablingAudio() {
        launch()
        click("+15 dB")
        compose.onNodeWithText("+20 dB").assertDoesNotExist()
        compose.onNodeWithText("+35 dB").assertDoesNotExist()
        compose.onNodeWithContentDescription("Increase boost by half a decibel").assertIsNotEnabled()
        scenario!!.recreate()
        await("recreated activity") { app.activityVisible }
        assertEquals(15f, app.settings.state.value.gainDb, 0f)
        val restored = SettingsStore(app)
        assertEquals(15f, restored.state.value.gainDb, 0f)
        assertFalse(app.engine.value.running)
    }

    @Test fun foregroundServiceAndNotificationStopLeaveTestPlayerAndVolumeAlone() {
        launch()
        val player = playerSession()
        val volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        start()
        await("Stop notification") { notifications().any { it.id == 17 } }
        val stop = notifications().first { it.id == 17 }.notification.actions
            .first { it.title.toString() == "Stop boost" }.actionIntent
        stop.send()
        await("notification Stop releases effects") { !app.engine.value.running }
        await("notification removed") { notifications().none { it.id == 17 } }
        assertFalse(app.engine.value.linked)
        assertEquals(0, app.engine.value.sessionCount)
        assertEquals(AudioTrack.PLAYSTATE_PLAYING, player.playState)
        assertEquals(volume, audio.getStreamVolume(AudioManager.STREAM_MUSIC))
    }

    @Test fun backgroundDisabledStopsOnHomeButNotConfigurationRecreation() {
        onMain { app.settings.update { it.copy(backgroundAudio = false) } }
        launch(); start()
        scenario!!.recreate()
        await("activity recreated") { app.activityVisible }
        assertTrue("Rotation/recreation must not stop the session", app.engine.value.running)
        device.pressHome()
        await("leaving with background disabled stops boost") { !app.engine.value.running }
    }

    @Test fun playerSessionOpenCloseAndModeSwitchDoNotAccumulateConnections() {
        onMain { app.settings.update { it.copy(mode = EffectMode.PLAYERS) } }
        launch(); start()
        assertFalse(app.engine.value.linked)
        val player = playerSession()
        announce(player, true)
        await("real player-session attachment attempted") {
            app.engine.value.diagnostics.contains("Session ${player.audioSessionId}:")
        }
        snapshot("player_session_capability")
        repeat(3) { announce(player, true) }
        SystemClock.sleep(500)
        assertTrue(app.engine.value.sessionCount <= 1)
        onMain { app.settings.update { it.copy(mode = EffectMode.SYSTEM) } }
        await("system mode owns only system chain") {
            val d = app.engine.value.diagnostics
            d.contains("Session 0:") && !d.contains("Session ${player.audioSessionId}:")
        }
        onMain { app.settings.update { it.copy(mode = EffectMode.PLAYERS) } }
        await("player mode restored") {
            val d = app.engine.value.diagnostics
            d.contains("Session ${player.audioSessionId}:") && !d.contains("Session 0:")
        }
        announce(player, false)
        await("closed session released") { app.engine.value.sessionCount == 0 && !app.engine.value.linked }
        assertTrue(app.engine.value.running)
    }

    @Test fun compareRestoresSelectionAndUnsupportedEffectsStayHonest() {
        launch()
        onMain { app.settings.update { it.copy(gainDb = 5f, preset = SoundPreset.VOICE) } }
        start()
        SystemClock.sleep(1200)
        snapshot("effect_capability_before_compare")
        if (app.engine.value.linked) {
            click("Compare original")
            await("comparison active") { app.engine.value.comparingOriginal }
            await("readback reduced or explicitly unavailable") {
                app.engine.value.reportedGainDb?.let { it <= 0.1f } ?: true
            }
            assertEquals(5f, app.settings.state.value.gainDb, 0f)
            assertEquals(SoundPreset.VOICE, app.settings.state.value.preset)
            click("Restore boost")
            await("comparison ended") { !app.engine.value.comparingOriginal }
        } else {
            compose.onNodeWithText("Compare original").performScrollTo().assertIsNotEnabled()
            assertNull(app.engine.value.reportedGainDb)
            assertFalse(app.engine.value.gainAvailable)
            // This passes fallback handling only; the JSON explicitly records linked=false.
        }
        click("Turn boost off")
        await("in-app Stop") { !app.engine.value.running }
    }

    @Test fun entitlementExpiryClampsAndRenewalDoesNotRestoreLouderGain() {
        launch(); proFixture()
        onMain {
            app.settings.update { it.copy(gainDb = 30f, preset = SoundPreset.VOICE) }
            app.profiles.add("Before expiry")
        }
        start()
        SystemClock.sleep(4000) // Let the native gain ramp finish before removing access.
        snapshot("pro_gain_before_expiry")
        onMain { app.access.setVerifiedPurchase(false) }
        await("Free gain ceiling enforced") { app.settings.state.value.gainDb == 15f }
        await("Android readback respects Free ceiling when available") {
            app.engine.value.reportedGainDb?.let { it <= 15.1f } ?: true
        }
        assertTrue(app.engine.value.running)
        assertEquals(1, app.profiles.state.value.size)
        assertEquals(SoundPreset.VOICE, app.settings.state.value.preset)
        proFixture()
        assertEquals(15f, app.settings.state.value.gainDb, 0f)
        assertEquals(35f, app.settings.state.value.limitDb, 0f)
    }

    @Test fun realRewardClockExpiryWhileBackgroundedDoesNotWaitForReopening() {
        launch()
        onMain {
            assertTrue(app.access.grantEarnedPass("device-fixture-${System.nanoTime()}"))
            app.settings.setLimit(35f)
            app.settings.update { it.copy(gainDb = 30f) }
        }
        start(); device.pressHome()
        onMain {
            // Expired persisted reward fixture; no real advertisement or system clock change.
            app.getSharedPreferences("feature_access", Context.MODE_PRIVATE).edit()
                .putLong("passWall", System.currentTimeMillis() - 3_601_000L)
                .putLong("lastWall", System.currentTimeMillis() - 3_601_000L).commit()
        }
        await("background timer notices pass expiry") {
            !app.access.state.value.pro && app.settings.state.value.gainDb == 15f
        }
        assertTrue(app.engine.value.running)
    }

    @Test fun adSuspensionReleasesEffectsAndLateResumeCannotRestartStoppedService() {
        launch(); start()
        onMain { app.adAudio.suspend() }
        await("ad gate disconnects") { !app.engine.value.linked && app.engine.value.sessionCount == 0 }
        command(BoostService.RECONNECT)
        SystemClock.sleep(500)
        assertFalse(app.engine.value.linked)
        assertTrue(app.engine.value.running)
        onMain { BoostService.stop(app) }
        await("Stop during ad") { !app.engine.value.running }
        onMain { app.adAudio.resume() }
        SystemClock.sleep(1200)
        assertFalse(app.engine.value.running)
    }

    @Test fun rememberBoostOffResetsBeforeEveryNewSession() {
        onMain { app.settings.update { it.copy(rememberBoost = false, gainDb = 15f) } }
        launch(); start()
        assertEquals(0f, app.settings.state.value.gainDb, 0f)
        click("Turn boost off")
        await("stopped") { !app.engine.value.running }
        click("+10 dB"); start()
        assertEquals(0f, app.settings.state.value.gainDb, 0f)
    }
}
