package com.audil.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure, configurable OpenAI-compatible API client.
 *
 * - Uses [ApiSettingsProvider] for settings (key from EncryptedSharedPreferences,
 *   non-secrets from DataStore).
 * - HEADERS-level logging only: never logs request/response bodies (prevents auth leakage).
 * - Configurable connect/read/write timeouts.
 * - Deterministic summary temperature (0.2) via [chatCompletion].
 * - Actionable typed exceptions instead of generic failures.
 */
@Singleton
class OpenAiApiClient @Inject constructor(
    private val settingsProvider: ApiSettingsProvider
) {
    private var cachedSettings: ApiSettings? = null
    private var cachedApi: OpenAiApi? = null

    /**
     * Build (or rebuild) the Retrofit service when settings change.
     */
    private fun resolveApi(): OpenAiApi {
        val settings = settingsProvider.getSettings()

        // Return cached if settings haven't changed
        if (cachedSettings == settings && cachedApi != null) {
            return cachedApi!!
        }

        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${settings.apiKey}")
                .build()
            chain.proceed(request)
        }

        // Use HEADERS level only — never log BODY to prevent auth token leakage
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        val normalizedUrl = ApiSettings.normalizeUrl(settings.baseUrl)

        val api = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAiApi::class.java)

        cachedSettings = settings
        cachedApi = api
        return api
    }

    /**
     * Upload an audio file for transcription.
     *
     * @param file Audio file (WAV, MP3, etc.).
     * @param model Transcription model name (defaults to settings.transcriptionModel if blank).
     * @return Transcribed text.
     * @throws ApiNotConfiguredException if API key or base URL is missing.
     * @throws ApiAuthenticationException on HTTP 401.
     * @throws ApiRateLimitException on HTTP 429.
     * @throws ApiServerException on other HTTP errors.
     * @throws ApiNetworkException on network failures.
     */
    suspend fun uploadAudio(file: File, model: String? = null): String = withContext(Dispatchers.IO) {
        val settings = settingsProvider.getSettings()
        if (!settings.isConfigured()) {
            throw ApiNotConfiguredException()
        }

        val effectiveModel = model ?: settings.transcriptionModel
        val api = resolveApi()

        val requestFile = file.asRequestBody("audio/wav".toMediaType())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val modelPart = effectiveModel.toRequestBody("text/plain".toMediaType())

        try {
            val response = api.transcribe(body, modelPart)
            handleHttpErrors(response)
            response.body()?.text ?: throw ApiServerException(200, "Empty transcription response body")
        } catch (e: ApiNotConfiguredException) {
            throw e
        } catch (e: ApiAuthenticationException) {
            throw e
        } catch (e: ApiRateLimitException) {
            throw e
        } catch (e: ApiServerException) {
            throw e
        } catch (e: ApiNetworkException) {
            throw e
        } catch (e: ConnectException) {
            throw ApiNetworkException("Cannot connect to ${settings.baseUrl}. Check the endpoint URL.", e)
        } catch (e: UnknownHostException) {
            throw ApiNetworkException("Unknown host: ${settings.baseUrl}. Check the endpoint URL.", e)
        } catch (e: SocketTimeoutException) {
            throw ApiNetworkException("Connection timed out. Check your network and endpoint.", e)
        } catch (e: IOException) {
            throw ApiNetworkException("Network error: ${e.message}", e)
        }
    }

    /**
     * Send a chat completion request.
     *
     * @param messages The conversation messages.
     * @param model Model name (defaults to settings.chatModel if blank).
     * @param temperature Sampling temperature.
     *        Use 0.2 for deterministic summaries, 0.7 for creative tasks.
     * @return The assistant's response text.
     * @throws ApiNotConfiguredException if API key or base URL is missing.
     * @throws ApiAuthenticationException on HTTP 401.
     * @throws ApiRateLimitException on HTTP 429.
     * @throws ApiServerException on other HTTP errors.
     * @throws ApiNetworkException on network failures.
     */
    suspend fun chatCompletion(
        messages: List<ChatMessage>,
        model: String? = null,
        temperature: Float = ApiSettings.DEFAULT_TEMPERATURE
    ): String = withContext(Dispatchers.IO) {
        val settings = settingsProvider.getSettings()
        if (!settings.isConfigured()) {
            throw ApiNotConfiguredException()
        }

        val effectiveModel = model ?: settings.chatModel
        val api = resolveApi()

        val request = ChatRequest(
            model = effectiveModel,
            messages = messages,
            temperature = temperature
        )

        try {
            val response = api.chat(request)
            handleHttpErrors(response)
            response.body()?.choices?.firstOrNull()?.message?.content
                ?: throw ApiServerException(200, "Empty chat completion response")
        } catch (e: ApiNotConfiguredException) {
            throw e
        } catch (e: ApiAuthenticationException) {
            throw e
        } catch (e: ApiRateLimitException) {
            throw e
        } catch (e: ApiServerException) {
            throw e
        } catch (e: ApiNetworkException) {
            throw e
        } catch (e: ConnectException) {
            throw ApiNetworkException("Cannot connect to ${settings.baseUrl}. Check the endpoint URL.", e)
        } catch (e: UnknownHostException) {
            throw ApiNetworkException("Unknown host: ${settings.baseUrl}. Check the endpoint URL.", e)
        } catch (e: SocketTimeoutException) {
            throw ApiNetworkException("Connection timed out. Check your network and endpoint.", e)
        } catch (e: IOException) {
            throw ApiNetworkException("Network error: ${e.message}", e)
        }
    }

    /**
     * Map HTTP status codes to typed, actionable exceptions.
     */
    private fun <T> handleHttpErrors(response: Response<T>) {
        if (response.isSuccessful) return
        val code = response.code()
        val errorBody = try {
            response.errorBody()?.string()?.take(500) ?: "No error body"
        } catch (_: Exception) {
            "Could not read error body"
        }
        when (code) {
            401 -> throw ApiAuthenticationException("Authentication failed (HTTP 401). $errorBody")
            429 -> throw ApiRateLimitException("Rate limit exceeded (HTTP 429). $errorBody")
            else -> throw ApiServerException(code, "API error (HTTP $code). $errorBody")
        }
    }

    /**
     * Invalidate the cached API instance so it's rebuilt on next call.
     * Call this after updating settings.
     */
    fun invalidateCache() {
        cachedSettings = null
        cachedApi = null
    }
}

interface OpenAiApi {
    @Multipart
    @POST("audio/transcriptions")
    suspend fun transcribe(
        @Part file: MultipartBody.Part,
        @Part("model") model: okhttp3.RequestBody
    ): Response<TranscriptionResponse>

    @POST("chat/completions")
    @Headers("Content-Type: application/json")
    suspend fun chat(
        @Body request: ChatRequest
    ): Response<ChatResponse>
}

data class TranscriptionResponse(val text: String)

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = ApiSettings.DEFAULT_TEMPERATURE
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage
)
