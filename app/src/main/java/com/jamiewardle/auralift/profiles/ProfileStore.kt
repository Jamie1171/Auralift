package com.jamiewardle.auralift.profiles

import android.content.Context
import androidx.core.content.edit
import com.jamiewardle.auralift.SettingsStore
import com.jamiewardle.auralift.access.AccessStore
import com.jamiewardle.auralift.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SavedSound(val id: String, val name: String, val gain: Float, val curve: List<Float>)

object ProfileCodec {
    const val MAX_BYTES = 64 * 1024
    const val MAX_PROFILES = 24
    fun encode(profiles: List<SavedSound>): String = JSONObject().put("format", "auralift-sounds")
        .put("version", 1).put("sounds", JSONArray().apply {
            profiles.forEach { p -> put(JSONObject().put("name", p.name).put("gainDb", p.gain)
                .put("curve", JSONArray(p.curve))) }
        }).toString(2)
    fun decode(text: String): List<SavedSound> {
        require(text.toByteArray().size <= MAX_BYTES) { "File too large" }
        val root = JSONObject(text)
        require(root.getString("format") == "auralift-sounds" && root.getInt("version") == 1) { "Unsupported format" }
        val array = root.getJSONArray("sounds")
        require(array.length() in 1..MAX_PROFILES) { "Invalid profile count" }
        return List(array.length()) { i ->
            val item = array.getJSONObject(i)
            val name = item.getString("name").trim()
            require(name.length in 1..48 && name.none { it.isISOControl() }) { "Invalid name" }
            val gain = item.getDouble("gainDb").toFloat()
            require(gain.isFinite() && gain in 0f..GainMath.MAX_GAIN_DB) { "Invalid gain" }
            val bands = item.getJSONArray("curve")
            require(bands.length() == 5) { "Invalid EQ" }
            val curve = List(5) { bands.getDouble(it).toFloat().also { v -> require(v.isFinite() && v in -6f..6f) } }
            SavedSound(UUID.randomUUID().toString(), name, gain, curve)
        }
    }
}

class ProfileStore(context: Context, private val settings: SettingsStore, private val access: AccessStore) {
    private val disk = context.getSharedPreferences("named_sounds", Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(runCatching { ProfileCodec.decode(disk.getString("sounds", "")!!) }.getOrDefault(emptyList()))
    val state = mutable.asStateFlow()
    private fun save(list: List<SavedSound>) {
        disk.edit(commit = true) { putString("sounds", ProfileCodec.encode(list)) }
        mutable.value = list
    }
    private fun requirePro() { access.refresh(); check(access.state.value.pro) { "Pro required" } }
    fun add(name: String) {
        requirePro()
        val clean = name.trim()
        require(clean.length in 1..48 && clean.none { it.isISOControl() })
        require(mutable.value.size < ProfileCodec.MAX_PROFILES)
        val p = settings.state.value
        save(mutable.value + SavedSound(UUID.randomUUID().toString(), clean, p.gainDb, p.curve))
    }
    fun load(id: String) {
        requirePro()
        val profile = mutable.value.first { it.id == id }
        settings.update { it.copy(gainDb = profile.gain, preset = SoundPreset.CUSTOM, customEq = profile.curve) }
    }
    fun remove(id: String) { save(mutable.value.filterNot { it.id == id }) }
    fun export(): String { requirePro(); return ProfileCodec.encode(mutable.value) }
    fun import(text: String): Int {
        requirePro()
        // Validate every entry before mutating; import never applies gain or changes active sound.
        val profiles = ProfileCodec.decode(text)
        require(mutable.value.size + profiles.size <= ProfileCodec.MAX_PROFILES)
        save(mutable.value + profiles)
        return profiles.size
    }
}
