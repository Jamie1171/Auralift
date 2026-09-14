package com.jamiewardle.auralift.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Run separately after host-side pm revoke; revoking in this process would kill the runner. */
@RunWith(AndroidJUnit4::class)
class PermissionDeniedDeviceTest : DeviceHarness() {
    override val grantNotifications = false
    @Test fun deniedMicrophoneAndNotificationsStillAllowBoostAndStop() {
        assumeTrue("Requires a fresh denied-permission installation; see device-test.sh",
            app.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED &&
                (Build.VERSION.SDK_INT < 33 || app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED))
        onMain {
            // Models a user who already declined the optional notification prompt.
            app.getSharedPreferences("device_ui", Context.MODE_PRIVATE).edit().putBoolean("notificationAsked", true).commit()
            app.settings.update { it.copy(spectrum = true) }
        }
        launch(); start()
        assertTrue(app.engine.value.running)
        click("Turn boost off")
        await("Stop without optional permissions") { !app.engine.value.running }
    }
}
