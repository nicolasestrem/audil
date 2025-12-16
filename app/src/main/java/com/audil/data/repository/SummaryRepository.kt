package com.audil.data.repository

import com.audil.domain.model.MeetingContext
import com.audil.nativelib.LlamaCppWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryRepository @Inject constructor(
    private val modelManager: ModelManager,
    private val llamaCpp: LlamaCppWrapper
) {

    suspend fun generateSummaryStream(
        transcript: String,
        context: MeetingContext,
        onProgress: (String) -> Unit
    ): Flow<String> = flow {
        // 1. Ensure Model exists
        // We use qwen-0.5b as default
        val modelName = "qwen-0.5b"
        
        onProgress("Checking summarization model...")
        if (!modelManager.isModelDownloaded(modelName)) {
            onProgress("Downloading $modelName...")
            var lastProgress = 0f
            modelManager.downloadModel(modelName) { progress ->
                // Throttle progress updates to avoid spamming flow
                if (progress - lastProgress > 0.1f) {
                   onProgress("Downloading: ${(progress * 100).toInt()}%")
                   lastProgress = progress
                }
            }
        }

        // 2. Load Model
        onProgress("Loading model...")
        val modelPath = modelManager.getModelPath(modelName) + "/qwen2.5-0.5b-instruct-q4_k_m.gguf"
        // Note: ModelManager in simulated mode creates this exact file name
        
        if (!llamaCpp.loadModel(modelPath)) {
            throw IllegalStateException("Failed to load model at $modelPath")
        }

        // 3. Construct Prompt
        val systemPrompt = context.getFullPrompt()
        val fullPrompt = buildPrompt(systemPrompt, transcript)

        onProgress("Generating summary...")
        
        // 4. Generate
        // In a real flow, we'd emit token by token.
        // For the wrapper we built, it takes a callback
        val result = llamaCpp.generateSummary(fullPrompt) { token ->
            // In a suspend function we can't easily emit from callback without callbackFlow
            // For now, we'll just gather the result in the wrapper/repository method
            // heavily simplified for this prototype structure
        }
        
        // Emitting the full result chunk-by-chunk simulation
        val words = result.split(" ")
        var buffer = ""
        words.forEach { word ->
            buffer += "$word "
            emit(buffer)
            kotlinx.coroutines.delay(20)
        }
    }
    
    private fun buildPrompt(systemInstruction: String, transcript: String): String {
        // Simple chat template
        return """
            <|im_start|>system
            $systemInstruction
            <|im_end|>
            <|im_start|>user
            TRANSCRIPT:
            $transcript
            <|im_end|>
            <|im_start|>assistant
        """.trimIndent()
    }
}
