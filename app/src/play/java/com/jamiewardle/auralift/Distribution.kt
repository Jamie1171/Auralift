package com.jamiewardle.auralift

import android.content.Context
import com.jamiewardle.auralift.access.*

object Distribution {
    const val owner = false
    // Test access is omitted from production BuildConfig; existing saved grants then become invalid.
    const val reviewCodeSha256 = BuildConfig.REVIEW_CODE_SHA256
    val storeLive = BuildConfig.STORE_LIVE
    fun ads(app: AuraliftApplication): AdPasses = PlayAdPasses(app)
    fun purchases(context: Context, access: AccessStore): Purchases = PlayPurchases(context, access)
}
