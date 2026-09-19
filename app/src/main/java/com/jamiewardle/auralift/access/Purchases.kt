package com.jamiewardle.auralift.access

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

enum class ProProduct(val id: String, val purchaseOptionId: String) {
    PRO("auralift_pro", "pro-lifetime"),
    SUPPORTER("auralift_supporter", "supporter-lifetime");

    // Keep the original documented base option working; never select a promotional offer.
    fun acceptsPurchaseOption(optionId: String?, offerId: String?): Boolean =
        offerId == null && (optionId == purchaseOptionId || optionId == "buy")
}
data class PurchaseState(val ready: Boolean = false, val prices: Map<ProProduct, String> = emptyMap(),
                         val busy: Boolean = false, val message: Int = 0)
interface Purchases {
    val state: StateFlow<PurchaseState>
    fun refresh()
    fun buy(activity: Activity, product: ProProduct)
    fun close()
}
