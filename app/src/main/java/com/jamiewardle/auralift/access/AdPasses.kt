package com.jamiewardle.auralift.access

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdPassState(val available: Boolean = false, val busy: Boolean = false,
                       val ready: Boolean = false, val privacyRequired: Boolean = false,
                       val message: Int = 0, val errorReference: String = "")
interface AdPasses {
    val state: StateFlow<AdPassState>
    fun onLaunch(activity: Activity)
    fun prepare(activity: Activity)
    fun show(activity: Activity)
    fun privacyOptions(activity: Activity)
    fun activityDestroyed(activity: Activity)
}

/** Synchronous main-thread gate: release audio effects BEFORE SDK playback can begin. */
class AdAudioGate {
    private val mutable = MutableStateFlow(false)
    val blocked = mutable.asStateFlow()
    internal var changed: ((Boolean) -> Unit)? = null
    fun suspend() { mutable.value = true; changed?.invoke(true) }
    fun resume() { mutable.value = false; changed?.invoke(false) }
}
