package com.audil.data.remote

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.annotations.SerializedName

@Singleton
class OpenAiApiClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var prefs: SharedPreferences? = null
    private var api: OpenAiApi? = null
    private var currentBaseUrl: String = "https://api.openai.com/v1/"

    init {
        setupPrefs()
        // Initialize with saved or default settings
        val savedBaseUrl = prefs?.getString(KEY_BASE_URL, "https://api.openai.com/v1/") 
        val savedKey = prefs?.getString(KEY_API_KEY, "")
        if (savedBaseUrl != null && savedKey != null) {
             currentBaseUrl = savedBaseUrl
             setupRetrofit(savedKey, savedBaseUrl)
        }
    }
    
    private fun setupPrefs() {
        // Using regular SharedPreferences for simplicity
        // TODO: Implement encryption for production
        prefs = context.getSharedPreferences("api_secrets", Context.MODE_PRIVATE)
    }

    fun updateConfig(apiKey: String, baseUrl: String) {
        prefs?.edit()?.apply {
            putString(KEY_API_KEY, apiKey)
            putString(KEY_BASE_URL, baseUrl)
            apply()
        }
        currentBaseUrl = baseUrl
        setupRetrofit(apiKey, baseUrl)
    }

    private fun setupRetrofit(apiKey: String, baseUrl: String) {
         val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAiApi::class.java)
    }

    suspend fun uploadAudio(file: File, model: String = "whisper-1"): String = withContext(Dispatchers.IO) {
        val service = api ?: throw IllegalStateException("API not configured. Please set API Key.")
        
        // Ensure file is mp3/wav/etc. OpenAI supports multiple
        val requestFile = file.asRequestBody("audio/wav".toMediaType())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val modelPart = model.toRequestBody("text/plain".toMediaType())
        
        val response = service.transcribe(body, modelPart)
        return@withContext response.text
    }
    
    suspend fun chatCompletion(
        messages: List<ChatMessage>, 
        model: String = "gpt-4o"
    ): String = withContext(Dispatchers.IO) {
        val service = api ?: throw IllegalStateException("API not configured")
        val request = ChatRequest(model = model, messages = messages)
        val response = service.chat(request)
        return@withContext response.choices.firstOrNull()?.message?.content ?: ""
    }

    companion object {
        const val KEY_API_KEY = "openai_api_key"
        const val KEY_BASE_URL = "openai_base_url"
    }
}

interface OpenAiApi {
    @Multipart
    @POST("audio/transcriptions")
    suspend fun transcribe(
        @Part file: MultipartBody.Part,
        @Part("model") model: okhttp3.RequestBody
    ): TranscriptionResponse
    
    @POST("chat/completions")
    @Headers("Content-Type: application/json")
    suspend fun chat(
        @Body request: ChatRequest
    ): ChatResponse
}

data class TranscriptionResponse(val text: String)

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f
)

data class ChatMessage(
    val role: String, // system, user, assistant
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage
)
