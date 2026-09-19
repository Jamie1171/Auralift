package com.jamiewardle.auralift.access

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.android.billingclient.api.*
import com.jamiewardle.auralift.BuildConfig
import com.jamiewardle.auralift.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Two non-consumable products, one entitlement. Only compiled into the Play edition. */
class PlayPurchases(private val context: Context, private val access: AccessStore) : Purchases {
    private val disk = context.getSharedPreferences("play_receipt", Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(PurchaseState(message = R.string.purchases_at_launch))
    override val state = mutable.asStateFlow()
    private val configured = BuildConfig.PLAY_PUBLIC_KEY.isNotBlank()
    private var connecting = false
    private var queryGeneration = 0
    private val billing = lazy { BillingClient.newBuilder(context)
        .setListener { result, _ ->
            mutable.value = mutable.value.copy(busy = false, message = when (result.responseCode) {
                BillingClient.BillingResponseCode.USER_CANCELED -> R.string.purchase_cancelled
                BillingClient.BillingResponseCode.OK -> R.string.checking_purchase
                else -> R.string.purchase_unavailable
            })
            if (result.responseCode == BillingClient.BillingResponseCode.OK || result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) refresh()
        }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection().build() }
    private val client get() = billing.value

    init {
        if (configured) {
            val cached = runCatching {
                val receipts = JSONArray(disk.getString("receipts", "[]"))
                buildList {
                    for (i in 0 until receipts.length()) receipts.getJSONObject(i).let { add(Purchase(it.getString("json"), it.getString("signature"))) }
                    // Preserve a verified 0.3 receipt on update.
                    if (disk.contains("json")) add(Purchase(disk.getString("json", "")!!, disk.getString("signature", "")!!))
                }
            }.getOrDefault(emptyList())
            access.setVerifiedPurchase(cached.any { valid(it) && it.isAcknowledged })
        }
    }
    private fun valid(p: Purchase) = PlayReceiptVerifier.valid(p, context.packageName, BuildConfig.PLAY_PUBLIC_KEY)

    override fun refresh() {
        access.refresh()
        if (!configured || connecting || mutable.value.busy) return
        if (client.isReady) { owned(); catalog(); return }
        connecting = true
        mutable.value = mutable.value.copy(busy = true, message = R.string.checking_purchase)
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                connecting = false
                if (result.responseCode == BillingClient.BillingResponseCode.OK) { owned(); catalog() }
                else mutable.value = PurchaseState(message = R.string.purchase_unavailable)
            }
            override fun onBillingServiceDisconnected() { connecting = false; mutable.value = mutable.value.copy(ready = false, busy = false) }
        })
    }
    private fun owned(onUnowned: (() -> Unit)? = null) {
        val generation = ++queryGeneration
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()) { result, purchases ->
            if (generation != queryGeneration) return@queryPurchasesAsync
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                mutable.value = mutable.value.copy(busy = false, message = R.string.purchase_unavailable)
                return@queryPurchasesAsync // Never revoke a verified purchase just because the network failed.
            }
            val valid = purchases.filter { valid(it) }
            val pending = purchases.any { p -> p.products.any { it in PRODUCTS } && p.purchaseState == Purchase.PurchaseState.PENDING }
            val cache = JSONArray()
            valid.filter { it.isAcknowledged }.forEach { cache.put(JSONObject().put("json", it.originalJson).put("signature", it.signature)) }
            disk.edit(commit = true) { clear(); putString("receipts", cache.toString()) }
            // A refund of one product cannot remove entitlement from the other valid product.
            access.setVerifiedPurchase(valid.isNotEmpty())
            mutable.value = mutable.value.copy(busy = false, message = when {
                valid.isNotEmpty() -> R.string.pro_restored
                pending -> R.string.purchase_pending
                else -> R.string.no_purchase_found
            })
            val acknowledgements = valid.filterNot { it.isAcknowledged }
            var remaining = acknowledgements.size
            var allAcknowledged = true
            acknowledgements.forEach { purchase ->
                client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()) { ack ->
                    if (generation == queryGeneration) {
                        allAcknowledged = allAcknowledged && ack.responseCode == BillingClient.BillingResponseCode.OK
                        remaining--
                        if (remaining == 0) {
                            if (allAcknowledged) owned() // Obtain signed acknowledged receipts for offline use.
                            else mutable.value = mutable.value.copy(message = R.string.purchase_confirming)
                        }
                    }
                }
            }
            if (valid.isEmpty() && !pending) onUnowned?.invoke()
        }
    }
    private fun catalog(launch: Activity? = null, selected: ProProduct? = null) {
        val params = QueryProductDetailsParams.newBuilder().setProductList(ProProduct.entries.map {
            QueryProductDetailsParams.Product.newBuilder().setProductId(it.id).setProductType(BillingClient.ProductType.INAPP).build()
        }).build()
        client.queryProductDetailsAsync(params) { result, query ->
            val products = query.productDetailsList.associateBy { it.productId }
            val offers = ProProduct.entries.mapNotNull { type ->
                products[type.id]?.oneTimePurchaseOfferDetailsList
                    ?.filter { type.acceptsPurchaseOption(it.purchaseOptionId, it.offerId) }
                    ?.minByOrNull { if (it.purchaseOptionId == type.purchaseOptionId) 0 else 1 }
                    ?.let { type to it }
            }.toMap()
            if (result.responseCode != BillingClient.BillingResponseCode.OK || offers.isEmpty()) {
                mutable.value = mutable.value.copy(ready = false, busy = false, prices = emptyMap(), message = R.string.purchase_unavailable)
                return@queryProductDetailsAsync
            }
            mutable.value = mutable.value.copy(ready = true, prices = offers.mapValues { it.value.formattedPrice })
            if (launch != null && selected != null) {
                val offer = offers[selected]; val product = products[selected.id]
                if (offer == null || product == null || launch.isFinishing || launch.isDestroyed || (access.state.value.permanent || access.state.value.review)) {
                    mutable.value = mutable.value.copy(busy = false); return@queryProductDetailsAsync
                }
                val flow = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product).apply { offer.offerToken?.let { setOfferToken(it) } }.build())).build()
                val response = client.launchBillingFlow(launch, flow)
                mutable.value = mutable.value.copy(busy = response.responseCode == BillingClient.BillingResponseCode.OK,
                    message = if (response.responseCode == BillingClient.BillingResponseCode.OK) R.string.checking_purchase else R.string.purchase_unavailable)
            }
        }
    }
    override fun buy(activity: Activity, product: ProProduct) {
        if ((access.state.value.permanent || access.state.value.review)) return
        if (!configured || !client.isReady || mutable.value.busy) { refresh(); return }
        mutable.value = mutable.value.copy(busy = true)
        // Check both products immediately before checkout: don't sell the same access twice.
        owned { mutable.value = mutable.value.copy(busy = true); catalog(activity, product) }
    }
    override fun close() { queryGeneration++; if (billing.isInitialized()) client.endConnection() }
    companion object { val PRODUCTS = ProProduct.entries.map { it.id }.toSet() }
}
