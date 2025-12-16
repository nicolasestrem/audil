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
    private val repository: SettingsRepository
) : ViewModel() {

    val theme: StateFlow<String> = repository.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.THEME_SYSTEM)

    val language: StateFlow<String> = repository.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val modelType: StateFlow<String> = repository.modelType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.MODEL_LOCAL_STANDARD)

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
            repository.setModelType(newType)
        }
    }
}
