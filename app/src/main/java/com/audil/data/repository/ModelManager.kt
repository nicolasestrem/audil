package com.audil.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val modelsDir = File(context.getExternalFilesDir(null), "models")

    init {
        if (!modelsDir.exists()) modelsDir.mkdirs()
    }

    suspend fun isModelDownloaded(modelName: String): Boolean = withContext(Dispatchers.IO) {
        val specificModelDir = File(modelsDir, modelName)
        // Basic check: simply check if directory exists and has content
        return@withContext specificModelDir.exists() && specificModelDir.listFiles()?.isNotEmpty() == true
    }

    suspend fun downloadModel(modelName: String, progressCallback: (Float) -> Unit) = withContext(Dispatchers.IO) {
        val url = getModelUrl(modelName)
        val targetDir = File(modelsDir, modelName)
        if (!targetDir.exists()) targetDir.mkdirs()

        // Placeholder for actual file download logic (e.g., zip download and extraction)
        
        Log.d("ModelManager", "Downloading $modelName from $url to ${targetDir.absolutePath}")
        
        // Simulator download
        for (i in 1..10) {
            progressCallback(i / 10f)
            Thread.sleep(200) 
        }
        
        // Create dummy files to pass isModelDownloaded check in prototype
        if (modelName == "diarization") {
            File(targetDir, "segmentation.onnx").createNewFile()
            File(targetDir, "embedding.onnx").createNewFile()
        } else if (modelName == "qwen-0.5b") {
            File(targetDir, "qwen2.5-0.5b-instruct-q4_k_m.gguf").createNewFile()
        } else {
            File(targetDir, "$modelName-encoder.int8.onnx").createNewFile()
            File(targetDir, "$modelName-decoder.int8.onnx").createNewFile()
            File(targetDir, "tokens.txt").createNewFile()
        }
    }

    fun getModelPath(modelName: String): String {
        return File(modelsDir, modelName).absolutePath
    }

    private fun getModelUrl(modelName: String): String {
        return when(modelName) {
            "tiny" -> "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/model.zip" 
            "base" -> "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-base/resolve/main/model.zip"
            "small" -> "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-small/resolve/main/model.zip"
            "diarization" -> "https://huggingface.co/csukuangfj/sherpa-onnx-pyannote-segmentation-3.0/resolve/main/model.zip"
            "qwen-0.5b" -> "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf"
            else -> ""
        }
    }
}
