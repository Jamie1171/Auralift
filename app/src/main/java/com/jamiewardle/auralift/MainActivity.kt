package com.jamiewardle.auralift

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.core.content.edit
import com.jamiewardle.auralift.audio.BoostService
import com.jamiewardle.auralift.ui.AuraliftScreen
import com.jamiewardle.auralift.controls.MediaActions
import com.jamiewardle.auralift.controls.word
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val app get() = application as AuraliftApplication
    private val notifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { startBoost() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            app.settings.state.map { it.lightTheme }.distinctUntilChanged().collect { light ->
                val style = if (light) SystemBarStyle.light(Color.TRANSPARENT, Color.rgb(13, 18, 20))
                    else SystemBarStyle.dark(Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
        }
        lifecycleScope.launch {
            app.terms.state.map { it?.version == app.terms.version }.distinctUntilChanged().collect { accepted ->
                if (accepted) { app.ads.onLaunch(this@MainActivity); app.purchases.refresh() }
            }
        }
        volumeControlStream = AudioManager.STREAM_MUSIC
        setContent { AuraliftScreen(app, ::toggleBoost, ::mediaKey) }
    }
    override fun onStart() {
        super.onStart(); app.activityVisible = true
        app.access.refresh(); if (app.terms.hasAcceptedCurrent()) app.purchases.refresh()
        BoostService.showFloating(this)
    }
    override fun onStop() {
        app.activityVisible = false
        if (!isChangingConfigurations && !app.settings.state.value.backgroundAudio && !app.adAudio.blocked.value) BoostService.stop(this)
        super.onStop()
    }
    override fun onDestroy() { app.ads.activityDestroyed(this); super.onDestroy() }
    private fun toggleBoost() {
        if (app.engine.value.running) { BoostService.stop(this); return }
        if (!app.terms.hasAcceptedCurrent()) return
        val ui = getSharedPreferences("device_ui", MODE_PRIVATE)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && !ui.getBoolean("notificationAsked", false)) {
            ui.edit { putBoolean("notificationAsked", true) }
            notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else startBoost()
    }
    private fun startBoost() {
        if (!app.terms.hasAcceptedCurrent()) return
        try { BoostService.start(this) }
        catch (e: RuntimeException) {
            app.engine.value = app.engine.value.copy(running = false, linked = false,
                message = word(R.string.start_failed), detail = word(R.string.keep_open_retry), diagnostics = e.toString())
        }
    }
    private fun mediaKey(key: Int) {
        MediaActions.send(this, key)
    }
}
