package com.jamiewardle.auralift.access

import android.app.Activity
import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/** Small SDK boundaries let tests exercise real controller timing without requesting ads. */
internal interface AdConsent {
    val canRequestAds: Boolean
    val privacyRequired: Boolean
    fun update(activity: Activity, done: (Int?) -> Unit)
    fun form(activity: Activity, done: (Int?) -> Unit)
    fun options(activity: Activity, done: (Int?) -> Unit)
}

internal class GoogleAdConsent(context: Context) : AdConsent {
    private val info by lazy { UserMessagingPlatform.getConsentInformation(context) }
    override val canRequestAds get() = info.canRequestAds()
    override val privacyRequired get() = info.privacyOptionsRequirementStatus ==
        ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    override fun update(activity: Activity, done: (Int?) -> Unit) =
        info.requestConsentInfoUpdate(activity, ConsentRequestParameters.Builder().build(),
            { done(null) }, { done(it.errorCode) })
    override fun form(activity: Activity, done: (Int?) -> Unit) =
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { done(it?.errorCode) }
    override fun options(activity: Activity, done: (Int?) -> Unit) =
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { done(it?.errorCode) }
}

internal interface RewardedLoader {
    fun initialize(appId: String)
    fun load(unitId: String, callback: AdLoadCallback<RewardedAd>)
}

internal class GoogleRewardedLoader(private val context: Context) : RewardedLoader {
    override fun initialize(appId: String) {
        // Next-Gen initialize is synchronous; its optional listener waits for mediation adapters.
        MobileAds.initialize(context, InitializationConfig.Builder(appId).build())
    }
    override fun load(unitId: String, callback: AdLoadCallback<RewardedAd>) =
        RewardedAd.load(AdRequest.Builder(unitId).build(), callback)
}
