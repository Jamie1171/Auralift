package com.jamiewardle.auralift

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.legal.TermsStore
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TermsAcceptanceUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = ApplicationProvider.getApplicationContext<AuraliftApplication>()

    @Test fun documentsCanBeReadBeforeAgreementAndAcceptanceNeverStartsBoost() {
        compose.runOnIdle { app.settings.update { it.copy(onboarded = true, gainDb = 10f) } }
        compose.onNodeWithText("Before you listen").assertIsDisplayed()
        compose.onNodeWithText("Enable boost").assertDoesNotExist()
        screenshot("terms-welcome")
        compose.onNodeWithText("Terms of use").performScrollTo().performClick()
        compose.onNodeWithText("Save a copy").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Agreement version: 2026-09-18.2", substring = true).assertExists()
        assertFalse(app.terms.hasAcceptedCurrent())
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Privacy policy").performScrollTo().performClick()
        compose.onNodeWithText("The record stays in private local storage", substring = true).assertExists()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Agree and continue").performScrollTo()
        screenshot("terms-accept")
        compose.onNodeWithText("Agree and continue").performClick()
        compose.waitUntil(10_000) { app.terms.hasAcceptedCurrent() }
        compose.onNodeWithText("Enable boost").assertExists()
        compose.runOnIdle {
            assertFalse(app.engine.value.running)
            assertEquals(10f, app.settings.state.value.gainDb)
            assertTrue(TermsStore(app).hasAcceptedCurrent())
        }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Before you listen").assertDoesNotExist()
        compose.onNodeWithText("Settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Terms of use").performScrollTo().performClick()
        compose.onNodeWithText("Save a copy").performScrollTo().assertIsDisplayed()
    }

    @Test fun decliningDoesNotWriteAgreementOrEnableAudio() {
        compose.onNodeWithText("Not now").performScrollTo().performClick()
        assertFalse(app.terms.hasAcceptedCurrent())
        assertNull(TermsStore(app).state.value)
        assertFalse(app.engine.value.running)
    }

    private fun screenshot(name: String) = compose.runOnIdle {
        val view = compose.activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        File("build/screenshots/$name.png").also { it.parentFile?.mkdirs() }.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
