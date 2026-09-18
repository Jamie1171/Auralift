package com.jamiewardle.auralift

import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.access.*
import com.jamiewardle.auralift.model.GainMath
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class ReviewAccessTest {
    private val app get() = ApplicationProvider.getApplicationContext<AuraliftApplication>()
    // Test-only credential; never the configured production code.
    private val code = "0123456789ABCDEF0123456789ABCDEF"
    private val hash get() = MessageDigest.getInstance("SHA-256").digest(code.toByteArray())
        .joinToString("") { "%02x".format(it) }
    @Before fun reset() { app.getSharedPreferences("feature_access", 0).edit().clear().commit() }

    @Test fun rejectsInvalidCodesAndOwnerOrUnconfiguredBuilds() {
        val access = AccessStore(app, false, hash)
        listOf("", "wrong", "F".repeat(32), code + "Z", " ".repeat(129)).forEach {
            assertFalse(access.activateReview(it)); assertFalse(access.state.value.pro)
        }
        assertFalse(AccessStore(app, false).activateReview(code))
        assertFalse(AccessStore(app, true, hash).activateReview(code))
    }

    @Test fun reusableOfflineGrantSurvivesRecreationAndExpiredPassWithoutBecomingPurchase() {
        val access = AccessStore(app, false, hash)
        assertTrue(access.activateReview("  " + code.lowercase().chunked(4).joinToString("-") + "\n"))
        assertTrue(access.activateReview(code))
        val recreated = AccessStore(app, false, hash)
        recreated.setVerifiedPurchase(false)
        app.getSharedPreferences("feature_access", 0).edit().putBoolean("passExpired", true).commit()
        recreated.refresh()
        assertTrue(recreated.state.value.pro); assertTrue(recreated.state.value.review)
        assertFalse(recreated.state.value.owner); assertFalse(recreated.state.value.permanent)
        assertEquals(0L, recreated.state.value.passRemainingMs)
        assertTrue(recreated.deactivateReview()); assertFalse(recreated.state.value.pro)
        assertTrue(recreated.activateReview(code))
    }

    @Test fun unlockingDoesNotRaiseGainAndRemovalClampsImmediately() {
        val access = AccessStore(app, false, hash)
        val settings = SettingsStore(app, { GainMath.allowance(access.state.value.pro) }, access::refresh)
        access.entitlementChanged = settings::enforceGainLimit
        settings.update { it.copy(gainDb = 10f) }
        assertTrue(access.activateReview(code))
        assertEquals(10f, settings.state.value.gainDb)
        assertEquals(35f, settings.state.value.limitDb)
        assertFalse(app.engine.value.running)
        settings.update { it.copy(gainDb = 30f) }
        assertTrue(access.deactivateReview())
        assertEquals(15f, settings.state.value.gainDb)
        access.activateReview(code)
        assertEquals(15f, settings.state.value.gainDb)
    }

    @Test fun endingReviewPreservesPurchasedOrEarnedEntitlements() {
        val access = AccessStore(app, false, hash)
        assertTrue(RewardClaim(access).earned())
        access.activateReview(code); access.deactivateReview()
        assertTrue(access.state.value.pro); assertTrue(access.state.value.passRemainingMs > 0)
        access.setVerifiedPurchase(true)
        access.activateReview(code); access.deactivateReview()
        assertTrue(access.state.value.pro); assertTrue(access.state.value.permanent)
    }

    @Test fun changedVerifierOrClearedDataDoesNotRetainReviewAccess() {
        AccessStore(app, false, hash).activateReview(code)
        assertFalse(AccessStore(app, false, "a".repeat(64)).state.value.review)
        assertFalse(AccessStore(app, false).state.value.review)
        app.getSharedPreferences("feature_access", 0).edit().clear().commit()
        val fresh = AccessStore(app, false, hash)
        assertFalse(fresh.state.value.pro)
        assertTrue(fresh.activateReview(code))
    }
}
