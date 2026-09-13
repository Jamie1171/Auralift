package com.jamiewardle.auralift

import com.jamiewardle.auralift.audio.GainController
import org.junit.Assert.*
import org.junit.Test

class GainControllerTest {
    @Test fun fullRangeReachesEffectInAndroidUnitsAndReadsBack() {
        var nativeMb = 0
        val control = GainController({ nativeMb = it }, { nativeMb.toFloat() })
        for (step in 0..140) control.apply(step * 0.25f)
        assertEquals(3500, nativeMb)
        assertEquals(35f, control.reportedGainDb!!, 0f)
        control.apply(0f)
        assertEquals(0, nativeMb)
        assertEquals(0f, control.reportedGainDb!!, 0f)
    }
    @Test fun silentDeviceCapIsReportedInsteadOfClaimingRequestedGain() {
        var nativeMb = 0
        val control = GainController({ nativeMb = minOf(it, 1200) }, { nativeMb.toFloat() })
        control.apply(30f)
        assertEquals(12f, control.reportedGainDb!!, 0f)
        assertNotNull(control.note)
    }
    @Test fun rejectedHigherGainKeepsWorkingEffectAtLastAcceptedLevel() {
        var nativeMb = 0
        val control = GainController({ if (it > 1800) throw IllegalArgumentException("Device range") else nativeMb = it }, { nativeMb.toFloat() })
        control.apply(18f)
        control.apply(18.25f)
        assertEquals(18f, control.maxGainDb, 0f)
        assertEquals(18f, control.reportedGainDb!!, 0f)
        control.apply(30f)
        assertEquals(1800, nativeMb)
        control.apply(0f)
        assertEquals(0, nativeMb)
    }
    @Test fun failedOrInvalidReadbackCannotMasqueradeAsAppliedGain() {
        val failing = GainController({}, { throw IllegalStateException("No readback") })
        failing.apply(30f)
        assertNull(failing.reportedGainDb)
        for (invalid in listOf(Float.NaN, Float.POSITIVE_INFINITY, -1f, 9000f)) {
            val control = GainController({}, { invalid })
            control.apply(30f)
            assertNull(control.reportedGainDb)
        }
    }
    @Test fun failedReductionPropagatesSoOwnerCanReleaseTheEffect() {
        var nativeMb = 0
        var fail = false
        val control = GainController({ if (fail) throw IllegalArgumentException("Broken effect") else nativeMb = it }, { nativeMb.toFloat() })
        control.apply(30f)
        fail = true
        assertThrows(IllegalArgumentException::class.java) { control.apply(0f) }
    }
}
