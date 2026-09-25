package com.jamiewardle.auralift

import android.app.Activity
import android.os.Looper
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.Button
import com.jamiewardle.auralift.controls.RepeatTouch
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 35])
@LooperMode(LooperMode.Mode.PAUSED)
class RepeatTouchTest {
    private val controller = Robolectric.buildActivity(Activity::class.java)
    private lateinit var button: Button
    private var clicks = 0
    private var allowed = true
    @Before fun setup() {
        val activity = controller.setup().get()
        button = Button(activity).apply {
            // API 26 drawable/elevation animations advance Robolectric's clock
            // during idleFor. Keep this input/timer fixture free of animations.
            background = null
            stateListAnimator = null
            setOnClickListener { clicks++ }
        }
        activity.setContentView(button)
        button.layout(0, 0, 100, 100)
        RepeatTouch.install(button, 300L) { allowed }
    }
    @After fun teardown() { controller.pause().stop().destroy() }
    private fun event(action: Int, x: Float = 20f) {
        MotionEvent.obtain(0, 0, action, x, 20f, 0).also { button.dispatchTouchEvent(it); it.recycle() }
    }
    private fun advance(ms: Long) = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ms))
    @Test fun tapAndAccessibleClickRemainSingleSteps() {
        event(MotionEvent.ACTION_DOWN); event(MotionEvent.ACTION_UP)
        assertEquals(1, clicks)
        advance(1000); assertEquals(1, clicks)
        button.performClick(); assertEquals(2, clicks)
    }
    @Test fun holdRepeatsAfterDelayWithoutExtraReleaseStep() {
        val beforeDown = android.os.SystemClock.uptimeMillis()
        event(MotionEvent.ACTION_DOWN)
        val holdDelay = ViewConfiguration.getLongPressTimeout().toLong()
        // Check both sides of the hold threshold and exact repeat/release counts.
        println("hold delay=$holdDelay beforeDown=$beforeDown afterDown=${android.os.SystemClock.uptimeMillis()} clicks=$clicks")
        assertEquals("No synchronous click on press", 0, clicks)
        advance(holdDelay / 2)
        assertEquals("No repeat halfway through hold delay=$holdDelay at ${android.os.SystemClock.uptimeMillis()}", 0, clicks)
        advance(holdDelay - holdDelay / 2 + 10); assertEquals(1, clicks)
        advance(600); assertEquals(3, clicks)
        event(MotionEvent.ACTION_UP); advance(1000)
        assertEquals(3, clicks)
    }
    @Test fun cancellationAndMovingOutsideDoNotClickOrRepeat() {
        event(MotionEvent.ACTION_DOWN); event(MotionEvent.ACTION_CANCEL); advance(1000)
        event(MotionEvent.ACTION_DOWN); event(MotionEvent.ACTION_MOVE, -1f)
        event(MotionEvent.ACTION_UP); advance(1000)
        assertEquals(0, clicks)
    }
    @Test fun revokedAccessStopsAnActiveHoldAndSuppressesReleaseClick() {
        event(MotionEvent.ACTION_DOWN)
        advance(ViewConfiguration.getLongPressTimeout().toLong())
        assertEquals(1, clicks)
        allowed = false; advance(1000); event(MotionEvent.ACTION_UP)
        assertEquals(1, clicks)
    }
    @Test fun removingThePanelCancelsAnActiveHold() {
        event(MotionEvent.ACTION_DOWN)
        advance(ViewConfiguration.getLongPressTimeout().toLong())
        assertEquals(1, clicks)
        controller.get().setContentView(Button(controller.get()))
        advance(1000); assertEquals(1, clicks)
    }
}
