package com.jamiewardle.auralift.ui

import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.media.AudioManager
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jamiewardle.auralift.AuraliftApplication
import com.jamiewardle.auralift.BuildConfig
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.controls.HapticTick
import com.jamiewardle.auralift.audio.BoostService
import com.jamiewardle.auralift.model.*
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun AuraliftScreen(app: AuraliftApplication, toggle: () -> Unit, mediaKey: (Int) -> Unit) {
    val prefs by app.settings.state.collectAsStateWithLifecycle()
    val state by app.engine.collectAsStateWithLifecycle()
    val access by app.access.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var extra by rememberSaveable { mutableStateOf<String?>(null) }
    val screenScroll = remember(tab, extra) { ScrollState(0) }
    var showIntro by rememberSaveable { mutableStateOf(false) }
    BackHandler(extra != null) { extra = null }
    LaunchedEffect(Unit) { while (true) { app.access.refresh(); delay(1000) } }
    val accent = if (access.pro) prefs.accent else Accent.MINT
    val theme = remember(accent, prefs.lightTheme) { appTheme(accent, prefs.lightTheme) }
    CompositionLocalProvider(LocalAppTheme provides theme) {
    MaterialTheme(colorScheme = theme.colors, shapes = Shapes(
        small = RoundedCornerShape(theme.corner / 2), medium = RoundedCornerShape(theme.corner),
        large = RoundedCornerShape(theme.corner), extraLarge = RoundedCornerShape(theme.corner))) {
        Box(Modifier.fillMaxSize()) {
        ThemeBackdrop(Modifier.matchParentSize())
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.imePadding(),
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
                    listOf(context.getString(R.string.ui_listen) to Icons.AutoMirrored.Rounded.VolumeUp, context.getString(R.string.ui_sound) to Icons.Rounded.Tune, context.getString(R.string.ui_settings) to Icons.Rounded.Settings).forEachIndexed { i, item ->
                        NavigationBarItem(selected = tab == i, onClick = { tab = i; extra = null },
                            icon = { Icon(item.second, null) }, label = { Text(item.first) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer))
                    }
                }
            }
        ) { inset ->
            Box(Modifier.fillMaxSize().padding(inset), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 600.dp).fillMaxWidth().verticalScroll(screenScroll).padding(horizontal = 22.dp)) {
                Header { extra = "pro" }
                if (extra != null) {
                    TextButton(onClick = { extra = null }) { Text(stringResource(R.string.back)) }
                    ExtraScreen(extra!!, app) { if (it == "listen") { tab = 0; extra = null } else extra = it }
                } else when (tab) {
                    0 -> Listen(app, prefs, state, { change ->
                        val before = app.settings.state.value.gainDb
                        app.settings.update(change)
                        if (before != app.settings.state.value.gainDb) HapticTick.play(context, prefs.haptics)
                    }, {
                        if (!prefs.onboarded && !state.running) showIntro = true else toggle()
                    }, mediaKey) { extra = "pro" }
                    1 -> { Sound(prefs, state, app); Spacer(Modifier.height(16.dp)); NamedSounds(app) { extra = "pro" } }
                    2 -> SettingsHub(app) { extra = it }
                }
                Spacer(Modifier.height(24.dp))
            }
            }
        }
        }
        if (showIntro) AlertDialog(
            onDismissRequest = { showIntro = false },
            icon = { Icon(Icons.Rounded.GraphicEq, null) },
            title = { Text(context.getString(R.string.ui_a_little_lift_start_low)) },
            text = { Text(context.getString(R.string.ui_play_something_familiar_enable_auralift_then_increase_the_boos), modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = { app.settings.update { it.copy(onboarded = true) }; showIntro = false; toggle() }) { Text(context.getString(R.string.ui_enable_auralift)) } },
            dismissButton = { TextButton(onClick = { showIntro = false }) { Text(context.getString(R.string.ui_later)) } }
        )
    }
    }
}

@Composable private fun Header(pro: () -> Unit) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.GraphicEq, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
        Text("auralift", Modifier.padding(start = 10.dp).weight(1f), fontSize = 26.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp)
        TextButton(onClick = pro) { Text(stringResource(R.string.pro_button), fontSize = 12.sp) }
    }
}

@Composable private fun Listen(app: AuraliftApplication, p: Preferences, s: EngineState, update: ((Preferences) -> Preferences) -> Unit, toggle: () -> Unit, mediaKey: (Int) -> Unit, explorePro: () -> Unit) {
    val context = LocalContext.current
    Text(context.getString(R.string.ui_bring_sound_closer), fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.8).sp)
    Text(context.getString(R.string.ui_more_clarity_on_your_terms), Modifier.padding(top = 8.dp, bottom = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    val limit = p.limitDb
    Panel {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(context.getString(R.string.ui_volume_boost), fontSize = 11.sp, letterSpacing = 1.6.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Surface(color = if (s.linked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape) {
                Text(if (s.linked && s.comparingOriginal) context.getString(R.string.ui_original) else if (s.linked) context.getString(R.string.ui_connected) else if (s.running) context.getString(R.string.ui_waiting) else context.getString(R.string.ui_off), Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 10.sp, letterSpacing = 0.8.sp,
                    color = if (s.linked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        GainDial(p.gainDb, limit, s.running && s.linked && !s.comparingOriginal)
        Text(context.getString(R.string.selected_range, limit.toInt()), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SmallControl(Icons.Rounded.Remove, context.getString(R.string.decrease_boost), p.gainDb > 0f) { update { it.copy(gainDb = max(0f, it.gainDb - 0.5f)) } }
            Slider(value = p.gainDb.coerceIn(0f, limit), onValueChange = { value -> update { it.copy(gainDb = (value * 2).roundToInt() / 2f) } },
                valueRange = 0f..limit, steps = max(0, (limit * 2).toInt() - 1), modifier = Modifier.weight(1f).semantics { contentDescription = context.getString(R.string.ui_boost_in_decibels) })
            SmallControl(Icons.Rounded.Add, context.getString(R.string.increase_boost), p.gainDb < limit) { update { it.copy(gainDb = min(limit, it.gainDb + 0.5f)) } }
        }
        GainChoices(listOf(0f) + GainMath.ranges.filter { it <= limit }, p.gainDb) { gain -> update { it.copy(gainDb = gain) } }
        if (s.running) {
            Text(s.gainReadback, Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            OutlinedButton(enabled = s.linked, onClick = { context.startService(Intent(context, BoostService::class.java).setAction(BoostService.COMPARE)) },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).heightIn(min = 48.dp)) {
                Text(if (s.comparingOriginal) context.getString(R.string.restore_boost) else context.getString(R.string.compare_original))
            }
        }
        Button(onClick = toggle, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 58.dp),
            shape = RoundedCornerShape(LocalAppTheme.current.corner * .7f), colors = if (s.running) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface) else ButtonDefaults.buttonColors()) {
            Icon(Icons.Rounded.PowerSettingsNew, null, Modifier.size(21.dp)); Spacer(Modifier.width(10.dp))
            Text(if (s.running) context.getString(R.string.ui_turn_boost_off) else context.getString(R.string.ui_enable_boost), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
    }
    AdPassCard(app, onDetails = explorePro, modifier = Modifier.padding(top = 14.dp))
    Row(Modifier.padding(vertical = 18.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Icon(if (s.linked) Icons.Rounded.CheckCircle else Icons.Rounded.Info, null, Modifier.size(18.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(if (s.running) s.message else stringResource(R.string.ready_when_you_are), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(if (s.running) s.detail else stringResource(R.string.start_your_audio), Modifier.padding(top = 3.dp), fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (s.running && s.linked) Text(context.getString(R.string.ui_android_s_setting_is_not_a_loudness_measurement), Modifier.padding(top = 3.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    MediaVolume()
    if (p.spectrum) { Spacer(Modifier.height(14.dp)); SpectrumPanel() }
    Spacer(Modifier.height(14.dp))
    Panel {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(context.getString(R.string.ui_your_media), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(context.getString(R.string.ui_control_the_current_player), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SmallControl(Icons.Rounded.SkipPrevious, context.getString(R.string.previous_track)) { mediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS) }
            SmallControl(Icons.Rounded.PlayArrow, context.getString(R.string.play_pause)) { mediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }
            SmallControl(Icons.Rounded.SkipNext, context.getString(R.string.next_track)) { mediaKey(KeyEvent.KEYCODE_MEDIA_NEXT) }
        }
    }
    Spacer(Modifier.height(14.dp)); SleepTimer(s)
}

@Composable private fun GainChoices(values: List<Float>, selected: Float, choose: (Float) -> Unit) {
    val context = LocalContext.current
    values.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { gain ->
                FilterChip(selected = abs(selected - gain) < 0.1f, onClick = { choose(gain) },
                    label = { Text(if (gain == 0f) context.getString(R.string.ui_0_db) else "+${gain.toInt()} dB", Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp))
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable private fun GainDial(gain: Float, limit: Float, active: Boolean) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current
    Box(Modifier.fillMaxWidth().height(228.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(218.dp).clearAndSetSemantics {}) {
            drawThemeDial(theme, (gain / limit).coerceIn(0f, 1f), active)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(context.getString(R.string.gain_number, gain), fontSize = 51.sp, fontWeight = if (theme.accent == Accent.AMBER) FontWeight.Medium else FontWeight.Light, letterSpacing = (-2).sp)
            Text(context.getString(R.string.ui_decibels), fontSize = 10.sp, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(context.getString(R.string.signal_amplitude, GainMath.amplitudePercent(gain)),
            Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp), fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun MediaVolume() {
    val context = LocalContext.current
    val audio = remember { context.getSystemService(AudioManager::class.java) }
    val max = remember { audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
    var volume by remember { mutableIntStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    LaunchedEffect(Unit) { while (true) { volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC); delay(750) } }
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, Modifier.size(21.dp), tint = MaterialTheme.colorScheme.primary)
            Text(context.getString(R.string.ui_media_volume), Modifier.weight(1f).padding(start = 10.dp), fontWeight = FontWeight.Medium)
            Text("${(volume * 100f / max).roundToInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        Slider(value = volume.toFloat(), valueRange = 0f..max.toFloat(), steps = max - 1, enabled = !audio.isVolumeFixed,
            onValueChange = {
                try { audio.setStreamVolume(AudioManager.STREAM_MUSIC, it.roundToInt(), 0); volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC) }
                catch (_: RuntimeException) { Toast.makeText(context, context.getString(R.string.ui_use_your_device_s_volume_buttons), Toast.LENGTH_SHORT).show() }
            }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = context.getString(R.string.ui_android_media_volume) })
        Text(if (audio.isVolumeFixed) context.getString(R.string.ui_this_output_controls_its_own_volume) else context.getString(R.string.ui_your_normal_android_volume_before_extra_boost), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun Sound(p: Preferences, s: EngineState, app: AuraliftApplication) {
    val context = LocalContext.current
    PageHeading(context.getString(R.string.ui_find_your_sound), context.getString(R.string.ui_a_few_thoughtful_starting_points_all_yours_to_adjust))
    SoundPreset.entries.filter { it != SoundPreset.CUSTOM }.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            pair.forEach { preset ->
                Surface(onClick = { app.settings.update { it.copy(preset = preset) } }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(LocalAppTheme.current.corner), color = if (p.preset == preset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = if (p.preset == preset) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) else null) {
                    Column(Modifier.padding(18.dp).heightIn(min = 98.dp)) {
                        Icon(when (preset) { SoundPreset.VOICE -> Icons.Rounded.RecordVoiceOver; SoundPreset.WARM -> Icons.Rounded.Waves; SoundPreset.DETAIL -> Icons.Rounded.AutoAwesome; else -> Icons.Rounded.GraphicEq }, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(23.dp))
                        Text(presetTitle(preset), Modifier.padding(top = 12.dp), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(presetSubtitle(preset), Modifier.padding(top = 4.dp), fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(context.getString(R.string.ui_equalizer), fontSize = 20.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(presetTitle(p.preset), color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
        }
        Text(context.getString(R.string.ui_shape_the_details), Modifier.padding(top = 4.dp, bottom = 14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        EqCurve(p.curve)
        val labels = listOf(context.getString(R.string.ui_60_hz_low_bass), context.getString(R.string.ui_230_hz_warmth), context.getString(R.string.ui_910_hz_body), context.getString(R.string.ui_3_6_khz_presence), context.getString(R.string.ui_14_khz_air))
        labels.forEachIndexed { i, label ->
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp)
                Text(context.getString(R.string.eq_decibels, p.curve[i].toInt()), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Slider(value = p.curve[i], onValueChange = { value ->
                val curve = p.curve.toMutableList().also { it[i] = value.roundToInt().toFloat() }
                app.settings.update { it.copy(preset = SoundPreset.CUSTOM, customEq = curve) }
            }, valueRange = -6f..6f, steps = 11, modifier = Modifier.fillMaxWidth().semantics { contentDescription = label })
        }
        Text(context.getString(R.string.ui_eq_peaks_are_normalised_to_avoid_adding_hidden_gain_bands_adap), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp)
        if (s.running && !s.eqAvailable) Text(context.getString(R.string.ui_equalizer_unavailable_on_this_connection_your_settings_are_sav), Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        TextButton(onClick = { app.settings.update { it.copy(preset = SoundPreset.BALANCED, customEq = List(5) { 0f }) } }) { Text(context.getString(R.string.ui_reset_equalizer)) }
    }
    Spacer(Modifier.height(16.dp))
    ProfilePanel(p, app)
}

@Composable private fun EqCurve(curve: List<Float>) {
    val context = LocalContext.current
    val accent = MaterialTheme.colorScheme.primary; val grid = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    Canvas(Modifier.fillMaxWidth().height(85.dp).clearAndSetSemantics {}) {
        for (i in 0..4) drawLine(grid, Offset(0f, size.height * i / 4f), Offset(size.width, size.height * i / 4f), 1.dp.toPx())
        val path = Path()
        curve.forEachIndexed { i, value ->
            val x = size.width * i / 4; val y = size.height * (1f - (value + 6f) / 12f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(accent, 3.dp.toPx(), Offset(x, y))
        }
        drawPath(path, accent, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable private fun ProfilePanel(p: Preferences, app: AuraliftApplication) {
    val context = LocalContext.current
    Panel {
        Text(context.getString(R.string.ui_saved_setups), fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Text(context.getString(R.string.ui_choose_and_load_a_setup_when_you_change_outputs), Modifier.padding(top = 5.dp, bottom = 10.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutputProfile.entries.forEach { profile ->
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { app.settings.update { it.copy(profile = profile) } }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = p.profile == profile, onClick = { app.settings.update { it.copy(profile = profile) } })
                Text(stringResource(when (profile) { OutputProfile.SPEAKER -> R.string.output_speaker; OutputProfile.HEADPHONES -> R.string.output_wired; OutputProfile.BLUETOOTH -> R.string.output_bluetooth }), fontSize = 14.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { app.settings.saveProfile(); Toast.makeText(context, context.getString(R.string.ui_setup_saved), Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text(context.getString(R.string.ui_save_current)) }
            Button(onClick = { val loaded = app.settings.loadProfile(p.profile); Toast.makeText(context, if (loaded) context.getString(R.string.ui_setup_loaded) else context.getString(R.string.ui_no_setup_saved_yet), Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text(context.getString(R.string.ui_load_setup)) }
        }
    }
}

@Composable private fun SleepTimer(s: EngineState) {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(s.timerEndsAt) { while (s.timerEndsAt > 0L) { now = SystemClock.elapsedRealtime(); delay(1000) } }
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Bedtime, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(context.getString(R.string.ui_sleep_timer), Modifier.weight(1f).padding(start = 10.dp), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(if (s.timerEndsAt > 0L) context.getString(R.string.minutes_count, ceil(max(0L, s.timerEndsAt - now) / 60000.0).toInt()) else context.getString(R.string.off), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0, 15, 30, 60).forEach { minutes ->
                TextButton(enabled = s.running, onClick = { context.startService(Intent(context, BoostService::class.java).setAction(BoostService.TIMER).putExtra("minutes", minutes)) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 2.dp)) { Text(if (minutes == 0) context.getString(R.string.off) else context.getString(R.string.minutes_count, minutes), fontSize = 13.sp) }
            }
        }
        Text(context.getString(R.string.ui_turns_off_boost_your_media_keeps_playing_android_may_delay_tim), fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable internal fun AudioSettings(p: Preferences, s: EngineState, app: AuraliftApplication) {
    val context = LocalContext.current
    var diagnostics by remember { mutableStateOf(false) }
    PageHeading(context.getString(R.string.ui_just_the_essentials), context.getString(R.string.ui_keep_your_listening_experience_in_your_hands))
    Panel {
        Text(context.getString(R.string.ui_boost_range), fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Text(stringResource(R.string.boost_range_hint, app.settings.maximumGain.toInt()), Modifier.padding(top = 8.dp, bottom = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp)
        GainChoices(GainMath.ranges.filter { it <= app.settings.maximumGain }, p.limitDb, app.settings::setLimit)
        Text(stringResource(R.string.digital_gain_hint), fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.height(14.dp))
    Panel {
        Text(context.getString(R.string.ui_audio_connection), fontSize = 20.sp, fontWeight = FontWeight.Medium)
        EffectMode.entries.forEach { mode ->
            Row(Modifier.fillMaxWidth().clickable { app.settings.update { it.copy(mode = mode) } }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = p.mode == mode, onClick = { app.settings.update { it.copy(mode = mode) } })
                Column(Modifier.weight(1f)) {
                    Text(if (mode == EffectMode.SYSTEM) context.getString(R.string.ui_system_mix) else context.getString(R.string.ui_player_sessions), fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    Text(if (mode == EffectMode.SYSTEM) context.getString(R.string.ui_try_first_support_varies_by_device_and_player) else context.getString(R.string.ui_for_players_that_share_an_audio_session_restart_playback_after), fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Text(if (!s.running) context.getString(R.string.ui_enable_boost_to_check_available_audio_effects) else if (s.nativeCompression) context.getString(R.string.ui_android_loudness_compression_is_available_it_does_not_guarante) else if (s.linked) context.getString(R.string.ui_equalizer_gain_fallback_no_loudness_compression_is_available_o) else context.getString(R.string.ui_no_controllable_gain_effect_connected), fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(enabled = s.running, onClick = { context.startService(Intent(context, BoostService::class.java).setAction(BoostService.RECONNECT)) }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 48.dp)) { Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(context.getString(R.string.ui_reconnect_audio_effects)) }
        TextButton(onClick = { diagnostics = true }) { Text(context.getString(R.string.ui_connection_details)) }
    }
    Spacer(Modifier.height(14.dp))
    Panel {
        SettingSwitch(context.getString(R.string.ui_reset_boost_on_output_changes), context.getString(R.string.ui_return_extra_gain_to_zero_when_audio_devices_connect_or_discon), p.resetOnRouteChange) { value -> app.settings.update { it.copy(resetOnRouteChange = value) } }
        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        SettingSwitch(context.getString(R.string.light_appearance), context.getString(R.string.light_hint), p.lightTheme) { value -> app.settings.update { it.copy(lightTheme = value) } }
    }
    Spacer(Modifier.height(14.dp))
    Panel {
        Text(context.getString(R.string.ui_listening_accessibility), fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Text(context.getString(R.string.ui_android_provides_mono_audio_left_right_balance_and_sound_ampli), Modifier.padding(top = 8.dp), fontSize = 13.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = { runCatching { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }) { Text(context.getString(R.string.ui_open_accessibility_settings)) }
    }
    Text("Auralift ${BuildConfig.VERSION_NAME}", Modifier.padding(top = 26.dp), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    Text(stringResource(R.string.audio_footer), Modifier.padding(top = 8.dp), fontSize = 12.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (diagnostics) AlertDialog(onDismissRequest = { diagnostics = false }, title = { Text(context.getString(R.string.ui_audio_connection)) }, text = { Text(s.diagnostics, fontSize = 12.sp, modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = { diagnostics = false }) { Text(context.getString(R.string.ui_done)) } },
        dismissButton = { TextButton(onClick = {
            context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(context.getString(R.string.ui_auralift_audio_connection), s.diagnostics))
            Toast.makeText(context, context.getString(R.string.ui_connection_details_copied), Toast.LENGTH_SHORT).show()
        }) { Text(context.getString(R.string.ui_copy_details)) } })
}

@Composable internal fun SettingSwitch(title: String, description: String, value: Boolean, change: (Boolean) -> Unit) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(description, Modifier.padding(top = 5.dp), fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = value, onCheckedChange = change, modifier = Modifier.semantics { contentDescription = title })
    }
}
@Composable internal fun PageHeading(title: String, subtitle: String) {
    val context = LocalContext.current
    Text(title, fontSize = 29.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.8).sp)
    Text(subtitle, Modifier.padding(top = 8.dp, bottom = 24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
}
@Composable internal fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(LocalAppTheme.current.corner),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .16f))) {
        Column(Modifier.padding(20.dp), content = content)
    }
}
@Composable private fun SmallControl(icon: ImageVector, label: String, enabled: Boolean = true, click: () -> Unit) {
    val context = LocalContext.current
    IconButton(onClick = click, enabled = enabled, modifier = Modifier.size(48.dp)) { Icon(icon, label, Modifier.size(23.dp)) }
}
