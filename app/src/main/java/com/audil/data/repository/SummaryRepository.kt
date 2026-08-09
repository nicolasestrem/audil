package com.audil.data.repository

import com.audil.data.remote.ApiSettings
import com.audil.data.remote.ChatMessage
import com.audil.data.remote.OpenAiApiClient
import com.audil.domain.model.MeetingContext
import com.audil.nativelib.LlamaCppWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryRepository @Inject constructor(
    private val modelManager: ModelManager,
    private val llamaCpp: LlamaCppWrapper,
    private val settingsRepository: SettingsRepository,
    private val apiClient: OpenAiApiClient
) {

    /**
     * Generates a meeting summary stream.
     *
     * Decides between Local or Remote generation based on [SettingsRepository.useRemoteGeneration].
     *
     * - **Remote**: Uses [OpenAiApiClient] — a secure, configured OpenAI-compatible client.
     *   Supports any OpenAI-compatible API (GPT-4, LM Studio, Ollama, etc.).
     * - **Local**: Uses the on-device `llama.cpp` wrapper for privacy and offline capability.
     */
    suspend fun generateSummaryStream(
        transcript: String,
        context: MeetingContext,
        onProgress: (String) -> Unit
    ): Flow<String> = flow {
        val useRemote = settingsRepository.useRemoteGeneration.first()

        if (useRemote) {
            generateRemoteSummary(transcript, context, onProgress)
        } else {
            generateLocalSummary(transcript, context, onProgress)
        }
    }

    private suspend fun FlowCollector<String>.generateRemoteSummary(
        transcript: String,
        context: MeetingContext,
        onProgress: (String) -> Unit
    ) {
        onProgress("Connecting to remote API...")

        val systemPrompt = context.getFullPrompt()
        val messages = listOf(
            ChatMessage("system", systemPrompt),
            ChatMessage("user", "TRANSCRIPT:\n$transcript")
        )

        onProgress("Generating summary...")
        // Use deterministic temperature (0.2) for reliable summary output.
        // Do NOT catch — let exceptions propagate as flow errors so the
        // ViewModel surfaces them as Error state, not disguised as Success.
        val content = apiClient.chatCompletion(
            messages = messages,
            temperature = ApiSettings.SUMMARY_TEMPERATURE
        )
        emit(content)
    }

    private suspend fun FlowCollector<String>.generateLocalSummary(
        transcript: String,
        context: MeetingContext,
        onProgress: (String) -> Unit
    ) {
        val modelName = "qwen-0.5b"

        onProgress("Checking summarization model...")
        if (!modelManager.isModelDownloaded(modelName)) {
            onProgress("Downloading $modelName...")
            var lastProgress = 0f
            modelManager.downloadModel(modelName) { progress ->
                if (progress - lastProgress > 0.1f) {
                    onProgress("Downloading: ${(progress * 100).toInt()}%")
                    lastProgress = progress
                }
            }
        }

        onProgress("Loading model...")
        val modelPath = modelManager.getModelPath(modelName) + "/qwen2.5-0.5b-instruct-q4_k_m.gguf"

        if (!llamaCpp.loadModel(modelPath)) {
            throw IllegalStateException("Failed to load model at $modelPath")
        }

        val systemPrompt = context.getFullPrompt()
        val fullPrompt = buildPrompt(systemPrompt, transcript)

        onProgress("Generating summary...")

        val result = llamaCpp.generateSummary(fullPrompt) { _ -> }

        val words = result.split(" ")
        val buffer = StringBuilder()

        words.forEach { word ->
            buffer.append(word).append(" ")
            emit(buffer.toString())
            delay(20)
        }
    }

    private fun buildPrompt(systemInstruction: String, transcript: String): String {
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
