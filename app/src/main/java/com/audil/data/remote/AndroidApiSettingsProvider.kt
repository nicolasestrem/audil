package com.audil.data.remote

import android.content.Context
import com.audil.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation of [ApiSettingsProvider] that reads from
 * [SettingsRepository] (DataStore for non-secrets + EncryptedSharedPreferences for API key).
 */
@Singleton
class AndroidApiSettingsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ApiSettingsProvider {

    /**
     * Returns the current [ApiSettings] snapshot.
     *
     * This blocks briefly to collect the first emission from each DataStore flow.
     * For a fully async version, prefer the [collectFlow] extension.
     */
    override fun getSettings(): ApiSettings {
        return runBlocking {
            ApiSettings(
                baseUrl = settingsRepository.remoteApiUrl.first(),
                apiKey = settingsRepository.remoteApiKey.first(),
                transcriptionModel = settingsRepository.transcriptionModel.first(),
                chatModel = settingsRepository.remoteModelName.first(),
                language = settingsRepository.language.first()
            )
        }
    }
}
