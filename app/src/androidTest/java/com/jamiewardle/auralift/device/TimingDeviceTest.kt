package com.jamiewardle.auralift.device

import android.os.PowerManager
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jamiewardle.auralift.audio.BoostService
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimingDeviceTest : DeviceHarness() {
    @Test fun cancelledTimerCannotStopReplacementAndRealDeadlineStopsSession() {
        launch(); start()
        command(BoostService.TIMER, 1)
        await("first timer set") { app.engine.value.timerEndsAt > 0 }
        val oldDeadline = app.engine.value.timerEndsAt
        command(BoostService.TIMER, 0)
        await("timer cancelled") { app.engine.value.timerEndsAt == 0L }
        command(BoostService.TIMER, 2)
        await("replacement timer set") { app.engine.value.timerEndsAt > oldDeadline }
        val untilOldDeadline = oldDeadline - SystemClock.elapsedRealtime() + 1200
        SystemClock.sleep(untilOldDeadline.coerceAtLeast(0))
        assertTrue("Cancelled timer must not stop replacement", app.engine.value.running)
        snapshot("survived_cancelled_deadline")
        // Allow the actual two-minute timer to complete. No fake clock or private deadline edits.
        await("replacement deadline stops session", 70_000) { !app.engine.value.running }
        assertEquals(0L, app.engine.value.timerEndsAt)
        assertFalse(app.engine.value.linked)
    }

    @Test fun screenOffServiceSoakAndReturnToStop() {
        val seconds = InstrumentationRegistry.getArguments().getString("soakSeconds", "30").toLong()
        require(seconds in 10..1800) { "soakSeconds must be 10..1800" }
        launch(); start()
        device.pressHome(); device.sleep()
        await("screen off") { !app.getSystemService(PowerManager::class.java).isInteractive }
        val deadline = SystemClock.elapsedRealtime() + seconds * 1000
        while (SystemClock.elapsedRealtime() < deadline) {
            assertTrue("Service stopped during ${seconds}s screen-off test", app.engine.value.running)
            SystemClock.sleep(minOf(1000, deadline - SystemClock.elapsedRealtime()).coerceAtLeast(1))
        }
        snapshot("screen_off_soak_${seconds}s")
        device.wakeUp(); shell("wm dismiss-keyguard"); device.pressMenu()
        scenario!!.onActivity { it.startActivity(android.content.Intent(it, com.jamiewardle.auralift.MainActivity::class.java)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)) }
        await("app foreground again") { app.activityVisible }
        click("Turn boost off")
        await("Stop after soak") { !app.engine.value.running }
    }
}
