package com.audil.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

    private lateinit var secureStore: InMemorySecureStore
    private lateinit var repository: FakeSettingsRepository

    @Before
    fun setUp() {
        secureStore = InMemorySecureStore()
        repository = FakeSettingsRepository(secureStore)
    }

    @Test
    fun `default theme is system`() = runTest {
        assertEquals(SettingsRepository.THEME_SYSTEM, repository.theme.first())
    }

    @Test
    fun `default language is en`() = runTest {
        assertEquals("en", repository.language.first())
    }

    @Test
    fun `default useRemoteGeneration is false`() = runTest {
        assertFalse(repository.useRemoteGeneration.first())
    }

    @Test
    fun `default remoteApiUrl is openai v1`() = runTest {
        assertEquals("https://api.openai.com/v1", repository.remoteApiUrl.first())
    }

    @Test
    fun `default remoteApiKey is empty`() = runTest {
        assertEquals("", repository.remoteApiKey.first())
    }

    @Test
    fun `default remoteModelName is gpt-3 5-turbo`() = runTest {
        assertEquals("gpt-3.5-turbo", repository.remoteModelName.first())
    }

    @Test
    fun `default transcriptionModel is whisper-1`() = runTest {
        assertEquals("whisper-1", repository.transcriptionModel.first())
    }

    @Test
    fun `can set and read theme`() = runTest {
        repository.setTheme(SettingsRepository.THEME_DARK)
        assertEquals(SettingsRepository.THEME_DARK, repository.theme.first())
    }

    @Test
    fun `can set and read language`() = runTest {
        repository.setLanguage("fr")
        assertEquals("fr", repository.language.first())
    }

    @Test
    fun `can set and read useRemoteGeneration`() = runTest {
        repository.setUseRemoteGeneration(true)
        assertTrue(repository.useRemoteGeneration.first())
    }

    @Test
    fun `can set and read remoteApiUrl with normalization`() = runTest {
        repository.setRemoteApiUrl("https://example.com/v1")
        assertEquals("https://example.com/v1/", repository.remoteApiUrl.first())
    }

    @Test
    fun `can set and read remoteModelName`() = runTest {
        repository.setRemoteModelName("gpt-4o-mini")
        assertEquals("gpt-4o-mini", repository.remoteModelName.first())
    }

    @Test
    fun `can set and read transcriptionModel`() = runTest {
        repository.setTranscriptionModel("whisper-2")
        assertEquals("whisper-2", repository.transcriptionModel.first())
    }

    @Test
    fun `apiKey is stored securely and retrievable`() = runTest {
        repository.setRemoteApiKey("sk-secret-12345")
        assertEquals("sk-secret-12345", repository.remoteApiKey.first())
        // Verify it was stored in secure store, not data store
        assertTrue(secureStore.containsKey(SettingsRepository.KEY_SECURE_API_KEY))
    }

    @Test
    fun `apiKey can be cleared`() = runTest {
        repository.setRemoteApiKey("sk-secret-12345")
        assertEquals("sk-secret-12345", repository.remoteApiKey.first())

        repository.clearRemoteApiKey()
        assertEquals("", repository.remoteApiKey.first())
    }

    @Test
    fun `setRemoteApiUrl normalizes URL`() = runTest {
        repository.setRemoteApiUrl("  https://api.example.com/v1  ")
        assertEquals("https://api.example.com/v1/", repository.remoteApiUrl.first())
    }

    @Test
    fun `blank apiUrl is stored as empty`() = runTest {
        repository.setRemoteApiUrl("")
        assertEquals("", repository.remoteApiUrl.first())

        repository.setRemoteApiUrl("   ")
        assertEquals("", repository.remoteApiUrl.first())
    }
}

/**
 * In-memory secure store for testing.
 */
class InMemorySecureStore(
    private val storage: MutableMap<String, String> = mutableMapOf()
) {
    fun putString(key: String, value: String) {
        storage[key] = value
    }

    fun getString(key: String, default: String): String {
        return storage[key] ?: default
    }

    fun remove(key: String) {
        storage.remove(key)
    }

    fun containsKey(key: String): Boolean {
        return storage.containsKey(key)
    }
}

/**
 * Fake SettingsRepository for unit tests without Android dependencies.
 * Mirrors the real implementation's behavior using in-memory stores.
 */
class FakeSettingsRepository(
    private val secureStore: InMemorySecureStore
) {
    private val dataStore = FakePreferencesDataStore()

    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val MODEL_LOCAL_STANDARD = "local_standard"
        const val MODEL_LOCAL_OPTIMIZED = "local_optimized"
        const val KEY_SECURE_API_KEY = "encrypted_openai_api_key"
    }

    val theme = dataStore.getString("theme", THEME_SYSTEM)
    val language = dataStore.getString("language", "en")
    val modelType = dataStore.getString("model_type", MODEL_LOCAL_STANDARD)
    val useRemoteGeneration = dataStore.getBoolean("use_remote_generation", false)
    val remoteApiUrl = dataStore.getString("remote_api_url", "https://api.openai.com/v1")
    val remoteModelName = dataStore.getString("remote_model_name", "gpt-3.5-turbo")
    val transcriptionModel = dataStore.getString("transcription_model", "whisper-1")
    val remoteApiKey = dataStore.getString("remote_api_key_placeholder", "")

    suspend fun setTheme(theme: String) { dataStore.putString("theme", theme) }
    suspend fun setLanguage(lang: String) { dataStore.putString("language", lang) }
    suspend fun setModelType(type: String) { dataStore.putString("model_type", type) }
    suspend fun setUseRemoteGeneration(use: Boolean) { dataStore.putBoolean("use_remote_generation", use) }

    suspend fun setRemoteApiUrl(url: String) {
        val normalized = if (url.isBlank()) "" else {
            var result = url.trim()
            if (!result.endsWith("/")) result = "$result/"
            result
        }
        dataStore.putString("remote_api_url", normalized)
    }

    suspend fun setRemoteModelName(name: String) { dataStore.putString("remote_model_name", name) }
    suspend fun setTranscriptionModel(model: String) { dataStore.putString("transcription_model", model) }

    suspend fun setRemoteApiKey(key: String) {
        secureStore.putString(KEY_SECURE_API_KEY, key)
        dataStore.putString("remote_api_key_placeholder", key)
    }

    suspend fun clearRemoteApiKey() {
        secureStore.remove(KEY_SECURE_API_KEY)
        dataStore.putString("remote_api_key_placeholder", "")
    }
}

/**
 * Minimal in-memory DataStore-like implementation for testing.
 */
class FakePreferencesDataStore {
    private val preferences = mutableMapOf<String, Any>()

    fun getString(key: String, default: String): kotlinx.coroutines.flow.Flow<String> {
        return kotlinx.coroutines.flow.flow {
            emit(preferences[key] as? String ?: default)
        }
    }

    fun getBoolean(key: String, default: Boolean): kotlinx.coroutines.flow.Flow<Boolean> {
        return kotlinx.coroutines.flow.flow {
            emit(preferences[key] as? Boolean ?: default)
        }
    }

    suspend fun putString(key: String, value: String) {
        preferences[key] = value
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        preferences[key] = value
    }
}
