package com.jamiewardle.auralift

import android.content.Intent
import android.net.MailTo
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.jamiewardle.auralift.support.SupportEmail
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
class SupportEmailTest {
    private val app get() = ApplicationProvider.getApplicationContext<AuraliftApplication>()

    @Test fun draftHasCorrectRecipientAndPreservesAccentsAndUriCharacters() {
        val message = "Égaliseur & volumen? +35 dB\nL’écran #1 = problème"
        val intent = SupportEmail.draft(app, "Assistance générale", message)
        assertEquals(Intent.ACTION_SENDTO, intent.action)
        val mail = MailTo.parse(intent.data.toString())
        assertEquals(SupportEmail.ADDRESS, mail.to)
        assertEquals("Auralift — Assistance générale", mail.subject)
        assertTrue(mail.body.endsWith(message))
        assertArrayEquals(arrayOf(SupportEmail.ADDRESS), intent.getStringArrayExtra(Intent.EXTRA_EMAIL))
        assertEquals("Auralift ${BuildConfig.VERSION_NAME}\nAssistance générale\n\n$message", intent.getStringExtra(Intent.EXTRA_TEXT))
        assertFalse(intent.hasExtra(Intent.EXTRA_STREAM))
        assertNull(intent.clipData)
    }

    @Test fun optionalAttachmentCarriesReadPermissionAndOptionalDiagnostics() {
        val uri = Uri.parse("content://test.screenshots/chosen.png")
        val intent = SupportEmail.draft(app, "Support", "An issue", "Selected diagnostic details", uri)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals(Intent.ACTION_SENDTO, intent.selector!!.action)
        assertEquals("mailto", intent.selector!!.data!!.scheme)
        assertEquals(uri, intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        assertEquals(uri, intent.clipData!!.getItemAt(0).uri)
        assertNotEquals(0, intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION)
        assertEquals(0, intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        assertTrue(intent.getStringExtra(Intent.EXTRA_TEXT)!!.endsWith("Selected diagnostic details"))
    }
}
