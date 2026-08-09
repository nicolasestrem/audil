package com.audil.presentation.transcription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audil.data.repository.TranscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TranscriptionViewModel @Inject constructor(
    private val repository: TranscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TranscriptionUiState>(TranscriptionUiState.Idle)
    val uiState: StateFlow<TranscriptionUiState> = _uiState.asStateFlow()

    private val _selectedModel = MutableStateFlow("whisper-1")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    fun setModel(model: String) {
        _selectedModel.value = model
    }

    fun transcribeRecording(file: File) {
        viewModelScope.launch {
            _uiState.value = TranscriptionUiState.Loading("Starting...")

            val result = repository.transcribe(
                audioFile = file,
                modelType = _selectedModel.value
            ) { status ->
                if (_uiState.value is TranscriptionUiState.Loading) {
                    _uiState.value = TranscriptionUiState.Loading(status)
                }
            }

            result.fold(
                onSuccess = { transcript ->
                    _uiState.value = TranscriptionUiState.Success(transcript)
                },
                onFailure = { error ->
                    _uiState.value = TranscriptionUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = TranscriptionUiState.Idle
    }
}

sealed class TranscriptionUiState {
    object Idle : TranscriptionUiState()
    data class Loading(val status: String) : TranscriptionUiState()
    data class Success(val transcript: String) : TranscriptionUiState()
    data class Error(val message: String) : TranscriptionUiState()
}
