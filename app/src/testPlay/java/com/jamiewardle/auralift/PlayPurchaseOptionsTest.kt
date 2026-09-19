package com.jamiewardle.auralift

import com.jamiewardle.auralift.access.ProProduct
import org.junit.Assert.*
import org.junit.Test

class PlayPurchaseOptionsTest {
    @Test fun configuredLifetimeOptionsAreAvailableForTheirOwnProducts() {
        assertEquals("auralift_pro", ProProduct.PRO.id)
        assertEquals("auralift_supporter", ProProduct.SUPPORTER.id)
        assertTrue(ProProduct.PRO.acceptsPurchaseOption("pro-lifetime", null))
        assertTrue(ProProduct.SUPPORTER.acceptsPurchaseOption("supporter-lifetime", null))
        assertFalse(ProProduct.PRO.acceptsPurchaseOption("supporter-lifetime", null))
        assertFalse(ProProduct.SUPPORTER.acceptsPurchaseOption("pro-lifetime", null))
    }

    @Test fun originalBaseOptionRemainsAvailableButOtherOffersAreRejected() {
        ProProduct.entries.forEach { product ->
            assertTrue(product.acceptsPurchaseOption("buy", null))
            assertFalse(product.acceptsPurchaseOption("rent", null))
            assertFalse(product.acceptsPurchaseOption(null, null))
            assertFalse(product.acceptsPurchaseOption("buy", "discount"))
            assertFalse(product.acceptsPurchaseOption(product.purchaseOptionId, "discount"))
        }
    }
}
