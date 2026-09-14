package com.jamiewardle.auralift.device

import android.os.PowerManager
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
