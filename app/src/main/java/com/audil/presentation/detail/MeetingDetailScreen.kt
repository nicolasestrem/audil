package com.audil.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.MoreVert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingDetailScreen(
    meetingId: Long,
    viewModel: MeetingDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onGenerateSummary: (Long) -> Unit
) {
    // Trigger load side effect
    androidx.compose.runtime.LaunchedEffect(meetingId) {
        viewModel.loadMeeting(meetingId)
    }
    
    val context_ = androidx.compose.ui.platform.LocalContext.current
    val message by viewModel.message.collectAsState()
    
    androidx.compose.runtime.LaunchedEffect(message) {
        message?.let { msg ->
            android.widget.Toast.makeText(context_, msg, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val meeting by viewModel.meeting.collectAsState()
    var menuExpanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meeting Details", color = com.audil.ui.theme.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = com.audil.ui.theme.TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = com.audil.ui.theme.TextPrimary)
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(com.audil.ui.theme.DeepCharcoal)
                    ) {
                         androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Export Audio to Folder", color = com.audil.ui.theme.TextPrimary) },
                            onClick = { 
                                viewModel.exportAudio()
                                menuExpanded = false 
                            }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Export as Text File", color = com.audil.ui.theme.TextPrimary) },
                            onClick = { 
                                viewModel.exportText()
                                menuExpanded = false 
                            }
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = com.audil.ui.theme.MidnightBlue
                )
            )
        },
        containerColor = com.audil.ui.theme.MidnightBlue
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            meeting?.let { m ->
                Text(
                    text = m.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = com.audil.ui.theme.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = com.audil.ui.theme.TextSecondary)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(m.timestamp)),
                        style = MaterialTheme.typography.bodyLarge,
                        color = com.audil.ui.theme.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${m.participantCount} Participants • ${m.type.displayName ?: m.type.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = com.audil.ui.theme.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Audio Player Section
                Text("Audio Recording", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = com.audil.ui.theme.TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                
                val isPlaying by viewModel.isPlaying.collectAsState()
                val progress by viewModel.playbackProgress.collectAsState()
                val isTranscribing by viewModel.isTranscribing.collectAsState()
                val transcriptionStatus by viewModel.transcriptionStatus.collectAsState()
                val isPreparingAudio by viewModel.isPreparingAudio.collectAsState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(com.audil.ui.theme.DeepCharcoal, MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        enabled = !isPreparingAudio
                    ) {
                        if (isPreparingAudio) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(12.dp),
                                color = com.audil.ui.theme.ElectricBlue
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = com.audil.ui.theme.ElectricBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(8.dp))

                    if (isPlaying) {
                        // Simple progress bar
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.weight(1f),
                            color = com.audil.ui.theme.ElectricBlue,
                            trackColor = com.audil.ui.theme.TextSecondary.copy(alpha = 0.3f)
                        )
                    } else {
                        Text(
                            text = if (isPreparingAudio) "Preparing..." else if (progress > 0) "Paused" else "Ready to play",
                            style = MaterialTheme.typography.bodyMedium,
                            color = com.audil.ui.theme.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Transcript / Summary Actions
                if (m.transcriptPath == null) {
                    if (isTranscribing) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                             CircularProgressIndicator(color = com.audil.ui.theme.ElectricBlue)
                             Spacer(modifier = Modifier.height(8.dp))
                             Text(transcriptionStatus, style = MaterialTheme.typography.bodySmall, color = com.audil.ui.theme.TextSecondary)
                         }
                    } else {
                        Button(
                            onClick = { viewModel.startTranscription() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = com.audil.ui.theme.ElectricBlue
                            )
                        ) {
                            Text("Transcribe Audio", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                } else {
                     Button(
                        onClick = { onGenerateSummary(m.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = com.audil.ui.theme.ElectricBlue
                        )
                    ) {
                        Text(if (m.summaryPath != null) "View Summary" else "Generate Summary", color = androidx.compose.ui.graphics.Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Transcript",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = com.audil.ui.theme.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Load transcript asynchronously via ViewModel
                    val transcriptContent by viewModel.transcriptContent.collectAsState()

                    LaunchedEffect(m.transcriptPath) {
                        if (m.transcriptPath != null) {
                            viewModel.loadTranscript()
                        }
                    }

                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(com.audil.ui.theme.DeepCharcoal, MaterialTheme.shapes.medium)
                            .padding(16.dp)
                    ) {
                        if (transcriptContent != null) {
                            Text(
                                text = transcriptContent!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = com.audil.ui.theme.TextSecondary
                            )
                        } else {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                                color = com.audil.ui.theme.ElectricBlue
                            )
                        }
                    }
                }
            } ?: run {
                Text("Loading...", style = MaterialTheme.typography.bodyLarge, color = com.audil.ui.theme.TextPrimary)
            }
        }
    }
}
