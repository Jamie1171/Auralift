package com.jamiewardle.auralift.access

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.edit
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.*
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.rewarded.*
import com.google.android.ump.*
import com.jamiewardle.auralift.*
import com.jamiewardle.auralift.audio.BoostService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/** User-initiated single rewarded ads only: no app-open, banners, interstitials or auto-reloads. */
class PlayAdPasses(private val app: AuraliftApplication) : AdPasses {
    private val appId = if (BuildConfig.DEBUG) TEST_APP else BuildConfig.ADMOB_APP_ID
    private val unitId = if (BuildConfig.DEBUG) TEST_REWARD else BuildConfig.REWARDED_AD_ID
    private val configured = appId.isNotBlank() && unitId.isNotBlank()
    private val mutable = MutableStateFlow(AdPassState(available = configured,
        message = if (configured) R.string.ad_pass_intro else R.string.ads_at_launch))
    override val state = mutable.asStateFlow()
    private val consent by lazy { UserMessagingPlatform.getConsentInformation(app) }
    private val disk = app.getSharedPreferences("ad_privacy", Activity.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val handler = Handler(Looper.getMainLooper())
    private var ad: RewardedAd? = null
    private var loadedAt = 0L
    private var generation = 0
    private var showing = false
    private var host = WeakReference<Activity>(null)
    private var initialized = false
    private fun usable(a: Activity) = !a.isFinishing && !a.isDestroyed
    private fun onMain(action: () -> Unit) { if (Looper.myLooper() == Looper.getMainLooper()) action() else handler.post(action) }
    private fun privacyState() { mutable.value = mutable.value.copy(privacyRequired =
        consent.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED) }
    private fun failed(message: Int = R.string.ad_unavailable) {
        ad = null; mutable.value = mutable.value.copy(busy = false, ready = false, message = message)
    }
    override fun onLaunch(activity: Activity) {
        // Refresh existing choices, without an unsolicited form or an ad SDK initialization.
        if (!configured || !disk.getBoolean("optedIn", false)) return
        consent.requestConsentInfoUpdate(activity, ConsentRequestParameters.Builder().build(),
            { privacyState() }, { privacyState() })
    }
    override fun prepare(activity: Activity) {
        app.access.refresh()
        if (!configured || app.access.state.value.pro || mutable.value.busy || !usable(activity)) return
        val request = ++generation
        host = WeakReference(activity); ad = null
        mutable.value = mutable.value.copy(busy = true, ready = false, message = R.string.ad_loading)
        disk.edit { putBoolean("optedIn", true) }
        // Cancels stalled loading; it NEVER ends an advert or grants a reward.
        handler.postDelayed({ if (request == generation && !showing && mutable.value.busy) { generation++; failed() } }, 60_000)
        consent.requestConsentInfoUpdate(activity, ConsentRequestParameters.Builder().build(), {
            if (request == generation && usable(activity)) {
                privacyState()
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    if (request == generation) {
                        privacyState()
                        if (error != null || !consent.canRequestAds()) failed(R.string.ad_privacy_unavailable)
                        else load(request)
                    }
                }
            }
        }, {
            if (request == generation) { privacyState(); failed(R.string.ad_privacy_unavailable) }
        })
    }
    private fun load(request: Int) {
        scope.launch {
            try {
                if (!initialized) {
                    withContext(Dispatchers.IO) { MobileAds.initialize(app, InitializationConfig.Builder(appId).build()) }
                    initialized = true
                }
                if (request != generation || !consent.canRequestAds() || app.access.state.value.pro) {
                    if (request == generation) failed(); return@launch
                }
                RewardedAd.load(AdRequest.Builder(unitId).build(), object : AdLoadCallback<RewardedAd> {
                    override fun onAdLoaded(loaded: RewardedAd) = onMain {
                        if (request == generation) {
                            if (app.access.state.value.pro || !consent.canRequestAds()) failed()
                            else { ad = loaded; loadedAt = SystemClock.elapsedRealtime()
                                mutable.value = mutable.value.copy(ready = true, busy = false, message = R.string.ad_ready) }
                        }
                    }
                    override fun onAdFailedToLoad(adError: LoadAdError) = onMain { if (request == generation) failed() }
                })
            } catch (_: Exception) { if (request == generation) failed() }
        }
    }
    override fun show(activity: Activity) {
        app.access.refresh()
        if (!usable(activity) || !activity.hasWindowFocus() || mutable.value.busy || app.access.state.value.pro) return
        val loaded = ad
        if (loaded == null || !consent.canRequestAds() || SystemClock.elapsedRealtime() - loadedAt > 55 * 60_000L) { failed(); return }
        ad = null; showing = true; host = WeakReference(activity)
        mutable.value = mutable.value.copy(ready = false, busy = true, message = R.string.ad_playing)
        val claim = RewardClaim(app.access)
        var rewarded = false
        var closed = false
        fun finish(message: Int) {
            if (closed) return
            closed = true; showing = false; app.adAudio.resume()
            mutable.value = mutable.value.copy(busy = false, ready = false, message = message)
        }
        loaded.adEventCallback = object : RewardedAdEventCallback {
            override fun onAdDismissedFullScreenContent() = onMain {
                finish(if (rewarded) R.string.ad_rewarded else R.string.ad_not_completed)
            }
            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) = onMain {
                claim.failed(); finish(R.string.ad_unavailable)
            }
        }
        try {
            // Synchronous release, also blocks reconnects, route changes and new service starts.
            app.adAudio.suspend()
            loaded.show(activity) { _ -> onMain {
                if (claim.earned()) { rewarded = true; mutable.value = mutable.value.copy(message = R.string.ad_rewarded) }
            } }
        } catch (_: RuntimeException) { claim.failed(); finish(R.string.ad_unavailable) }
    }
    override fun privacyOptions(activity: Activity) {
        if (!configured || mutable.value.busy || !usable(activity)) return
        generation++; ad = null
        mutable.value = mutable.value.copy(busy = true, ready = false)
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            privacyState()
            mutable.value = mutable.value.copy(busy = false, message = if (error == null) R.string.ad_privacy_updated else R.string.ad_privacy_unavailable)
        }
    }
    override fun activityDestroyed(activity: Activity) {
        if (host.get() !== activity) return
        if (showing) {
            // Never re-enable audio while an SDK-owned fullscreen activity might still be playing.
            // Destruction ends this listening session; the ad's close callback clears the gate.
            BoostService.stop(app)
        } else { generation++; failed() }
        host.clear()
    }
    companion object {
        const val TEST_APP = "ca-app-pub-3940256099942544~3347511713"
        const val TEST_REWARD = "ca-app-pub-3940256099942544/5224354917"
    }
}
