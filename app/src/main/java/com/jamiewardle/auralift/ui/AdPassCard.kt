package com.jamiewardle.auralift.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jamiewardle.auralift.AuraliftApplication
import com.jamiewardle.auralift.Distribution
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.access.AccessState
import com.jamiewardle.auralift.access.AdPassState
import com.jamiewardle.auralift.access.PassClock
import kotlin.math.ceil

/** Both entry points use the same ad instance, consent flow and earned-reward callback. */
@Composable internal fun AdPassCard(app: AuraliftApplication, onDetails: (() -> Unit)? = null,
                                    modifier: Modifier = Modifier) {
    val access by app.access.state.collectAsStateWithLifecycle()
    val ads by app.ads.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    AdPassCardContent(access, ads,
        onPrepare = { context.activity()?.let(app.ads::prepare) },
        onWatch = { context.activity()?.let(app.ads::show) },
        onDetails = onDetails, ownerEdition = Distribution.owner, modifier = modifier)
}

@Composable internal fun AdPassCardContent(access: AccessState, ads: AdPassState,
    onPrepare: () -> Unit, onWatch: () -> Unit, onDetails: (() -> Unit)? = null,
    ownerEdition: Boolean = false, modifier: Modifier = Modifier) {
    // An earned pass keeps a visible countdown. Permanent access needs no ad offer.
    if (access.owner || access.permanent) return
    val active = access.pro && access.passRemainingMs > 0
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(LocalAppTheme.current.corner),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .3f))) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(if (active) Icons.Rounded.CheckCircle else Icons.Rounded.AutoAwesome, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Text(stringResource(if (active) R.string.preview_active else R.string.ad_pass),
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }
            if (active) {
                Text(stringResource(R.string.preview_minutes, ceil(access.passRemainingMs / 60_000.0).toInt()),
                    style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 10.dp))
                LinearProgressIndicator(progress = { (access.passRemainingMs.toFloat() / PassClock.DURATION).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
            } else {
                Text(stringResource(R.string.ad_pass_intro), style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp))
            }
            Text(stringResource(R.string.ad_pass_benefits), style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp))
            Text(stringResource(if (onDetails == null) R.string.ad_pass_rules else R.string.ad_pass_short_rules),
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 10.dp))
            if (!access.pro) {
                Button(enabled = ads.available && !ads.busy && !ownerEdition, onClick = {
                    // Preparing never opens a delayed ad automatically; watching needs a second tap.
                    if (ads.ready) onWatch() else onPrepare()
                }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 52.dp)) {
                    Text(stringResource(if (ads.busy) R.string.ad_loading else if (ads.ready) R.string.watch_ad else R.string.prepare_ad))
                }
                val message = if (ownerEdition && onDetails != null) R.string.ad_pass_owner_hint else ads.message
                if (message != 0 && message != R.string.ad_pass_intro) Text(stringResource(message),
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 10.dp))
            }
            if (onDetails != null) TextButton(onClick = onDetails, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.ad_pass_details))
            }
        }
    }
}
