package com.audil.data.remote

import org.junit.Assert.*
import org.junit.Test

class ApiSettingsTest {

    @Test
    fun `defaultApiSettings has correct production defaults`() {
        val settings = ApiSettings.DEFAULT

        assertEquals("https://api.openai.com/v1", settings.baseUrl)
        assertEquals("", settings.apiKey)
        assertEquals("whisper-1", settings.transcriptionModel)
        assertEquals("gpt-4o", settings.chatModel)
    }

    @Test
    fun `normalizeUrl ensures trailing slash`() {
        assertEquals("https://api.openai.com/v1/", ApiSettings.normalizeUrl("https://api.openai.com/v1"))
        assertEquals("https://api.openai.com/v1/", ApiSettings.normalizeUrl("https://api.openai.com/v1/"))
        assertEquals("https://api.openai.com/v1/", ApiSettings.normalizeUrl("https://api.openai.com/v1//"))
    }

    @Test
    fun `normalizeUrl strips whitespace`() {
        assertEquals("https://api.openai.com/v1/", ApiSettings.normalizeUrl("  https://api.openai.com/v1  "))
    }

    @Test
    fun `normalizeUrl handles localhost with port`() {
        assertEquals("http://localhost:8080/v1/", ApiSettings.normalizeUrl("http://localhost:8080/v1"))
    }

    @Test
    fun `normalizeUrl handles custom domain`() {
        assertEquals("https://llm.example.com/api/v1/", ApiSettings.normalizeUrl("https://llm.example.com/api/v1"))
    }

    @Test
    fun `normalizeUrl returns empty string for blank input`() {
        assertEquals("", ApiSettings.normalizeUrl(""))
        assertEquals("", ApiSettings.normalizeUrl("   "))
    }

    @Test
    fun `apiSettings equality works`() {
        val a = ApiSettings(baseUrl = "https://api.openai.com/v1", apiKey = "sk-abc")
        val b = ApiSettings(baseUrl = "https://api.openai.com/v1", apiKey = "sk-abc")
        assertEquals(a, b)
    }

    @Test
    fun `apiSettings copy preserves other fields`() {
        val original = ApiSettings(
            baseUrl = "https://api.openai.com/v1",
            apiKey = "sk-abc",
            transcriptionModel = "whisper-1",
            chatModel = "gpt-4o",
            language = "en"
        )
        val updated = original.copy(chatModel = "gpt-4o-mini")
        assertEquals("https://api.openai.com/v1", updated.baseUrl)
        assertEquals("sk-abc", updated.apiKey)
        assertEquals("whisper-1", updated.transcriptionModel)
        assertEquals("gpt-4o-mini", updated.chatModel)
        assertEquals("en", updated.language)
    }

    @Test
    fun `isConfigured returns true when apiKey and baseUrl are set`() {
        assertTrue(ApiSettings(apiKey = "sk-test", baseUrl = "https://x.com/v1").isConfigured())
        assertFalse(ApiSettings(apiKey = "", baseUrl = "https://x.com/v1").isConfigured())
        assertFalse(ApiSettings(apiKey = "sk-test", baseUrl = "").isConfigured())
        assertFalse(ApiSettings.DEFAULT.isConfigured())
    }

    @Test
    fun `summaryDefaults use low temperature for deterministic output`() {
        val settings = ApiSettings(apiKey = "sk-test", baseUrl = "https://x.com/v1")
        assertEquals(0.2f, settings.summaryTemperature)
    }

    @Test
    fun `redactedApiKey masks the key for logging`() {
        val settings = ApiSettings(apiKey = "sk-1234abcdefcdef", baseUrl = "https://x.com/v1")
        assertEquals("sk-1234...cdef", settings.redactedApiKey())

        val short = ApiSettings(apiKey = "sk-12", baseUrl = "https://x.com/v1")
        assertEquals("sk-12", short.redactedApiKey())

        val empty = ApiSettings(apiKey = "", baseUrl = "https://x.com/v1")
        assertEquals("<not set>", empty.redactedApiKey())
    }
}
