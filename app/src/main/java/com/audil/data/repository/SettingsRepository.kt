package com.audil.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.audil.data.remote.ApiSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    /**
     * Encrypted storage for the API key.
     * Uses AndroidX Security Crypto EncryptedSharedPreferences with AES-256-GCM.
     */
    private val securePrefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            SECURE_PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val SECURE_PREFS_NAME = "audil_secure_prefs"

        // Secure keys (EncryptedSharedPreferences)
        const val KEY_SECURE_API_KEY = "encrypted_openai_api_key"

        // DataStore keys (non-secrets)
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_MODEL_TYPE = stringPreferencesKey("model_type")
        val KEY_USE_REMOTE_GENERATION = booleanPreferencesKey("use_remote_generation")
        val KEY_REMOTE_API_URL = stringPreferencesKey("remote_api_url")
        val KEY_REMOTE_MODEL_NAME = stringPreferencesKey("remote_model_name")
        val KEY_TRANSCRIPTION_MODEL = stringPreferencesKey("transcription_model")

        // Defaults
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val MODEL_LOCAL_STANDARD = "local_standard"
        const val MODEL_LOCAL_OPTIMIZED = "local_optimized"
    }

    // ---- DataStore flows (non-secrets) ----

    val theme: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: THEME_SYSTEM
    }

    val language: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "en"
    }

    val modelType: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_MODEL_TYPE] ?: MODEL_LOCAL_STANDARD
    }

    val useRemoteGeneration: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_USE_REMOTE_GENERATION] ?: false
    }

    val remoteApiUrl: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_REMOTE_API_URL] ?: "https://api.openai.com/v1"
    }

    val remoteModelName: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_REMOTE_MODEL_NAME] ?: "gpt-3.5-turbo"
    }

    val transcriptionModel: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_TRANSCRIPTION_MODEL] ?: "whisper-1"
    }

    /**
     * API key flow from EncryptedSharedPreferences (secure storage).
     * Returns empty string if no key is set.
     */
    val remoteApiKey: Flow<String> = kotlinx.coroutines.flow.flow {
        emit(securePrefs.getString(KEY_SECURE_API_KEY, "") ?: "")
        // Note: EncryptedSharedPreferences doesn't support listeners natively,
        // so this flow emits on collection. For reactive updates, use a callback flow
        // with OnSharedPreferenceChangeListener on a separate plain prefs file
        // that mirrors the key-exists state.
    }

    // ---- Writers ----

    suspend fun setTheme(theme: String) {
        dataStore.edit { prefs -> prefs[KEY_THEME] = theme }
    }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { prefs -> prefs[KEY_LANGUAGE] = lang }
    }

    suspend fun setModelType(type: String) {
        dataStore.edit { prefs -> prefs[KEY_MODEL_TYPE] = type }
    }

    suspend fun setUseRemoteGeneration(use: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_USE_REMOTE_GENERATION] = use }
    }

    suspend fun setRemoteApiUrl(url: String) {
        val normalized = ApiSettings.normalizeUrl(url)
        dataStore.edit { prefs ->
            prefs[KEY_REMOTE_API_URL] = if (normalized.isEmpty()) "" else normalized
        }
    }

    suspend fun setRemoteModelName(name: String) {
        dataStore.edit { prefs -> prefs[KEY_REMOTE_MODEL_NAME] = name }
    }

    suspend fun setTranscriptionModel(model: String) {
        dataStore.edit { prefs -> prefs[KEY_TRANSCRIPTION_MODEL] = model }
    }

    /**
     * Store the API key in EncryptedSharedPreferences (secure).
     */
    suspend fun setRemoteApiKey(key: String) {
        securePrefs.edit().putString(KEY_SECURE_API_KEY, key).apply()
    }

    /**
     * Remove the API key from secure storage.
     */
    suspend fun clearRemoteApiKey() {
        securePrefs.edit().remove(KEY_SECURE_API_KEY).apply()
    }
}
