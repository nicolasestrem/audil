package com.audil.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audil.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    val theme: StateFlow<String> = repository.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.THEME_SYSTEM)

    val language: StateFlow<String> = repository.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val modelType: StateFlow<String> = repository.modelType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.MODEL_LOCAL_STANDARD)

    private val _isDownloading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading

    fun setTheme(newTheme: String) {
        viewModelScope.launch {
            repository.setTheme(newTheme)
        }
    }

    fun setLanguage(newLang: String) {
        viewModelScope.launch {
            repository.setLanguage(newLang)
        }
    }

    fun setModelType(newType: String) {
        viewModelScope.launch {
            if (newType == SettingsRepository.MODEL_LOCAL_OPTIMIZED && modelType.value != newType) {
                _isDownloading.value = true
                android.widget.Toast.makeText(context, "Downloading optimized model...", android.widget.Toast.LENGTH_SHORT).show()
                kotlinx.coroutines.delay(3000) // Simulate download
                _isDownloading.value = false
                android.widget.Toast.makeText(context, "Download complete", android.widget.Toast.LENGTH_SHORT).show()
            }
            repository.setModelType(newType)
        }
    }
}
