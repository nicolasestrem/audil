package com.audil.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
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
        // Check for specific files expected from Sherpa-ONNX structure
        if (!specificModelDir.exists()) return@withContext false
        
        val files = specificModelDir.listFiles() ?: return@withContext false
        
        // Basic check: do we have Onnx files OR gguf files?
        // Note: The extraction process should place encoder/decoder/tokens in this directory
        files.any { it.name.endsWith(".onnx") || it.name.endsWith(".gguf") }
    }

    /**
     * Downloads a model from the configured URL.
     * 
     * Supports two types of URLs:
     * 1. **.tar.bz2 Archives** (e.g., Sherpa-ONNX models): Downloaded and extracted.
     * 2. **.gguf Direct Files** (e.g., Qwen/Llama GGUF): Downloaded directly to the target directory without extraction.
     * 
     * @param modelName The internal identifier for the model (e.g., "qwen-0.5b", "tiny").
     * @param progressCallback Callback for download progress (0.0 to 1.0).
     */
    suspend fun downloadModel(modelName: String, progressCallback: (Float) -> Unit) = withContext(Dispatchers.IO) {
        val urlString = getModelUrl(modelName)
        if (urlString.isEmpty()) {
            Log.e("ModelManager", "Invalid model name: $modelName")
            return@withContext
        }

        val targetDir = File(modelsDir, modelName)
        if (!targetDir.exists()) targetDir.mkdirs()

        // Determine if it's an archive or direct file
        val isArchive = urlString.endsWith(".tar.bz2")
        val fileName = if (isArchive) "model_archive.tar.bz2" else urlString.substringAfterLast("/")
        val downloadFile = File(targetDir, fileName)
        
        try {
            Log.d("ModelManager", "Downloading $modelName from $urlString")
            
            val url = URL(urlString)
            val connection = url.openConnection()
            val totalSize = connection.contentLength
            
            val input = BufferedInputStream(url.openStream())
            val output = FileOutputStream(downloadFile)
            
            val buffer = ByteArray(1024 * 8)
            var bytesRead = 0
            var totalBytesRead = 0L
            
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                
                if (totalSize > 0) {
                    val progress = totalBytesRead.toFloat() / totalSize
                    progressCallback(progress)
                }
            }
            
            output.flush()
            output.close()
            input.close()
            
            if (isArchive) {
                Log.d("ModelManager", "Download complete. Extracting...")
                // Extract .tar.bz2
                extractTarBz2(downloadFile, targetDir)
                // Cleanup archive
                downloadFile.delete()
                Log.d("ModelManager", "Extraction complete")
            } else {
                 Log.d("ModelManager", "Download complete.")
            }
            
            progressCallback(1.0f) // Done
            
        } catch (e: Exception) {
            Log.e("ModelManager", "Error downloading/extracting model", e)
            downloadFile.delete() // Cleanup on failure
            throw e
        }
    }

    private fun extractTarBz2(archive: File, outputDir: File) {
        val fileInputStream = FileInputStream(archive)
        val bufferedInputStream = BufferedInputStream(fileInputStream)
        val bzip2InputStream = BZip2CompressorInputStream(bufferedInputStream)
        val tarInputStream = TarArchiveInputStream(bzip2InputStream)

        var entry: TarArchiveEntry? = null
        while (tarInputStream.nextTarEntry.also { entry = it } != null) {
            val currentEntry = entry ?: break
            
            // Sherpa release tarballs often have a top-level directory e.g., "sherpa-onnx-whisper-tiny/"
            // We want to flatten this into our targetDir: models/tiny/
            val entryName = currentEntry.name
            val fileName = File(entryName).name // Flatten path
            
            if (currentEntry.isDirectory) {
                continue
            }
            
            val outputFile = File(outputDir, fileName)
            val parent = outputFile.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }
            
            val outputStream = FileOutputStream(outputFile)
            val buffer = ByteArray(1024 * 8)
            var len: Int
            while (tarInputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
            }
            outputStream.close()
        }
        tarInputStream.close()
    }

    fun getModelPath(modelName: String): String {
        return File(modelsDir, modelName).absolutePath
    }

    private fun getModelUrl(modelName: String): String {
        return when(modelName) {
            "tiny" -> "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-whisper-tiny.tar.bz2"
            "base" -> "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-whisper-base.tar.bz2"
            "small" -> "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-whisper-small.tar.bz2"
            // For optimized models (e.g. quantization or specific languages), we can add more specific URLs later.
            // Using standard multilingual models for now as requested.
            "diarization" -> "https://github.com/k2-fsa/sherpa-onnx/releases/download/speaker-segmentation-models/sherpa-onnx-pyannote-segmentation-3-0.tar.bz2" 
            "qwen-0.5b" -> "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf"
            else -> ""
        }
    }
}
