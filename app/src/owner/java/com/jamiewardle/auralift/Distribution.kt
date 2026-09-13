package com.jamiewardle.auralift

import android.app.Activity
import android.content.Context
import com.jamiewardle.auralift.access.*
import kotlinx.coroutines.flow.MutableStateFlow

object Distribution {
    const val owner = true
    const val storeLive = false
    fun ads(app: AuraliftApplication): AdPasses = object : AdPasses {
        override val state = MutableStateFlow(AdPassState(message = R.string.owner_no_ads))
        override fun onLaunch(activity: Activity) = Unit
        override fun prepare(activity: Activity) = Unit
        override fun show(activity: Activity) = Unit
        override fun privacyOptions(activity: Activity) = Unit
        override fun activityDestroyed(activity: Activity) = Unit
    }
    fun purchases(context: Context, access: AccessStore): Purchases = object : Purchases {
        override val state = MutableStateFlow(PurchaseState(message = R.string.owner_no_purchases))
        override fun refresh() = Unit
        override fun buy(activity: Activity, product: ProProduct) = Unit
        override fun close() = Unit
    }
}
