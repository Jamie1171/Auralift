package com.jamiewardle.auralift.access

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AccessState(val pro: Boolean = false, val owner: Boolean = false,
                       val permanent: Boolean = false, val passRemainingMs: Long = 0)

/** Local clock, not DRM. Wall time carries an earned pass across a reboot. */
object PassClock {
    const val DURATION = 60 * 60_000L
    fun remaining(startElapsed: Long, startWall: Long, startBoot: Int,
                  elapsed: Long, wall: Long, boot: Int, lastWall: Long = startWall): Long {
        if (startElapsed < 0 || startWall <= 0 || wall < maxOf(startWall, lastWall)) return 0
        val sameBoot = startBoot == boot
        if (sameBoot && elapsed < startElapsed) return 0
        val used = if (sameBoot) maxOf(elapsed - startElapsed, wall - startWall) else wall - startWall
        return (DURATION - used).coerceIn(0, DURATION)
    }
}

class AccessStore(private val context: Context, private val ownerBuild: Boolean) {
    private val disk = context.getSharedPreferences("feature_access", Context.MODE_PRIVATE)
    // In memory: simulations cannot survive a new Owner process or affect the Play package.
    private var ownerSimulation = false
    private var purchased = false
    private val mutable = MutableStateFlow(AccessState())
    val state = mutable.asStateFlow()
    internal var entitlementChanged: (() -> Unit)? = null
    init {
        // Honour a still-active 0.3 courtesy preview once, without creating a new free trial.
        if (!disk.getBoolean("passMigration", false)) disk.edit(commit = true) {
            putBoolean("passMigration", true)
            if (disk.getBoolean("previewUsed", false) && !disk.getBoolean("previewExpired", false)) {
                putLong("passElapsed", disk.getLong("previewElapsed", -1))
                putLong("passWall", disk.getLong("previewWall", 0))
                putInt("passBoot", disk.getInt("previewBoot", -2))
            }
        }
        refresh()
    }
    private fun boot() = Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
    fun refresh() {
        val owner = ownerBuild && !ownerSimulation
        val wall = System.currentTimeMillis()
        val remaining = if (!disk.getBoolean("passExpired", false)) PassClock.remaining(
            disk.getLong("passElapsed", -1), disk.getLong("passWall", 0), disk.getInt("passBoot", -2),
            SystemClock.elapsedRealtime(), wall, boot(), disk.getLong("lastWall", 0)) else 0
        if (disk.contains("passWall")) disk.edit {
            if (remaining == 0L) putBoolean("passExpired", true)
            else putLong("lastWall", wall)
        }
        val wasPro = mutable.value.pro
        mutable.value = AccessState(owner || purchased || remaining > 0, owner, purchased,
            if (owner || purchased) 0 else remaining)
        if (wasPro != mutable.value.pro) entitlementChanged?.invoke()
    }
    /** Called only by a completed SDK reward or the isolated Owner simulator. No install grant. */
    internal fun grantEarnedPass(rewardId: String): Boolean {
        refresh()
        if (rewardId.isBlank() || disk.getString("lastReward", null) == rewardId || state.value.pro) return false
        val saved = disk.edit().putString("lastReward", rewardId).putBoolean("passExpired", false)
            .putLong("passElapsed", SystemClock.elapsedRealtime()).putLong("passWall", System.currentTimeMillis())
            .putLong("lastWall", System.currentTimeMillis()).putInt("passBoot", boot()).commit()
        refresh()
        return saved && state.value.pro
    }
    internal fun setVerifiedPurchase(value: Boolean) { purchased = value; refresh() }
    internal fun simulateFree() {
        check(ownerBuild); ownerSimulation = true; purchased = false
        disk.edit(commit = true) { clear(); putBoolean("passMigration", true) }; refresh()
    }
    internal fun simulateExpired() {
        check(ownerBuild); ownerSimulation = true; purchased = false
        disk.edit(commit = true) { putBoolean("passExpired", true) }; refresh()
    }
    internal fun simulateReward() { check(ownerBuild); simulateFree(); RewardClaim(this).earned() }
    internal fun simulatePurchase() { check(ownerBuild); simulateFree(); setVerifiedPurchase(true) }
    internal fun restoreOwner() { check(ownerBuild); ownerSimulation = false; refresh() }
}

/** The rewarded callback is idempotent, including a late callback after dismissal. */
internal class RewardClaim(private val access: AccessStore, private val id: String = java.util.UUID.randomUUID().toString()) {
    private var settled = false
    fun earned(): Boolean {
        if (settled) return false
        settled = true
        return access.grantEarnedPass(id)
    }
    fun failed() { settled = true }
}
