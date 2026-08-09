package com.audil.presentation.recording

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun RecordingScreen(
    viewModel: RecordingViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onMeetingSaved: ((Long) -> Unit)? = null
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val duration by viewModel.recordingDuration.collectAsState()
    val savedMeetingId by viewModel.savedMeetingId.collectAsState()

    // Breathing pulse animation for the idle record button
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    LaunchedEffect(savedMeetingId) {
        savedMeetingId?.let { id ->
            onMeetingSaved?.invoke(id)
            viewModel.clearSavedMeetingId()
        }
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleRecording()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Timer — serif, tabular, warm ───────────────────────
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.Serif,
                fontFeatureSettings = "tnum"
            ),
            color = if (isRecording)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(56.dp))

        // ── Record Button — 80dp, breathing when idle ──────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.scale(if (!isRecording) breatheScale else 1f)
        ) {
            Button(
                onClick = {
                    if (isRecording) {
                        viewModel.toggleRecording()
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = if (isRecording) "Stop" else "Record",
                    modifier = Modifier.size(if (isRecording) 28.dp else 34.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Status ─────────────────────────────────────────────
        Text(
            text = if (isRecording) "Recording…" else "Tap to begin",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif
            ),
            color = if (isRecording)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
