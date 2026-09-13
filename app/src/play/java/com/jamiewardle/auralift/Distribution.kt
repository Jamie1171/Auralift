package com.jamiewardle.auralift

import android.content.Context
import com.jamiewardle.auralift.access.*

object Distribution {
    const val owner = false
    val storeLive = BuildConfig.STORE_LIVE
    fun ads(app: AuraliftApplication): AdPasses = PlayAdPasses(app)
    fun purchases(context: Context, access: AccessStore): Purchases = PlayPurchases(context, access)
}
