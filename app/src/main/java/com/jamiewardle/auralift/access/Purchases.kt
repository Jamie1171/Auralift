package com.jamiewardle.auralift.access

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

enum class ProProduct(val id: String) { PRO("auralift_pro"), SUPPORTER("auralift_supporter") }
data class PurchaseState(val ready: Boolean = false, val prices: Map<ProProduct, String> = emptyMap(),
                         val busy: Boolean = false, val message: Int = 0)
interface Purchases {
    val state: StateFlow<PurchaseState>
    fun refresh()
    fun buy(activity: Activity, product: ProProduct)
    fun close()
}
