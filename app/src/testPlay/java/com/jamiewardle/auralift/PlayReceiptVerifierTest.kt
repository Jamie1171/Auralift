package com.jamiewardle.auralift

import com.android.billingclient.api.Purchase
import com.jamiewardle.auralift.access.PlayReceiptVerifier
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.security.KeyPairGenerator
import java.security.Signature
import java.util.Base64

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class PlayReceiptVerifierTest {
    private val pair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    private val key = Base64.getEncoder().encodeToString(pair.public.encoded)
    private val packageName = "com.jamiewardle.auralift"
    private val json = """{"packageName":"com.jamiewardle.auralift","productId":"auralift_pro","purchaseState":0,"token":"test-receipt","acknowledged":true}"""
    private fun sign(value: String) = Base64.getEncoder().encodeToString(Signature.getInstance("SHA1withRSA").run {
        initSign(pair.private); update(value.toByteArray(Charsets.UTF_8)); sign()
    })
    private fun valid(value: String) = PlayReceiptVerifier.valid(Purchase(value, sign(value)), packageName, key)

    @Test fun validPurchasedReceiptHasMatchingProductPackageAndSignature() {
        assertTrue(valid(json))
        assertTrue(valid(json.replace("auralift_pro", "auralift_supporter")))
        assertEquals(Purchase.PurchaseState.PURCHASED, Purchase(json, sign(json)).purchaseState)
    }
    @Test fun tamperingAndInvalidKeysCannotUnlockPro() {
        assertFalse(PlayReceiptVerifier.valid(Purchase(json.replace("test-receipt", "changed"), sign(json)), packageName, key))
        assertFalse(PlayReceiptVerifier.valid(Purchase(json, "not a signature"), packageName, key))
        assertFalse(PlayReceiptVerifier.valid(Purchase(json, sign(json)), packageName, "not a key"))
    }
    @Test fun pendingWrongProductWrongPackageAndEmptyTokenAreRejected() {
        assertFalse(valid(json.replace("\"purchaseState\":0", "\"purchaseState\":4")))
        assertFalse(valid(json.replace("auralift_pro", "other_product")))
        assertFalse(valid(json.replace(packageName, "com.example.other")))
        assertFalse(valid(json.replace("test-receipt", "")))
    }
}
