package com.audil.presentation.recording

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.random.Random
import androidx.compose.animation.core.*
import kotlin.math.*

@Composable
fun RecordingScreen(
    viewModel: RecordingViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val duration by viewModel.recordingDuration.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Waveform Visualization
        com.audil.ui.components.AudilCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Waveform(isRecording = isRecording)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Permissions
        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.toggleRecording()
            }
        }

        RecordButton(
            isRecording = isRecording,
            onClick = { 
                if (isRecording) {
                    viewModel.toggleRecording()
                } else {
                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }
            }
        )
        
        if (isRecording) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Recording in progress...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun Waveform(isRecording: Boolean) {
    val color = MaterialTheme.colorScheme.primary

    // Memoize wave data points (only recalculate when recording state changes)
    val wavePoints = androidx.compose.runtime.remember(isRecording) {
        if (isRecording) {
            // Precompute normalized X values
            (0..100).map { i ->
                val normalizedX = (i.toFloat() / 100) * 4 * Math.PI.toFloat()
                Pair(i, normalizedX)
            }
        } else {
            emptyList()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (!isRecording) {
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                strokeWidth = 4f
            )
            return@Canvas
        }

        val width = size.width
        val height = size.height
        val path = Path()
        val step = width / 100

        path.moveTo(0f, height / 2)

        // Use precomputed points
        for ((i, normalizedX) in wavePoints) {
            val x = i * step
            val wave1 = kotlin.math.sin((normalizedX + phase).toDouble()).toFloat()
            val wave2 = kotlin.math.sin((normalizedX * 1.5 + phase * 2).toDouble()).toFloat() * 0.5f
            val amplitude = (height / 3) * (wave1 + wave2) * 0.5f
            val y = (height / 2) + amplitude
            path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 6f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
fun RecordButton(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(96.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.White, if (isRecording) MaterialTheme.shapes.small else CircleShape)
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
