package com.jamiewardle.auralift.controls

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.RemoteViews
import com.jamiewardle.auralift.*
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.audio.BoostService

class BoostTile : TileService() {
    override fun onStartListening() {
        val snapshot = (application as AuraliftApplication).engine.value
        qsTile?.apply {
            state = if (snapshot.running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = "Auralift"
            if (Build.VERSION.SDK_INT >= 29) subtitle = if (snapshot.running) word(R.string.tap_to_stop) else word(R.string.open_full_controls)
            updateTile()
        }
    }
    // Android 8–13 only have the Intent overload. Android 14+ always use PendingIntent.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        if ((application as AuraliftApplication).engine.value.running) BoostService.stop(this)
        else {
            val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= 34) startActivityAndCollapse(PendingIntent.getActivity(this, 5, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            else @Suppress("DEPRECATION") startActivityAndCollapse(intent)
        }
        onStartListening()
    }
    companion object {
        fun refresh(context: Context) { requestListeningState(context, ComponentName(context, BoostTile::class.java)) }
    }
}

class BoostWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) = refresh(context)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.jamiewardle.auralift.WIDGET_STOP") BoostService.stop(context)
        super.onReceive(context, intent)
    }
    companion object {
        fun refresh(context: Context) {
            val state = (context.applicationContext as AuraliftApplication).engine.value
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, BoostWidget::class.java))
            val open = PendingIntent.getActivity(context, 6, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val stop = PendingIntent.getBroadcast(context, 7, Intent(context, BoostWidget::class.java).setAction("com.jamiewardle.auralift.WIDGET_STOP"), PendingIntent.FLAG_IMMUTABLE)
            ids.forEach { id ->
                manager.updateAppWidget(id, RemoteViews(context.packageName, R.layout.boost_widget).apply {
                    setTextViewText(R.id.widget_title, when {
                        state.running && state.comparingOriginal -> "Auralift\nOriginal"
                        state.running && state.linked && state.reportedGainDb != null -> "Auralift\n${"%+.1f".format(state.reportedGainDb)} dB"
                        else -> "Auralift"
                    })
                    setTextViewText(R.id.widget_action, if (state.running) context.word(R.string.stop_boost) else context.word(R.string.open_controls))
                    setOnClickPendingIntent(R.id.widget_title, open)
                    setOnClickPendingIntent(R.id.widget_action, if (state.running) stop else open)
                })
            }
        }
    }
}
