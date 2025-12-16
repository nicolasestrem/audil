package com.audil.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.audil.data.repository.SettingsRepository
import com.audil.ui.theme.DeepCharcoal
import com.audil.ui.theme.ElectricBlue
import com.audil.ui.theme.MidnightBlue
import com.audil.ui.theme.TextPrimary
import com.audil.ui.theme.TextSecondary
import com.audil.ui.components.AudilScaffold
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToModelSelection: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.theme.collectAsState()
    val currentLang by viewModel.language.collectAsState()
    val currentModel by viewModel.modelType.collectAsState()

    AudilScaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Settings", color = TextPrimary) },
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
        ) {
            
            // Language & Region
            SettingsGroupTitle("Language & Region")
            SettingsCard {
                Column(
                    modifier = Modifier.fillMaxWidth().clickable { /* TODO: Language Selection */ }
                ) {
                    Text("Transcription Language", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text("Language used for voice transcription", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(ElectricBlue, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentLang.uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (currentLang == "en") "English" else currentLang, color = TextPrimary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance
            SettingsGroupTitle("Appearance")
            Text("Theme", style = MaterialTheme.typography.titleSmall, color = TextPrimary, modifier = Modifier.padding(bottom = 4.dp))
            Text("Choose how the app looks and feels", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(bottom = 12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ThemeOptionCard(
                    title = "Light",
                    selected = currentTheme == SettingsRepository.THEME_LIGHT,
                    color = Color.White,
                    onClick = { viewModel.setTheme(SettingsRepository.THEME_LIGHT) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionCard(
                    title = "Dark",
                    selected = currentTheme == SettingsRepository.THEME_DARK,
                    color = DeepCharcoal,
                    onClick = { viewModel.setTheme(SettingsRepository.THEME_DARK) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionCard(
                    title = "System",
                    selected = currentTheme == SettingsRepository.THEME_SYSTEM,
                    color = Color.Gray, // Simplified representation
                    onClick = { viewModel.setTheme(SettingsRepository.THEME_SYSTEM) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Model Selection
            SettingsGroupTitle("Model Selection")
            SettingsCard(onClick = onNavigateToModelSelection) {
                val modelName = if (currentModel == SettingsRepository.MODEL_LOCAL_STANDARD) 
                    "Standard model (multilingual)" else "Optimized model (multilingual)"
                val modelDesc = if (currentModel == SettingsRepository.MODEL_LOCAL_STANDARD)
                    "Faster performance and smaller file size" else "Highest accuracy available"
                val modelSize = if (currentModel == SettingsRepository.MODEL_LOCAL_STANDARD)
                    "142 MB" else "465 MB"

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(modelName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(modelDesc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(modelSize, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun SettingsGroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SettingsCard(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}



@Composable
fun ThemeOptionCard(
    title: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .border(
                    if (selected) 2.dp else 1.dp,
                    if (selected) Color.White else TextSecondary.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                .background(Color.Black, RoundedCornerShape(12.dp)), // Background behind the preview
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = if (selected) Color.White else TextSecondary)
    }
}
