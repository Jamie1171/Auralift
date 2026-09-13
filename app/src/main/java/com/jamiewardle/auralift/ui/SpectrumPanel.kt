package com.jamiewardle.auralift.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.AuraliftApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import kotlin.math.*

@Composable
internal fun SpectrumPanel() {
    val context = LocalContext.current
    val blocked by (context.applicationContext as AuraliftApplication).adAudio.blocked.collectAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var visible by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
    var values by remember { mutableStateOf(List(24) { 0f }) }
    var status by remember { mutableIntStateOf(R.string.spectrum_waiting) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ -> visible = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(visible, blocked) {
        if (!visible || blocked || context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            values = List(24) { 0f }; status = R.string.spectrum_permission; return@LaunchedEffect
        }
        var visualizer: Visualizer? = null
        try {
            visualizer = Visualizer(0)
            val fx = visualizer
            fx.captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(1024)
            check(fx.setEnabled(true) == Visualizer.SUCCESS)
            val data = ByteArray(fx.captureSize)
            while (true) {
                check(fx.getFft(data) == Visualizer.SUCCESS)
                val rate = fx.samplingRate / 1000f
                values = List(24) { bar ->
                    val low = 40f * (500f).pow(bar / 24f)
                    val high = 40f * (500f).pow((bar + 1) / 24f)
                    val start = (low * data.size / rate).toInt().coerceIn(1, data.size / 2 - 1)
                    val end = (high * data.size / rate).toInt().coerceIn(start, data.size / 2 - 1)
                    val peak = (start..end).maxOf { bin -> hypot(data[bin * 2].toFloat(), data[bin * 2 + 1].toFloat()) }
                    (ln(1f + peak) / ln(182f)).coerceIn(0f, 1f)
                }
                status = R.string.spectrum_live
                delay(100)
            }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: RuntimeException) { values = List(24) { 0f }; status = R.string.spectrum_unavailable }
        finally { runCatching { visualizer?.release() } }
    }
    val color = MaterialTheme.colorScheme.primary
    Panel {
        Text(stringResource(R.string.music_spectrum), style = MaterialTheme.typography.titleMedium)
        Canvas(Modifier.fillMaxWidth().height(84.dp).padding(vertical = 8.dp).clearAndSetSemantics {}) {
            val step = size.width / values.size
            values.forEachIndexed { i, value ->
                val height = value * size.height
                drawRoundRect(color, Offset(i * step, size.height - height), Size(step * 0.64f, maxOf(1f, height)))
            }
        }
        Text(stringResource(status), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
