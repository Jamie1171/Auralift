package com.jamiewardle.auralift

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
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
class LocalizationUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = ApplicationProvider.getApplicationContext<AuraliftApplication>()
    private fun word(id: Int) = compose.activity.getString(id)
    private fun click(id: Int) = compose.onNodeWithText(word(id)).performScrollTo().performClick()
    private fun back() = compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

    @Test @Config(qualifiers = "es-rES-w360dp-h640dp-xhdpi")
    fun spanishDocumentsAndSupportAreLocalized() = checkLanguage("es", "Condiciones de uso", "Política de privacidad", "Comentarios y asistencia")

    @Test @Config(qualifiers = "fr-rFR-w360dp-h640dp-xhdpi")
    fun frenchDocumentsAndSupportAreLocalized() = checkLanguage("fr", "Conditions d’utilisation", "Politique de confidentialité", "Commentaires et assistance")

    private fun checkLanguage(language: String, termsTitle: String, privacyTitle: String, supportTitle: String) {
        compose.runOnIdle { app.settings.update { it.copy(onboarded = true) } }
        click(R.string.terms_of_use)
        compose.onNodeWithText("Auralift — $termsTitle", substring = true).assertExists()
        click(R.string.document_english)
        compose.onNodeWithText("Auralift — Terms of use", substring = true).assertExists()
        click(R.string.document_app_language)
        compose.onNodeWithText("Auralift — $termsTitle", substring = true).assertExists()
        back()
        click(R.string.privacy_policy)
        compose.onNodeWithText("Auralift — $privacyTitle", substring = true).assertExists()
        back()
        click(R.string.terms_agree)
        compose.waitUntil(10_000) { app.terms.hasAcceptedCurrent() }
        assertEquals(language, app.terms.state.value!!.language)
        assertFalse(app.engine.value.running)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText(word(R.string.ui_settings), useUnmergedTree = true).performClick()
        click(R.string.help_feedback)
        click(R.string.share_feedback)
        compose.onNodeWithText(supportTitle).assertExists()
        compose.onNodeWithText("auralift.support@gmail.com").assertExists()
        compose.onNodeWithText(word(R.string.feedback_message)).performTextInput("Test de support local")
        compose.onNodeWithText(word(R.string.choose_where_to_send)).performScrollTo().assertIsEnabled()
    }

    @Test @Config(qualifiers = "es-rES-w360dp-h640dp-xhdpi")
    fun englishChoiceIsRetainedWhenReturningFromDocumentsAndIsRecorded() {
        click(R.string.terms_of_use)
        click(R.string.document_english)
        back()
        compose.onNodeWithText("Idioma del documento: English").assertExists()
        click(R.string.privacy_policy)
        compose.onNodeWithText("Auralift — Privacy policy", substring = true).assertExists()
        back()
        click(R.string.terms_agree)
        compose.waitUntil(10_000) { app.terms.hasAcceptedCurrent() }
        assertEquals("en", app.terms.state.value!!.language)
    }
}
