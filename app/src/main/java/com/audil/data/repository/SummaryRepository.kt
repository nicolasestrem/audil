package com.audil.data.repository

import com.audil.domain.model.MeetingContext
import com.audil.nativelib.LlamaCppWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import com.audil.data.repository.SettingsRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryRepository @Inject constructor(
    private val modelManager: ModelManager,
    private val llamaCpp: LlamaCppWrapper,
    private val settingsRepository: SettingsRepository
) {

    /**
     * Generates a meeting summary stream.
     *
     * Decides between Local or Remote generation based on the [SettingsRepository.useRemoteGeneration] preference.
     * 
     * - **Remote**: Connects to an OpenAI-compatible API (configured via [SettingsRepository.remoteApiUrl]).
     *   Useful for using stronger models (GPT-4) or local inference servers (LM Studio, Ollama).
     * - **Local**: Uses the on-device `llama.cpp` wrapper with a downloaded GGUF model (default: Qwen 2.5 0.5B).
     *   Ensures privacy and offline capability.
     */
    suspend fun generateSummaryStream(
        transcript: String,
        context: MeetingContext,
        onProgress: (String) -> Unit
    ): Flow<String> = flow {
        // Check settings first
        val useRemote = settingsRepository.useRemoteGeneration.first()
        
        if (useRemote) {
            generateRemoteSummary(transcript, context, onProgress)
        } else {
            generateLocalSummary(transcript, context, onProgress)
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<String>.generateRemoteSummary(
        transcript: String,
        context: MeetingContext,
        onProgress: (String) -> Unit
    ) {
        onProgress("Connecting to remote API...")
        val apiUrl = settingsRepository.remoteApiUrl.first()
        val apiKey = settingsRepository.remoteApiKey.first()
        val modelName = settingsRepository.remoteModelName.first()

        // Build OkHttp with Auth Interceptor
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl(if (apiUrl.endsWith("/")) apiUrl else "$apiUrl/")
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()

        val service = retrofit.create(com.audil.data.remote.OpenAiApi::class.java)
        
        val systemPrompt = context.getFullPrompt()
        val messages = listOf(
            com.audil.data.remote.ChatMessage("system", systemPrompt),
            com.audil.data.remote.ChatMessage("user", "TRANSCRIPT:\n$transcript")
        )

        try {
            onProgress("Sending request...")
            // Note: API interface method names might differ, checking OpenAiApi definition
            val request = com.audil.data.remote.ChatRequest(
                model = modelName,
                messages = messages
            )
            // Header is injected via interceptor usually, but here we are building retrofit manually
            // We need to add logic OR rely on the manual builder I added. 
            // My manual builder didn't add an auth interceptor!
            // I should use the auth logic from my previous snippet but adapted to the interface.
            
            // Wait, OpenAiApi.chat() doesn't take an Authorization header param.
            // It expects an Interceptor to handle it.
            // In my manual builder in generateRemoteSummary, I need to add that interceptor.
             
            val response = service.chat(request)
            val content = response.choices.firstOrNull()?.message?.content ?: "No summary generated."
            emit(content)
        } catch (e: Exception) {
            emit("Error generating summary: ${e.message}")
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<String>.generateLocalSummary(
        transcript: String,
        context: MeetingContext,
        onProgress: (String) -> Unit
    ) {
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
