package com.jamiewardle.auralift.support

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.jamiewardle.auralift.BuildConfig

/** Creates a user-reviewed email draft; never sends a message or claims delivery. */
object SupportEmail {
    const val ADDRESS = "auralift.support@gmail.com"

    fun draft(context: Context, category: String, message: String,
        diagnostics: String? = null, screenshot: Uri? = null): Intent {
        val body = "Auralift ${BuildConfig.VERSION_NAME}\n$category\n\n${message.trim()}" +
            if (diagnostics != null) "\n\n$diagnostics" else ""
        val subject = "Auralift — $category"
        // Keep user text out of the mailto query: Android MailTo decodes before
        // splitting query parameters, which can truncate text containing '&'.
        val intent = if (screenshot == null) Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$ADDRESS"))
        else Intent(Intent.ACTION_SEND).apply {
            selector = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            type = context.contentResolver.getType(screenshot) ?: "image/*"
            putExtra(Intent.EXTRA_STREAM, screenshot)
            clipData = ClipData.newRawUri("Screenshot", screenshot)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return intent.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ADDRESS))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
    }
}
