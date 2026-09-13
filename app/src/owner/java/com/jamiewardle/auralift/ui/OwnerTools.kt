package com.jamiewardle.auralift.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jamiewardle.auralift.AuraliftApplication
import com.jamiewardle.auralift.R

@Composable internal fun OwnerTools(app: AuraliftApplication) {
    Panel {
        Text(stringResource(R.string.owner_tools), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.owner_tools_hint), Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { app.access.simulateFree() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.test_free)) }
        OutlinedButton(onClick = { app.access.simulateReward() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.test_ad_reward)) }
        OutlinedButton(onClick = { app.access.simulatePurchase() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.test_purchase)) }
        OutlinedButton(onClick = { app.access.simulateExpired() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.test_expired)) }
        Button(onClick = { app.access.restoreOwner() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.restore_owner)) }
    }
}
