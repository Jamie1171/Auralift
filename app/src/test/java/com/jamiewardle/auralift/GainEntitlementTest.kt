package com.jamiewardle.auralift

import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.access.*
import com.jamiewardle.auralift.audio.BoostService
import com.jamiewardle.auralift.controls.OverlayPermission
import com.jamiewardle.auralift.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.*
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class GainEntitlementTest {
    private val app get() = ApplicationProvider.getApplicationContext<AuraliftApplication>()

    @Test fun freeUpgradeClampsOldGainAndLaterUnlockNeverRestoresIt() {
        val disk = app.getSharedPreferences("auralift", 0)
        disk.edit().clear().putInt("gainRangeVersion", 2).putFloat("limit", 30f).putFloat("gain", 30f).commit()
        var pro = false
        val settings = SettingsStore(app, { GainMath.allowance(pro) })
        assertEquals(15f, settings.state.value.limitDb)
        assertEquals(15f, settings.state.value.gainDb)
        assertEquals(15f, disk.getFloat("gain", 0f))
        pro = true; settings.enforceGainLimit()
        assertEquals(35f, settings.state.value.limitDb)
        assertEquals(15f, settings.state.value.gainDb)
    }

    @Test fun ownerUpgradePreservesGainAndLowerCustomRangeRoundsDown() {
        val disk = app.getSharedPreferences("auralift", 0)
        disk.edit().clear().putInt("gainRangeVersion", 2).putFloat("limit", 30f).putFloat("gain", 24f).commit()
        val full = SettingsStore(app, { 35f })
        assertEquals(35f, full.state.value.limitDb)
        assertEquals(24f, full.state.value.gainDb)
        disk.edit().clear().putInt("gainRangeVersion", 2).putFloat("limit", 12f).putFloat("gain", 11f).commit()
        val lower = SettingsStore(app, { 35f })
        assertEquals(10f, lower.state.value.limitDb)
        assertEquals(10f, lower.state.value.gainDb)
    }

    @Test fun freeWritesAndSavedOutputSetupsCannotExceedAllowance() {
        var pro = true
        val settings = SettingsStore(app, { GainMath.allowance(pro) })
        settings.update { it.copy(gainDb = 35f) }; settings.saveProfile()
        pro = false; settings.enforceGainLimit()
        settings.update { it.copy(gainDb = 999f) }
        assertEquals(15f, settings.state.value.gainDb)
        settings.update { it.copy(gainDb = 0f) }; settings.loadProfile(OutputProfile.SPEAKER)
        assertEquals(15f, settings.state.value.gainDb)
        pro = true; settings.enforceGainLimit()
        assertEquals(15f, settings.state.value.gainDb)
        settings.setLimit(10f)
        pro = false; settings.enforceGainLimit(); pro = true; settings.enforceGainLimit()
        assertEquals(10f, settings.state.value.limitDb)
        assertEquals(10f, settings.state.value.gainDb)
    }

    @Test fun expiryWhileBackgroundedOrDuringAdKeepsSessionAndReducesGain() {
        if (Distribution.owner) app.access.simulateFree()
        assertTrue(RewardClaim(app.access).earned())
        app.settings.update { it.copy(gainDb = 35f, backgroundAudio = true) }
        val controller = Robolectric.buildService(BoostService::class.java).create()
        controller.get().onStartCommand(Intent().setAction(BoostService.START), 0, 1)
        app.adAudio.suspend()
        app.getSharedPreferences("feature_access", 0).edit().putBoolean("passExpired", true).commit()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1100))
        assertFalse(app.access.state.value.pro)
        assertTrue(app.engine.value.running)
        assertEquals(15f, app.settings.state.value.gainDb)
        app.adAudio.resume()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1100))
        assertTrue(app.engine.value.running)
        assertEquals(15f, app.settings.state.value.gainDb)
        assertTrue(RewardClaim(app.access).earned())
        assertEquals(35f, app.settings.state.value.limitDb)
        assertEquals(15f, app.settings.state.value.gainDb)
        controller.get().onStartCommand(Intent().setAction(BoostService.STOP), 0, 1)
        assertFalse(app.engine.value.running)
        controller.destroy()
    }

    @Test fun foregroundGainEditRefreshesAnExpiredPassImmediately() {
        if (Distribution.owner) app.access.simulateFree()
        RewardClaim(app.access).earned()
        app.settings.update { it.copy(gainDb = 35f) }
        app.getSharedPreferences("feature_access", 0).edit().putBoolean("passExpired", true).commit()
        // No UI/service polling tick is needed for a control to enforce expiry.
        app.settings.update { it.copy(gainDb = 34.5f) }
        assertFalse(app.access.state.value.pro)
        assertEquals(15f, app.settings.state.value.gainDb)
    }

    @Test fun overlaySettingsOnlyTargetsOurAppOnSupportedAndroidVersions() {
        assertEquals("package:${app.packageName}", OverlayPermission.intent(app.packageName, 26).dataString)
        assertNull(OverlayPermission.intent(app.packageName, 30).data)
        assertNull(OverlayPermission.intent(app.packageName, 36).data)
    }
}
