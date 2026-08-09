package com.audil.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

class OpenAiApiClientTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var client: OpenAiApiClient
    private lateinit var settingsProvider: InMemoryApiSettingsProvider

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
        settingsProvider = InMemoryApiSettingsProvider(
            ApiSettings(
                apiKey = "sk-test-key-12345",
                baseUrl = mockServer.url("/v1").toString()
            )
        )
        client = OpenAiApiClient(settingsProvider)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun `transcribe sends correct authorization header`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"text":"Hello world"}""")
        )

        val tempFile = File.createTempFile("test", ".wav").apply {
            writeBytes(ByteArray(100) { 0 })
        }

        val result = client.uploadAudio(tempFile, "whisper-1")

        assertEquals("Hello world", result)

        val request = mockServer.takeRequest()
        assertEquals("Bearer sk-test-key-12345", request.getHeader("Authorization"))
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("audio/transcriptions") == true)
    }

    @Test
    fun `chat sends correct headers and body with summary temperature`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"choices":[{"message":{"role":"assistant","content":"Summary text"}}]}""")
        )

        val messages = listOf(
            ChatMessage("system", "Summarize this."),
            ChatMessage("user", "TRANSCRIPT:\nSome text here.")
        )

        val result = client.chatCompletion(messages, "gpt-4o-mini", temperature = 0.2f)

        assertEquals("Summary text", result)

        val request = mockServer.takeRequest()
        assertEquals("Bearer sk-test-key-12345", request.getHeader("Authorization"))
        assertEquals("application/json", request.getHeader("Content-Type"))
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"temperature\":0.2"))
        assertTrue(body.contains("gpt-4o-mini"))
        assertTrue(body.contains("Summarize this."))
    }

    @Test
    fun `throws actionable exception when API not configured`() = runTest {
        val unconfiguredClient = OpenAiApiClient(
            InMemoryApiSettingsProvider(ApiSettings.DEFAULT)
        )
        try {
            unconfiguredClient.uploadAudio(
                File.createTempFile("test", ".wav"),
                "whisper-1"
            )
            fail("Expected exception")
        } catch (e: ApiNotConfiguredException) {
            assertTrue(e.message!!.contains("not configured"))
        }
    }

    @Test
    fun `throws actionable exception on HTTP 401`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error":{"message":"Invalid API key"}}""")
        )

        val tempFile = File.createTempFile("test", ".wav").apply {
            writeBytes(ByteArray(100) { 0 })
        }

        try {
            client.uploadAudio(tempFile, "whisper-1")
            fail("Expected exception")
        } catch (e: ApiAuthenticationException) {
            assertTrue(e.message!!.contains("401"))
        }
    }

    @Test
    fun `throws actionable exception on HTTP 429 rate limit`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setBody("""{"error":{"message":"Rate limit exceeded"}}""")
        )

        val tempFile = File.createTempFile("test", ".wav").apply {
            writeBytes(ByteArray(100) { 0 })
        }

        try {
            client.uploadAudio(tempFile, "whisper-1")
            fail("Expected exception")
        } catch (e: ApiRateLimitException) {
            assertTrue(e.message!!.contains("429"))
        }
    }

    @Test
    fun `throws actionable exception on network failure`() = runTest {
        // Shut down the server to simulate network failure
        val port = mockServer.port
        mockServer.shutdown()

        val offlineProvider = InMemoryApiSettingsProvider(
            ApiSettings(
                apiKey = "sk-test",
                baseUrl = "http://localhost:$port/v1"
            )
        )
        val offlineClient = OpenAiApiClient(offlineProvider)

        try {
            offlineClient.uploadAudio(
                File.createTempFile("test", ".wav"),
                "whisper-1"
            )
            fail("Expected exception")
        } catch (e: ApiNetworkException) {
            assertTrue(e.message!!.contains("Network") || e.cause != null)
        }
    }

    @Test
    fun `chatCompletion default temperature is 0 7f`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"choices":[{"message":{"role":"assistant","content":"text"}}]}""")
        )

        client.chatCompletion(
            listOf(ChatMessage("user", "hello")),
            "gpt-4o"
        )

        val request = mockServer.takeRequest()
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"temperature\":0.7"))
    }

    @Test
    fun `transcribe uses default model when not specified`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"text":"result"}""")
        )

        val tempFile = File.createTempFile("test", ".wav").apply {
            writeBytes(ByteArray(100) { 0 })
        }

        client.uploadAudio(tempFile)

        val request = mockServer.takeRequest()
        val body = request.body.readUtf8()
        // Should include model parameter
        assertTrue(body.contains("whisper-1"))
    }
}

/**
 * Fake settings provider for testing without Android dependencies.
 */
class InMemoryApiSettingsProvider(
    initialSettings: ApiSettings = ApiSettings.DEFAULT
) : ApiSettingsProvider {
    private var _settings: ApiSettings = initialSettings

    override fun getSettings(): ApiSettings = _settings

    fun update(settings: ApiSettings) {
        _settings = settings
    }
}
