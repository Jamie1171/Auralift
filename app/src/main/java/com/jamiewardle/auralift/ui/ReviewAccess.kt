package com.jamiewardle.auralift.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.access.AccessStore

@Composable internal fun ReviewAccess(access: AccessStore, enabled: Boolean = true) {
    if (!access.reviewAvailable) return
    val state by access.state.collectAsStateWithLifecycle()
    var open by remember { mutableStateOf(false) }
    var endFailed by remember { mutableStateOf(false) }
    if (state.review) {
        Panel {
            Text(stringResource(R.string.review_active), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.review_active_hint), Modifier.padding(top = 8.dp))
            TextButton(onClick = { endFailed = !access.deactivateReview() }, enabled = enabled) {
                Text(stringResource(R.string.review_end))
            }
            if (endFailed) Text(stringResource(R.string.review_end_failed), color = MaterialTheme.colorScheme.error)
        }
    } else if (!open) {
        TextButton(onClick = { open = true }, enabled = enabled) { Text(stringResource(R.string.review_access)) }
    }
    if (open) {
        // Deliberately not saved in instance state, analytics or diagnostic reports.
        var code by remember { mutableStateOf("") }
        var invalid by remember { mutableStateOf(false) }
        Panel {
            Text(stringResource(R.string.review_access), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.review_hint), Modifier.padding(top = 8.dp))
            OutlinedTextField(value = code, onValueChange = {
                code = it.take(128); invalid = false
            }, label = { Text(stringResource(R.string.review_code)) }, singleLine = true,
                isError = invalid, modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters,
                    autoCorrectEnabled = false))
            if (invalid) Text(stringResource(R.string.review_invalid), color = MaterialTheme.colorScheme.error)
            Button(enabled = enabled && code.isNotBlank(), onClick = {
                if (access.activateReview(code)) open = false else invalid = true
            }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Text(stringResource(R.string.review_unlock))
            }
            TextButton(onClick = { open = false }) { Text(stringResource(R.string.cancel)) }
        }
    }
}
