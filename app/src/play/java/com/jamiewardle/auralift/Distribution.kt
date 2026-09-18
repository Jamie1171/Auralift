package com.jamiewardle.auralift

import android.content.Context
import com.jamiewardle.auralift.access.*

object Distribution {
    const val owner = false
    // Only a digest is public; the reusable access code is supplied privately to Play reviewers.
    const val reviewCodeSha256 = "4250f81606678bbcf9232aed82b63665f6e1f2fa7ce54ea646e29835cac67e44"
    val storeLive = BuildConfig.STORE_LIVE
    fun ads(app: AuraliftApplication): AdPasses = PlayAdPasses(app)
    fun purchases(context: Context, access: AccessStore): Purchases = PlayPurchases(context, access)
}
