package com.audil.data.repository

import com.audil.data.remote.TranscriptionClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that delegates transcription to an injected [TranscriptionClient].
 *
 * The client is selected at injection time:
 * - [com.audil.data.remote.RemoteTranscriptionClient] for OpenAI Whisper API
 * - [com.audil.data.remote.LocalTranscriptionClient] for on-device SherpaOnnx
 *
 * Diarization is intentionally not supported — realtime speaker diarization
 * is unsupported in this version. See SOTA research for future possibility.
 */
@Singleton
class TranscriptionRepository @Inject constructor(
    private val transcriptionClient: TranscriptionClient
) {

    suspend fun transcribe(
        audioFile: File,
        modelType: String = "whisper-1",
        language: String? = null,
        onProgress: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress("Transcribing...")
            val result = transcriptionClient.transcribe(audioFile, modelType, language)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
