package com.jamiewardle.auralift

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.Config

// These framework/logic checks do not need Robolectric native font loading.
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
class CompatibilitySmokeTest {
    @Test fun nativeActivityStartsWithoutEnablingAudioOrRequiringPermissions() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val app = ApplicationProvider.getApplicationContext<AuraliftApplication>()
        assertFalse(app.engine.value.running)
        assertEquals(0f, app.settings.state.value.gainDb)
        assertFalse(controller.get().isFinishing)
        controller.pause().stop().destroy()
        assertFalse(app.engine.value.running)
    }
}
