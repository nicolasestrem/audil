package com.audil.data.remote

import com.audil.nativelib.SherpaOnnxWrapper
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abstraction over transcription providers (remote OpenAI Whisper or local SherpaOnnx).
 *
 * Selected elegantly through injection: bind the desired implementation in the DI module.
 * Remote transcription adapts to OpenAiApiClient.uploadAudio(file, model, language).
 * Local transcription is retained only if genuinely available (SherpaOnnx initialized).
 */
interface TranscriptionClient {
    /**
     * Transcribe an audio file.
     *
     * @param file the audio file to transcribe
     * @param model the model identifier (e.g. "whisper-1" for remote, "tiny"/"base"/"small" for local)
     * @param language optional ISO language code (en, fr, etc.) — anticipated for future use
     * @return Result containing the transcript text, or failure
     */
    suspend fun transcribe(
        file: File,
        model: String = "whisper-1",
        language: String? = null
    ): Result<String>
}

/**
 * Remote transcription using OpenAI-compatible API via [OpenAiApiClient].
 */
@Singleton
class RemoteTranscriptionClient @Inject constructor(
    private val apiClient: OpenAiApiClient
) : TranscriptionClient {
    override suspend fun transcribe(
        file: File,
        model: String,
        language: String?
    ): Result<String> {
        return try {
            val transcript = apiClient.uploadAudio(file, model)
            Result.success(transcript)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Local transcription using on-device SherpaOnnx engine.
 * Only genuinely available when the engine is properly initialized.
 */
@Singleton
class LocalTranscriptionClient @Inject constructor(
    private val sherpaOnnx: SherpaOnnxWrapper,
    private val modelManager: com.audil.data.repository.ModelManager
) : TranscriptionClient {
    override suspend fun transcribe(
        file: File,
        model: String,
        language: String?
    ): Result<String> {
        return try {
            if (!modelManager.isModelDownloaded(model)) {
                return Result.failure(IllegalStateException("Model $model not downloaded"))
            }
            val modelPath = modelManager.getModelPath(model)
            sherpaOnnx.initRecognizer(modelPath, model)
            val transcript = sherpaOnnx.transcribe(file)
            Result.success(transcript)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
