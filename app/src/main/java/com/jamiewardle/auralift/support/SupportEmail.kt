package com.jamiewardle.auralift.support

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.jamiewardle.auralift.BuildConfig

/** Creates a user-reviewed email draft; never sends a message or claims delivery. */
object SupportEmail {
    const val ADDRESS = "auraforgelabssupport+auralift@gmail.com"

    fun draft(context: Context, category: String, message: String,
        diagnostics: String? = null, screenshot: Uri? = null): Intent {
        val details = diagnostics?.ifBlank {
            "Android ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}\n${Build.MANUFACTURER} ${Build.MODEL}"
        }
        val body = "Auralift ${BuildConfig.VERSION_NAME}\n$category\n\n${message.trim()}" +
            if (details != null) "\n\n$details" else ""
        // Gmail's SENDTO entry point may ignore EXTRA_TEXT / EXTRA_SUBJECT.
        // Send the whole draft to an actual SEND handler instead, without a selector.
        return Intent(Intent.ACTION_SEND).apply {
            type = if (screenshot == null) "text/plain"
                else context.contentResolver.getType(screenshot) ?: "image/*"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ADDRESS))
            putExtra(Intent.EXTRA_SUBJECT, "Auralift — $category")
            putExtra(Intent.EXTRA_TEXT, body)
            if (screenshot != null) {
                putExtra(Intent.EXTRA_STREAM, screenshot)
                clipData = ClipData.newRawUri("Screenshot", screenshot)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    fun chooser(context: Context, draft: Intent, title: String): Intent {
        val pm = context.packageManager
        // Discover email packages through mailto, but resolve their SEND activities.
        // A SENDTO activity is not necessarily able to receive a shared image.
        val emailPackages = pm.queryIntentActivities(
            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")), PackageManager.MATCH_DEFAULT_ONLY
        ).map { it.activityInfo.packageName }.toSet()
        val targets = pm.queryIntentActivities(draft, PackageManager.MATCH_DEFAULT_ONLY)
            .filter { it.activityInfo.packageName in emailPackages }
            .map { ComponentName(it.activityInfo.packageName, it.activityInfo.name) }
            .distinct()
            .map { Intent(draft).setComponent(it) }
        if (targets.isEmpty()) throw ActivityNotFoundException("No email app accepts this draft")
        return Intent.createChooser(targets.first(), title).apply {
            if (targets.size > 1) putExtra(Intent.EXTRA_INITIAL_INTENTS, targets.drop(1).toTypedArray())
            // Carry URI access through the chooser as well as every target intent.
            clipData = draft.clipData
            addFlags(draft.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
