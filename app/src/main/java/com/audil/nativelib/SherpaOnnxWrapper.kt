package com.audil.nativelib

import android.content.Context
import android.util.Log
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.WaveReader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the sherpa-onnx Android AAR for offline speech recognition.
 *
 * Uses OfflineRecognizer with Whisper models (tiny/base/small).
 */
@Singleton
class SherpaOnnxWrapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Volatile
    private var recognizer: OfflineRecognizer? = null

    @Volatile
    private var isInitialized = false

    private val modelLock = Any()

    /**
     * Initialize the recognizer with the given model.
     *
     * @param modelDir  path to directory containing encoder.onnx, decoder.onnx, tokens.txt
     * @param modelName model identifier (e.g. "tiny", "base", "small") — used for logging
     */
    suspend fun initRecognizer(
        modelDir: String,
        modelName: String
    ) = withContext(Dispatchers.IO) {
        synchronized(modelLock) {
            // Release previous recognizer if any
            recognizer?.release()
            recognizer = null
            isInitialized = false

            try {
                // Sherpa whisper models use model-name prefixed filenames:
                // tiny-encoder.onnx, base-decoder.onnx, small-tokens.txt etc.
                // Also support the unprefixed fallback for custom models.
                val dir = File(modelDir)
                val files = dir.listFiles() ?: return@withContext
                val encoder = files.find { it.name.endsWith("-encoder.onnx") || it.name == "encoder.onnx" }?.absolutePath
                val decoder = files.find { it.name.endsWith("-decoder.onnx") || it.name == "decoder.onnx" }?.absolutePath
                val tokens  = files.find { it.name.endsWith("-tokens.txt") || it.name == "tokens.txt" }?.absolutePath

                if (encoder == null || decoder == null || tokens == null) {
                    Log.w(TAG, "Model files missing in $modelDir — expected *-encoder.onnx, *-decoder.onnx, *-tokens.txt")
                    return@withContext
                }

                val whisperConfig = OfflineWhisperModelConfig(
                    encoder = encoder,
                    decoder = decoder,
                    language = "",
                    task = "transcribe",
                    tailPaddings = -1,
                    enableTokenTimestamps = false,
                    enableSegmentTimestamps = false
                )

                val modelConfig = OfflineModelConfig(
                    whisper = whisperConfig,
                    tokens = tokens,
                    numThreads = 4,
                    debug = false,
                    provider = "cpu",
                    modelType = ""
                )

                val featConfig = FeatureConfig(
                    sampleRate = 16000,
                    featureDim = 80,
                    dither = 0f
                )

                val recConfig = OfflineRecognizerConfig(
                    featConfig = featConfig,
                    modelConfig = modelConfig
                )

                recognizer = OfflineRecognizer(
                    assetManager = null,
                    config = recConfig
                )

                isInitialized = true
                Log.i(TAG, "Sherpa-ONNX initialized with model '$modelName' from $modelDir")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Sherpa-ONNX with '$modelName'", e)
                recognizer = null
                isInitialized = false
                throw e
            }
        }
    }

    /**
     * Transcribe an audio file using the initialized recognizer.
     *
     * @param audioFile  WAV file (16kHz mono recommended, but WaveReader handles resampling)
     * @return transcribed text
     * @throws IllegalStateException if recognizer not initialized
     */
    @Throws(IllegalStateException::class, Exception::class)
    suspend fun transcribe(audioFile: File): String = withContext(Dispatchers.IO) {
        val rec = recognizer ?: throw IllegalStateException("Recognizer not initialized. Call initRecognizer first.")

        synchronized(modelLock) {
            val wave = WaveReader.readWave(audioFile.absolutePath)
            val stream = rec.createStream()
            try {
                stream.acceptWaveform(wave.samples, wave.sampleRate)
                rec.decode(stream)
                val result = rec.getResult(stream)
                result.text
            } finally {
                stream.release()
            }
        }
    }

    /**
     * Release the recognizer and free native resources.
     */
    fun release() {
        synchronized(modelLock) {
            recognizer?.release()
            recognizer = null
            isInitialized = false
            Log.i(TAG, "Sherpa-ONNX released")
        }
    }

    companion object {
        private const val TAG = "SherpaOnnxWrapper"
    }
}
