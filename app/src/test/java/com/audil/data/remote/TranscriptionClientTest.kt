package com.audil.data.remote

import com.audil.data.repository.ModelManager
import com.audil.nativelib.SherpaOnnxWrapper
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class TranscriptionClientTest {

    // --- Remote Client Tests ---

    @Test
    fun `remote client delegates to OpenAiApiClient uploadAudio`() {
        val apiClient = mockk<OpenAiApiClient>()
        val client = RemoteTranscriptionClient(apiClient)
        val file = File("test.wav")

        coEvery { apiClient.uploadAudio(file, "whisper-1") } returns "Hello world"

        val result = runBlockingTest {
            client.transcribe(file, "whisper-1")
        }

        assertTrue(result.isSuccess)
        assertEquals("Hello world", result.getOrNull())
        coVerify { apiClient.uploadAudio(file, "whisper-1") }
    }

    @Test
    fun `remote client ignores language parameter gracefully`() {
        val apiClient = mockk<OpenAiApiClient>()
        val client = RemoteTranscriptionClient(apiClient)
        val file = File("test.wav")

        coEvery { apiClient.uploadAudio(file, "whisper-1") } returns "Bonjour"

        val result = runBlockingTest {
            client.transcribe(file, "whisper-1", "fr")
        }

        assertTrue(result.isSuccess)
        assertEquals("Bonjour", result.getOrNull())
        // language is anticipated but gracefully ignored for now
    }

    @Test
    fun `remote client returns failure on API error`() {
        val apiClient = mockk<OpenAiApiClient>()
        val client = RemoteTranscriptionClient(apiClient)
        val file = File("test.wav")

        coEvery { apiClient.uploadAudio(any(), any()) } throws RuntimeException("Network error")

        val result = runBlockingTest {
            client.transcribe(file, "whisper-1")
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
    }

    @Test
    fun `remote client returns failure when API not configured`() {
        val apiClient = mockk<OpenAiApiClient>()
        val client = RemoteTranscriptionClient(apiClient)
        val file = File("test.wav")

        coEvery { apiClient.uploadAudio(any(), any()) } throws IllegalStateException("API not configured")

        val result = runBlockingTest {
            client.transcribe(file, "whisper-1")
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    // --- Local Client Tests ---

    @Test
    fun `local client returns failure when model not downloaded`() {
        val sherpaOnnx = mockk<SherpaOnnxWrapper>()
        val modelManager = mockk<ModelManager>()
        val client = LocalTranscriptionClient(sherpaOnnx, modelManager)
        val file = File("test.wav")

        coEvery { modelManager.isModelDownloaded("tiny") } returns false

        val result = runBlockingTest {
            client.transcribe(file, "tiny")
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not downloaded") == true)
    }

    @Test
    fun `local client transcribes when model is available`() {
        val sherpaOnnx = mockk<SherpaOnnxWrapper>()
        val modelManager = mockk<ModelManager>()
        val client = LocalTranscriptionClient(sherpaOnnx, modelManager)
        val file = File("test.wav")

        coEvery { modelManager.isModelDownloaded("tiny") } returns true
        every { modelManager.getModelPath("tiny") } returns "/models/tiny"
        coEvery { sherpaOnnx.initRecognizer("/models/tiny", "tiny") } just Runs
        coEvery { sherpaOnnx.transcribe(file) } returns "On-device transcript"

        val result = runBlockingTest {
            client.transcribe(file, "tiny")
        }

        assertTrue(result.isSuccess)
        assertEquals("On-device transcript", result.getOrNull())
    }

    @Test
    fun `local client returns failure on engine error`() {
        val sherpaOnnx = mockk<SherpaOnnxWrapper>()
        val modelManager = mockk<ModelManager>()
        val client = LocalTranscriptionClient(sherpaOnnx, modelManager)
        val file = File("test.wav")

        coEvery { modelManager.isModelDownloaded("tiny") } returns true
        every { modelManager.getModelPath("tiny") } returns "/models/tiny"
        coEvery { sherpaOnnx.initRecognizer(any(), any()) } just Runs
        coEvery { sherpaOnnx.transcribe(any()) } throws RuntimeException("Engine crash")

        val result = runBlockingTest {
            client.transcribe(file, "tiny")
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Engine crash") == true)
    }

    @Test
    fun `local client ignores language parameter gracefully`() {
        val sherpaOnnx = mockk<SherpaOnnxWrapper>()
        val modelManager = mockk<ModelManager>()
        val client = LocalTranscriptionClient(sherpaOnnx, modelManager)
        val file = File("test.wav")

        coEvery { modelManager.isModelDownloaded("base") } returns true
        every { modelManager.getModelPath("base") } returns "/models/base"
        coEvery { sherpaOnnx.initRecognizer("/models/base", "base") } just Runs
        coEvery { sherpaOnnx.transcribe(file) } returns "Guten Tag"

        val result = runBlockingTest {
            client.transcribe(file, "base", "de")
        }

        assertTrue(result.isSuccess)
        assertEquals("Guten Tag", result.getOrNull())
    }

    // --- Helper ---
    private fun runBlockingTest(block: suspend () -> Result<String>): Result<String> {
        return kotlinx.coroutines.runBlocking { block() }
    }
}
