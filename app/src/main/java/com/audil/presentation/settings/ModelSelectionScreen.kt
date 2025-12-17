package com.audil.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.audil.data.repository.SettingsRepository
import com.audil.ui.theme.ElectricBlue
import com.audil.ui.theme.TextPrimary
import com.audil.ui.theme.TextSecondary
import com.audil.ui.components.AudilScaffold
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectionScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentModel by viewModel.modelType.collectAsState()

    val isDownloading by viewModel.isDownloading.collectAsState()

    AudilScaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Model Selection", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Text(
                    "AI Model",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Choose the model that best fits your needs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Standard Model
                ModelSelectionCard(
                    title = "Standard model (multilingual)",
                    description = "Faster performance and smaller file size\nSupports all languages",
                    size = "142 MB",
                    isSelected = currentModel == SettingsRepository.MODEL_LOCAL_STANDARD,
                    onClick = { if (!isDownloading) viewModel.setModelType(SettingsRepository.MODEL_LOCAL_STANDARD) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Optimized Model
                ModelSelectionCard(
                    title = "Optimized Model (Multilingual)",
                    description = "Highest accuracy available\nLarger file size, slower performance\nSupports all languages except Hindi/Gujarati",
                    size = "465 MB",
                    isSelected = currentModel == SettingsRepository.MODEL_LOCAL_OPTIMIZED,
                    onClick = { if (!isDownloading) viewModel.setModelType(SettingsRepository.MODEL_LOCAL_OPTIMIZED) }
                )
            }

            if (isDownloading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                        .clickable(enabled = false) {}, // Block clicks
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ElectricBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Downloading Model...", color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun ModelSelectionCard(
    title: String,
    description: String,
    size: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) ElectricBlue else TextSecondary.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .background(Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(size, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            
            // Radio Button Logic
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        2.dp,
                        if (isSelected) ElectricBlue else TextSecondary,
                        CircleShape
                    )
                    .padding(4.dp)
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ElectricBlue, CircleShape)
                    )
                }
            }
        }
    }
}
