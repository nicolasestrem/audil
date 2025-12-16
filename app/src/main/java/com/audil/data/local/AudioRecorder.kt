package com.audil.data.local

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
    fun isRecording(): Boolean
}

class AndroidAudioRecorder @Inject constructor() : AudioRecorder {

    private var recorder: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null

    companion object {
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val IO_BUFFER_SIZE = 65536  // 64KB buffer for efficient I/O
    }

    @SuppressLint("MissingPermission")
    override fun start(outputFile: File) {
        if (isRecording) return

        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        recorder?.startRecording()
        isRecording = true

        recordingThread = Thread {
            writeAudioDataToFile(outputFile, bufferSize)
        }
        recordingThread?.start()
    }

    override fun stop() {
        if (!isRecording) return

        isRecording = false
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        recordingThread?.join()
        recorder = null
        recordingThread = null
    }

    override fun isRecording(): Boolean {
        return isRecording
    }

    private fun writeAudioDataToFile(outputFile: File, bufferSize: Int) {
        val data = ByteArray(bufferSize)
        val fileOutputStream = java.io.BufferedOutputStream(
            FileOutputStream(outputFile),
            IO_BUFFER_SIZE
        )

        try {
            // Write placeholder for WAV header
            fileOutputStream.write(ByteArray(44))

            while (isRecording) {
                val read = recorder?.read(data, 0, bufferSize) ?: 0
                if (read > 0) {
                    fileOutputStream.write(data, 0, read)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fileOutputStream.flush()  // Ensure all data written
                fileOutputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Update WAV header with correct file size
        updateWavHeader(outputFile)
    }
    
    private fun updateWavHeader(file: File) {
        val fileSize = file.length()
        val totalDataLen = fileSize - 8
        val totalAudioLen = fileSize - 44
        val byteRate = SAMPLE_RATE * 16 * 1 / 8
        
        val header = ByteArray(44)
        val randomAccessFile = RandomAccessFile(file, "rw")
        
        randomAccessFile.seek(0)
        
        // RIFF/WAVE header
        header[0] = 'R'.code.toByte() 
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        
        // Total data len
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        
        header[16] = 16 // PCM chunk size
        header[17] = 0
        header[18] = 0
        header[19] = 0
        
        header[20] = 1 // Audio format 1=PCM
        header[21] = 0
        
        header[22] = 1 // Channels 1=Mono
        header[23] = 0
        
        header[24] = (SAMPLE_RATE and 0xff).toByte()
        header[25] = ((SAMPLE_RATE shr 8) and 0xff).toByte()
        header[26] = ((SAMPLE_RATE shr 16) and 0xff).toByte()
        header[27] = ((SAMPLE_RATE shr 24) and 0xff).toByte()
        
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        
        header[32] = 2 // Block align
        header[33] = 0
        
        header[34] = 16 // Bits per sample
        header[35] = 0
        
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        
        // Total audio len
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
        
        randomAccessFile.write(header)
        randomAccessFile.close()
    }
}
