package com.dino.chronorex.ui.export

import android.content.Intent
import java.util.ArrayList
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.dino.chronorex.export.ExportResult
import com.dino.chronorex.ui.components.ChronoRexCard
import com.dino.chronorex.ui.components.ChronoRexPrimaryButton
import com.dino.chronorex.ui.components.ChronoRexSecondaryButton
import com.dino.chronorex.ui.theme.spacing
import kotlinx.coroutines.launch

@Composable
fun ExportScreen(
    state: ExportUiState,
    onGenerateCsv: () -> Unit,
    onGeneratePdf: () -> Unit,
    onClearError: () -> Unit,
    onBack: () -> Unit,
    saveToDownloads: suspend (ExportResult) -> List<android.net.Uri>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(MaterialTheme.spacing.lg)
                .imePadding()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
        ) {
            Text(
                text = "Exports stay offline until you share them.",
                style = MaterialTheme.typography.titleMedium
            )
            ExportCard(
                title = "CSV data export",
                description = "Generates per-entity CSVs with a data dictionary.",
                isProcessing = state.processingCsv,
                hasResult = state.csvResult != null,
                onGenerate = onGenerateCsv,
                onShare = state.csvResult?.let { result ->
                    {
                        shareExport(context, result)
                    }
                },
                onSave = state.csvResult?.let { result ->
                    {
                        scope.launch {
                            saveToDownloads(result)
                            snackbarHostState.showSnackbar("Saved CSV files to Downloads")
                        }
                    }
                }
            )
            ExportCard(
                title = "Clinician PDF",
                description = "One-page summary with trends, recent symptoms, and disclaimer.",
                isProcessing = state.processingPdf,
                hasResult = state.pdfResult != null,
                onGenerate = onGeneratePdf,
                onShare = state.pdfResult?.let { result ->
                    {
                        shareExport(context, result)
                    }
                },
                onSave = state.pdfResult?.let { result ->
                    {
                        scope.launch {
                            saveToDownloads(result)
                            snackbarHostState.showSnackbar("Saved PDF to Downloads")
                        }
                    }
                }
            )
            ChronoRexSecondaryButton(text = "Back", modifier = Modifier.fillMaxWidth(), onClick = onBack)
        }
    }

    state.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            onClearError()
        }
    }
}

@Composable
private fun ExportCard(
    title: String,
    description: String,
    isProcessing: Boolean,
    hasResult: Boolean,
    onGenerate: () -> Unit,
    onShare: (() -> Unit)?,
    onSave: (() -> Unit)?
) {
    ChronoRexCard {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
        )
        if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.padding(top = MaterialTheme.spacing.sm))
        }
        ChronoRexPrimaryButton(
            text = if (hasResult) "Regenerate" else "Generate",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.sm),
            onClick = onGenerate
        )
        if (hasResult) {
            ChronoRexPrimaryButton(
                text = "Share",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.xs),
                onClick = { onShare?.invoke() },
                enabled = onShare != null
            )
            ChronoRexPrimaryButton(
                text = "Save to device",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.xs),
                onClick = { onSave?.invoke() },
                enabled = onSave != null
            )
        }
    }
}

private fun shareExport(context: android.content.Context, result: ExportResult) {
    val intent = if (result.uris.size == 1) {
        Intent(Intent.ACTION_SEND).apply {
            type = result.mimeType
            putExtra(Intent.EXTRA_STREAM, result.uris.first())
            putExtra(Intent.EXTRA_SUBJECT, result.label)
            putExtra(Intent.EXTRA_TEXT, result.description)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } else {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = result.mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(result.uris))
            putExtra(Intent.EXTRA_SUBJECT, result.label)
            putExtra(Intent.EXTRA_TEXT, result.description)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(Intent.createChooser(intent, "Share ChronoRex export"))
}
