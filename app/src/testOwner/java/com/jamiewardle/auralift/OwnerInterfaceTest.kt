package com.jamiewardle.auralift

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.model.Accent
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w412dp-h915dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OwnerInterfaceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = ApplicationProvider.getApplicationContext<AuraliftApplication>()
    @Test fun ownerCanTestEarnedPassAndExpiryWithoutRaisingGain() {
        compose.runOnIdle { app.settings.update { it.copy(gainDb = 24f, accent = Accent.OCEAN) } }
        compose.onNodeWithText("Pro").performClick()
        screenshot("pro-owner")
        compose.onNodeWithText("Test Free").performScrollTo().performClick()
        compose.runOnIdle { assertFalse(app.access.state.value.pro); assertEquals(15f, app.settings.state.value.gainDb) }
        compose.onNodeWithText("Simulate completed ad → 1 hour Pro").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(app.access.state.value.pro); assertFalse(app.access.state.value.owner) }
        compose.onNodeWithText("Expire the Ad Pass").performScrollTo().performClick()
        compose.runOnIdle { assertFalse(app.access.state.value.pro); assertEquals(15f, app.settings.state.value.gainDb) }
        compose.onNodeWithText("Restore all owner features").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(app.access.state.value.owner) }
    }
    @Test fun nativeSettingsThemesHelpAndPoliciesAreReachable() {
        compose.onNodeWithText("Settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Appearance").performScrollTo().performClick()
        compose.onNodeWithText("Orchid").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(Accent.ORCHID, app.settings.state.value.accent) }
        screenshot("themes")
        compose.onNodeWithText("Back").performScrollTo().performClick()
        compose.onNodeWithText("Permissions & background").performScrollTo().performClick()
        screenshot("permissions")
        compose.onNodeWithText("Floating player").assertDoesNotExist()
        compose.onNodeWithText("Back").performScrollTo().performClick()
        compose.onNodeWithText("Floating player").performScrollTo().performClick()
        compose.onNodeWithText("Enable floating player").assertExists()
        screenshot("floating-player")
        compose.onNodeWithText("Back").performScrollTo().performClick()
        compose.onNodeWithText("Help & feedback").performScrollTo().performClick()
        compose.onNodeWithText("Why does the boost sound unchanged?").performClick()
        screenshot("help")
        compose.onNodeWithText("Share feedback").performScrollTo().performClick()
        compose.onNodeWithText("Your feedback").performTextInput("Testing feedback locally.")
        compose.onNodeWithText("Choose where to send").performScrollTo().assertIsEnabled()
        screenshot("feedback")
        compose.onNodeWithText("Settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Privacy policy").performScrollTo().performClick()
        compose.onNodeWithText("Auralift — Privacy policy", substring = true).assertExists()
    }
    private fun screenshot(name: String) {
        compose.mainClock.advanceTimeBy(300)
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val dir = File("build/screenshots").apply { mkdirs() }
            File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
}
