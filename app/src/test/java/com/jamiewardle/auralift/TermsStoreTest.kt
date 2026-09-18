package com.jamiewardle.auralift

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.audio.BoostService
import com.jamiewardle.auralift.legal.TermsStore
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class TermsStoreTest {
    private val app get() = ApplicationProvider.getApplicationContext<AuraliftApplication>()

    @Test fun oldWarningFlagDoesNotImplyAgreementAndDirectServiceCannotBypassIt() {
        app.settings.update { it.copy(onboarded = true, gainDb = 10f, floatingControls = true) }
        assertFalse(app.terms.hasAcceptedCurrent())
        val controller = Robolectric.buildService(BoostService::class.java).create()
        try {
            controller.get().onStartCommand(Intent().setAction(BoostService.START), 0, 1)
            controller.get().onStartCommand(Intent().setAction(BoostService.SHOW_FLOATING), 0, 2)
            controller.get().onStartCommand(Intent().setAction(BoostService.RECONNECT), 0, 3)
            assertFalse(app.engine.value.running)
            val chains = BoostService::class.java.getDeclaredField("chains").apply { isAccessible = true }
            assertTrue((chains.get(controller.get()) as Map<*, *>).isEmpty())
            assertNull(app.terms.state.value)
        } finally { controller.destroy() }
    }

    @Test fun acceptanceSurvivesReloadWithoutStartingAudioOrChangingGain() {
        val time = 1_800_000_000_000L
        val store = TermsStore(app, clock = { time })
        app.settings.update { it.copy(gainDb = 10f) }
        assertTrue(store.accept())
        val receipt = TermsStore(app).state.value!!
        assertEquals(store.version, receipt.version)
        assertEquals(store.documentSha256, receipt.documentSha256)
        assertEquals(BuildConfig.VERSION_NAME, receipt.appVersion)
        assertEquals(BuildConfig.VERSION_CODE, receipt.appVersionCode)
        assertEquals(time, receipt.acceptedAtEpochMs)
        assertEquals(10f, app.settings.state.value.gainDb)
        assertFalse(app.engine.value.running)
        assertTrue(store.accept())
        assertEquals(receipt, TermsStore(app).state.value)
    }

    @Test fun materialVersionChangeRequiresNewAgreementButAnAppUpdateDoesNot() {
        val old = TermsStore(app, version = "older", document = "Old terms", clock = { 1234L })
        assertTrue(old.accept())
        val current = TermsStore(app)
        assertFalse(current.hasAcceptedCurrent())
        assertEquals("older", current.state.value!!.version)
        assertTrue(current.accept())
        assertTrue(TermsStore(app).hasAcceptedCurrent())
        assertEquals(current.documentSha256, current.state.value!!.documentSha256)
        // Same agreement version permits editorial-only changes and keeps the original receipt.
        val editorial = TermsStore(app, document = current.document + "\n")
        assertTrue(editorial.hasAcceptedCurrent())
        assertEquals(current.state.value, editorial.state.value)
    }

    @Test fun missingCorruptOrIncompleteRecordsRequireAcceptanceAgain() {
        val disk = File(app.noBackupFilesDir, TermsStore.FILE_NAME)
        disk.writeText("not json")
        assertFalse(TermsStore(app).hasAcceptedCurrent())
        disk.writeText("{\"version\":\"${TermsStore.CURRENT_VERSION}\"}")
        assertFalse(TermsStore(app).hasAcceptedCurrent())
        assertTrue(TermsStore(app).accept())
        disk.delete()
        assertFalse(TermsStore(app).hasAcceptedCurrent())
    }

    @Test fun failedPersistenceDoesNotGrantAccess() {
        val disk = File(app.noBackupFilesDir, TermsStore.FILE_NAME)
        assertTrue(disk.mkdir())
        File(disk, "block-replacement").writeText("test")
        val store = TermsStore(app)
        assertFalse(store.accept())
        assertFalse(store.hasAcceptedCurrent())
    }
}
