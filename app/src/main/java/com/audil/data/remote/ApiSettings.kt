package com.audil.data.remote

/**
 * Immutable settings for an OpenAI-compatible API connection.
 *
 * @param baseUrl Normalized base URL (always ends with `/`).
 * @param apiKey API key stored in EncryptedSharedPreferences.
 * @param transcriptionModel Model name for audio transcription (default "whisper-1").
 * @param chatModel Model name for chat completions (default "gpt-4o").
 * @param language Language code for transcription (default "en").
 */
data class ApiSettings(
    val baseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val transcriptionModel: String = "whisper-1",
    val chatModel: String = "gpt-4o",
    val language: String = "en"
) {
    companion object {
        val DEFAULT = ApiSettings()

        /** Temperature for deterministic summary generation. */
        const val SUMMARY_TEMPERATURE = 0.2f

        /** Default temperature for creative/general chat. */
        const val DEFAULT_TEMPERATURE = 0.7f

        /**
         * Normalize a base URL for safe use with Retrofit.
         * - Strips leading/trailing whitespace.
         * - Ensures exactly one trailing slash.
         * - Returns empty string for blank input.
         */
        fun normalizeUrl(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return ""
            val withoutTrailing = trimmed.trimEnd('/')
            return "$withoutTrailing/"
        }
    }

    /** Temperature used for summary requests (deterministic). */
    val summaryTemperature: Float get() = SUMMARY_TEMPERATURE

    /** Whether the client has enough configuration to make API calls. */
    fun isConfigured(): Boolean = apiKey.isNotBlank() && baseUrl.isNotBlank()

    /** Returns a redacted version of the API key safe for logging. */
    fun redactedApiKey(): String {
        if (apiKey.isEmpty()) return "<not set>"
        if (apiKey.length <= 6) return apiKey
        return "${apiKey.take(7)}...${apiKey.takeLast(4)}"
    }
}

/**
 * Provider interface so OpenAiApiClient doesn't depend on Android Context directly.
 * Implementations wire EncryptedSharedPreferences + DataStore.
 */
interface ApiSettingsProvider {
    fun getSettings(): ApiSettings
}
