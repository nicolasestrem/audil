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
 * Diarization Engine - STUB VERSION
 * 
 * This is a stub implementation since the sherpa-onnx library is not included.
 * To enable speaker diarization:
 * 1. Add the sherpa-onnx library (see SHERPA_ONNX_SETUP.md)
 * 2. Replace this file with the actual implementation
 */
@Singleton
class DiarizationEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class DiarizationSegment(
        val start: Float,
        val end: Float,
        val speaker: Int
    )

    suspend fun initDiarization(modelDir: String) = withContext(Dispatchers.IO) {
        Log.w(TAG, "Sherpa-ONNX not available - speaker diarization disabled")
    }

    suspend fun diarize(audioFile: File): List<DiarizationSegment> = withContext(Dispatchers.IO) {
        Log.w(TAG, "Sherpa-ONNX not available - returning empty diarization")
        return@withContext emptyList()
    }

    fun release() {
        // No-op
    }

    companion object {
        private const val TAG = "DiarizationEngine"
    }
}
