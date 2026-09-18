package com.jamiewardle.auralift

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.access.AccessStore
import com.jamiewardle.auralift.ui.ReviewAccess
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReviewAccessInterfaceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun reviewerCanCorrectCodeUnlockAndReturnToFree() {
        val app = ApplicationProvider.getApplicationContext<AuraliftApplication>()
        val code = "0123456789ABCDEF0123456789ABCDEF"
        val digest = MessageDigest.getInstance("SHA-256").digest(code.toByteArray()).joinToString("") { "%02x".format(it) }
        val access = AccessStore(app, false, digest)
        compose.setContent { MaterialTheme { ReviewAccess(access) } }
        compose.onNodeWithText("Review access").performClick()
        compose.onNodeWithText("Access code").performTextInput("wrong")
        compose.onNodeWithText("Unlock Pro for review").performClick()
        compose.onNodeWithText("Code not recognised or access could not be saved. Check the code and try again.").assertIsDisplayed()
        compose.onNodeWithText("Access code").performTextReplacement(code)
        compose.onNodeWithText("Unlock Pro for review").performClick()
        compose.onNodeWithText("Review access active").assertIsDisplayed()
        compose.runOnIdle { assertTrue(access.state.value.review) }
        compose.onNodeWithText("End review access").performClick()
        compose.runOnIdle { assertFalse(access.state.value.pro) }
        compose.onNodeWithText("Review access").assertIsDisplayed()
    }
}
