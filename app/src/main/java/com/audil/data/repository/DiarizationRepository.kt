package com.audil.data.repository

import com.audil.nativelib.DiarizationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiarizationRepository @Inject constructor(
    private val modelManager: ModelManager,
    private val diarizationEngine: DiarizationEngine
) {

    suspend fun processDiarization(
        audioFile: File,
        onProgress: (String) -> Unit
    ): Result<List<DiarizationEngine.DiarizationSegment>> = withContext(Dispatchers.IO) {
        try {
            onProgress("Checking diarization models...")
            // We reuse "small" or specific diarization model key
            // Ideally we should have "diarization-v1" or similar
            if (!modelManager.isModelDownloaded("diarization")) {
                onProgress("Downloading speaker models...")
                modelManager.downloadModel("diarization") { /* progress */ }
            }

            onProgress("Initializing diarizer...")
            val modelPath = modelManager.getModelPath("diarization")
            diarizationEngine.initDiarization(modelPath)

            onProgress("Identifying speakers...")
            val segments = diarizationEngine.diarize(audioFile)
            
            Result.success(segments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
