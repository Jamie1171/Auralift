package com.jamiewardle.auralift.controls

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

internal object OverlayPermission {
    fun appInfoIntent(packageName: String) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))

    fun intent(packageName: String, sdk: Int = Build.VERSION.SDK_INT) =
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            // Android 11+ deliberately opens the app list and ignores package deep links.
            if (sdk < 30) data = Uri.parse("package:$packageName")
        }
}
