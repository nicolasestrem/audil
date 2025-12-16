package com.audil.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audil.data.local.entity.MeetingEntity
import com.audil.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingDetailViewModel @Inject constructor(
    private val app: android.app.Application,
    private val repository: HistoryRepository,
    private val transcriptionRepository: com.audil.data.repository.TranscriptionRepository
) : ViewModel() {

    private val _meeting = MutableStateFlow<MeetingEntity?>(null)
    val meeting: StateFlow<MeetingEntity?> = _meeting.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()
    
    // Transcription State
    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()
    
    private val _transcriptionStatus = MutableStateFlow("")
    val transcriptionStatus: StateFlow<String> = _transcriptionStatus.asStateFlow()
    
    private val _message = MutableStateFlow<String?>(null) // UI Message (Toast/Snackbar)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var progressJob: kotlinx.coroutines.Job? = null

    fun loadMeeting(id: Long) {
        viewModelScope.launch {
            _meeting.value = repository.getMeetingById(id)
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }

    fun togglePlayPause() {
        val path = _meeting.value?.audioPath ?: return
        
        if (mediaPlayer == null) {
            mediaPlayer = android.media.MediaPlayer().apply {
                try {
                    setDataSource(path)
                    prepare()
                    setOnCompletionListener { 
                        _isPlaying.value = false 
                        _playbackProgress.value = 1f
                        stopProgressTracker()
                    }
                    start()
                    _isPlaying.value = true
                    startProgressTracker()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                _isPlaying.value = false
                stopProgressTracker()
            } else {
                mediaPlayer!!.start()
                _isPlaying.value = true
                startProgressTracker()
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (_isPlaying.value && mediaPlayer != null) {
                if (mediaPlayer!!.duration > 0) {
                    _playbackProgress.value = mediaPlayer!!.currentPosition.toFloat() / mediaPlayer!!.duration.toFloat()
                }
                kotlinx.coroutines.delay(100)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
    }
    
    fun startTranscription() {
        val m = _meeting.value ?: return
        val file = java.io.File(m.audioPath)
        
        if (!file.exists()) {
            _transcriptionStatus.value = "Audio file not found"
            return
        }
        
        viewModelScope.launch {
            _isTranscribing.value = true
            _transcriptionStatus.value = "Initializing..."
            
            val result = transcriptionRepository.transcribe(file) { status ->
                _transcriptionStatus.value = status
            }
            
            result.onSuccess { transcript ->
                _transcriptionStatus.value = "Saving..."
                // Save transcript to file
                val transcriptFile = java.io.File(file.parent, "TRANSCRIPT_${m.id}.txt")
                transcriptFile.writeText(transcript)
                
                // Update DB
                val updatedMeeting = m.copy(transcriptPath = transcriptFile.absolutePath)
                repository.saveMeeting(updatedMeeting) // Repository handles update/insert
                _meeting.value = updatedMeeting
                
                _isTranscribing.value = false
                _transcriptionStatus.value = ""
            }.onFailure { e ->
                _transcriptionStatus.value = "Error: ${e.message}"
                _isTranscribing.value = false
            }
        }
    }
    
    fun exportAudio() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val m = _meeting.value ?: return@launch
            val src = java.io.File(m.audioPath)
            if (!src.exists()) {
                _message.value = "Audio file not found"
                return@launch
            }

            val destDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS)
            if (!destDir.exists()) destDir.mkdirs()
            
            val dest = java.io.File(destDir, "Audil_${src.name}")
            try {
                src.copyTo(dest, overwrite = true)
                _message.value = "Audio exported to Documents/Audil_${src.name}"
            } catch (e: Exception) {
                _message.value = "Export failed: ${e.message}"
            }
        }
    }

    fun exportText() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val m = _meeting.value ?: return@launch
            
            val content = StringBuilder()
            content.append("Title: ${m.title}\n")
            content.append("Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(m.timestamp))}\n\n")
            
            if (m.summaryPath != null) {
                val sumFile = java.io.File(m.summaryPath!!)
                if (sumFile.exists()) {
                    content.append("SUMMARY:\n${sumFile.readText()}\n\n")
                }
            }
            
            if (m.transcriptPath != null) {
                val transFile = java.io.File(m.transcriptPath!!)
                if (transFile.exists()) {
                    content.append("TRANSCRIPT:\n${transFile.readText()}\n")
                }
            }
            
            val destDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS)
            if (!destDir.exists()) destDir.mkdirs()
             
            val fileName = "Audil_Export_${m.id}.txt"
            val dest = java.io.File(destDir, fileName)
            
            try {
                dest.writeText(content.toString())
                _message.value = "Text exported to Documents/$fileName"
            } catch (e: Exception) {
                _message.value = "Export failed: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
