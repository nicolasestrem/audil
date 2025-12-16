package com.audil.data.repository

import com.audil.nativelib.SherpaOnnxWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionRepository @Inject constructor(
    private val modelManager: ModelManager,
    private val sherpaOnnx: SherpaOnnxWrapper,
    private val diarizationRepository: DiarizationRepository
) {

    suspend fun transcribe(
        audioFile: File, 
        modelType: String = "tiny", // tiny, base, small
        enableDiarization: Boolean = false,
        onProgress: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Check & Download Transcription Model
            onProgress("Checking model...")
            if (!modelManager.isModelDownloaded(modelType)) {
                onProgress("Downloading model $modelType...")
                modelManager.downloadModel(modelType) { /* progress */ }
            }

            // 2. Initialize Transcription Engine
            onProgress("Initializing engine...")
            val modelPath = modelManager.getModelPath(modelType)
            sherpaOnnx.initRecognizer(modelPath, modelType)

            // 3. Transcribe Audio
            onProgress("Transcribing...")
            val transcriptText = sherpaOnnx.transcribe(audioFile)
            
            // 4. Perform Diarization (Optional)
            if (enableDiarization) {
                onProgress("Running Diarization (Beta)...")
                val diarizationResult = diarizationRepository.processDiarization(audioFile, onProgress)
                
                // For this prototype, we'll just append speaker segments to the text
                diarizationResult.onSuccess { segments ->
                    val sb = StringBuilder()
                    sb.append(transcriptText).append("\n\n--- Speaker Segments (Beta) ---\n")
                    segments.forEach { 
                        sb.append(String.format("Speaker %d: %.2fs - %.2fs\n", it.speaker, it.start, it.end))
                    }
                    return@withContext Result.success(sb.toString())
                }
            }
            
            Result.success(transcriptText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
