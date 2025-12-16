package com.audil.nativelib

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sherpa-ONNX Wrapper - STUB VERSION
 * 
 * This is a stub implementation since the sherpa-onnx library is not included.
 * To enable speech recognition:
 * 1. Add the sherpa-onnx library (see SHERPA_ONNX_SETUP.md)
 * 2. Replace this file with the actual implementation
 */
@Singleton
class SherpaOnnxWrapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun initRecognizer(
        modelDir: String,
        modelName: String
    ) = withContext(Dispatchers.IO) {
        Log.w(TAG, "Sherpa-ONNX not available - speech recognition disabled")
    }

    suspend fun transcribe(audioFile: File): String = withContext(Dispatchers.IO) {
        Log.w(TAG, "Sherpa-ONNX not available - returning empty transcription")
        return@withContext ""
    }

    fun release() {
        // No-op
    }

    companion object {
        private const val TAG = "SherpaOnnxWrapper"
    }
}
