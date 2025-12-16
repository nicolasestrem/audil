package com.audil.nativelib

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlamaCppWrapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var isModelLoaded = false
    private var currentModelPath: String? = null

    suspend fun loadModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        if (!File(modelPath).exists()) {
            Log.e(TAG, "Model file not found: $modelPath")
            return@withContext false
        }
        
        // Simulating native model loading
        Log.d(TAG, "Loading model from $modelPath...")
        delay(1000) // Simulate generic load time
        isModelLoaded = true
        currentModelPath = modelPath
        return@withContext true
    }

    suspend fun generateSummary(
        prompt: String,
        onToken: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        if (!isModelLoaded) throw IllegalStateException("Model not loaded")

        Log.d(TAG, "Generating summary for prompt: $prompt")
        
        // Simulating specific LLM output based on prompts
        // In a real implementation this would call a JNI function
        val simulatedResponse = if (prompt.contains("Action Items")) {
             """
             ## Meeting Summary
             The team discussed the upcoming Q4 goals and aligned on the marketing strategy.
             
             ## Action Items
             - [ ] Implement new login flow (Owner: Alice)
             - [ ] Update documentation (Owner: Bob)
             - [ ] Schedule team offsite (Owner: Charlie)
             """.trimIndent()
        } else {
            """
            Here is a summary of the transcript provided. Key points include discussion on project timelines, budget allocation for the next quarter, and staffing needs.
            """.trimIndent()
        }

        val tokens = simulatedResponse.split(" ")
        val sb = StringBuilder()
        
        tokens.forEach { token ->
            delay(50) // Simulate token generation speed
            val chunk = "$token "
            onToken(chunk)
            sb.append(chunk)
        }
        
        return@withContext sb.toString().trim()
    }
    
    fun unload() {
        isModelLoaded = false
        currentModelPath = null
    }

    companion object {
        private const val TAG = "LlamaCppWrapper"
    }
}
