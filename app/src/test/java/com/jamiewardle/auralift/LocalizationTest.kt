package com.jamiewardle.auralift

import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.Config
import java.util.Locale

// These framework/logic checks do not need Robolectric native font loading.
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
class LocalizationTest {
    @Test fun translatedNativeResourcesAndGainFormattingWorkOnSupportedApis() {
        val app = ApplicationProvider.getApplicationContext<AuraliftApplication>()
        listOf("es" to "Escuchar", "fr" to "Écouter").forEach { (tag, listen) ->
            val context = app.createConfigurationContext(Configuration(app.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) })
            assertEquals(listen, context.getString(R.string.ui_listen))
            assertTrue(context.getString(R.string.android_target, 12.5f).contains("12,5"))
            assertTrue(context.getString(R.string.preview_minutes, 25).contains("25"))
        }
    }
}
