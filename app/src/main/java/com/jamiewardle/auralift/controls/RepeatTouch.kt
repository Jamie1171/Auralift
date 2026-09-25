package com.jamiewardle.auralift.controls

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

/** Keeps normal/accessible clicks; a hold repeats without an extra release click. */
internal object RepeatTouch {
    @SuppressLint("ClickableViewAccessibility")
    fun install(view: View, intervalMillis: Long, allowed: () -> Boolean = { true }) {
        val handler = Handler(Looper.getMainLooper())
        var pressed = false
        var repeated = false
        lateinit var tick: Runnable
        fun stop() {
            pressed = false
            view.isPressed = false
            handler.removeCallbacks(tick)
        }
        tick = Runnable {
            if (!pressed || !view.isAttachedToWindow || !view.isEnabled || !allowed()) {
                stop()
            } else {
                repeated = true
                view.performClick()
                if (pressed) handler.postDelayed(tick, intervalMillis)
            }
        }
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = Unit
            override fun onViewDetachedFromWindow(v: View) = stop()
        })
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    stop()
                    repeated = false
                    pressed = view.isEnabled && allowed()
                    view.isPressed = pressed
                    if (pressed) handler.postDelayed(tick, ViewConfiguration.getLongPressTimeout().toLong())
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.x < 0 || event.y < 0 || event.x >= view.width || event.y >= view.height) stop()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val click = pressed && !repeated && view.isEnabled && allowed()
                    stop()
                    if (click) view.performClick()
                    true
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_DOWN -> { stop(); true }
                else -> false
            }
        }
    }
}
