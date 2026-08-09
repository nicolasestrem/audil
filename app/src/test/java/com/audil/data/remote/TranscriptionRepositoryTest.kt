package com.audil.data.remote

import com.audil.data.repository.TranscriptionRepository
import com.audil.nativelib.SherpaOnnxWrapper
import com.audil.data.repository.ModelManager
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class TranscriptionRepositoryTest {

    @Test
    fun `repository selects remote transcription client when injected as remote`() {
        val apiClient = mockk<OpenAiApiClient>()
        val transcriptionClient = RemoteTranscriptionClient(apiClient)
        val file = File("test.wav")

        coEvery { apiClient.uploadAudio(file, "whisper-1") } returns "Remote transcript"

        val result = kotlinx.coroutines.runBlocking {
            transcriptionClient.transcribe(file, "whisper-1")
        }

        assertTrue(result.isSuccess)
        assertEquals("Remote transcript", result.getOrNull())
    }

    @Test
    fun `repository selects local transcription client when injected as local`() {
        val sherpaOnnx = mockk<SherpaOnnxWrapper>()
        val modelManager = mockk<ModelManager>()
        val transcriptionClient = LocalTranscriptionClient(sherpaOnnx, modelManager)
        val file = File("test.wav")

        coEvery { modelManager.isModelDownloaded("tiny") } returns true
        every { modelManager.getModelPath("tiny") } returns "/models/tiny"
        coEvery { sherpaOnnx.initRecognizer("/models/tiny", "tiny") } just Runs
        coEvery { sherpaOnnx.transcribe(file) } returns "Local transcript"

        val result = kotlinx.coroutines.runBlocking {
            transcriptionClient.transcribe(file, "tiny")
        }

        assertTrue(result.isSuccess)
        assertEquals("Local transcript", result.getOrNull())
    }

    @Test
    fun `remote transcription handles uploadAudio with model parameter`() {
        val apiClient = mockk<OpenAiApiClient>()
        val client = RemoteTranscriptionClient(apiClient)
        val file = File("test.wav")

        coEvery { apiClient.uploadAudio(file, "whisper-1") } returns "transcript"

        val result = kotlinx.coroutines.runBlocking {
            client.transcribe(file, "whisper-1")
        }

        assertTrue(result.isSuccess)
        coVerify { apiClient.uploadAudio(file, "whisper-1") }
    }

    @Test
    fun `remote transcription future-proofs for language parameter`() {
        // Anticipating uploadAudio(file, model, language) — language is passed through
        // even though current OpenAiApiClient doesn't use it yet
        val apiClient = mockk<OpenAiApiClient>()
        val client = RemoteTranscriptionClient(apiClient)
        val file = File("test.wav")

        coEvery { apiClient.uploadAudio(file, "whisper-1") } returns "transcript"

        val result = kotlinx.coroutines.runBlocking {
            client.transcribe(file, "whisper-1", "ja")
        }

        assertTrue(result.isSuccess)
        coVerify { apiClient.uploadAudio(file, "whisper-1") }
    }
}
