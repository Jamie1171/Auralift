package com.jamiewardle.auralift

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.support.SupportEmail
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
class SupportEmailTest {
    private val app get() = ApplicationProvider.getApplicationContext<AuraliftApplication>()
    private val mailProbe = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
    private fun receiver(pkg: String, activity: String) = ResolveInfo().apply {
        activityInfo = ActivityInfo().apply { packageName = pkg; name = activity; exported = true }
        isDefault = true
    }
    private fun register(intent: Intent, pkg: String, activity: String) {
        shadowOf(app.packageManager).addResolveInfoForIntent(intent, receiver(pkg, activity))
    }
    private fun target(chooser: Intent) = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!

    @Test fun textDraftUsesSendAndPreservesAccentsAndUriCharacters() {
        val message = "Égaliseur & volumen? +35 dB\nL’écran #1 = problème"
        val draft = SupportEmail.draft(app, "Assistance générale", message)
        register(mailProbe, "example.mail", "MailtoActivity")
        register(draft, "example.mail", "ShareActivity")
        val intent = target(SupportEmail.chooser(app, draft, "Email"))
        assertEquals("ShareActivity", intent.component!!.className)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertNull(intent.selector)
        assertNull(intent.data)
        assertEquals("Auralift — Assistance générale", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertArrayEquals(arrayOf(SupportEmail.ADDRESS), intent.getStringArrayExtra(Intent.EXTRA_EMAIL))
        assertEquals("Auralift ${BuildConfig.VERSION_NAME}\nAssistance générale\n\n$message", intent.getStringExtra(Intent.EXTRA_TEXT))
        assertFalse(intent.hasExtra(Intent.EXTRA_STREAM))
        assertNull(intent.clipData)
    }

    @Test fun attachmentAndBodyReachSendActivityWithReadAccessThroughChooser() {
        val uri = Uri.parse("content://test.screenshots/chosen.png")
        val draft = SupportEmail.draft(app, "Support", "An issue", "Selected diagnostic details", uri)
        register(mailProbe, "example.mail", "MailtoActivity")
        register(draft, "example.mail", "ShareActivity")
        val chooser = SupportEmail.chooser(app, draft, "Email")
        val intent = target(chooser)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("ShareActivity", intent.component!!.className)
        assertNull(intent.selector)
        assertEquals("image/*", intent.type)
        assertEquals(uri, intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        assertEquals(uri, intent.clipData!!.getItemAt(0).uri)
        assertEquals(uri, chooser.clipData!!.getItemAt(0).uri)
        assertNotEquals(0, intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION)
        assertNotEquals(0, chooser.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION)
        assertEquals(0, intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        assertTrue(intent.getStringExtra(Intent.EXTRA_TEXT)!!.endsWith("An issue\n\nSelected diagnostic details"))
        assertEquals("Auralift — Support", intent.getStringExtra(Intent.EXTRA_SUBJECT))
    }

    @Test fun chooserExcludesNonEmailAppsAndPreservesOtherEmailTargets() {
        val draft = SupportEmail.draft(app, "Support", "The complete message")
        register(mailProbe, "example.firstmail", "MailtoActivity")
        register(mailProbe, "example.secondmail", "MailtoActivity")
        register(draft, "example.social", "ShareActivity")
        register(draft, "example.firstmail", "ShareActivity")
        register(draft, "example.secondmail", "ShareActivity")
        val chooser = SupportEmail.chooser(app, draft, "Email")
        val targets = listOf(target(chooser)) + chooser.getParcelableArrayExtra(Intent.EXTRA_INITIAL_INTENTS)!!.map { it as Intent }
        assertEquals(setOf("example.firstmail", "example.secondmail"), targets.map { it.component!!.packageName }.toSet())
        targets.forEach {
            assertEquals(draft.getStringExtra(Intent.EXTRA_TEXT), it.getStringExtra(Intent.EXTRA_TEXT))
            assertArrayEquals(arrayOf(SupportEmail.ADDRESS), it.getStringArrayExtra(Intent.EXTRA_EMAIL))
        }
    }

    @Test(expected = ActivityNotFoundException::class)
    fun mailtoOnlyClientCannotBeLaunchedWithAnUnsupportedAttachment() {
        val draft = SupportEmail.draft(app, "Support", "An issue", screenshot = Uri.parse("content://test/image"))
        register(mailProbe, "example.mail", "MailtoActivity")
        register(draft, "example.social", "ShareActivity")
        SupportEmail.chooser(app, draft, "Email")
    }

    @Test fun requestedDeviceDetailsExistBeforeAudioServiceHasProducedDiagnostics() {
        val draft = SupportEmail.draft(app, "Support", "An issue", diagnostics = "")
        val body = draft.getStringExtra(Intent.EXTRA_TEXT)!!
        assertTrue(body.contains("Android ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}"))
        assertTrue(body.contains("${Build.MANUFACTURER} ${Build.MODEL}"))
        val without = SupportEmail.draft(app, "Support", "An issue").getStringExtra(Intent.EXTRA_TEXT)!!
        assertFalse(without.contains(" / API "))
    }
}
