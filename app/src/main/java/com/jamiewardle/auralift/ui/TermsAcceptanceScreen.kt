package com.jamiewardle.auralift.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jamiewardle.auralift.AuraliftApplication
import com.jamiewardle.auralift.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable internal fun TermsAcceptanceScreen(app: AuraliftApplication) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var document by rememberSaveable { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val previous by app.terms.state.collectAsState()
    BackHandler(document != null) { document = null }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            val scroll = remember(document) { androidx.compose.foundation.ScrollState(0) }
            Column(Modifier.widthIn(max = 600.dp).fillMaxWidth().verticalScroll(scroll).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (document != null) {
                    TextButton(onClick = { document = null }) { Text(stringResource(R.string.back)) }
                    PolicyScreen(document!!)
                } else {
                    Text("Auralift", style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(if (previous == null) R.string.terms_welcome else R.string.terms_updated),
                        style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.terms_safety), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.terms_summary), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.terms_review), style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = { document = "terms" }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.terms_of_use))
                    }
                    OutlinedButton(onClick = { document = "privacy" }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.privacy_policy))
                    }
                    Text(stringResource(R.string.terms_accept_explanation), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.terms_local_record), style = MaterialTheme.typography.bodySmall)
                    if (failed) Text(stringResource(R.string.terms_save_failed), color = MaterialTheme.colorScheme.error)
                    Button(onClick = {
                        saving = true; failed = false
                        scope.launch {
                            val saved = withContext(Dispatchers.IO) { app.terms.accept() }
                            failed = !saved; saving = false
                        }
                    }, enabled = !saving, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Text(stringResource(if (saving) R.string.terms_saving else R.string.terms_agree))
                    }
                    TextButton(onClick = { context.activity()?.finish() }, enabled = !saving,
                        modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.terms_not_now)) }
                }
            }
        }
    }
}

/** Shared offline documents, reachable both before agreement and from Settings. */
@Composable internal fun PolicyScreen(page: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val content = remember(page) { context.assets.open("legal/$page.txt").bufferedReader().use { it.readText() } }
    var exportFailed by remember(page) { mutableStateOf(false) }
    // Keep the chosen document across rotation while the system file picker is open.
    var exportText by rememberSaveable { mutableStateOf("") }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) scope.launch {
            val text = exportText
            exportFailed = !withContext(Dispatchers.IO) {
                runCatching {
                    val stream = context.contentResolver.openOutputStream(uri) ?: return@runCatching false
                    stream.bufferedWriter().use { it.write(text) }
                    true
                }.getOrDefault(false)
            }
        }
    }
    PageHeading(stringResource(if (page == "privacy") R.string.privacy_policy else R.string.terms_of_use),
        stringResource(R.string.policy_language))
    OutlinedButton(onClick = {
        exportText = content
        save.launch("Auralift-$page.txt")
    }) { Text(stringResource(R.string.terms_save_copy)) }
    if (exportFailed) Text(stringResource(R.string.terms_export_failed), color = MaterialTheme.colorScheme.error)
    Panel { SelectionContainer { Text(content, style = MaterialTheme.typography.bodyMedium) } }
}
