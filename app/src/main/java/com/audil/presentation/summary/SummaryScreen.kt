package com.audil.presentation.summary

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.audil.domain.model.MeetingType
import com.audil.ui.components.AudilCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    meetingId: Long,
    viewModel: SummaryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    LaunchedEffect(meetingId) { viewModel.loadMeeting(meetingId) }

    val uiState by viewModel.uiState.collectAsState()
    val meetingContext by viewModel.selectedContext.collectAsState()
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(4.dp))

            Text("Summary", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)

            Spacer(Modifier.height(24.dp))

            // Config
            Text("Meeting Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp).clickable { expanded = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(meetingContext.type.displayName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Icon(Icons.Default.ChevronRight, "Select", Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        MeetingType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName, style = MaterialTheme.typography.bodyMedium) },
                                onClick = { viewModel.setContext(type); expanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Participants: ${meetingContext.participantCount}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                value = meetingContext.participantCount.toFloat(),
                onValueChange = { viewModel.setParticipantCount(it.toInt()) },
                valueRange = 1f..20f, steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(Modifier.height(24.dp))

            // Generate
            Button(
                onClick = { viewModel.generateSummary() },
                enabled = uiState !is SummaryUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState is SummaryUiState.Loading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.size(12.dp))
                    Text((uiState as SummaryUiState.Loading).status, style = MaterialTheme.typography.labelLarge)
                } else {
                    Text("Generate", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (uiState is SummaryUiState.Error) {
                Spacer(Modifier.height(12.dp))
                Text(
                    (uiState as SummaryUiState.Error).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(32.dp))

            if (uiState is SummaryUiState.Success) {
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(16.dp))

                Text("Result", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))

                AudilCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        (uiState as SummaryUiState.Success).summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { viewModel.saveSummary((uiState as SummaryUiState.Success).summary) }) {
                            Icon(Icons.Default.Save, null, Modifier.size(16.dp))
                            Spacer(Modifier.size(4.dp))
                            Text("Save", style = MaterialTheme.typography.labelLarge)
                        }
                        TextButton(onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, (uiState as SummaryUiState.Success).summary)
                            }
                            ContextCompat.startActivity(context, Intent.createChooser(sendIntent, "Share"), null)
                        }) {
                            Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                            Spacer(Modifier.size(4.dp))
                            Text("Share", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
