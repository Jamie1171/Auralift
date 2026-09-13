package com.jamiewardle.auralift.controls

import android.app.KeyguardManager
import android.content.Context
import androidx.compose.ui.graphics.toArgb
import android.graphics.drawable.GradientDrawable
import android.os.PowerManager
import android.provider.Settings
import android.view.*
import android.widget.*
import com.jamiewardle.auralift.AuraliftApplication
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.model.*
import com.jamiewardle.auralift.ui.appTheme
import kotlin.math.abs

/** Small, user-dismissable native overlay, owned by the existing audio service. */
class FloatingControls(private val context: Context, private val compare: () -> Unit, private val stop: () -> Unit) {
    private val app get() = context.applicationContext as AuraliftApplication
    private val windows = context.getSystemService(WindowManager::class.java)
    private var view: LinearLayout? = null
    private var gain: TextView? = null
    private var compareButton: Button? = null
    private var expanded = false
    private var signature = ""
    private var x = 12
    private var y = 140
    private var params: WindowManager.LayoutParams? = null
    private fun dp(value: Int) = (value * context.resources.displayMetrics.density).toInt()
    private val appearance get() = appTheme(app.settings.state.value.accent, app.settings.state.value.lightTheme)

    fun refresh() {
        val prefs = app.settings.state.value
        val permitted = prefs.floatingControls && app.access.state.value.pro && app.engine.value.running &&
            !app.activityVisible && !app.adAudio.blocked.value && Settings.canDrawOverlays(context) &&
            context.getSystemService(PowerManager::class.java).isInteractive &&
            !context.getSystemService(KeyguardManager::class.java).isKeyguardLocked
        if (!permitted) { dismiss(); return }
        val key = "${prefs.accent}/${prefs.lightTheme}/${context.localized().resources.configuration.locales}/${context.resources.configuration.orientation}"
        if (key != signature) { dismiss(); signature = key }
        if (view == null) show()
        gain?.text = context.word(R.string.floating_gain, prefs.gainDb)
        compareButton?.text = context.word(if (app.engine.value.comparingOriginal) R.string.restore_boost else R.string.compare_original)
    }
    private fun background(color: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(appearance.corner.value.toInt()).toFloat() }
    private fun show() {
        val accent = appearance.colors.primary.toArgb()
        val panel = LinearLayout(context.localized()).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(8), dp(8), dp(8), dp(8))
            background = background(appearance.colors.surface.toArgb()); elevation = dp(12).toFloat()
        }
        val header = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(context.localized()).apply {
            text = context.word(R.string.floating_gain, app.settings.state.value.gainDb)
            textSize = 16f; setTextColor(accent); gravity = Gravity.CENTER
            minHeight = dp(48); contentDescription = context.word(R.string.drag_floating)
            isClickable = true; isFocusable = true
            setOnClickListener { expanded = !expanded; dismiss(); show() }
        }
        gain = title
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(button("×", context.word(R.string.close_floating), requiresPro = false) {
            app.settings.update { it.copy(floatingControls = false) }; dismiss()
        }, LinearLayout.LayoutParams(dp(48), dp(48)))
        drag(title)
        panel.addView(header)
        if (expanded) {
            val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            val scroll = ScrollView(context).apply { addView(body) }
            panel.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
            fun row(vararg controls: Button) = LinearLayout(context).also { r ->
                controls.forEach { r.addView(it, LinearLayout.LayoutParams(0, -2, 1f)) }; body.addView(r)
            }
            row(button("−", context.word(R.string.decrease_boost)) { adjustGain(-0.5f) },
                button("+", context.word(R.string.increase_boost)) { adjustGain(0.5f) })
            row(button(context.word(R.string.volume_down), context.word(R.string.volume_down)) { volume(-1) },
                button(context.word(R.string.volume_up), context.word(R.string.volume_up)) { volume(1) })
            compareButton = button(context.word(R.string.compare_original), context.word(R.string.compare_original), action = compare)
            body.addView(compareButton)
            row(button("|◀", context.word(R.string.previous_track)) { MediaActions.send(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS) },
                button("▶ / Ⅱ", context.word(R.string.play_pause)) { MediaActions.send(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) },
                button("▶|", context.word(R.string.next_track)) { MediaActions.send(context, KeyEvent.KEYCODE_MEDIA_NEXT) })
            row(button(context.word(R.string.preset_balanced), context.word(R.string.preset_balanced)) { preset(SoundPreset.BALANCED) },
                button(context.word(R.string.preset_voice), context.word(R.string.preset_voice)) { preset(SoundPreset.VOICE) })
            row(button(context.word(R.string.preset_warm), context.word(R.string.preset_warm)) { preset(SoundPreset.WARM) },
                button(context.word(R.string.preset_detail), context.word(R.string.preset_detail)) { preset(SoundPreset.DETAIL) })
            body.addView(button(context.word(R.string.stop_boost), context.word(R.string.stop_boost), requiresPro = false, action = stop).apply { setTextColor(accent) })
        }
        val width = minOf(dp(if (expanded) 300 else 225), context.resources.displayMetrics.widthPixels - dp(16))
        val height = if (expanded) minOf(dp(440), context.resources.displayMetrics.heightPixels - dp(80)).coerceAtLeast(dp(140)) else WindowManager.LayoutParams.WRAP_CONTENT
        val p = WindowManager.LayoutParams(width, height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            android.graphics.PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = this@FloatingControls.x.coerceIn(0, maxOf(0, context.resources.displayMetrics.widthPixels - width))
            y = this@FloatingControls.y.coerceIn(dp(24), maxOf(dp(24), context.resources.displayMetrics.heightPixels - dp(if (expanded) 430 else 100)))
        }
        try { windows.addView(panel, p); view = panel; params = p }
        catch (_: RuntimeException) { app.settings.update { it.copy(floatingControls = false) }; dismiss() }
    }
    private fun button(text: String, label: String, requiresPro: Boolean = true, action: () -> Unit) = Button(context.localized()).apply {
        this.text = text; contentDescription = label; isAllCaps = false; textSize = 13f
        minHeight = dp(48); minimumHeight = dp(48); minimumWidth = dp(48)
        setTextColor(appearance.colors.onSurface.toArgb())
        backgroundTintList = android.content.res.ColorStateList.valueOf(appearance.colors.surfaceVariant.toArgb())
        setPadding(dp(6), 0, dp(6), 0)
        setOnClickListener { app.access.refresh(); if (!requiresPro || app.access.state.value.pro) action() else dismiss() }
    }
    private fun adjustGain(delta: Float) {
        app.settings.update { it.copy(gainDb = (it.gainDb + delta).coerceIn(0f, it.limitDb)) }
        HapticTick.play(context, app.settings.state.value.haptics); refresh()
    }
    private fun preset(preset: SoundPreset) { app.settings.update { it.copy(preset = preset) } }
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
                        p.y = (startY + dy.toInt()).coerceIn(dp(24), maxOf(dp(24), context.resources.displayMetrics.heightPixels - (view?.height ?: dp(100)) - dp(24)))
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
        view = null; params = null; gain = null; compareButton = null
    }
}
