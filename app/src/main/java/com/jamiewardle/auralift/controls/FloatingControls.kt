package com.jamiewardle.auralift.controls

import android.app.KeyguardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.compose.ui.graphics.toArgb
import com.jamiewardle.auralift.AuraliftApplication
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.model.*
import com.jamiewardle.auralift.ui.appTheme
import kotlin.math.abs

/** The window can remain open while its service has released every audio effect. */
class FloatingControls(private val context: Context, private val compare: () -> Unit, private val toggleBoost: () -> Unit) {
    private val app get() = context.applicationContext as AuraliftApplication
    private val windows = context.getSystemService(WindowManager::class.java)
    private var view: LinearLayout? = null
    private var gain: TextView? = null
    private var status: TextView? = null
    private var bubble: ImageButton? = null
    private var compareButton: Button? = null
    private var boostButton: Button? = null
    private val presetButtons = linkedMapOf<SoundPreset, Button>()
    private var expanded = false
    private var signature = ""
    private var x = 12
    private var y = 140
    private var params: WindowManager.LayoutParams? = null
    private fun dp(value: Int) = (value * context.resources.displayMetrics.density).toInt()
    private val appearance get() = appTheme(app.settings.state.value.accent, app.settings.state.value.lightTheme)

    fun refresh() {
        val prefs = app.settings.state.value
        val permitted = prefs.floatingControls && app.access.state.value.pro &&
            !app.activityVisible && !app.adAudio.blocked.value && Settings.canDrawOverlays(context) &&
            context.getSystemService(PowerManager::class.java).isInteractive &&
            !context.getSystemService(KeyguardManager::class.java).isKeyguardLocked
        if (!permitted) { dismiss(); return }
        val key = "${prefs.accent}/${prefs.lightTheme}/${context.localized().resources.configuration.locales}/${context.resources.configuration.orientation}"
        if (key != signature) { dismiss(); signature = key }
        if (view == null) show()
        val running = app.engine.value.running
        val colors = appearance.colors
        gain?.text = context.word(R.string.floating_gain, prefs.gainDb)
        status?.text = context.word(if (running) R.string.boost_on else R.string.boost_off)
        compareButton?.apply {
            text = context.word(if (app.engine.value.comparingOriginal) R.string.restore_boost else R.string.compare_original)
            contentDescription = text
            isEnabled = running
            alpha = if (running) 1f else .45f
        }
        boostButton?.apply {
            text = context.word(if (running) R.string.stop_boost else R.string.ui_enable_boost)
            contentDescription = text
        }
        bubble?.apply {
            imageTintList = ColorStateList.valueOf((if (running) colors.onPrimary else colors.primary).toArgb())
            background = shape((if (running) colors.primary else colors.surface).toArgb(), colors.primary.toArgb(), circle = true)
            if (Build.VERSION.SDK_INT >= 30) stateDescription = context.word(if (running) R.string.boost_on else R.string.boost_off)
        }
        presetButtons.forEach { (preset, button) ->
            val selected = prefs.preset == preset
            button.isSelected = selected
            button.setTextColor((if (selected) colors.primary else colors.onSurface).toArgb())
            button.background = shape((if (selected) colors.primaryContainer else colors.surfaceVariant).toArgb(),
                (if (selected) colors.primary else colors.outlineVariant).toArgb(), border = if (selected) 2 else 1)
            if (Build.VERSION.SDK_INT >= 30) button.stateDescription = context.word(if (selected) R.string.theme_selected else R.string.floating_not_selected)
        }
    }
    private fun shape(color: Int, stroke: Int? = null, border: Int = 1, circle: Boolean = false) = GradientDrawable().apply {
        shape = if (circle) GradientDrawable.OVAL else GradientDrawable.RECTANGLE
        setColor(color)
        if (!circle) cornerRadius = dp(appearance.corner.value.toInt().coerceAtMost(24)).toFloat()
        if (stroke != null) setStroke(dp(border), stroke)
    }
    private fun resize(expand: Boolean) { expanded = expand; dismiss(); refresh() }
    private fun show() {
        val colors = appearance.colors
        val panel = LinearLayout(context.localized()).apply {
            orientation = LinearLayout.VERTICAL
            setOnTouchListener { _, event ->
                if (expanded && event.actionMasked == MotionEvent.ACTION_OUTSIDE) {
                    resize(false)
                    true
                } else false
            }
            if (expanded) {
                setPadding(dp(8), dp(8), dp(8), dp(8))
                background = shape(colors.surface.toArgb(), colors.outline.toArgb())
            }
            elevation = dp(12).toFloat()
        }
        if (!expanded) {
            val handle = ImageButton(context.localized()).apply {
                setImageResource(R.drawable.ic_wave)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setPadding(dp(18), dp(18), dp(18), dp(18))
                contentDescription = context.word(R.string.drag_floating)
                setOnClickListener { resize(true) }
            }
            bubble = handle
            panel.addView(handle, LinearLayout.LayoutParams(dp(64), dp(64)))
            drag(handle)
        } else {
            val header = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
            val title = TextView(context.localized()).apply {
                textSize = 14f; setTextColor(colors.primary.toArgb()); gravity = Gravity.CENTER
                minHeight = dp(48); contentDescription = context.word(R.string.drag_floating)
                isClickable = true; isFocusable = true
                setOnClickListener { resize(false) }
            }
            gain = title
            header.addView(title, LinearLayout.LayoutParams(0, -2, 1f)); drag(title)
            header.addView(button("−", context.word(R.string.minimise_floating), requiresPro = false) { resize(false) }, LinearLayout.LayoutParams(dp(48), dp(48)))
            header.addView(button("×", context.word(R.string.close_floating), requiresPro = false) {
                app.settings.update { it.copy(floatingControls = false) }; expanded = false; dismiss()
            }, LinearLayout.LayoutParams(dp(48), dp(48)))
            panel.addView(header)
            status = TextView(context.localized()).apply {
                textSize = 12f; gravity = Gravity.CENTER; setTextColor(colors.onSurfaceVariant.toArgb())
                setPadding(0, dp(3), 0, dp(7))
            }
            panel.addView(status)
            val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            val scroll = ScrollView(context).apply { isFillViewport = true; addView(body) }
            panel.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
            fun row(vararg controls: Button) = LinearLayout(context).also { r ->
                controls.forEach { r.addView(it, LinearLayout.LayoutParams(0, dp(48), 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }) }; body.addView(r)
            }
            row(button("−", context.word(R.string.decrease_boost), repeatMillis = 300L) { adjustGain(-0.5f) },
                button("+", context.word(R.string.increase_boost), repeatMillis = 300L) { adjustGain(0.5f) })
            row(button(context.word(R.string.volume_down), context.word(R.string.volume_down), repeatMillis = 180L) { volume(-1) },
                button(context.word(R.string.volume_up), context.word(R.string.volume_up), repeatMillis = 180L) { volume(1) })
            compareButton = button(context.word(R.string.compare_original), context.word(R.string.compare_original), action = compare)
            row(compareButton!!)
            row(button("|◀", context.word(R.string.previous_track)) { MediaActions.send(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS) },
                button("▶ / Ⅱ", context.word(R.string.play_pause)) { MediaActions.send(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) },
                button("▶|", context.word(R.string.next_track)) { MediaActions.send(context, KeyEvent.KEYCODE_MEDIA_NEXT) })
            fun presetButton(preset: SoundPreset, label: Int) = button(context.word(label), context.word(label)) {
                app.settings.update { it.copy(preset = if (it.preset == preset) SoundPreset.BALANCED else preset) }
                refresh()
            }.also { presetButtons[preset] = it }
            row(presetButton(SoundPreset.BALANCED, R.string.preset_balanced), presetButton(SoundPreset.VOICE, R.string.preset_voice))
            row(presetButton(SoundPreset.WARM, R.string.preset_warm), presetButton(SoundPreset.DETAIL, R.string.preset_detail))
            boostButton = button(context.word(R.string.stop_boost), context.word(R.string.stop_boost), requiresPro = false) {
                // Stop is always available. Starting is still an explicit action.
                if (app.engine.value.running || app.access.state.value.pro) toggleBoost()
                refresh()
            }.apply { setTextColor(colors.primary.toArgb()) }
            row(boostButton!!)
        }
        val width = minOf(dp(if (expanded) 300 else 64), context.resources.displayMetrics.widthPixels - dp(16))
        val height = if (expanded) minOf(dp(500), context.resources.displayMetrics.heightPixels - dp(80)).coerceAtLeast(dp(140)) else dp(64)
        val p = WindowManager.LayoutParams(width, height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                (if (expanded) WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH else 0),
            android.graphics.PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = this@FloatingControls.x.coerceIn(0, maxOf(0, context.resources.displayMetrics.widthPixels - width))
            y = this@FloatingControls.y.coerceIn(dp(24), maxOf(dp(24), context.resources.displayMetrics.heightPixels - height - dp(24)))
        }
        try { windows.addView(panel, p); view = panel; params = p }
        catch (_: RuntimeException) { app.settings.update { it.copy(floatingControls = false) }; dismiss() }
    }
    private fun button(text: String, label: String, requiresPro: Boolean = true, repeatMillis: Long? = null, action: () -> Unit) = Button(context.localized()).apply {
        this.text = text; contentDescription = label; isAllCaps = false; textSize = 13f
        minHeight = dp(48); minimumHeight = dp(48); minimumWidth = dp(48)
        setTextColor(appearance.colors.onSurface.toArgb())
        background = shape(appearance.colors.surfaceVariant.toArgb())
        setPadding(dp(6), 0, dp(6), 0)
        setOnClickListener { app.access.refresh(); if (!requiresPro || app.access.state.value.pro) action() else dismiss() }
        if (repeatMillis != null) RepeatTouch.install(this, repeatMillis) {
            app.access.refresh()
            app.access.state.value.pro && app.settings.state.value.floatingControls &&
                !app.activityVisible && !app.adAudio.blocked.value && Settings.canDrawOverlays(context)
        }
    }
    private fun adjustGain(delta: Float) {
        app.settings.update { it.copy(gainDb = (it.gainDb + delta).coerceIn(0f, it.limitDb)) }
        HapticTick.play(context, app.settings.state.value.haptics); refresh()
    }
    private fun volume(direction: Int) {
        runCatching { context.getSystemService(android.media.AudioManager::class.java)
            .adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, direction, 0) }
    }
    @Suppress("ClickableViewAccessibility")
    private fun drag(handle: View) {
        var originX = 0f; var originY = 0f; var startX = 0; var startY = 0; var moved = false
        handle.setOnTouchListener { v, event ->
            val p = params ?: return@setOnTouchListener false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { originX = event.rawX; originY = event.rawY; startX = p.x; startY = p.y; moved = false; true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - originX; val dy = event.rawY - originY
                    if (abs(dx) + abs(dy) > ViewConfiguration.get(context).scaledTouchSlop) moved = true
                    if (moved) {
                        p.x = (startX + dx.toInt()).coerceIn(0, maxOf(0, context.resources.displayMetrics.widthPixels - p.width))
                        p.y = (startY + dy.toInt()).coerceIn(dp(24), maxOf(dp(24), context.resources.displayMetrics.heightPixels - (view?.height ?: dp(64)) - dp(24)))
                        x = p.x; y = p.y
                        runCatching { windows.updateViewLayout(view, p) }.onFailure { dismiss() }
                    }; true
                }
                MotionEvent.ACTION_UP -> { if (!moved) v.performClick(); true }
                MotionEvent.ACTION_CANCEL -> true
                else -> false
            }
        }
    }
    fun dismiss() {
        view?.let { runCatching { windows.removeViewImmediate(it) } }
        view = null; params = null; gain = null; status = null; bubble = null; compareButton = null; boostButton = null
        presetButtons.clear()
    }
}
