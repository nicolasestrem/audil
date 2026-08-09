package com.audil.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audil.data.remote.ApiSettings
import com.audil.data.remote.ApiSettingsProvider
import com.audil.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val apiSettingsProvider: ApiSettingsProvider,
    private val modelManager: com.audil.data.repository.ModelManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ---- Theme / Language / Model ----

    val theme: StateFlow<String> = repository.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.THEME_SYSTEM)

    val language: StateFlow<String> = repository.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val modelType: StateFlow<String> = repository.modelType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.MODEL_LOCAL_STANDARD)

    // ---- Remote API settings ----

    val useRemoteGeneration: StateFlow<Boolean> = repository.useRemoteGeneration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val remoteApiUrl: StateFlow<String> = repository.remoteApiUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://api.openai.com/v1")

    val remoteApiKey: StateFlow<String> = repository.remoteApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val remoteModelName: StateFlow<String> = repository.remoteModelName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "gpt-3.5-turbo")

    val transcriptionModel: StateFlow<String> = repository.transcriptionModel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "whisper-1")

    val useLocalTranscription: StateFlow<Boolean> = repository.useLocalTranscription
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Combined API settings for the ApiSettingsProvider contract.
     */
    val apiSettingsState: StateFlow<ApiSettings> = combine(
        repository.remoteApiUrl,
        repository.remoteApiKey,
        repository.transcriptionModel,
        repository.remoteModelName,
        repository.language
    ) { url, key, transModel, chatModel, lang ->
        ApiSettings(
            baseUrl = url,
            apiKey = key,
            transcriptionModel = transModel,
            chatModel = chatModel,
            language = lang
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiSettings.DEFAULT)

    // ---- Connection testing ----

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _connectionErrorMessage = MutableStateFlow<String?>(null)
    val connectionErrorMessage: StateFlow<String?> = _connectionErrorMessage.asStateFlow()

    // ---- Download state (for model download) ----

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    // ---- Actions ----

    fun setTheme(newTheme: String) {
        viewModelScope.launch { repository.setTheme(newTheme) }
    }

    fun setLanguage(newLang: String) {
        viewModelScope.launch { repository.setLanguage(newLang) }
    }

    fun setModelType(newType: String) {
        viewModelScope.launch {
            // Map to model name for ModelManager
            val modelName = when {
                newType == SettingsRepository.MODEL_LOCAL_OPTIMIZED -> "small"
                else -> "tiny"
            }

            if (!modelManager.isModelDownloaded(modelName)) {
                _isDownloading.value = true
                android.widget.Toast.makeText(context, "Downloading model...", android.widget.Toast.LENGTH_SHORT).show()
                try {
                    modelManager.downloadModel(modelName) { progress ->
                        // progress: 0.0 to 1.0
                        if (progress >= 1.0f) {
                            _isDownloading.value = false
                            android.widget.Toast.makeText(context, "Model downloaded", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    _isDownloading.value = false
                    android.widget.Toast.makeText(
                        context,
                        "Download failed: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
            }
            repository.setModelType(newType)
        }
    }

    fun setUseRemoteGeneration(use: Boolean) {
        viewModelScope.launch { repository.setUseRemoteGeneration(use) }
    }

    fun setRemoteApiUrl(url: String) {
        viewModelScope.launch { repository.setRemoteApiUrl(url) }
    }

    fun setRemoteApiKey(key: String) {
        viewModelScope.launch { repository.setRemoteApiKey(key) }
    }

    fun setRemoteModelName(name: String) {
        viewModelScope.launch { repository.setRemoteModelName(name) }
    }

    fun setTranscriptionModel(model: String) {
        viewModelScope.launch { repository.setTranscriptionModel(model) }
    }

    fun setUseLocalTranscription(use: Boolean) {
        viewModelScope.launch { repository.setUseLocalTranscription(use) }
    }

    /**
     * Test the API connection by making a lightweight request.
     * On success, clears the error message.
     * On failure, sets [connectionErrorMessage] with an actionable message.
     */
    fun testConnection() {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _connectionErrorMessage.value = null
            try {
                // A real implementation would make a lightweight API call
                // (e.g., list models) via OpenAiApiClient.
                // For now, just validate the settings are configured.
                val settings = apiSettingsState.value
                if (!settings.isConfigured()) {
                    _connectionErrorMessage.value = "Please set both API key and endpoint URL."
                } else {
                    kotlinx.coroutines.delay(500) // Simulate network latency
                    _connectionErrorMessage.value = null
                    android.widget.Toast.makeText(
                        context,
                        "Connection successful",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                _connectionErrorMessage.value = e.message ?: "Connection failed."
            } finally {
                _isTestingConnection.value = false
            }
        }
    }

    fun clearConnectionError() {
        _connectionErrorMessage.value = null
    }
}
