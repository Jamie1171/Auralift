package com.jamiewardle.auralift

import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.access.*
import com.jamiewardle.auralift.model.*
import com.jamiewardle.auralift.profiles.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.Config

// These framework/logic checks do not need Robolectric native font loading.
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
class AccessAndProfilesTest {
    private val context get() = ApplicationProvider.getApplicationContext<AuraliftApplication>()
    @Test fun passSurvivesRebootButNeverExtendsForClockRollback() {
        assertEquals(PassClock.DURATION - 5000, PassClock.remaining(1000, 10000, 8, 6000, 15000, 8))
        assertEquals(0, PassClock.remaining(1000, 10000, 8, 3601000, 3610000, 8))
        assertEquals(0, PassClock.remaining(1000, 10000, 8, 900, 15000, 8))
        assertEquals(0, PassClock.remaining(1000, 10000, 8, 6000, 9999, 8))
        assertEquals(PassClock.DURATION - 5000, PassClock.remaining(1000, 10000, 8, 100, 15000, 9))
        assertEquals(0, PassClock.remaining(1000, 10000, 8, 100, 12000, 9, lastWall = 14000))
    }
    @Test fun onlyCompletedRewardsGrantAPassAndDuplicateCallbacksCannotRenewIt() {
        val access = AccessStore(context, false)
        assertFalse(access.state.value.pro)
        val cancelled = RewardClaim(access, "cancelled")
        cancelled.failed()
        assertFalse(cancelled.earned())
        assertFalse(access.state.value.pro)
        val reward = RewardClaim(access, "earned")
        assertTrue(reward.earned())
        assertFalse(reward.earned())
        val reopened = AccessStore(context, false)
        assertTrue(reopened.state.value.pro)
        assertTrue(reopened.state.value.passRemainingMs > 0)
        // No stacking, even from a different callback while the pass is active.
        assertFalse(RewardClaim(reopened, "second").earned())
        context.getSharedPreferences("feature_access", 0).edit().putBoolean("passExpired", true).commit()
        reopened.refresh()
        assertFalse(reopened.state.value.pro)
        assertFalse(reopened.grantEarnedPass("earned"))
        assertTrue(RewardClaim(reopened, "new-ad").earned())
    }
    @Test fun permanentPurchaseHasNoTimerAndRevocationFallsBackToAnEarnedPass() {
        val access = AccessStore(context, false)
        assertTrue(RewardClaim(access).earned())
        access.setVerifiedPurchase(true)
        assertTrue(access.state.value.permanent)
        assertEquals(0L, access.state.value.passRemainingMs)
        access.setVerifiedPurchase(false)
        assertFalse(access.state.value.permanent)
        assertTrue(access.state.value.passRemainingMs > 0)
    }
    @Test fun publicBuildCannotUseOwnerSimulation() {
        val public = AccessStore(context, false)
        assertThrows(IllegalStateException::class.java) { public.restoreOwner() }
        assertThrows(IllegalStateException::class.java) { public.simulateFree() }
        assertThrows(IllegalStateException::class.java) { public.simulateReward() }
        assertThrows(IllegalStateException::class.java) { public.simulatePurchase() }
        assertFalse(public.state.value.pro)
    }
    @Test fun expiryCapsAudioAndRetainsStoredSoundsButBlocksLoading() {
        val owner = AccessStore(context, true)
        val settings = SettingsStore(context, { GainMath.allowance(owner.state.value.pro) }, owner::refresh)
        owner.entitlementChanged = settings::enforceGainLimit
        settings.update { it.copy(gainDb = 35f, preset = SoundPreset.VOICE) }
        val profiles = ProfileStore(context, settings, owner)
        profiles.add("Speech")
        owner.simulateExpired()
        assertFalse(owner.state.value.pro)
        assertEquals(15f, settings.state.value.gainDb)
        assertEquals(SoundPreset.VOICE, settings.state.value.preset)
        assertEquals(1, profiles.state.value.size)
        assertThrows(IllegalStateException::class.java) { profiles.load(profiles.state.value.first().id) }
        assertEquals(35f, profiles.state.value.first().gain)
        owner.restoreOwner()
        assertEquals(35f, settings.state.value.limitDb)
        assertEquals(15f, settings.state.value.gainDb)
        profiles.remove(profiles.state.value.first().id)
        assertTrue(profiles.state.value.isEmpty())
    }
    @Test fun importsAreAtomicAndNeverApplyGain() {
        val settings = SettingsStore(context, { GainMath.MAX_GAIN_DB })
        val access = AccessStore(context, true)
        val profiles = ProfileStore(context, settings, access)
        val valid = ProfileCodec.encode(listOf(SavedSound("x", "Test", 35f, List(5) { -2f })))
        assertEquals(1, profiles.import(valid))
        assertEquals(0f, settings.state.value.gainDb)
        assertThrows(IllegalArgumentException::class.java) { profiles.import(valid.replace("35", "99")) }
        assertEquals(1, profiles.state.value.size)
        profiles.load(profiles.state.value.first().id)
        assertEquals(35f, settings.state.value.gainDb)
    }
    @Test fun invalidProfilePayloadsAreRejectedBeforePersistence() {
        val sound = SavedSound("x", "Test", 6f, List(5) { 0f })
        val valid = ProfileCodec.encode(listOf(sound))
        assertEquals("Test", ProfileCodec.decode(valid).single().name)
        assertThrows(IllegalArgumentException::class.java) { ProfileCodec.decode("x".repeat(ProfileCodec.MAX_BYTES + 1)) }
        assertThrows(IllegalArgumentException::class.java) { ProfileCodec.decode(ProfileCodec.encode(List(25) { sound })) }
        assertThrows(IllegalArgumentException::class.java) { ProfileCodec.decode(valid.replace("auralift-sounds", "other")) }
    }
}
