package com.jamiewardle.auralift

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.jamiewardle.auralift.access.AccessState
import com.jamiewardle.auralift.access.AdPassState
import com.jamiewardle.auralift.ui.AdPassCardContent
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w412dp-h915dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AdPassCardTest {
    @get:Rule val compose = createComposeRule()

    @Test fun preparingAndWatchingRequireSeparateUserActions() {
        val ads = mutableStateOf(AdPassState(available = true))
        var prepares = 0
        var watches = 0
        compose.setContent { MaterialTheme {
            AdPassCardContent(AccessState(), ads.value, { prepares++ }, { watches++ })
        } }
        compose.runOnIdle { assertEquals(0, prepares); assertEquals(0, watches) }
        compose.onNodeWithText("Prepare Ad Pass").performClick()
        compose.runOnIdle {
            assertEquals(1, prepares); assertEquals(0, watches)
            ads.value = ads.value.copy(busy = true)
        }
        compose.onNodeWithText("Preparing your ad…").assertIsNotEnabled()
        compose.runOnIdle { ads.value = ads.value.copy(busy = false, ready = true) }
        compose.onNodeWithText("Watch ad · unlock 1 hour").assertIsEnabled()
        compose.runOnIdle { assertEquals(0, watches) }
        compose.onNodeWithText("Watch ad · unlock 1 hour").performClick()
        compose.runOnIdle { assertEquals(1, prepares); assertEquals(1, watches) }
    }

    @Test fun activePassShowsTimeWhilePermanentAccessHidesTheOffer() {
        val access = mutableStateOf(AccessState(pro = true, passRemainingMs = 59 * 60_000L))
        compose.setContent { MaterialTheme {
            AdPassCardContent(access.value, AdPassState(available = true, ready = true),
                { fail("Preparation must be user initiated") }, { fail("Watching must be user initiated") })
        } }
        compose.onNodeWithText("59 minutes of Pro remaining").assertIsDisplayed()
        compose.onNodeWithText("Watch ad · unlock 1 hour").assertDoesNotExist()
        compose.runOnIdle { access.value = AccessState(pro = true, permanent = true) }
        compose.onNodeWithText("Ad Pass is active").assertDoesNotExist()
        compose.onNodeWithText("Ad Pass · 1 hour of Pro").assertDoesNotExist()
        compose.runOnIdle { access.value = AccessState(pro = true, owner = true) }
        compose.onNodeWithText("Ad Pass · 1 hour of Pro").assertDoesNotExist()
    }

    @Test fun unavailableAdsLeaveTheRewardActionDisabled() {
        compose.setContent { MaterialTheme {
            AdPassCardContent(AccessState(), AdPassState(available = false, message = R.string.ad_unavailable),
                { fail("Unavailable ads must not prepare") }, { fail("Unavailable ads must not show") })
        } }
        compose.onNodeWithText("Prepare Ad Pass").assertIsNotEnabled()
        compose.onNodeWithText("No ad is available right now.", substring = true).assertIsDisplayed()
    }
}
