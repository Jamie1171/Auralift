package com.jamiewardle.auralift

import android.app.NotificationManager
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.audio.BoostService
import com.jamiewardle.auralift.controls.RestartReminder
import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class BackgroundAndAdAudioTest {
    private val app get() = ApplicationProvider.getApplicationContext<AuraliftApplication>()
    @Before fun proFixture() { assertTrue(app.terms.accept()); app.access.setVerifiedPurchase(true) }
    // Inspect owned chains because a simulated audio HAL cannot establish acoustic gain.
    private fun chains(service: BoostService) = (BoostService::class.java.getDeclaredField("chains").apply { isAccessible = true }.get(service) as Map<*, *>).size
    @Test fun screenOffSessionLivesAndAdsReleaseEffectsBeforeAnyReconnect() {
        app.settings.update { it.copy(gainDb = 30f, backgroundAudio = true) }
        app.activityVisible = false
        val controller = Robolectric.buildService(BoostService::class.java).create()
        controller.get().onStartCommand(Intent().setAction(BoostService.START), 0, 1)
        val service = controller.get()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1100))
        assertTrue(app.engine.value.running)
        assertEquals(1, chains(service))
        app.adAudio.suspend()
        assertEquals(0, chains(service))
        assertTrue(app.engine.value.running)
        service.onStartCommand(Intent().setAction(BoostService.RECONNECT), 0, 1)
        app.sendBroadcast(Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).putExtra(AudioEffect.EXTRA_AUDIO_SESSION, 123))
        app.settings.update { it.copy(gainDb = 24f) }
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1100))
        assertEquals(0, chains(service))
        assertEquals(24f, app.settings.state.value.gainDb)
        app.adAudio.resume()
        assertEquals(1, chains(service))
        controller.destroy()
        assertFalse(app.engine.value.running)
        app.adAudio.resume()
        assertFalse(app.engine.value.running)
    }
    @Test fun serviceStartedDuringAdCannotAttachEffects() {
        app.adAudio.suspend()
        val controller = Robolectric.buildService(BoostService::class.java).create()
        controller.get().onStartCommand(Intent().setAction(BoostService.START), 0, 1)
        assertEquals(0, chains(controller.get()))
        controller.destroy(); app.adAudio.resume()
        assertFalse(app.engine.value.running)
    }
    @Test fun startingWithMemoryOffResetsBeforeAttachingAndDoesNotStopTheService() {
        app.activityVisible = true
        app.settings.update { it.copy(rememberBoost = false, gainDb = 15f) }
        val controller = Robolectric.buildService(BoostService::class.java).create()
        try {
            controller.get().onStartCommand(Intent().setAction(BoostService.START), 0, 1)
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1100))
            assertTrue(app.engine.value.running)
            assertEquals(0f, app.settings.state.value.gainDb)
            assertEquals(1, chains(controller.get()))
        } finally { controller.destroy() }
    }
    @Test fun floatingOnlyStartAndReconnectNeverCreateEffects() {
        org.robolectric.shadows.ShadowSettings.setCanDrawOverlays(true)
        app.activityVisible = true
        app.settings.update { it.copy(floatingControls = true, gainDb = 30f) }
        val controller = Robolectric.buildService(BoostService::class.java).create()
        val service = controller.get()
        try {
            service.onStartCommand(Intent().setAction(BoostService.SHOW_FLOATING), 0, 1)
            service.onStartCommand(Intent().setAction(BoostService.RECONNECT), 0, 2)
            app.adAudio.suspend(); app.adAudio.resume()
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1100))
            assertFalse(app.engine.value.running)
            assertEquals(0, chains(service))
            assertEquals(30f, app.settings.state.value.gainDb)
            service.onStartCommand(Intent().setAction(BoostService.START), 0, 3)
            assertTrue(app.engine.value.running)
            assertEquals(1, chains(service))
            service.onStartCommand(Intent().setAction(BoostService.STOP), 0, 4)
            app.adAudio.suspend(); app.adAudio.resume()
            service.onStartCommand(Intent().setAction(BoostService.RECONNECT), 0, 5)
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1100))
            assertFalse(app.engine.value.running)
            assertEquals(0, chains(service))
            assertTrue(app.settings.state.value.floatingControls)
            val notification = shadowOf(app.getSystemService(NotificationManager::class.java)).allNotifications.single()
            assertEquals(app.getString(R.string.close_floating), notification.actions.single().title.toString())
        } finally {
            controller.destroy()
            org.robolectric.shadows.ShadowSettings.setCanDrawOverlays(false)
        }
    }
    @Test fun rebootReceiverDoesNothingByDefaultAndNeverStartsAudio() {
        val receiver = RestartReminder()
        receiver.onReceive(app, Intent(Intent.ACTION_BOOT_COMPLETED))
        assertFalse(app.engine.value.running)
        assertEquals(0, shadowOf(app.getSystemService(NotificationManager::class.java)).allNotifications.size)
        app.settings.update { it.copy(restartReminder = true, gainDb = 30f) }
        receiver.onReceive(app, Intent(Intent.ACTION_BOOT_COMPLETED))
        assertFalse(app.engine.value.running)
        assertEquals(30f, app.settings.state.value.gainDb)
        // Depending on API permissions the reminder can be posted or suppressed; never audio.
        assertTrue(shadowOf(app.getSystemService(NotificationManager::class.java)).allNotifications.size <= 1)
    }
}
