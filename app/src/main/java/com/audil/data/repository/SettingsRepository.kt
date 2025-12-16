package com.audil.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

    companion object {
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_MODEL_TYPE = stringPreferencesKey("model_type")
        
        // Defaults
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        
        const val MODEL_LOCAL_STANDARD = "local_standard"
        const val MODEL_LOCAL_OPTIMIZED = "local_optimized"
    }

    val theme: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: THEME_SYSTEM
    }

    val language: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "en"
    }

    val modelType: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_MODEL_TYPE] ?: MODEL_LOCAL_STANDARD
    }

    suspend fun setTheme(theme: String) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme
        }
    }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = lang
        }
    }

    suspend fun setModelType(type: String) {
        dataStore.edit { prefs ->
            prefs[KEY_MODEL_TYPE] = type
        }
    }
}
