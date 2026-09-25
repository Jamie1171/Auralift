package com.jamiewardle.auralift.device

import android.os.PowerManager
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.ui.test.*
import com.jamiewardle.auralift.audio.BoostService
import com.jamiewardle.auralift.model.SoundPreset
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.jamiewardle.auralift.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FloatingDeviceTest : DeviceHarness() {
    private val handle get() = By.desc(app.getString(R.string.drag_floating))
    private fun showBubble() {
        launch(); proFixture()
        shell("appops set ${app.packageName} SYSTEM_ALERT_WINDOW allow")
        assertTrue("Test device must support overlay grants", Settings.canDrawOverlays(app))
        onMain { app.settings.update { it.copy(floatingControls = true, gainDb = 5f) } }
        start(); device.pressHome()
        assertTrue("Overlay should appear when app is backgrounded", device.wait(Until.hasObject(handle), 12_000))
    }

    @Test fun floatingControlsChangeGainAndCloseWithoutStoppingBoost() {
        showBubble()
        device.findObject(handle).click()
        val increase = By.desc(app.getString(R.string.increase_boost))
        assertTrue(device.wait(Until.hasObject(increase), 5000))
        device.findObject(increase).click()
        await("overlay changes real saved gain") { app.settings.state.value.gainDb == 5.5f }
        device.findObject(By.desc(app.getString(R.string.close_floating))).click()
        assertTrue(device.wait(Until.gone(handle), 5000))
        assertTrue(app.engine.value.running)
        assertFalse(app.settings.state.value.floatingControls)
    }

    @Test fun outsideTapCollapsesWithoutDisablingPlayerOrBoost() {
        showBubble()
        device.findObject(handle).click()
        val minimise = By.desc(app.getString(R.string.minimise_floating))
        assertTrue(device.wait(Until.hasObject(minimise), 5000))
        // Tap well below the bounded overlay, without covering the underlying app.
        device.click(device.displayWidth - 8, device.displayHeight - 150)
        assertTrue(device.wait(Until.gone(minimise), 5000))
        assertTrue(device.wait(Until.hasObject(handle), 5000))
        assertTrue(app.settings.state.value.floatingControls)
        assertTrue(app.engine.value.running)
    }

    @Test fun stopResetsSoundKeepsPlayerAndCanRestartWithoutOpeningApp() {
        showBubble()
        val player = playerSession()
        val volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        device.findObject(handle).click()
        val warm = By.desc(app.getString(R.string.preset_warm))
        val detail = By.desc(app.getString(R.string.preset_detail))
        val balanced = By.desc(app.getString(R.string.preset_balanced))
        assertTrue(device.wait(Until.hasObject(warm), 5000))
        device.findObject(warm).click()
        await("Warm selected") { app.settings.state.value.preset == SoundPreset.WARM && device.findObject(warm).isSelected }
        device.findObject(warm).click()
        await("same preset returns to Balanced") { app.settings.state.value.preset == SoundPreset.BALANCED && device.findObject(balanced).isSelected }
        device.findObject(warm).click(); device.findObject(detail).click()
        await("Detail replaces Warm") { device.findObject(detail).isSelected && !device.findObject(warm).isSelected }
        capture("floating-expanded-selected")
        command(BoostService.TIMER, 10)
        device.findObject(By.desc(app.getString(R.string.stop_boost))).click()
        await("Stop releases boost and resets sound") {
            !app.engine.value.running && !app.engine.value.linked && app.engine.value.sessionCount == 0 &&
                app.settings.state.value.gainDb == 0f && app.settings.state.value.preset == SoundPreset.BALANCED
        }
        assertEquals(0L, app.engine.value.timerEndsAt)
        assertTrue(app.settings.state.value.floatingControls)
        assertTrue(device.hasObject(handle))
        assertEquals(AudioTrack.PLAYSTATE_PLAYING, player.playState)
        assertEquals(volume, audio.getStreamVolume(AudioManager.STREAM_MUSIC))
        val enable = By.desc(app.getString(R.string.ui_enable_boost))
        assertTrue(device.wait(Until.hasObject(enable), 5000))
        capture("floating-stopped")
        device.findObject(enable).click()
        await("restart directly in floating player") { app.engine.value.running }
        device.findObject(By.desc(app.getString(R.string.stop_boost))).click()
        await("second Stop") { !app.engine.value.running }
        device.findObject(By.desc(app.getString(R.string.close_floating))).click()
        assertTrue(device.wait(Until.gone(handle), 5000))
        await("closing idle player removes its notification") { notifications().none { it.id == 17 } }
        assertFalse(app.settings.state.value.floatingControls)
    }

    @Test fun settingsLaunchesCircleWithoutBoostAndCanToggleOffThenOn() {
        launch(); proFixture()
        shell("appops set ${app.packageName} SYSTEM_ALERT_WINDOW allow")
        compose.onNodeWithText("Settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Floating player").performClick()
        val toggle = compose.onNodeWithContentDescription(app.getString(R.string.floating_enable))
        toggle.performScrollTo().performClick()
        await("idle player notification") { notifications().any { it.id == 17 } }
        assertFalse(app.engine.value.running)
        toggle.performClick()
        await("Settings off shuts down idle service") { notifications().none { it.id == 17 } }
        toggle.performClick()
        device.pressHome()
        assertTrue(device.wait(Until.hasObject(handle), 12_000))
        capture("floating-circle")
        val bounds = device.findObject(handle).visibleBounds
        assertEquals("Collapsed widget is circular", bounds.width(), bounds.height())
        assertTrue("Compact circle is at most 68 dp", bounds.width() <= 68 * app.resources.displayMetrics.density)
        assertFalse(app.engine.value.running)
        device.findObject(handle).click()
        val minimise = By.desc(app.getString(R.string.minimise_floating))
        assertTrue(device.wait(Until.hasObject(minimise), 5000))
        device.findObject(minimise).click()
        assertTrue(device.wait(Until.gone(minimise), 5000))
        // Resize removes one window before the replacement reaches accessibility.
        assertTrue(device.wait(Until.hasObject(handle), 5000))
        assertTrue(app.settings.state.value.floatingControls)
        // Reopening the app restores the opted-in widget, without enabling audio.
        scenario!!.close(); scenario = null; launch(); device.pressHome()
        assertTrue(device.wait(Until.hasObject(handle), 12_000))
        assertFalse(app.engine.value.running)
    }

    @Test fun overlayHidesWhileScreenOffThenOnPermissionLossAndExpiry() {
        showBubble()
        device.sleep()
        await("display asleep") { !app.getSystemService(PowerManager::class.java).isInteractive }
        SystemClockWait.oneServiceTick()
        // UiAutomator can inspect the hierarchy while asleep; no coordinate taps are used.
        assertFalse("Overlay must not remain on the locked/off display", device.hasObject(handle))
        assertTrue(app.engine.value.running)
        device.wakeUp(); shell("wm dismiss-keyguard"); device.pressMenu()
        assertTrue(device.wait(Until.hasObject(handle), 12_000))
        shell("appops set ${app.packageName} SYSTEM_ALERT_WINDOW deny")
        assertTrue(device.wait(Until.gone(handle), 12_000))
        assertTrue(app.engine.value.running)
        shell("appops set ${app.packageName} SYSTEM_ALERT_WINDOW allow")
        assertTrue(device.wait(Until.hasObject(handle), 12_000))
        onMain { app.access.setVerifiedPurchase(false) }
        assertTrue(device.wait(Until.gone(handle), 12_000))
        assertTrue(app.engine.value.running)
    }
}

private object SystemClockWait {
    fun oneServiceTick() = android.os.SystemClock.sleep(1500)
}
