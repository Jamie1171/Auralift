package com.jamiewardle.auralift

import android.app.Activity
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.jamiewardle.auralift.access.*
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
class PlayAdLoadingTest {
    private lateinit var app: AuraliftApplication
    private lateinit var activity: Activity
    private lateinit var consent: FakeConsent
    private lateinit var loader: FakeLoader
    private lateinit var ads: PlayAdPasses

    private class FakeConsent : AdConsent {
        override var canRequestAds = false
        override val privacyRequired = false
        lateinit var updated: (Int?) -> Unit
        lateinit var answered: (Int?) -> Unit
        override fun update(activity: Activity, done: (Int?) -> Unit) { updated = done }
        override fun form(activity: Activity, done: (Int?) -> Unit) { answered = done }
        override fun options(activity: Activity, done: (Int?) -> Unit) { answered = done }
    }
    private class FakeLoader : RewardedLoader {
        var initialized = 0
        val callbacks = mutableListOf<AdLoadCallback<RewardedAd>>()
        override fun initialize(appId: String) { initialized++ }
        override fun load(unitId: String, callback: AdLoadCallback<RewardedAd>) { callbacks += callback }
    }
    @Before fun setup() {
        app = ApplicationProvider.getApplicationContext()
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        consent = FakeConsent(); loader = FakeLoader()
        ads = PlayAdPasses(app, consent, loader, Dispatchers.Unconfined)
        assertFalse(app.access.state.value.pro)
    }
    private fun advance(seconds: Long) = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(seconds))
    private fun load() {
        ads.prepare(activity)
        consent.updated(null)
        consent.canRequestAds = true
        consent.answered(null)
        advance(0)
    }

    @Test fun readingConsentForTwoMinutesDoesNotConsumeAdLoadTimeout() {
        ads.prepare(activity)
        advance(40)
        consent.updated(null)
        advance(120)
        assertTrue(ads.state.value.busy)
        assertEquals(0, loader.initialized)
        consent.canRequestAds = true
        consent.answered(null)
        advance(0)
        assertEquals(1, loader.callbacks.size)
        advance(59)
        assertTrue(ads.state.value.busy)
        advance(1)
        assertFalse(ads.state.value.busy)
        assertEquals("AD_LOAD/TIMEOUT", ads.state.value.errorReference)
        assertFalse(app.access.state.value.pro)
    }
    @Test fun consentUpdateErrorUsesOnlyUmpApprovedPreviousConsent() {
        ads.prepare(activity)
        consent.canRequestAds = true
        consent.updated(2)
        advance(0)
        assertEquals(1, loader.callbacks.size)
        assertFalse(app.access.state.value.pro)
    }
    @Test fun consentErrorWithoutPermissionDoesNotInitializeAdsAndCanRetry() {
        ads.prepare(activity)
        consent.updated(2)
        advance(0)
        assertEquals(0, loader.initialized)
        assertFalse(ads.state.value.busy)
        assertEquals("CONSENT_UPDATE/2", ads.state.value.errorReference)
        assertEquals(R.string.ad_privacy_unavailable, ads.state.value.message)
        load()
        assertEquals(1, loader.callbacks.size)
        assertEquals("", ads.state.value.errorReference)
    }
    @Test fun failedFormCanUseUmpApprovedConsentButNeverOverridesDenial() {
        ads.prepare(activity); consent.updated(null)
        consent.answered(2)
        assertEquals(0, loader.initialized)
        ads.prepare(activity); consent.updated(null)
        consent.canRequestAds = true
        consent.answered(2); advance(0)
        assertEquals(1, loader.callbacks.size)
    }
    @Test fun lateConsentResultAfterTimeoutCannotStartAds() {
        ads.prepare(activity)
        advance(60)
        assertEquals("CONSENT_UPDATE/TIMEOUT", ads.state.value.errorReference)
        consent.canRequestAds = true
        consent.updated(null)
        advance(0)
        assertEquals(0, loader.initialized)
        assertFalse(ads.state.value.busy)
    }
    @Test fun sdkErrorsAreDistinguishedAndOldCallbackCannotOverwriteRetry() {
        load()
        val old = loader.callbacks.single()
        old.onAdFailedToLoad(LoadAdError(LoadAdError.ErrorCode.NETWORK_ERROR, "offline"))
        assertEquals(R.string.ad_network_error, ads.state.value.message)
        assertEquals("AD_LOAD/NETWORK_ERROR", ads.state.value.errorReference)
        assertFalse(ads.state.value.busy)
        load()
        old.onAdFailedToLoad(LoadAdError(LoadAdError.ErrorCode.NO_FILL, "late"))
        assertTrue(ads.state.value.busy)
        assertEquals("", ads.state.value.errorReference)
        loader.callbacks.last().onAdFailedToLoad(LoadAdError(LoadAdError.ErrorCode.INVALID_REQUEST, "bad unit"))
        assertEquals(R.string.ad_configuration_error, ads.state.value.message)
        assertFalse(app.access.state.value.pro)
        assertFalse(app.adAudio.blocked.value)
    }
    @Test fun destroyingHostInvalidatesConsentAndLoadingCallbacks() {
        ads.prepare(activity)
        ads.activityDestroyed(activity)
        consent.canRequestAds = true
        consent.updated(null)
        advance(0)
        assertEquals(0, loader.initialized)
        load()
        ads.activityDestroyed(activity)
        loader.callbacks.single().onAdFailedToLoad(LoadAdError(LoadAdError.ErrorCode.NETWORK_ERROR, "late"))
        assertEquals("", ads.state.value.errorReference)
        assertFalse(ads.state.value.busy)
        assertFalse(app.access.state.value.pro)
    }
}
