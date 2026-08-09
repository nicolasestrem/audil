package com.audil.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audil.data.repository.SummaryRepository
import com.audil.domain.model.MeetingContext
import com.audil.domain.model.MeetingType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val app: android.app.Application,
    private val repository: SummaryRepository,
    private val historyRepository: com.audil.data.repository.HistoryRepository,
    private val settingsRepository: com.audil.data.repository.SettingsRepository
) : androidx.lifecycle.AndroidViewModel(app) {

    private val _uiState = MutableStateFlow<SummaryUiState>(SummaryUiState.Idle)
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    private val _selectedContext = MutableStateFlow(MeetingContext(MeetingType.STANDUP))
    val selectedContext: StateFlow<MeetingContext> = _selectedContext.asStateFlow()

    // LRU cache for transcripts (max 5 in memory)
    private val transcriptCache = object : LinkedHashMap<Long, String>(
        5, 0.75f, true  // accessOrder = true for LRU
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, String>?): Boolean {
            return size > 5
        }
    }

    private val cacheLock = Any()

    private var currentMeetingId: Long = 0
    private var currentTranscript: String = ""

    fun loadMeeting(id: Long) {
        currentMeetingId = id
        _uiState.value = SummaryUiState.Loading("Loading meeting...")
        viewModelScope.launch {
            val meeting = historyRepository.getMeetingById(id)
            if (meeting != null) {
                var loadedText = ""
                if (meeting.transcriptPath != null) {
                    val cached = synchronized(cacheLock) {
                        transcriptCache[id]
                    }

                    if (cached != null) {
                        loadedText = cached
                    } else {
                        withContext(Dispatchers.IO) {
                            val file = java.io.File(meeting.transcriptPath)
                            if (file.exists()) {
                                val text = file.readText()
                                synchronized(cacheLock) {
                                    transcriptCache[id] = text
                                }
                                loadedText = text
                            }
                        }
                    }
                }

                // NO simulated fallback — if no transcript, report error
                if (loadedText.isBlank()) {
                    _uiState.value = SummaryUiState.Error(
                        "No transcript available. Please transcribe the recording first."
                    )
                } else {
                    currentTranscript = loadedText
                    _uiState.value = SummaryUiState.Idle
                }
            } else {
                _uiState.value = SummaryUiState.Error("Meeting not found")
            }
        }
    }

    fun setContext(type: MeetingType) {
        _selectedContext.value = _selectedContext.value.copy(type = type)
    }

    fun setParticipantCount(count: Int) {
        _selectedContext.value = _selectedContext.value.copy(participantCount = count)
    }

    fun generateSummary() {
        if (currentTranscript.isBlank()) {
            _uiState.value = SummaryUiState.Error("No transcript available (Empty)")
            return
        }

        viewModelScope.launch {
            repository.generateSummaryStream(currentTranscript, _selectedContext.value) { status ->
                _uiState.value = SummaryUiState.Loading(status)
            }
                .onStart { _uiState.value = SummaryUiState.Loading("Initializing AI...") }
                .catch { e -> _uiState.value = SummaryUiState.Error(e.message ?: "Unknown error") }
                .collect { partialSummary ->
                    _uiState.value = SummaryUiState.Success(partialSummary)
                }
        }
    }

    fun saveSummary(summary: String) {
        viewModelScope.launch {
            val filename = "SUMMARY_${currentMeetingId}.md"
            val file = java.io.File(app.getExternalFilesDir(null), filename)
            file.writeText(summary)

            historyRepository.updateSummary(currentMeetingId, file.absolutePath, summary.take(100))
        }
    }

    fun exportSummary(summary: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, summary)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        app.startActivity(android.content.Intent.createChooser(intent, "Share Summary"))
    }
}

sealed class SummaryUiState {
    object Idle : SummaryUiState()
    data class Loading(val status: String) : SummaryUiState()
    data class Success(val summary: String) : SummaryUiState()
    data class Error(val message: String) : SummaryUiState()
}
