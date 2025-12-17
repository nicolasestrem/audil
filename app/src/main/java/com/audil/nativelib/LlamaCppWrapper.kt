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
    @Volatile
    private var isModelLoaded = false

    @Volatile
    private var currentModelPath: String? = null

    private val modelLock = Any()

    suspend fun loadModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        // Check if already loaded (outside synchronized for early return)
        if (isModelLoaded && currentModelPath == modelPath) {
            return@withContext true
        }

        synchronized(modelLock) {
            // Double-check after acquiring lock
            if (isModelLoaded && currentModelPath == modelPath) {
                return@withContext true
            }

            if (!File(modelPath).exists()) {
                Log.e(TAG, "Model file not found: $modelPath")
                return@withContext false
            }

            // Unload previous model if different
            if (isModelLoaded && currentModelPath != modelPath) {
                unload()
            }
        }

        // Perform time-consuming operations outside synchronized block
        Log.d(TAG, "Loading model from $modelPath...")
        delay(1000) // Simulate load time

        synchronized(modelLock) {
            isModelLoaded = true
            currentModelPath = modelPath
        }

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
        // Generate a dynamic summary based on the prompt content
        val simulatedResponse = generateDynamicResponse(prompt)

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
    
    private fun generateDynamicResponse(prompt: String): String {
        return if (prompt.contains("Action Items")) {
            """
            ## Meeting Summary
            The discussion focused on: ${prompt.take(50).replace("\n", " ")}...
            
            ## Key Outcomes
            - Aligned on project milestones
            - Reviewed budget constraints
            
            ## Action Items
            - [ ] Follow up on discussed points
            - [ ] Schedule next review
            """.trimIndent()
        } else {
            val topic = prompt.split(" ").take(5).joinToString(" ")
            """
            ## Executive Summary
            This meeting covered several key topics regarding $topic...
            
            ## Details
            The participants discussed the current status and future steps. 
            Key decisions were made regarding the timeline and resource allocation.
            
            The tone was collaborative and focused on problem-solving.
            """.trimIndent()
        }
    }
    
    fun unload() {
        isModelLoaded = false
        currentModelPath = null
    }

    companion object {
        private const val TAG = "LlamaCppWrapper"
    }
}
