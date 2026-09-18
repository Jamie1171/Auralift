package com.jamiewardle.auralift.legal

import android.content.Context
import android.util.AtomicFile
import com.jamiewardle.auralift.BuildConfig
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class TermsAcceptance(
    val version: String,
    val documentSha256: String,
    val appVersion: String,
    val appVersionCode: Int,
    val acceptedAtEpochMs: Long
)

/** Local acknowledgement, not identity verification, a trusted clock or a liability waiver. */
class TermsStore(
    context: Context,
    val version: String = CURRENT_VERSION,
    val document: String = context.assets.open("legal/terms.txt").bufferedReader().use { it.readText() },
    private val clock: () -> Long = System::currentTimeMillis
) {
    val documentSha256 = MessageDigest.getInstance("SHA-256")
        .digest(document.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    private val disk = AtomicFile(File(context.noBackupFilesDir, FILE_NAME))
    private val mutableState = MutableStateFlow(read())
    val state = mutableState.asStateFlow()

    // Bump the agreement version for material changes. Editorial changes may keep
    // the version; the receipt still identifies the exact text originally accepted.
    fun hasAcceptedCurrent() = state.value?.version == version

    @Synchronized fun accept(): Boolean {
        if (hasAcceptedCurrent()) return true
        val record = TermsAcceptance(version, documentSha256, BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE, clock())
        if (record.acceptedAtEpochMs <= 0) return false
        val json = JSONObject().put("version", record.version)
            .put("documentSha256", record.documentSha256).put("appVersion", record.appVersion)
            .put("appVersionCode", record.appVersionCode).put("acceptedAtEpochMs", record.acceptedAtEpochMs)
        var stream: java.io.FileOutputStream? = null
        return try {
            stream = disk.startWrite()
            stream.write(json.toString().toByteArray(Charsets.UTF_8))
            disk.finishWrite(stream)
            // AtomicFile can log a failed rename without throwing; verify before granting access.
            if (read() != record) return false
            mutableState.value = record
            true
        } catch (_: java.io.IOException) {
            disk.failWrite(stream)
            false
        }
    }

    private fun read(): TermsAcceptance? = try {
        val json = JSONObject(disk.openRead().bufferedReader().use { it.readText() })
        TermsAcceptance(json.getString("version"), json.getString("documentSha256"),
            json.getString("appVersion"), json.getInt("appVersionCode"), json.getLong("acceptedAtEpochMs"))
            .takeIf { it.version.isNotBlank() && it.documentSha256.matches(Regex("[0-9a-f]{64}")) &&
                it.appVersion.isNotBlank() && it.appVersionCode > 0 && it.acceptedAtEpochMs > 0 }
    } catch (_: java.io.IOException) { null }
      catch (_: org.json.JSONException) { null }

    companion object {
        const val CURRENT_VERSION = "2026-09-18.1"
        internal const val FILE_NAME = "terms-acceptance.json"
    }
}
