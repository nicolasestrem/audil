package com.audil.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.audil.data.repository.SettingsRepository
import com.audil.ui.components.AudilCard
import com.audil.ui.components.AudilScaffold

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
    
    var showLanguageDialog by remember { mutableStateOf(false) }

    AudilScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            
            // Language & Region
            SettingsGroupTitle("Language & Region")
            AudilCard(
                modifier = Modifier.fillMaxWidth().clickable { showLanguageDialog = true }
            ) {
                Row(
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Transcription Language", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Language used for voice transcription", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        if (currentLang == "en") "English" else currentLang.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance
            SettingsGroupTitle("Appearance")
            AudilCard {
                Column {
                    Text("Theme", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Choose how the app looks and feels", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ThemeOptionCard(
                            title = "Light",
                            selected = currentTheme == SettingsRepository.THEME_LIGHT,
                            color = Color(0xFFFAFAFA),
                            onClick = { viewModel.setTheme(SettingsRepository.THEME_LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            title = "Dark",
                            selected = currentTheme == SettingsRepository.THEME_DARK,
                            color = Color(0xFF1E1E1E),
                            onClick = { viewModel.setTheme(SettingsRepository.THEME_DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            title = "System",
                            selected = currentTheme == SettingsRepository.THEME_SYSTEM,
                            color = Color.Gray,
                            onClick = { viewModel.setTheme(SettingsRepository.THEME_SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Model Selection
            SettingsGroupTitle("Model Selection")
            AudilCard(modifier = Modifier.clickable(onClick = onNavigateToModelSelection)) {
                val modelName = if (currentModel == SettingsRepository.MODEL_LOCAL_STANDARD) 
                    "Standard (Tiny)" else "Base / Small"
                val modelDesc = if (currentModel == SettingsRepository.MODEL_LOCAL_STANDARD)
                    "Faster performance (Multilingual)" else "Higher accuracy"
                val modelSize = if (currentModel == SettingsRepository.MODEL_LOCAL_STANDARD)
                    "~40 MB" else "~75-200 MB"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(modelName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(modelDesc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(modelSize, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    
    if (showLanguageDialog) {
        val languages = listOf("en" to "English", "fr" to "French", "es" to "Spanish", "de" to "German", "it" to "Italian", "pt" to "Portuguese")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    languages.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentLang == code,
                                onClick = { 
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsGroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
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
                .background(color, RoundedCornerShape(12.dp))
                .padding(2.dp), // Inner padding
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
               Box(
                   modifier = Modifier
                       .fillMaxSize()
                       .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                       .padding(2.dp)
               ) {
                   // Ring effect
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {}
               }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            title, 
            style = MaterialTheme.typography.bodyMedium, 
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
