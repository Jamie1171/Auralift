package com.jamiewardle.auralift.ui

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jamiewardle.auralift.*
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.controls.*
import com.jamiewardle.auralift.model.*
import com.jamiewardle.auralift.profiles.ProfileCodec
import com.jamiewardle.auralift.access.ProProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil

@Composable internal fun SettingsHub(app: AuraliftApplication, navigate: (String) -> Unit) {
    PageHeading(stringResource(R.string.settings_heading), stringResource(R.string.settings_subtitle))
    Surface(onClick = { navigate("floating") }, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LocalAppTheme.current.corner), color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .45f))) {
        Column(Modifier.padding(22.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.PictureInPictureAlt, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.weight(1f))
                ProBadge()
            }
            Text(stringResource(R.string.floating_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 14.dp))
            Text(stringResource(R.string.floating_hint), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.floating_open), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    Panel {
        MenuRow(R.string.personalise, R.string.personalise_hint, Icons.Rounded.Palette) { navigate("appearance") }
        MenuRow(R.string.audio_controls, R.string.audio_controls_hint, Icons.Rounded.Tune) { navigate("audio") }
        MenuRow(R.string.running_controls, R.string.running_hint, Icons.Rounded.Notifications) { navigate("running") }
        MenuRow(R.string.language, R.string.language_hint, Icons.Rounded.Language) { navigate("language") }
    }
    Spacer(Modifier.height(16.dp))
    Panel {
        MenuRow(R.string.auralift_pro, R.string.pro_hint, Icons.Rounded.AutoAwesome) { navigate("pro") }
        MenuRow(R.string.help_feedback, R.string.help_hint, Icons.Rounded.HelpOutline) { navigate("help") }
        MenuRow(R.string.privacy_policy, R.string.privacy_hint, Icons.Rounded.PrivacyTip) { navigate("privacy") }
        MenuRow(R.string.terms_of_use, R.string.terms_hint, Icons.Rounded.Description) { navigate("terms") }
    }
    val ads by app.ads.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    if (ads.privacyRequired) TextButton(onClick = { context.activity()?.let { app.ads.privacyOptions(it) } }, enabled = !ads.busy) { Text(stringResource(R.string.ad_privacy_choices)) }
    Spacer(Modifier.height(16.dp))
    Text("Auralift ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelLarge)
    Text(stringResource(R.string.local_audio), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable private fun MenuRow(title: Int, description: Int, icon: ImageVector, click: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = click).padding(vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(23.dp), tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable internal fun ExtraScreen(page: String, app: AuraliftApplication, navigate: (String) -> Unit) {
    val p by app.settings.state.collectAsStateWithLifecycle()
    val s by app.engine.collectAsStateWithLifecycle()
    when (page) {
        "appearance" -> AppearanceScreen(app) { navigate("pro") }
        "audio" -> {
            AudioSettings(p, s, app)
            Spacer(Modifier.height(16.dp))
            ListeningOptions(app) { navigate("pro") }
        }
        "running" -> RunningScreen(app)
        "floating" -> FloatingPlayerScreen(app, { navigate("pro") }, { navigate("listen") })
        "language" -> LanguageScreen()
        "pro" -> ProScreen(app)
        "help" -> HelpScreen { navigate(it) }
        "feedback" -> FeedbackScreen(app)
        "privacy", "terms" -> PolicyScreen(page)
    }
}

@Composable private fun AppearanceScreen(app: AuraliftApplication, pro: () -> Unit) {
    val p by app.settings.state.collectAsStateWithLifecycle()
    val access by app.access.state.collectAsStateWithLifecycle()
    PageHeading(stringResource(R.string.personalise), stringResource(R.string.appearance_subtitle))
    Panel { SettingSwitch(stringResource(R.string.light_appearance), stringResource(R.string.light_hint), p.lightTheme) { value -> app.settings.update { it.copy(lightTheme = value) } } }
    Spacer(Modifier.height(16.dp))
    Accent.entries.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { accent ->
                val selected = (if (access.pro) p.accent else Accent.MINT) == accent
                Surface(onClick = {
                    if (accent != Accent.MINT && !access.pro) pro()
                    else app.settings.update { it.copy(accent = accent) }
                }, shape = RoundedCornerShape(24.dp), modifier = Modifier.weight(1f).padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))) {
                    Column {
                        ThemePreview(accent, p.lightTheme)
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(when (accent) { Accent.MINT -> R.string.theme_mint; Accent.OCEAN -> R.string.theme_ocean; Accent.AMBER -> R.string.theme_amber; Accent.ORCHID -> R.string.theme_orchid }), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                                if (selected) Icon(Icons.Rounded.CheckCircle, stringResource(R.string.theme_selected), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Text(stringResource(when (accent) { Accent.MINT -> R.string.theme_mint_hint; Accent.OCEAN -> R.string.theme_ocean_hint; Accent.AMBER -> R.string.theme_amber_hint; Accent.ORCHID -> R.string.theme_orchid_hint }), modifier = Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodySmall)
                            Text(stringResource(if (accent == Accent.MINT) R.string.included else R.string.pro_button), modifier = Modifier.padding(top = 10.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ListeningOptions(app: AuraliftApplication, pro: () -> Unit) {
    val p by app.settings.state.collectAsStateWithLifecycle()
    val access by app.access.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed -> app.settings.update { it.copy(spectrum = allowed) } }
    var spectrumDisclosure by remember { mutableStateOf(false) }
    Panel {
        Text(stringResource(R.string.listening_preferences), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(14.dp))
        SettingSwitch(stringResource(R.string.booster_memory), stringResource(R.string.booster_memory_hint), p.rememberBoost) { value -> app.settings.update { it.copy(rememberBoost = value) } }
        Spacer(Modifier.height(18.dp))
        SettingSwitch(stringResource(R.string.sleep_fade), stringResource(R.string.sleep_fade_hint), p.sleepFade && access.pro) { value ->
            if (value && !access.pro) pro() else app.settings.update { it.copy(sleepFade = value) }
        }
        Spacer(Modifier.height(18.dp))
        SettingSwitch(stringResource(R.string.music_spectrum), stringResource(R.string.spectrum_hint), p.spectrum) { enabled ->
            if (!enabled) app.settings.update { it.copy(spectrum = false) }
            else if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) app.settings.update { it.copy(spectrum = true) }
            else spectrumDisclosure = true
        }
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.vibration), style = MaterialTheme.typography.titleMedium)
        Haptics.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { level ->
                    FilterChip(selected = p.haptics == level, onClick = {
                        app.settings.update { it.copy(haptics = level) }; HapticTick.play(context, level)
                    }, label = { Text(stringResource(when (level) { Haptics.OFF -> R.string.off; Haptics.LIGHT -> R.string.haptic_light; Haptics.MEDIUM -> R.string.haptic_medium; Haptics.STRONG -> R.string.haptic_strong })) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp))
                }
            }
        }
    }
    if (spectrumDisclosure) AlertDialog(onDismissRequest = { spectrumDisclosure = false },
        title = { Text(stringResource(R.string.music_spectrum)) }, text = { Text(stringResource(R.string.spectrum_disclosure)) },
        confirmButton = { TextButton(onClick = { spectrumDisclosure = false; permission.launch(Manifest.permission.RECORD_AUDIO) }) { Text(stringResource(R.string.continue_label)) } },
        dismissButton = { TextButton(onClick = { spectrumDisclosure = false }) { Text(stringResource(R.string.cancel)) } })
}

@Composable private fun RunningScreen(app: AuraliftApplication) {
    val context = LocalContext.current
    val p by app.settings.state.collectAsStateWithLifecycle()
    var refresh by remember { mutableIntStateOf(0) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh++ }
        lifecycle.addObserver(observer); onDispose { lifecycle.removeObserver(observer) }
    }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh++ }
    val notificationsAllowed = remember(refresh) {
        (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
            NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            context.getSystemService(NotificationManager::class.java).getNotificationChannel("audio_controls")?.importance != NotificationManager.IMPORTANCE_NONE
    }
    val unrestricted = remember(refresh) { context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName) }
    PageHeading(stringResource(R.string.running_controls), stringResource(R.string.running_subtitle))
    Panel {
        Text(stringResource(R.string.notifications), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(if (notificationsAllowed) R.string.notifications_on else R.string.notifications_off), Modifier.padding(vertical = 10.dp))
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Button(onClick = { notifications.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text(stringResource(R.string.allow_notifications)) }
        }
        OutlinedButton(onClick = { context.openSettings(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }) { Text(stringResource(R.string.notification_settings)) }
    }
    Spacer(Modifier.height(16.dp))
    Panel {
        SettingSwitch(stringResource(R.string.background_audio), stringResource(R.string.background_hint), p.backgroundAudio) { value -> app.settings.update { it.copy(backgroundAudio = value) } }
        Spacer(Modifier.height(14.dp))
        Text(stringResource(if (unrestricted) R.string.battery_unrestricted else R.string.battery_managed), style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { context.openSettings(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }) { Text(stringResource(R.string.app_battery_settings)) }
        Text(stringResource(R.string.battery_guidance), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = { context.openSettings(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }) { Text(stringResource(R.string.battery_optimization_list)) }
        Spacer(Modifier.height(16.dp))
        SettingSwitch(stringResource(R.string.restart_reminder), stringResource(R.string.restart_reminder_hint), p.restartReminder) { value -> app.settings.update { it.copy(restartReminder = value) } }
        if (p.restartReminder && !notificationsAllowed) Text(stringResource(R.string.restart_needs_notifications), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun LanguageScreen() {
    val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    PageHeading(stringResource(R.string.language), stringResource(R.string.language_subtitle))
    Panel {
        listOf("" to stringResource(R.string.follow_system), "en" to "English", "es" to "Español", "fr" to "Français").forEach { (tag, label) ->
            Row(Modifier.fillMaxWidth().clickable { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag)) }.heightIn(min = 58.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = current == tag, onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag)) })
                Text(label)
            }
        }
    }
}

@Composable private fun ProScreen(app: AuraliftApplication) {
    val access by app.access.state.collectAsStateWithLifecycle()
    val purchase by app.purchases.state.collectAsStateWithLifecycle()
    val ads by app.ads.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    PageHeading(stringResource(R.string.pro_heading), stringResource(R.string.pro_subtitle))
    Panel {
        Text(stringResource(when { access.owner -> R.string.owner_unlocked; access.passRemainingMs > 0 -> R.string.preview_active; access.pro -> R.string.pro_unlocked; else -> R.string.free_plan }), style = MaterialTheme.typography.titleLarge)
        if (access.passRemainingMs > 0) Text(stringResource(R.string.preview_minutes, ceil(access.passRemainingMs / 60_000.0).toInt()), Modifier.padding(top = 8.dp))
        Text(stringResource(R.string.free_features), Modifier.padding(top = 14.dp), style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(Modifier.padding(vertical = 18.dp))
        Text(stringResource(R.string.pro_features), style = MaterialTheme.typography.bodyMedium)
    }
    if (!access.owner && !access.permanent) {
        Spacer(Modifier.height(16.dp))
        Panel {
            Text(stringResource(R.string.ad_pass), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.ad_pass_intro), Modifier.padding(top = 10.dp))
            Text(stringResource(R.string.ad_pass_rules), Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!access.pro) Button(enabled = ads.available && !ads.busy, onClick = {
                context.activity()?.let { if (ads.ready) app.ads.show(it) else app.ads.prepare(it) }
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text(stringResource(if (ads.busy) R.string.ad_loading else if (ads.ready) R.string.watch_ad else R.string.prepare_ad))
            }
            if (ads.message != 0) Text(stringResource(ads.message), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 10.dp))
        }
        Spacer(Modifier.height(16.dp))
        Panel {
            Text(stringResource(R.string.lifetime_pro), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.purchase_options_hint), Modifier.padding(vertical = 12.dp))
            ProProduct.entries.forEach { product ->
                val price = purchase.prices[product]
                val title = stringResource(if (product == ProProduct.PRO) R.string.lifetime_pro else R.string.supporter_pro)
                Text(title, Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium)
                if (product == ProProduct.SUPPORTER) Text(stringResource(R.string.supporter_hint), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                Button(enabled = !Distribution.owner && price != null && purchase.ready && !purchase.busy && !ads.busy,
                    onClick = { context.activity()?.let { app.purchases.buy(it, product) } },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp).heightIn(min = 52.dp)) {
                    Text(if (price != null) stringResource(R.string.buy_once, price) else stringResource(R.string.purchases_at_launch))
                }
            }
        }
    }
    if (!Distribution.owner) {
        TextButton(onClick = { app.purchases.refresh() }, enabled = !purchase.busy && !ads.busy) { Text(stringResource(R.string.restore_purchase)) }
        if (purchase.message != 0) Text(stringResource(purchase.message), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (ads.privacyRequired) TextButton(onClick = { context.activity()?.let { app.ads.privacyOptions(it) } }, enabled = !ads.busy) { Text(stringResource(R.string.ad_privacy_choices)) }
    Spacer(Modifier.height(16.dp))
    OwnerTools(app)
}

private fun Context.activity(): Activity? = when (this) { is Activity -> this; is ContextWrapper -> baseContext.activity(); else -> null }

@Composable internal fun NamedSounds(app: AuraliftApplication, pro: () -> Unit) {
    val context = LocalContext.current
    val sounds by app.profiles.state.collectAsStateWithLifecycle()
    val access by app.access.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var pendingExport by remember { mutableStateOf<String?>(null) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val content = pendingExport; pendingExport = null
        if (uri != null && content != null) scope.launch {
            busy = true
            error = runCatching { withContext(Dispatchers.IO) { context.contentResolver.openOutputStream(uri, "wt")!!.use { it.write(content.toByteArray()) } } }.isFailure
            busy = false
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            error = runCatching {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)!!.use { input ->
                        val buffer = ByteArray(ProfileCodec.MAX_BYTES + 1)
                        var size = 0
                        while (size < buffer.size) {
                            val count = input.read(buffer, size, buffer.size - size)
                            if (count < 0) break
                            size += count
                        }
                        buffer.copyOf(size)
                    }
                }
                require(bytes.size <= ProfileCodec.MAX_BYTES)
                app.profiles.import(bytes.toString(Charsets.UTF_8))
            }.isFailure
            busy = false
        }
    }
    Panel {
        Text(stringResource(R.string.named_sounds), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.named_sounds_hint), Modifier.padding(top = 6.dp, bottom = 14.dp), style = MaterialTheme.typography.bodySmall)
        if (access.pro) {
            OutlinedTextField(value = name, onValueChange = { name = it.take(48); error = false }, label = { Text(stringResource(R.string.profile_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(enabled = name.isNotBlank() && sounds.size < ProfileCodec.MAX_PROFILES && !busy, onClick = {
                error = runCatching { app.profiles.add(name); name = "" }.isFailure
            }) { Text(stringResource(R.string.save_sound)) }
        } else OutlinedButton(onClick = pro) { Text(stringResource(R.string.explore_pro)) }
        sounds.forEach { sound ->
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(sound.name, style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.saved_gain, sound.gain), style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { if (access.pro) error = runCatching { app.profiles.load(sound.id) }.isFailure else pro() }) { Text(stringResource(R.string.load)) }
                IconButton(onClick = { app.profiles.remove(sound.id) }) { Icon(Icons.Rounded.DeleteOutline, stringResource(R.string.delete_sound, sound.name)) }
            }
        }
        if (access.pro) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(enabled = sounds.isNotEmpty() && !busy, onClick = {
                    runCatching { pendingExport = app.profiles.export(); export.launch("Auralift-sounds.json") }.onFailure { error = true }
                }) { Text(stringResource(R.string.export_sounds)) }
                TextButton(enabled = !busy, onClick = { import.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }) { Text(stringResource(R.string.import_sounds)) }
            }
        }
        if (error) Text(stringResource(R.string.profile_error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun HelpScreen(navigate: (String) -> Unit) {
    val context = LocalContext.current
    PageHeading(stringResource(R.string.help_feedback), stringResource(R.string.help_subtitle))
    Panel {
        listOf(R.string.faq_quiet_q to R.string.faq_quiet_a, R.string.faq_background_q to R.string.faq_background_a,
            R.string.faq_distortion_q to R.string.faq_distortion_a, R.string.faq_compat_q to R.string.faq_compat_a,
            R.string.faq_preview_q to R.string.faq_preview_a, R.string.faq_spectrum_q to R.string.faq_spectrum_a).forEach { (question, answer) ->
            var expanded by rememberSaveable { mutableStateOf(false) }
            Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(question), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null)
            }
            if (expanded) Text(stringResource(answer), Modifier.padding(bottom = 16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        }
        TextButton(onClick = { navigate("feedback") }) { Text(stringResource(R.string.share_feedback)) }
        TextButton(onClick = {
            if (!Distribution.storeLive) Toast.makeText(context, context.word(R.string.rating_at_launch), Toast.LENGTH_LONG).show()
            else if (!context.openSettings(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.jamiewardle.auralift"))))
                context.openSettings(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.jamiewardle.auralift")))
        }) { Text(stringResource(R.string.rate_us)) }
        TextButton(onClick = { context.openSettings(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC)) }) { Text(stringResource(R.string.open_music_player)) }
    }
}

@Composable private fun FeedbackScreen(app: AuraliftApplication) {
    val context = LocalContext.current
    var category by rememberSaveable { mutableIntStateOf(R.string.feedback_not_working) }
    var message by rememberSaveable { mutableStateOf("") }
    var includeDetails by rememberSaveable { mutableStateOf(false) }
    var attachment by rememberSaveable { mutableStateOf<String?>(null) }
    val image = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { attachment = it?.toString() }
    PageHeading(stringResource(R.string.share_feedback), stringResource(R.string.feedback_subtitle))
    Panel {
        listOf(R.string.feedback_not_working, R.string.feedback_noise, R.string.feedback_stopping, R.string.feedback_idea).chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { item ->
                FilterChip(selected = category == item, onClick = { category = item }, label = { Text(stringResource(item)) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp))
            } }
        }
        OutlinedTextField(value = message, onValueChange = { message = it.take(4000) }, label = { Text(stringResource(R.string.feedback_message)) }, minLines = 5, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
        SettingSwitch(stringResource(R.string.include_diagnostics), stringResource(R.string.diagnostics_hint), includeDetails) { includeDetails = it }
        TextButton(onClick = { if (attachment == null) image.launch("image/*") else attachment = null }) { Text(stringResource(if (attachment == null) R.string.attach_screenshot else R.string.remove_screenshot)) }
        Button(enabled = message.trim().length >= 6, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), onClick = {
            val text = "Auralift ${BuildConfig.VERSION_NAME}\n${context.getString(category)}\n\n${message.trim()}" +
                if (includeDetails) "\n\n${app.engine.value.diagnostics}" else ""
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, "Auralift feedback"); putExtra(Intent.EXTRA_TEXT, text)
                attachment?.let { value ->
                    val uri = Uri.parse(value); type = context.contentResolver.getType(uri) ?: "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri); clipData = ClipData.newUri(context.contentResolver, "Screenshot", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            context.openSettings(Intent.createChooser(intent, context.word(R.string.share_feedback)))
        }) { Text(stringResource(R.string.choose_where_to_send)) }
        Text(stringResource(R.string.feedback_privacy), Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun PolicyScreen(page: String) {
    val context = LocalContext.current
    val content = remember(page) { context.assets.open("legal/$page.txt").bufferedReader().use { it.readText() } }
    PageHeading(stringResource(if (page == "privacy") R.string.privacy_policy else R.string.terms_of_use), stringResource(R.string.policy_language))
    Panel { Text(content, style = MaterialTheme.typography.bodyMedium) }
}
