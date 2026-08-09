package com.audil.presentation.transcription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

@Composable
fun TranscriptionScreen(
    recordingFile: File?,
    viewModel: TranscriptionViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Transcribe Recording",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Transcription Engine:", style = MaterialTheme.typography.titleMedium)

        Column(Modifier.selectableGroup()) {
            // Remote option (OpenAI Whisper)
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (selectedModel == "whisper-1"),
                        onClick = { viewModel.setModel("whisper-1") },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedModel == "whisper-1"),
                    onClick = null
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = "OpenAI Whisper (Remote)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Cloud-based, highest accuracy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Local options
            listOf("tiny", "base", "small").forEach { model ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (model == selectedModel),
                            onClick = { viewModel.setModel(model) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (model == selectedModel),
                        onClick = null
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = "On-Device ${model.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = when (model) {
                                "tiny" -> "Fast, low resource usage"
                                "base" -> "Balanced speed and accuracy"
                                "small" -> "Best local accuracy"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (recordingFile != null) {
            Button(
                onClick = { viewModel.transcribeRecording(recordingFile) },
                enabled = uiState !is TranscriptionUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Transcription")
            }
        } else {
            Text("No recording selected", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = uiState) {
            is TranscriptionUiState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.status)
            }
            is TranscriptionUiState.Success -> {
                Text("Transcript:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.transcript, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Done")
                }
            }
            is TranscriptionUiState.Error -> {
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
    }
}
