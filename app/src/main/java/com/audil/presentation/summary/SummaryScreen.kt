package com.audil.presentation.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.audil.domain.model.MeetingType
import com.audil.ui.theme.*
import com.audil.ui.components.AudilScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    meetingId: Long,
    viewModel: SummaryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    LaunchedEffect(meetingId) {
        viewModel.loadMeeting(meetingId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val context by viewModel.selectedContext.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    AudilScaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Generate Summary", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MidnightBlue
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlue)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            
            // Configuration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Configuration", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Meeting Type Selector
                    Text("Meeting Type", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MidnightBlue, RoundedCornerShape(8.dp))
                            .clickable { expanded = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(context.type.displayName, color = TextPrimary)
                            Icon(Icons.Default.ChevronRight, contentDescription = "Select", tint = TextSecondary)
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(DeepCharcoal)
                        ) {
                            MeetingType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName, color = TextPrimary) },
                                    onClick = {
                                        viewModel.setContext(type)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Participant Count
                    Text("Participants: ${context.participantCount}", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Slider(
                        value = context.participantCount.toFloat(),
                        onValueChange = { viewModel.setParticipantCount(it.toInt()) },
                        valueRange = 1f..20f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricBlue,
                            activeTrackColor = ElectricBlue,
                            inactiveTrackColor = TextSecondary.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Generate Button
            Button(
                onClick = { viewModel.generateSummary() },
                enabled = uiState !is SummaryUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    disabledContainerColor = ElectricBlue.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState is SummaryUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text((uiState as SummaryUiState.Loading).status)
                } else {
                    Text("Generate with AI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            // Error Message
            if (uiState is SummaryUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (uiState as SummaryUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Result Area
            if (uiState is SummaryUiState.Success) {
                Text("Summary", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            (uiState as SummaryUiState.Success).summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = TextSecondary.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { viewModel.saveSummary((uiState as SummaryUiState.Success).summary) }) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = ElectricBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", color = ElectricBlue)
                            }
                            TextButton(onClick = { viewModel.exportSummary((uiState as SummaryUiState.Success).summary) }) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = ElectricBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", color = ElectricBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}
