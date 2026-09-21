package com.jamiewardle.auralift.access

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.edit
import com.google.android.libraries.ads.mobile.sdk.common.*
import com.google.android.libraries.ads.mobile.sdk.rewarded.*
import com.jamiewardle.auralift.*
import com.jamiewardle.auralift.audio.BoostService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/** User-initiated single rewarded ads only: no app-open, banners, interstitials or auto-reloads. */
class PlayAdPasses internal constructor(private val app: AuraliftApplication,
    private val consent: AdConsent, private val loader: RewardedLoader,
    private val initializationDispatcher: CoroutineDispatcher = Dispatchers.IO) : AdPasses {
    constructor(app: AuraliftApplication) : this(app, GoogleAdConsent(app), GoogleRewardedLoader(app))
    private val appId = if (BuildConfig.DEBUG) TEST_APP else BuildConfig.ADMOB_APP_ID
    private val unitId = if (BuildConfig.DEBUG) TEST_REWARD else BuildConfig.REWARDED_AD_ID
    private val configured = appId.isNotBlank() && unitId.isNotBlank()
    private val mutable = MutableStateFlow(AdPassState(available = configured,
        message = if (configured) R.string.ad_pass_intro else R.string.ads_at_launch))
    override val state = mutable.asStateFlow()
    private val disk = app.getSharedPreferences("ad_privacy", Activity.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val handler = Handler(Looper.getMainLooper())
    private var ad: RewardedAd? = null
    private var loadedAt = 0L
    private var generation = 0
    private var showing = false
    private var host = WeakReference<Activity>(null)
    private var initialized = false
    private var timeout: Runnable? = null
    private fun usable(a: Activity) = !a.isFinishing && !a.isDestroyed
    private fun onMain(action: () -> Unit) { if (Looper.myLooper() == Looper.getMainLooper()) action() else handler.post(action) }
    private fun privacyState() { mutable.value = mutable.value.copy(privacyRequired = consent.privacyRequired) }
    private fun clearTimeout() { timeout?.let(handler::removeCallbacks); timeout = null }
    private fun watchTimeout(request: Int, stage: String) {
        clearTimeout()
        timeout = Runnable {
            if (request == generation && !showing && mutable.value.busy)
                failed(R.string.ad_timed_out, "$stage/TIMEOUT")
        }.also { handler.postDelayed(it, 60_000) }
    }
    private fun failed(message: Int = R.string.ad_unavailable, reference: String = "") {
        generation++ // Late SDK callbacks may never revive a failed/cancelled request.
        clearTimeout()
        ad = null
        mutable.value = mutable.value.copy(busy = false, ready = false, message = message, errorReference = reference)
    }
    override fun onLaunch(activity: Activity) {
        // Refresh existing choices, without an unsolicited form or ad SDK initialization.
        if (!configured || !disk.getBoolean("optedIn", false)) return
        consent.update(activity) { onMain { privacyState() } }
    }
    override fun prepare(activity: Activity) {
        app.access.refresh()
        if (!configured || app.access.state.value.pro || mutable.value.busy || !usable(activity)) return
        val request = ++generation
        host = WeakReference(activity); ad = null
        mutable.value = mutable.value.copy(busy = true, ready = false, message = R.string.ad_loading, errorReference = "")
        disk.edit { putBoolean("optedIn", true) }
        watchTimeout(request, "CONSENT_UPDATE")
        consent.update(activity) { updateError -> onMain {
            if (request != generation) return@onMain
            if (!usable(activity)) { failed(reference = "HOST/CLOSED"); return@onMain }
            clearTimeout()
            privacyState()
            if (updateError != null) {
                // UMP, not our own cached preference, decides whether previous consent is usable.
                if (consent.canRequestAds) load(request)
                else failed(R.string.ad_privacy_unavailable, "CONSENT_UPDATE/$updateError")
            } else {
                // Reading/answering a consent form is not an ad-network timeout.
                consent.form(activity) { formError -> onMain {
                    if (request == generation) {
                        privacyState()
                        if (!usable(activity)) failed(reference = "HOST/CLOSED")
                        else if (consent.canRequestAds) load(request)
                        else failed(R.string.ad_privacy_unavailable, "CONSENT_FORM/${formError ?: "NOT_READY"}")
                    }
                } }
            }
        } }
    }
    private fun load(request: Int) {
        watchTimeout(request, "AD_LOAD")
        scope.launch {
            try {
                if (!initialized) {
                    withContext(initializationDispatcher) { loader.initialize(appId) }
                    initialized = true
                }
                if (request != generation || !consent.canRequestAds || app.access.state.value.pro) {
                    if (request == generation) failed(); return@launch
                }
                loader.load(unitId, object : AdLoadCallback<RewardedAd> {
                    override fun onAdLoaded(loaded: RewardedAd) = onMain {
                        if (request == generation) {
                            if (app.access.state.value.pro || !consent.canRequestAds) failed()
                            else { clearTimeout(); ad = loaded; loadedAt = SystemClock.elapsedRealtime()
                                mutable.value = mutable.value.copy(ready = true, busy = false, message = R.string.ad_ready) }
                        }
                    }
                    override fun onAdFailedToLoad(adError: LoadAdError) = onMain {
                        if (request == generation) failed(loadErrorMessage(adError.code), "AD_LOAD/${adError.code.name}")
                    }
                })
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) {
                if (request == generation) failed(R.string.ad_load_failed, "SDK/${error.javaClass.simpleName}")
            }
        }
    }
    override fun show(activity: Activity) {
        app.access.refresh()
        if (!usable(activity) || !activity.hasWindowFocus() || mutable.value.busy || app.access.state.value.pro) return
        val loaded = ad
        if (loaded == null || !consent.canRequestAds || SystemClock.elapsedRealtime() - loadedAt > 55 * 60_000L) { failed(); return }
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
                claim.failed(); finish(R.string.ad_load_failed)
                mutable.value = mutable.value.copy(errorReference = "AD_SHOW/${fullScreenContentError.code}")
            }
        }
        try {
            // Synchronous release, also blocks reconnects, route changes and new service starts.
            app.adAudio.suspend()
            loaded.show(activity) { _ -> onMain {
                if (claim.earned()) { rewarded = true; mutable.value = mutable.value.copy(message = R.string.ad_rewarded) }
            } }
        } catch (error: RuntimeException) {
            claim.failed(); finish(R.string.ad_load_failed)
            mutable.value = mutable.value.copy(errorReference = "AD_SHOW/${error.javaClass.simpleName}")
        }
    }
    override fun privacyOptions(activity: Activity) {
        if (!configured || mutable.value.busy || !usable(activity)) return
        val request = ++generation
        clearTimeout(); ad = null; host = WeakReference(activity)
        mutable.value = mutable.value.copy(busy = true, ready = false, errorReference = "")
        consent.options(activity) { error -> onMain {
            if (request == generation) {
                privacyState()
                mutable.value = mutable.value.copy(busy = false,
                    message = if (error == null) R.string.ad_privacy_updated else R.string.ad_privacy_unavailable,
                    errorReference = if (error == null) "" else "CONSENT_OPTIONS/$error")
            }
        } }
    }
    override fun activityDestroyed(activity: Activity) {
        if (host.get() !== activity) return
        if (showing) {
            // Never re-enable audio while an SDK-owned fullscreen activity might still be playing.
            // Destruction ends this listening session; the ad's close callback clears the gate.
            BoostService.stop(app)
        } else { failed() }
        host.clear()
    }
    companion object {
        internal fun loadErrorMessage(code: LoadAdError.ErrorCode): Int = when (code) {
            LoadAdError.ErrorCode.NETWORK_ERROR -> R.string.ad_network_error
            LoadAdError.ErrorCode.TIMEOUT -> R.string.ad_timed_out
            LoadAdError.ErrorCode.INVALID_REQUEST, LoadAdError.ErrorCode.APP_ID_MISSING -> R.string.ad_configuration_error
            LoadAdError.ErrorCode.NO_FILL -> R.string.ad_unavailable
            else -> R.string.ad_load_failed
        }
        const val TEST_APP = "ca-app-pub-3940256099942544~3347511713"
        const val TEST_REWARD = "ca-app-pub-3940256099942544/5224354917"
    }
}
