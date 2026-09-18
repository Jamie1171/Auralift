package com.jamiewardle.auralift.device

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.*
import androidx.core.os.LocaleListCompat
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.controls.word
import org.junit.Assert.*
import org.junit.Test

class LanguageDeviceTest : DeviceHarness() {
    private fun word(id: Int) = app.word(id)
    private fun settings() = compose.onNodeWithText(word(R.string.ui_settings), useUnmergedTree = true).performClick()

    @Test fun selectedLanguagesSurviveRecreationAndCoverPoliciesAndSupport() {
        launch()
        val receipt = app.terms.state.value
        try {
            for ((tag, name, heading) in listOf(Triple("en", "English", "Language"),
                Triple("es", "Español", "Idioma"), Triple("fr", "Français", "Langue"))) {
                settings()
                click(word(R.string.language))
                click(name)
                compose.waitUntil(12_000) {
                    compose.onAllNodesWithText(heading).fetchSemanticsNodes().isNotEmpty()
                }
                scenario!!.recreate()
                compose.waitForIdle()
                compose.onNodeWithText(heading).assertExists()
                assertEquals(tag, AppCompatDelegate.getApplicationLocales().toLanguageTags())
                assertEquals(receipt, app.terms.state.value)
                click(word(R.string.back))
                click(word(R.string.privacy_policy))
                val title = when (tag) {
                    "es" -> "Auralift — Política de privacidad"
                    "fr" -> "Auralift — Politique de confidentialité"
                    else -> "Auralift — Privacy policy"
                }
                compose.onNodeWithText(title, substring = true).assertExists()
                capture("privacy-$tag")
                settings()
                click(word(R.string.help_feedback))
                click(word(R.string.share_feedback))
                compose.onNodeWithText("auralift.support@gmail.com").assertExists()
                compose.onNodeWithText(word(R.string.feedback_message)).performTextInput("Test support message")
                capture("support-$tag")
                compose.onNodeWithText(word(R.string.choose_where_to_send)).performScrollTo().assertIsEnabled()
                compose.onNodeWithText(word(R.string.copy_support_email)).performScrollTo().assertIsDisplayed()
                assertFalse(app.engine.value.running)
            }
        } finally {
            onMain { AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList()) }
            compose.waitForIdle()
        }
    }
}
