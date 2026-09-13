package com.jamiewardle.auralift.access

import com.android.billingclient.api.Purchase
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

/** Local signature verification is defence in depth, not a substitute for server verification. */
internal object PlayReceiptVerifier {
    fun valid(p: Purchase, packageName: String, publicKey: String): Boolean = runCatching {
        if (p.purchaseState != Purchase.PurchaseState.PURCHASED || (p.products.size != 1 || p.products.single() !in PlayPurchases.PRODUCTS) ||
            p.packageName != packageName || p.purchaseToken.isBlank()) return false
        val key = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)))
        Signature.getInstance("SHA1withRSA").run {
            initVerify(key); update(p.originalJson.toByteArray(Charsets.UTF_8)); verify(Base64.getDecoder().decode(p.signature))
        }
    }.getOrDefault(false)

}
