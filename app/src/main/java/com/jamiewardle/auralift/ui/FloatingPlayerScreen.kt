package com.jamiewardle.auralift.ui

import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jamiewardle.auralift.AuraliftApplication
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.controls.OverlayPermission

@Composable internal fun ProBadge() {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
        Text(stringResource(R.string.pro_button), Modifier.padding(horizontal = 12.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable internal fun FloatingPlayerScreen(app: AuraliftApplication, pro: () -> Unit, listen: () -> Unit) {
    val context = LocalContext.current
    val p by app.settings.state.collectAsStateWithLifecycle()
    val access by app.access.state.collectAsStateWithLifecycle()
    val engine by app.engine.collectAsStateWithLifecycle()
    var refresh by remember { mutableIntStateOf(0) }
    var disclosure by rememberSaveable { mutableStateOf(false) }
    var enableRequested by rememberSaveable { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh++ }
        lifecycle.addObserver(observer); onDispose { lifecycle.removeObserver(observer) }
    }
    val allowed = remember(refresh) { Settings.canDrawOverlays(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        app.access.refresh()
        val granted = Settings.canDrawOverlays(context)
        if (!granted || (enableRequested && app.access.state.value.pro)) {
            app.settings.update { it.copy(floatingControls = granted && app.access.state.value.pro) }
        }
        enableRequested = false; refresh++
    }
    ProBadge()
    Spacer(Modifier.height(14.dp))
    PageHeading(stringResource(R.string.floating_title), stringResource(R.string.floating_hint))
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.PictureInPictureAlt, null, tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.floating_everywhere), Modifier.padding(start = 12.dp), style = MaterialTheme.typography.titleMedium)
        }
        Text(stringResource(R.string.floating_instructions), Modifier.padding(top = 14.dp), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(18.dp))
        SettingSwitch(stringResource(R.string.floating_enable), stringResource(if (access.pro) R.string.floating_unlocked else R.string.floating_locked),
            p.floatingControls && access.pro && allowed) { enabled ->
            app.access.refresh()
            when {
                !enabled -> app.settings.update { it.copy(floatingControls = false) }
                !app.access.state.value.pro -> pro()
                Settings.canDrawOverlays(context) -> app.settings.update { it.copy(floatingControls = true) }
                else -> { enableRequested = true; disclosure = true }
            }
        }
        if (!access.pro) Button(onClick = pro, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text(stringResource(R.string.explore_pro)) }
    }
    Spacer(Modifier.height(16.dp))
    Panel {
        Text(stringResource(R.string.floating_permission_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.floating_permission_hint), Modifier.padding(top = 10.dp), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(if (allowed) R.string.floating_permission_on else R.string.floating_permission_off),
            Modifier.padding(top = 14.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        if (access.pro) OutlinedButton(onClick = { enableRequested = false; disclosure = true }, modifier = Modifier.padding(top = 8.dp)) {
            Text(stringResource(R.string.floating_manage_permission))
        }
        Text(stringResource(R.string.floating_protected_apps), Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.height(16.dp))
    Panel {
        Text(stringResource(if (engine.running) R.string.floating_session_ready else R.string.floating_session_needed), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.floating_visibility), Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
        if (!engine.running) TextButton(onClick = listen) { Text(stringResource(R.string.floating_go_listen)) }
    }
    if (disclosure) AlertDialog(onDismissRequest = { disclosure = false; enableRequested = false },
        title = { Text(stringResource(R.string.floating_permission_title)) },
        text = { Text(stringResource(if (Build.VERSION.SDK_INT >= 30) R.string.floating_permission_list else R.string.floating_permission_direct,
            context.applicationInfo.loadLabel(context.packageManager).toString())) },
        confirmButton = { TextButton(onClick = {
            disclosure = false
            runCatching { launcher.launch(OverlayPermission.intent(context.packageName)) }.onFailure {
                enableRequested = false
                Toast.makeText(context, R.string.overlay_unavailable, Toast.LENGTH_LONG).show()
            }
        }) { Text(stringResource(R.string.continue_label)) } },
        dismissButton = { TextButton(onClick = { disclosure = false; enableRequested = false }) { Text(stringResource(R.string.cancel)) } })
}
