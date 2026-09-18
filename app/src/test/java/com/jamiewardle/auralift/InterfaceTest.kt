package com.jamiewardle.auralift

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.model.SoundPreset
import com.jamiewardle.auralift.model.EngineState
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
class InterfaceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = ApplicationProvider.getApplicationContext<AuraliftApplication>()
    @Before fun proFixture() { compose.runOnIdle { assertTrue(app.terms.accept()); app.access.setVerifiedPurchase(true) } }
    @Test fun interfaceControlsPersistTheirRealSettings() {
        compose.onNodeWithText("Enable boost").assertExists()
        compose.onNodeWithContentDescription("Increase boost by half a decibel").performClick()
        compose.runOnIdle { assertEquals(0.5f, app.settings.state.value.gainDb) }
        compose.onNodeWithText("+35 dB").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(35f, app.settings.state.value.gainDb) }
        compose.onNodeWithContentDescription("Decrease boost by half a decibel").performClick()
        compose.runOnIdle { assertEquals(34.5f, app.settings.state.value.gainDb) }
        compose.onNodeWithText("+10 dB").performClick()
        compose.runOnIdle { assertEquals(10f, app.settings.state.value.gainDb) }
        compose.onNodeWithText("Bring sound closer.").performScrollTo()
        screenshot("listen")
        compose.onNodeWithText("Sound", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Voice").performClick()
        compose.runOnIdle { assertEquals(SoundPreset.VOICE, app.settings.state.value.preset) }
        screenshot("sound")
        compose.onNodeWithText("Settings", useUnmergedTree = true).performClick()
        screenshot("settings")
        compose.onNodeWithText("Audio controls").performScrollTo().performClick()
        compose.onNodeWithText("+5 dB").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(5f, app.settings.state.value.limitDb)
            assertEquals(5f, app.settings.state.value.gainDb)
        }
        compose.onNodeWithText("+35 dB").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(35f, app.settings.state.value.limitDb) }
        screenshot("audio-settings")
    }
    @Test fun connectionFeedbackDistinguishesSelectionFromAndroidReadback() {
        compose.runOnIdle {
            app.settings.update { it.copy(gainDb = 35f) }
            // Presentation fixture only; simulated UI does not establish physical gain.
            app.engine.value = EngineState(running = true, linked = true, reportedGainDb = 12f,
                gainReadback = "Android target: +12.0 dB", nativeCompression = true)
        }
        compose.onNodeWithText("Android target: +12.0 dB").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Compare original").assertExists()
        compose.onNodeWithText("Turn boost off").performScrollTo().assertIsDisplayed()
        screenshot("comparison")
        compose.runOnIdle { app.engine.value = app.engine.value.copy(comparingOriginal = true) }
        compose.onNodeWithText("Restore boost").assertExists()
        compose.runOnIdle { assertEquals(35f, app.settings.state.value.gainDb) }
    }
    @Test fun enablingRequiresFirstUseAcknowledgementAndDoesNotStartEarly() {
        compose.onNodeWithText("Enable boost").performClick()
        compose.onNodeWithText("A little lift. Start low.").assertIsDisplayed()
        compose.runOnIdle { assertFalse(app.engine.value.running) }
        compose.onNodeWithText("Later").performClick()
        compose.runOnIdle { assertFalse(app.settings.state.value.onboarded) }
    }
    @Test fun proExplainsEqualPurchasesAndBackgroundReminderIsOptIn() {
        compose.runOnIdle { if (Distribution.owner) app.access.simulateFree() else app.access.setVerifiedPurchase(false) }
        compose.onNodeWithText("Settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Auralift Pro").performScrollTo().performClick()
        compose.onNodeWithText("Ad Pass · 1 hour of Pro").performScrollTo().assertIsDisplayed()
        screenshot("ad-pass")
        compose.onNodeWithText("Supporter Pro").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Exactly the same Pro features. Choose this higher price only if you would like to support independent development.").assertExists()
        screenshot("pro-options")
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Permissions & background").performScrollTo().performClick()
        compose.runOnIdle { assertFalse(app.settings.state.value.restartReminder) }
        compose.onNodeWithText("Remind me after restarting").performScrollTo().assertIsDisplayed()
        screenshot("background")
    }
    @Test fun freeUiShowsFifteenAndFloatingPlayerHasItsOwnProPage() {
        compose.runOnIdle { if (Distribution.owner) app.access.simulateFree() else app.access.setVerifiedPurchase(false) }
        compose.onNodeWithText("+15 dB").performScrollTo().performClick()
        compose.onNodeWithText("+20 dB").assertDoesNotExist()
        compose.onNodeWithText("+35 dB").assertDoesNotExist()
        compose.runOnIdle { assertEquals(15f, app.settings.state.value.gainDb) }
        compose.onNodeWithText("Ad Pass · 1 hour of Pro").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("About Ad Pass").performScrollTo().assertIsDisplayed()
        screenshot("listen-ad-pass")
        compose.onNodeWithText("Bring sound closer.").performScrollTo()
        screenshot("free-listen")
        compose.onNodeWithText("Settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Floating player").assertIsDisplayed().performClick()
        compose.onNodeWithText("Explore Pro").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("One permission for Auralift").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertFalse(app.settings.state.value.floatingControls) }
    }
    @Test fun floatingPermissionExplainsTheSingleAppGrantBeforeAndroidSettings() {
        compose.onNodeWithText("Settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Floating player").performClick()
        compose.onNodeWithContentDescription("Enable floating player").performScrollTo().performClick()
        compose.onNodeWithText("Android will show a list of apps.", substring = true).assertIsDisplayed()
        compose.runOnIdle { assertFalse(app.settings.state.value.floatingControls) }
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle { assertFalse(app.settings.state.value.floatingControls) }
        compose.onNodeWithText("Android blocked the switch?").performScrollTo().performClick()
        compose.onNodeWithText("Open App info").performScrollTo().assertIsDisplayed()
        screenshot("floating-permission-help")
        compose.onNodeWithText("Open App info").performClick()
        compose.runOnIdle {
            val intent = org.robolectric.Shadows.shadowOf(compose.activity).nextStartedActivityForResult.intent
            assertEquals(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
            assertEquals("package:${app.packageName}", intent.dataString)
            assertFalse(app.settings.state.value.floatingControls)
        }
    }
    @Test fun listenPassTracksEarnedAccessAndExpiryWithoutStartingBoost() {
        compose.runOnIdle { if (Distribution.owner) app.access.simulateFree() else app.access.setVerifiedPurchase(false) }
        compose.onNodeWithText("About Ad Pass").performScrollTo().performClick()
        compose.onNodeWithText(app.getString(R.string.pro_heading)).assertExists()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.runOnIdle {
            assertFalse(app.engine.value.running)
            com.jamiewardle.auralift.access.RewardClaim(app.access).earned()
            assertTrue(app.access.state.value.pro)
            assertFalse(app.engine.value.running)
        }
        compose.onNodeWithText("Ad Pass is active").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Prepare Ad Pass").assertDoesNotExist()
        screenshot("listen-active-pass")
        compose.runOnIdle {
            app.settings.update { it.copy(gainDb = 35f) }
            app.getSharedPreferences("feature_access", 0).edit().putBoolean("passExpired", true).commit()
            app.access.refresh()
            assertEquals(15f, app.settings.state.value.gainDb)
            assertFalse(app.engine.value.running)
        }
        compose.onNodeWithText("Ad Pass · 1 hour of Pro").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ad Pass is active").assertDoesNotExist()
    }
    @Test fun completeThemesRenderInDarkAndLightAppearance() {
        for (accent in com.jamiewardle.auralift.model.Accent.entries) {
            for (light in listOf(false, true)) {
                compose.runOnIdle { app.settings.update { it.copy(accent = accent, lightTheme = light, gainDb = 15f) } }
                compose.onNodeWithText("Enable boost").assertExists()
                screenshot("theme-${accent.name.lowercase()}-${if (light) "light" else "dark"}")
            }
        }
    }
    private fun screenshot(name: String) {
        val dir = File("build/screenshots").apply { mkdirs() }
        compose.mainClock.advanceTimeBy(300)
        compose.runOnIdle {
            // Render the real native view tree. PixelCopy frame callbacks are not supplied
            // by this simulated display, so captureToImage would wait indefinitely.
            val view = compose.activity.window.decorView
            check(view.width > 0 && view.height > 0)
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
}
