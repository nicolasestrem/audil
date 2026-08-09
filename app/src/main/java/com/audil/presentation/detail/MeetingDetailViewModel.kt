package com.audil.presentation.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audil.data.local.entity.MeetingEntity
import com.audil.data.repository.HistoryRepository
import com.audil.data.repository.TranscriptionRepository
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
    private val transcriptionRepository: TranscriptionRepository
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

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _transcriptContent = MutableStateFlow<String?>(null)
    val transcriptContent: StateFlow<String?> = _transcriptContent.asStateFlow()

    private val _isPreparingAudio = MutableStateFlow(false)
    val isPreparingAudio: StateFlow<Boolean> = _isPreparingAudio.asStateFlow()

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var progressJob: kotlinx.coroutines.Job? = null

    companion object {
        private const val TAG = "MeetingDetailViewModel"
    }

    fun loadMeeting(id: Long) {
        viewModelScope.launch {
            _meeting.value = repository.getMeetingById(id)
        }
    }

    fun loadTranscript() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val path = _meeting.value?.transcriptPath ?: return@launch
            val content = try {
                java.io.File(path).readText()
            } catch (e: Exception) {
                "Error loading transcript: ${e.message}"
            }
            _transcriptContent.value = content
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun togglePlayPause() {
        val path = _meeting.value?.audioPath ?: return

        if (mediaPlayer == null) {
            _isPreparingAudio.value = true
            mediaPlayer = android.media.MediaPlayer().apply {
                try {
                    setDataSource(path)
                    setOnPreparedListener {
                        start()
                        _isPlaying.value = true
                        _isPreparingAudio.value = false
                        startProgressTracker()
                    }
                    setOnCompletionListener {
                        _isPlaying.value = false
                        _playbackProgress.value = 1f
                        stopProgressTracker()
                    }
                    setOnErrorListener { _, what, extra ->
                        _isPreparingAudio.value = false
                        _message.value = "Playback error (code: $what, extra: $extra)"
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        true
                    }
                    prepareAsync()
                } catch (e: Exception) {
                    _isPreparingAudio.value = false
                    _message.value = "Failed to load audio: ${e.message}"
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
                mediaPlayer?.let { player ->
                    if (player.duration > 0) {
                        val progress = player.currentPosition.toFloat() / player.duration.toFloat()
                        if (kotlin.math.abs(progress - _playbackProgress.value) > 0.01f) {
                            _playbackProgress.value = progress
                        }
                    }
                }
                kotlinx.coroutines.delay(200)
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
                val transcriptFile = java.io.File(file.parent, "TRANSCRIPT_${m.id}.txt")
                transcriptFile.writeText(transcript)

                // Use dedicated updateTranscript method
                repository.updateTranscript(m.id, transcriptFile.absolutePath)
                val updatedMeeting = m.copy(transcriptPath = transcriptFile.absolutePath)
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

            val filename = "Audil_${src.name}"
            val mimeType = "audio/mpeg"

            try {
                val resolver = app.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_MUSIC + "/Audil")
                }

                val uri = resolver.insert(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw java.io.IOException("Failed to create MediaStore entry")

                resolver.openOutputStream(uri)?.use { out ->
                    src.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }

                _message.value = "Audio exported to Music/Audil/$filename"
            } catch (e: Exception) {
                e.printStackTrace()
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

            m.summaryPath?.let { summaryPath ->
                val sumFile = java.io.File(summaryPath)
                if (sumFile.exists()) {
                    content.append("SUMMARY:\n${sumFile.readText()}\n\n")
                }
            }

            m.transcriptPath?.let { transcriptPath ->
                val transFile = java.io.File(transcriptPath)
                if (transFile.exists()) {
                    content.append("TRANSCRIPT:\n${transFile.readText()}\n")
                }
            }

            val fileName = "Audil_Export_${m.id}.txt"

            try {
                val resolver = app.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOCUMENTS + "/Audil")
                }

                val uri = resolver.insert(android.provider.MediaStore.Files.getContentUri("external"), contentValues)
                    ?: throw java.io.IOException("Failed to create MediaStore entry")

                resolver.openOutputStream(uri)?.use { out ->
                    out.write(content.toString().toByteArray())
                }

                _message.value = "Text exported to Documents/Audil/$fileName"
            } catch (e: Exception) {
                e.printStackTrace()
                _message.value = "Export failed: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        progressJob = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
