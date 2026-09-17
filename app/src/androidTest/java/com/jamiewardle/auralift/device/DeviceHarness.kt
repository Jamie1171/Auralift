package com.jamiewardle.auralift.device

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.jamiewardle.auralift.*
import com.jamiewardle.auralift.audio.BoostService
import com.jamiewardle.auralift.model.Haptics
import com.jamiewardle.auralift.model.Preferences
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File

/** Runs only in an isolated emulator/Test Lab installation. No fixture enters the app APK. */
abstract class DeviceHarness {
    protected val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    protected val app get() = instrumentation.targetContext.applicationContext as AuraliftApplication
    protected val device get() = UiDevice.getInstance(instrumentation)
    protected val audio get() = app.getSystemService(AudioManager::class.java)
    protected var scenario: ActivityScenario<MainActivity>? = null
    private val snapshots = JSONArray()
    private var startedAt = 0L
    private val tracks = mutableListOf<AudioTrack>()
    protected open val grantNotifications = true

    @get:Rule(order = 1) val compose = createEmptyComposeRule()
    @get:Rule(order = 0) val evidence = object : TestWatcher() {
        override fun starting(description: Description) { startedAt = SystemClock.elapsedRealtime() }
        override fun succeeded(description: Description) = report(description, "passed", null)
        override fun failed(e: Throwable, description: Description) = report(description, "failed", e)
        override fun skipped(e: org.junit.AssumptionViolatedException, description: Description) = report(description, "skipped", e)
    }

    @Before fun prepareDevice() {
        device.wakeUp()
        shell("wm dismiss-keyguard")
        device.pressMenu()
        onMain { app.settings.update { it.copy(floatingControls = false) }; BoostService.stop(app) }
        await("previous session stopped") { !app.engine.value.running }
        shell("appops set ${app.packageName} SYSTEM_ALERT_WINDOW deny")
        // Granting is safe here; revoking a runtime permission would kill this instrumented process.
        if (grantNotifications && Build.VERSION.SDK_INT >= 33) {
            instrumentation.uiAutomation.grantRuntimePermission(app.packageName, Manifest.permission.POST_NOTIFICATIONS)
        }
        onMain {
            app.adAudio.resume()
            if (Distribution.owner) app.access.simulateFree() else {
                app.getSharedPreferences("feature_access", Context.MODE_PRIVATE).edit().clear()
                    .putBoolean("passMigration", true).commit()
                app.access.setVerifiedPurchase(false)
            }
            app.settings.update { Preferences(onboarded = true, haptics = Haptics.OFF) }
            app.settings.setLimit(15f)
            app.profiles.state.value.toList().forEach { app.profiles.remove(it.id) }
        }
    }

    @After fun cleanDevice() {
        snapshot("before_cleanup")
        // Save the state before teardown; a passing test must not hide an active failure.
        onMain {
            app.settings.update { it.copy(floatingControls = false) }
            BoostService.stop(app)
            app.adAudio.resume()
        }
        await("service cleanup") { !app.engine.value.running }
        tracks.forEach { runCatching { it.stop(); it.release() } }
        tracks.clear()
        scenario?.close(); scenario = null
        device.wakeUp()
        shell("wm dismiss-keyguard")
        shell("appops set ${app.packageName} SYSTEM_ALERT_WINDOW deny")
    }

    protected fun launch() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        await("activity visible") { app.activityVisible }
        compose.waitForIdle()
    }
    protected fun click(text: String) = compose.onNodeWithText(text).performScrollTo().performClick()
    protected fun onMain(block: () -> Unit) = instrumentation.runOnMainSync(block)
    protected fun await(label: String, timeoutMs: Long = 12_000, condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return
            SystemClock.sleep(100)
        }
        assertTrue("Timed out: $label\n${app.engine.value.diagnostics}", condition())
    }
    protected fun start() {
        click("Enable boost")
        await("foreground service started") { app.engine.value.running }
        snapshot("service_started")
    }
    protected fun command(action: String, minutes: Int? = null) = onMain {
        app.startService(Intent(app, BoostService::class.java).setAction(action).apply {
            minutes?.let { putExtra("minutes", it) }
        })
    }
    protected fun proFixture() = onMain {
        // An internal test fixture, not a purchase, ad delivery, or new public unlock path.
        app.access.setVerifiedPurchase(true)
        app.settings.setLimit(35f)
    }
    protected fun shell(command: String): String = instrumentation.uiAutomation.executeShellCommand(command).use {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().use { reader -> reader.readText() }
    }
    protected fun notifications() = app.getSystemService(NotificationManager::class.java).activeNotifications

    /** Controlled, muted test source. Never claim this measures speakers or another media app. */
    protected fun playerSession(): AudioTrack {
        val frames = 4410
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(44100)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(frames * 2).build()
        tracks += track
        assertEquals(frames, track.write(ShortArray(frames), 0, frames))
        assertEquals(AudioTrack.STATE_INITIALIZED, track.state)
        track.setVolume(0f)
        assertEquals(AudioTrack.SUCCESS, track.setLoopPoints(0, frames, -1))
        track.play()
        return track
    }
    protected fun announce(track: AudioTrack, opening: Boolean) = onMain {
        app.sendBroadcast(Intent(if (opening) AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION
            else AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
            .setPackage(app.packageName).putExtra(AudioEffect.EXTRA_AUDIO_SESSION, track.audioSessionId)
            .putExtra(AudioEffect.EXTRA_PACKAGE_NAME, app.packageName))
    }
    protected fun snapshot(label: String) {
        val state = app.engine.value
        snapshots.put(JSONObject().put("label", label).put("elapsedMs", SystemClock.elapsedRealtime() - startedAt)
            .put("running", state.running).put("linked", state.linked).put("sessionCount", state.sessionCount)
            .put("selectedGainDb", app.settings.state.value.gainDb)
            .put("androidReadbackDb", state.reportedGainDb ?: JSONObject.NULL)
            .put("diagnostics", state.diagnostics))
        Log.i("AuraliftDeviceTest", "$label: ${state.diagnostics}")
    }
    protected fun capture(name: String) {
        val runnerOutput = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val dir = (if (runnerOutput.isNullOrBlank()) File(app.getExternalFilesDir(null), "auralift-test-results")
            else File(runnerOutput, "auralift-test-results")).apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
    private fun report(description: Description, result: String, failure: Throwable?) {
        val name = "${description.className.substringAfterLast('.')}-${description.methodName}"
        // Gradle's device runner pulls this directory BEFORE uninstalling the APK.
        // Firebase/manual instrumentation uses the app-specific fallback directory.
        val runnerOutput = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val dir = (if (runnerOutput.isNullOrBlank()) File(app.getExternalFilesDir(null), "auralift-test-results")
            else File(runnerOutput, "auralift-test-results")).apply { mkdirs() }
        val report = JSONObject().put("test", description.displayName).put("result", result)
            .put("elapsedMs", SystemClock.elapsedRealtime() - startedAt)
            .put("manufacturer", Build.MANUFACTURER).put("model", Build.MODEL).put("api", Build.VERSION.SDK_INT)
            .put("package", app.packageName).put("version", BuildConfig.VERSION_NAME)
            .put("pageSize", android.system.Os.sysconf(android.system.OsConstants._SC_PAGESIZE))
            .put("observations", snapshots).put("failure", failure?.stackTraceToString() ?: JSONObject.NULL)
            .put("scope", "Software and Android effect readback only; no acoustic, OEM endurance, live ad or payment certification.")
        File(dir, "$name.json").writeText(report.toString(2))
        if (failure != null) runCatching {
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
        }
    }
}
