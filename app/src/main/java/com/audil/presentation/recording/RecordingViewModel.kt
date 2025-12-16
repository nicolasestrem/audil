package com.audil.presentation.recording

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.audil.service.RecordingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.audil.data.repository.HistoryRepository
import com.audil.data.local.entity.MeetingEntity
import com.audil.domain.model.MeetingType

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val app: Application,
    private val historyRepository: HistoryRepository
) : AndroidViewModel(app) {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    private var timerJob: Job? = null
    private var startTime: Long = 0
    private var currentFile: File? = null

    fun toggleRecording() {
        if (_isRecording.value) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        val fileName = "REC_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.wav"
        val file = File(app.getExternalFilesDir(null), fileName)
        currentFile = file
        
        val intent = Intent(app, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START_RECORDING
            putExtra(RecordingService.EXTRA_FILE_PATH, file.absolutePath)
        }
        app.startService(intent)
        
        _isRecording.value = true
        startTime = System.currentTimeMillis()
        startTimer()
    }

    private fun stopRecording() {
        val intent = Intent(app, RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP_RECORDING
        }
        app.startService(intent)
        
        _isRecording.value = false
        stopTimer()
        
        // Save to History
        if (currentFile != null) {
            saveToHistory(currentFile!!, startTime, _recordingDuration.value)
        }
    }
    
    private fun saveToHistory(file: File, timestamp: Long, duration: Long) {
        viewModelScope.launch {
            val meeting = MeetingEntity(
                timestamp = timestamp,
                durationMs = duration,
                title = "New Meeting " + SimpleDateFormat("Md HH:mm", Locale.getDefault()).format(Date(timestamp)),
                type = MeetingType.TEAM_MEETING, // Default
                participantCount = 2,
                audioPath = file.absolutePath
            )
            historyRepository.saveMeeting(meeting)
        }
    }

    private fun startTimer() {
        _recordingDuration.value = 0
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(100)
                _recordingDuration.value = System.currentTimeMillis() - startTime
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        // Do NOT reset _recordingDuration.value here, we need it for saving.
    }
}
