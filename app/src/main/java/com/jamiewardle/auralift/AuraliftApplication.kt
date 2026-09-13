package com.jamiewardle.auralift

import android.app.Application
import com.jamiewardle.auralift.model.EngineState
import com.jamiewardle.auralift.model.GainMath
import kotlinx.coroutines.flow.MutableStateFlow
import com.jamiewardle.auralift.access.AccessStore
import com.jamiewardle.auralift.access.Purchases
import com.jamiewardle.auralift.access.AdPasses
import com.jamiewardle.auralift.access.AdAudioGate
import com.jamiewardle.auralift.profiles.ProfileStore

class AuraliftApplication : Application() {
    lateinit var settings: SettingsStore; private set
    val engine = MutableStateFlow(EngineState())
    lateinit var access: AccessStore; private set
    lateinit var purchases: Purchases; private set
    lateinit var profiles: ProfileStore; private set
    val adAudio = AdAudioGate()
    lateinit var ads: AdPasses; private set
    var activityVisible = false
    override fun onCreate() {
        super.onCreate()
        access = AccessStore(this, Distribution.owner)
        // Load any locally verified purchase before applying the startup gain ceiling.
        purchases = Distribution.purchases(this, access)
        settings = SettingsStore(this, { GainMath.allowance(access.state.value.pro) }, access::refresh)
        access.entitlementChanged = settings::enforceGainLimit
        ads = Distribution.ads(this)
        profiles = ProfileStore(this, settings, access)
    }
}
